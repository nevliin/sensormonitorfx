package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.main.*;
import de.hft.ss17.cebarround.CeBarRoundObserver;
import de.hfts.sensormonitor.exceptions.IllegalSensorAmountException;
import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.exceptions.DatabaseConnectException;
import de.hfts.sensormonitor.sensor.DataSensor;
import de.hfts.sensormonitor.sensor.CeBarRoundDataSensorV2;
import de.hfts.sensormonitor.sensor.CeBarRoundDataSensor;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.*;
import javafx.stage.DirectoryChooser;

/**
 * Class to handle all input/output actions, both from the JAR itself and the OS
 */
public class IO {

    private List<String> languages = new ArrayList<>();
    private List<String> styles = new ArrayList<String>();
    private List<String> tables = new ArrayList<>(); // List of all tables in the database
    private String columns = "(TIME TIMESTAMP NOT NULL, ROWID INT, SENSORID INT NOT NULL, SENSORTYPE INT NOT NULL, TEMPERATURE INT, PRESSURE INT, REVOLUTIONS INT);";
    private Connection conn; // Connection to the H2 embedded database
    private Properties prop; // Properties file loaded from the home.dir/.sensormonitor
    private Statement stat; // Statement to execute queries and commands
    private ResourceBundle langpack;

    /**
     * Standard constructor; tries to load the configuration and creates it if
     * it doesn't exist yet.
     */
    public IO() {

        String currentUsersHomeDir = System.getProperty("user.home");

        // Look for the directory .sensormonitor
        boolean isSensormonitorDirExistant = false;
        File home = new File(currentUsersHomeDir);
        String[] files = home.list();
        for (String s : files) {
            if (s.equalsIgnoreCase(".sensormonitor")) {
                isSensormonitorDirExistant = true;
            }
        }

        // If the directory doesn't exist, create it
        if (!isSensormonitorDirExistant) {
            File folder = new File(currentUsersHomeDir + File.separator + ".sensormonitor");
            folder.mkdir();
        }

        // Try to load the properties from home.dir/.sensormonitor
        boolean isPropertiesExistant = true;
        prop = new Properties();
        try {
            FileInputStream stream = new FileInputStream(currentUsersHomeDir + File.separator + ".sensormonitor" + File.separator + "config.properties");
            prop.load(stream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("defaultconfig/config.properties");
            try {
                prop.load(stream);
            } catch (IOException ex1) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex1);
            }
            isPropertiesExistant = false;
        } catch (IOException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If the properties file does not exist...
        if (!isPropertiesExistant) {
            // ... make the user pick a save directory for the database ...
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Database Directory");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File dir = directoryChooser.showDialog(null);

            FileOutputStream output = null;
            if (dir != null) {
                try {
                    // ... set it in the default config.properties and save them to home.dir/.sensormonitor
                    prop.setProperty("savepath", dir.getAbsolutePath());
                    output = new FileOutputStream(currentUsersHomeDir + File.separator + ".sensormonitor" + File.separator + "config.properties");
                    prop.store(output, null);
                } catch (IOException ex) {
                    Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } else {
                System.exit(0);
            }
        }
        this.langpack = ResourceBundle.getBundle("lang.lang", new Locale(getConfigProp("lang")));
    }

// <--- Database Operations -->    
    /**
     * Connect to the H2 database located in the folder specified in the
     * properties and get a list of all tables
     *
     * @throws DatabaseConnectException
     */
    public void connectDB() throws DatabaseConnectException {
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:" + getConfigProp("savepath") + "/recordings", "root", "root");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet res = meta.getTables(null, null, null,
                    new String[]{"TABLE"});
            while (res.next()) {
                tables.add(res.getString("TABLE_NAME"));
            }
            res.close();
            stat = conn.createStatement();

        } catch (ClassNotFoundException | IllegalStateException | SQLException ex) {
            Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseConnectException();
        }
    }

    /**
     * Returns the list of tables in the database
     *
     * @return
     */
    public List<String> getTables() {
        return this.tables;
    }

    /**
     * Create a new table in the database with a generic name
     *
     * @return
     */
    public String createGenericTable() {
        boolean flag = true;
        String genericName = null;
        // Create a generic name and make sure a table with that name does not already exist
        do {
            genericName = generateRandomString(10);
            if (!tables.contains(genericName)) {
                flag = false;
            }
        } while (flag);

        // Create the table with SQL
        try {
            String exe = "CREATE TABLE " + genericName + " " + columns;
            stat.execute(exe);
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return genericName;

    }

    /**
     * Rename the specified table (oldname) to newname
     *
     * @param oldname
     * @param newname
     * @throws IllegalTableNameException
     */
    public void renameTable(String oldname, String newname) throws IllegalTableNameException {
        if (!newname.toUpperCase().matches("[a-zA-Z][a-zA-Z0-9_]{1,30}") || isTableExistant(newname.toUpperCase())) {
            throw new IllegalTableNameException();
        }
        try {
            stat.execute("ALTER TABLE " + oldname + " RENAME TO " + newname);
            tables.add(newname.toUpperCase());
            tables.remove(oldname.toUpperCase());
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save recorded data in the specified table
     *
     * @param table
     * @param insertstmt
     * @param timestamp
     */
    public void saveData(String table, String insertstmt, Timestamp timestamp) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " VALUES (?," + insertstmt + ");");
            ps.setTimestamp(1, timestamp);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads a table/recording from the database and returns it as ResultSet
     *
     * @param table
     * @return
     */
    public ResultSet loadRecording(String table) {
        ResultSet rs = null;
        try {
            rs = stat.executeQuery("SELECT * FROM " + table);
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    /**
     * Drops all tables in the database
     */
    public void dropAllTables() {
        for (String table : tables) {
            try {
                stat.execute("DROP TABLE " + table);
            } catch (SQLException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        tables = new ArrayList<>();
    }

    /**
     * Drops a specific table in the database
     *
     * @param name
     */
    public void dropTable(String name) {
        try {
            stat.execute("DROP TABLE " + name);
            tables.remove(name);
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns true if a table with this name exists in the database
     *
     * @param name
     * @return
     */
    public boolean isTableExistant(String name) {
        if (tables.contains(name.toUpperCase())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Closes the connection to the database
     */
    public void closeConnection() {
        try {
            conn.close();
            stat.close();
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generate a random String as placeholder name for a table
     *
     * @param length
     * @return
     */
    private String generateRandomString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String result = "";
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            result += alphabet.charAt(rand.nextInt(alphabet.length()));
        }
        return result;
    }

// <--- Properties operations --->
    /**
     * Get a config property
     *
     * @param key
     */
    public String getConfigProp(String key) {
        return prop.getProperty(key);
    }

    /**
     * Set a config property
     *
     * @param key
     * @param value
     */
    public void setConfigProp(String key, String value) {
        prop.setProperty(key, value);
    }

    /**
     * Saves the config.properties
     */
    public void saveConfigProperties() {
        try {
            FileOutputStream output = new FileOutputStream(System.getProperty("user.home") + File.separator + ".sensormonitor" + File.separator + "config.properties");
            prop.store(output, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get a List of the sensors defined in sensors.properties
     *
     * @return
     * @throws IllegalSensorAmountException
     */
    public List<DataSensor> loadSensors() throws IllegalSensorAmountException {        
        List<DataSensor> result = new ArrayList<>();
        Properties sensors = new Properties();
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("defaultconfig/sensors.properties");
        try {
            sensors.load(stream);
        } catch (IOException ex1) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex1);
        }
        if (Integer.valueOf(sensors.getProperty("sensortyp1")) + Integer.valueOf(sensors.getProperty("sensortyp2")) > 10) {
            throw new IllegalSensorAmountException();
        }
        for (int i = 0; i < Integer.valueOf(sensors.getProperty("sensortyp1")); i++) {
            result.add(new CeBarRoundDataSensor());
        }
        for (int i = 0; i < Integer.valueOf(sensors.getProperty("sensortyp2")); i++) {
            result.add(new CeBarRoundDataSensorV2());
        }
        return result;
    }

// <--- ResourceBundles operations --->        
    /**
     * Get a hashmap of the available language packs with the corresponding
     * Locale
     *
     * @return
     */
    public HashMap<String, Locale> getAvailableLanguages() {
        HashMap<String, Locale> result = new HashMap<>();
        try {
            URI uri = this.getClass().getResource("/lang").toURI();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = new File(file.toString());
                        languages.add(f.getName());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String s : languages) {
            String s1 = s.split("_")[1];
            String abbreviation = s1.split("\\.")[0];
            result.put(new Locale(abbreviation).getDisplayLanguage(new Locale(abbreviation)), new Locale(abbreviation));
        }
        return result;
    }

// <--- CSS Stylesheet operations --->
    /**
     * Get a list of the available stylesheets (excluding base.css)
     *
     * @return
     */
    public List<String> getAvailableSkins() {
        List<String> result = new ArrayList<>();
        try {
            URI uri = this.getClass().getResource("/stylesheets").toURI();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = new File(file.toString());
                        styles.add(f.getName());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String s : styles) {
            String abbreviation = s.split("\\.")[0];
            if (!abbreviation.equalsIgnoreCase("base")) {
                result.add(abbreviation);
            }
        }
        return result;
    }

    /**
     * Get a specific stylesheet from the JAR
     *
     * @param name
     * @return
     */
    public String getStyleSheet(String name) {
        try {
            URL url = this.getClass().getClassLoader().getResource("stylesheets/" + name + ".css");
            return url.toExternalForm();
        } catch (NullPointerException e) {
            setConfigProp("style", "default");
            saveConfigProperties();
            return getStyleSheet("default");
        }
    }

// <--- CSV file operations --->
    /**
     * Export a recording from the database into a CSV file in the selected
     * directory
     *
     * @param recordingname
     * @param exportpath
     */
    public void exportRecording(String recordingname, String exportpath) {
        exportpath += "/" + recordingname + ".csv";
        FileWriter writer;
        try {
            writer = new FileWriter(exportpath);
            StringBuilder sb = new StringBuilder();
            ResultSet rs = loadRecording(recordingname);
            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append(rs.getString(i));
                    sb.append(';');
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Imports a recording from the selected CSV file into the database
     *
     * @param file
     * @throws IllegalTableNameException
     * @throws IOException
     * @throws ParseException
     */
    public void importRecording(File file) throws IllegalTableNameException, IOException, ParseException {
        String name = file.getName();
        String[] namesplit = name.split("\\.");
        if (!namesplit[0].toUpperCase().matches("[a-zA-Z][a-zA-Z0-9_]{1,30}") || isTableExistant(namesplit[0].toUpperCase())) {
            throw new IllegalTableNameException();
        }
        String genericName = createGenericTable();
        renameTable(genericName, namesplit[0]);
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = in.readLine()) != null) {
            String[] linesplit = line.split(";");
            Timestamp timestamp = null;
            String insertstmt = "";
            for (int i = 0; i < linesplit.length; i++) {
                if (i == 0) {
                    timestamp = Timestamp.valueOf(linesplit[i]);
                } else if (i == linesplit.length - 1) {
                    insertstmt += linesplit[i];
                } else {
                    insertstmt += linesplit[i] + ", ";
                }
            }
            this.saveData(namesplit[0], insertstmt, timestamp);
        }
    }

    public ResourceBundle getLangpack() {
        return langpack;
    }

    public void setLangpack(ResourceBundle langpack) {
        this.langpack = langpack;
    }


    
}

