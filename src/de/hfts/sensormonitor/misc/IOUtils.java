package de.hfts.sensormonitor.misc;

import de.hft.ss17.cebarround.*;
import de.hfts.sensormonitor.exceptions.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javafx.stage.DirectoryChooser;

/**
 * IOUtils --- Handles all input/output actions, both from the JAR itself and the
 * OS
 *
 * @author Polarix IT Solutions
 */
public class IOUtils {

    // -------------- PUBLIC FIELDS --------------------------------------------
    /**
     * Format for SensorEvent dates
     */
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * List of availables language ResourceBundle's
     */
    private static Map<String, String> languages = new HashMap<>();
    /**
     * List of available CSS stylesheets, excluding base.css
     */
    private static List<String> styles = new ArrayList<String>();
    /**
     * Configuration properties file loaded from the home.dir/.sensormonitor
     */
    private static Properties configProp;
    /**
     * Language ResourceBundle for all texts visible to the user
     */
    private static ResourceBundle langpack;

    /**
     * List of tables in the connected H2 database
     */
    private static List<String> tables = new ArrayList<>();
    /**
     * Template for the columns of tables for recordings
     */
    private static String columns = "(TIME TIMESTAMP NOT NULL, ROWID INT, SENSORID BIGINT NOT NULL, SENSORTYPE CHARACTER(30) NOT NULL, TEMPERATURE INT, PRESSURE INT, REVOLUTIONS INT);";
    /**
     * Connection to the H2 embedded database
     */
    private static Connection conn;
    /**
     * Statement to execute queries and commands for the database
     */
    private static Statement stat;

    private static final int tableColumns = 7;
    private static final int maxConnectedSensors = 10;

    // -------------- GETTERS & SETTERS ----------------------------------------    
    /**
     * Returns the list of tables in the database
     *
     * @return List of tables in the database
     */
    public static List<String> getTables() {
        return tables;
    }

    /**
     * Get a config configProperty
     *
     * @param key Key of the property
     * @return Value related to the key
     */
    public static String getConfigProp(String key) {
        return configProp.getProperty(key);
    }

    /**
     * Set a config configProperty
     *
     * @param key Key of the property
     * @param value Value related to the key
     */
    public static void setConfigProp(String key, String value) {
        configProp.setProperty(key, value);
    }

    /**
     * Returns the loaded language ResourceBundle
     *
     * @return Display language pack of the application
     */
    public static ResourceBundle getLangpack() {
        return langpack;
    }

    /**
     * Sets the language ResourceBundle
     *
     * @param langpack Display language pack of the application
     */
    public static void setLangpack(ResourceBundle langpack) {
        IOUtils.langpack = langpack;
    }

    /**
     * Get a value from the loaded language ResourceBundle
     *
     * @param key Key of the text
     * @return Text related to the key
     */
    public static String getLangpackString(String key) {
        return langpack.getString(key);
    }

    /**
     * Get a list of all available language ResourceBundle's
     *
     * @return List of all available language ResourceBundle's
     */
    public static Map<String, String> getLanguages() {
        return languages;
    }

    /**
     * Get a list of all available stylesheets excluding base.css
     *
     * @return List of all available stylesheets excluding base.css
     */
    public static List<String> getStyles() {
        return styles;
    }

    /**
     * Get a Statement for interacting with the connected database
     *
     * @return Statement for interacting with the connected database
     */
    public static Statement getStatement() {
        return stat;
    }

    // -------------- DATABASE METHODS -----------------------------------------
    /**
     * Connect to the H2 database located in the folder specified in the
     * configProperties and get a list of all tables
     *
     * @throws java.lang.ClassNotFoundException Thrown when no driver for the
     * database can be found
     * @throws java.sql.SQLException Thrown when an exception occurs while
     * connecting to the database; usually because the database is already
     * connected to another application
     */
    public static void connectDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:" + getConfigProp("savepath") + File.separator + "recordings", "root", "root");
        stat = conn.createStatement();
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, null,
                new String[]{"TABLE"});
        tables.clear();
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
        rs.close();
    }

    /**
     * Create a new table in the database with a generic name
     *
     * @return Name of the table
     */
    public static String createGenericTable() {
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
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
        return genericName;

    }

    /**
     * Rename the specified table (oldname) to newname
     *
     * @param oldname Old name of the table
     * @param newname New name of the table
     * @throws IllegalTableNameException Thrown when the table name cannot be
     * applied because it doesn't match the SQL naming requirements or is
     * already in use
     */
    public static void renameTable(String oldname, String newname) throws IllegalTableNameException {
        if (!newname.toUpperCase().matches("[a-zA-Z][a-zA-Z0-9_]{1,30}") || tables.contains(newname.toUpperCase())) {
            throw new IllegalTableNameException();
        }
        try {
            stat.execute("ALTER TABLE " + oldname + " RENAME TO " + newname);
            tables.add(newname.toUpperCase());
            tables.remove(oldname.toUpperCase());
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save recorded data in the specified table
     *
     * @param table Table in the database
     * @param insertstmt Statement containing the data to be insered (excluding
     * the timestamp)
     * @param timestamp Timestamp of the data
     */
    public static void saveData(String table, String insertstmt, Timestamp timestamp) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " VALUES (?," + insertstmt + ");");
            ps.setTimestamp(1, timestamp);
            ps.execute();
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads a table/recording from the database and returns it as ResultSet
     *
     * @param table Name of the database table/recording
     * @return ResultSet containing the data of the table
     */
    public static ResultSet loadRecording(String table) {
        ResultSet rs = null;
        try {
            rs = stat.executeQuery("SELECT * FROM " + table);
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    /**
     * Clear the list of database tables, get a list of all tables from the
     * database and add them to the list
     */
    public static void reloadTables() {
        tables.clear();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, null,
                    new String[]{"TABLE"});
            tables.clear();
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Drops all tables in the database
     */
    public static void dropAllTables() {
        for (String table : tables) {
            try {
                stat.execute("DROP TABLE " + table);
            } catch (SQLException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        tables = new ArrayList<>();
    }

    /**
     * Drops a specific table in the database
     *
     * @param table Name of the table to be dropped
     */
    public static void dropTable(String table) {
        try {
            stat.execute("DROP TABLE " + table);
            tables.remove(table);
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes the connection to the database
     */
    public static void closeConnection() {
        if (conn != null && stat != null) {
            try {
                conn.close();
                stat.close();
            } catch (SQLException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Generate a random String as placeholder name for a table
     *
     * @param length Length of the String
     * @return String of random characters
     */
    private static String generateRandomString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String result = "";
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            result += alphabet.charAt(rand.nextInt(alphabet.length()));
        }
        return result;
    }

    // -------------- PROPERTIES METHODS ---------------------------------------
    /**
     * Tries to load the configuration and creates it if it doesn't exist yet.
     * IMPORTANT: Do not use LogHandler.LOGGER in this method as it can only be
     * created after loading the configuration
     */
    public static void loadConfiguration() {
        LogHandler.langpack = ResourceBundle.getBundle("lang.logging", new Locale("en"));

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
            File logfolder = new File(currentUsersHomeDir + File.separator + ".sensormonitor" + File.separator + "logs");
            folder.mkdir();
            logfolder.mkdir();
        }

        // Try to load the configProperties from user.home/.sensormonitor
        boolean isPropertiesExistant = true;
        configProp = new Properties();
        try {
            FileInputStream stream = new FileInputStream(currentUsersHomeDir + File.separator + ".sensormonitor" + File.separator + "config.properties");
            configProp.load(stream);
            InputStream stream2 = IOUtils.class.getClassLoader().getResourceAsStream("defaultconfig/config.properties");
            Properties templateProp = new Properties();
            templateProp.load(stream2);
            try {
                validateProperties(configProp, templateProp);
            } catch (IllegalConfigurationException ex) {
                new ExceptionDialog(IOUtils.getLangpackString(ex.getExceptionKey()), null);
                InputStream stream3 = IOUtils.class.getClassLoader().getResourceAsStream("defaultconfig/config.properties");
                try {
                    configProp.load(stream3);
                } catch (IOException ex1) {
                    Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex1);
                }
                isPropertiesExistant = false;
            }
        } catch (FileNotFoundException ex) {
            InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream("defaultconfig/config.properties");
            try {
                configProp.load(stream);
            } catch (IOException ex1) {
                Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex1);
            }
            isPropertiesExistant = false;
        } catch (IOException ex) {
            Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If the configProperties file does not exist...
        if (!isPropertiesExistant) {
            // ... make the user pick a save directory for the database ...
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Database Directory");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File dir = directoryChooser.showDialog(null);

            FileOutputStream output = null;
            if (dir != null) {
                try {
                    // ... set it in the default config.configProperties and save them to home.dir/.sensormonitor
                    configProp.setProperty("savepath", dir.getAbsolutePath());
                    output = new FileOutputStream(currentUsersHomeDir + File.separator + ".sensormonitor" + File.separator + "config.properties");
                    configProp.store(output, null);
                } catch (IOException ex) {
                    Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
            } else {
                System.exit(0);
            }
        }
        langpack = ResourceBundle.getBundle("lang.lang", new Locale(getConfigProp("lang")));
    }

    /**
     * Saves the config.properties
     */
    public static void saveConfigProperties() {
        try {
            FileOutputStream output = new FileOutputStream(System.getProperty("user.home") + File.separator + ".sensormonitor" + File.separator + "config.properties");
            configProp.store(output, null);
        } catch (FileNotFoundException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get a List of the sensors defined in sensors.properties
     *
     * @return List of sensors
     * @throws IllegalSensorAmountException Thrown when the amount of sensors to
     * be connected is larger than the maximum of connected sensors
     * (maxConnectedSensors)
     */
    public static List<BaseSensor> loadSensors() throws IllegalSensorAmountException {
        List<BaseSensor> result = new ArrayList<>();
        Properties sensors = new Properties();
        InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream("defaultconfig/sensors.properties");
        try {
            sensors.load(stream);
        } catch (IOException ex1) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex1);
        }
        if (Integer.valueOf(sensors.getProperty("sensortyp1")) + Integer.valueOf(sensors.getProperty("sensortyp2")) > maxConnectedSensors) {
            throw new IllegalSensorAmountException();
        }
        for (int i = 0; i < Integer.valueOf(sensors.getProperty("sensortyp1")); i++) {
            result.add(new CeBarRoundDataSensor());
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        for (int i = 0; i < Integer.valueOf(sensors.getProperty("sensortyp2")); i++) {
            result.add(new CeBarRoundDataSensorV2());
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    // -------------- RESOURCEBUNDLE METHODS -----------------------------------
    /**
     * Load all available language packs from the JAR and add them to the
     * HashMap languages with the corresponding name
     */
    public static void loadAvailableLanguages() {
        ArrayList<String> langs = new ArrayList<>();
        try {
            URI uri = IOUtils.class.getResource("/lang").toURI();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = new File(file.toString());
                        langs.add(f.getName());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        } catch (URISyntaxException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
        languages.clear();
        for (String s : langs) {
            String s1 = s.split("_")[1];
            String abbreviation = s1.split("\\.")[0];
            languages.put(new Locale(abbreviation).getDisplayLanguage(new Locale(abbreviation)), abbreviation);
        }
    }

    // -------------- CSS STYLESHEET METHODS -----------------------------------
    /**
     * Load a list of the available stylesheets from the JAR (excluding
     * base.css) and add them to the List styles
     */
    public static void loadAvailableStyles() {
        ArrayList<String> stylesUncut = new ArrayList<>();
        try {
            URI uri = IOUtils.class.getResource("/stylesheets").toURI();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = new File(file.toString());
                        stylesUncut.add(f.getName());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        } catch (URISyntaxException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
        styles.clear();
        for (String s : stylesUncut) {
            String abbreviation = s.split("\\.")[0];
            if (!abbreviation.equalsIgnoreCase("base")) {
                styles.add(abbreviation);
            }
        }
    }

    /**
     * Get the path to a specific stylesheet from the JAR
     *
     * @param name Name of the stylesheet (excluding .css)
     * @return Path to a CSS Stylesheet in the JAR
     */
    public static String getStyleSheet(String name) {
        try {
            URL url = IOUtils.class.getClassLoader().getResource("stylesheets/" + name + ".css");
            return url.toExternalForm();
        } catch (NullPointerException e) {
            setConfigProp("style", "default");
            saveConfigProperties();
            return getStyleSheet("default");
        }
    }

    // -------------- CSV FILE METHODS -----------------------------------------
    /**
     * Export a recording from the database into a CSV file in the selected
     * directory
     *
     * @param recordingname Name of the database table/recording
     * @param exportpath Path the folder the recording should be exported to
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void exportRecording(String recordingname, String exportpath) throws IOException, SQLException {
        exportpath += File.separator + recordingname + ".csv";
        FileWriter writer;
        writer = new FileWriter(exportpath);
        StringBuilder sb = new StringBuilder();
        ResultSet rs = loadRecording(recordingname);
        while (rs.next()) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                if (rs.getMetaData().getColumnName(i).equalsIgnoreCase("SENSORTYPE")) {
                    sb.append("'" + rs.getString(i) + "'");
                    sb.append(';');
                } else {
                    sb.append(rs.getString(i));
                    sb.append(';');
                }
            }
            sb.append('\n');
        }
        writer.write(sb.toString());
        writer.close();
    }

    /**
     * Imports a recording from the selected CSV file into the database
     *
     * @param file CSV file containing the recording
     * @throws IllegalTableNameException Thrown when the name of the CSV file
     * cannot be applied as table name because it doesn't match the SQL naming
     * requirements or is already in use
     * @throws IOException
     * @throws ParseException Thrown when data in the file can not be parsed to
     * the appropriate data type
     * @throws ImportRecordingException Thrown when the file is not a CSV file
     * or the amount of columns doesn't match the required amount of table
     * columns (tableColumns)
     */
    public static void importRecording(File file) throws IllegalTableNameException, IOException, ParseException, ImportRecordingException {
        String name = file.getName();
        String[] namesplit = name.split("\\.");
        if (!namesplit[0].toUpperCase().matches("[a-zA-Z][a-zA-Z0-9_]{1,30}") || tables.contains(namesplit[0].toUpperCase())) {
            throw new IllegalTableNameException();
        }
        if (!namesplit[1].equals("csv")) {
            throw new ImportRecordingException();
        }
        String genericName = createGenericTable();
        renameTable(genericName, namesplit[0]);
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = in.readLine()) != null) {
            String[] linesplit = line.split(";");
            if (linesplit.length != tableColumns) {
                dropTable(namesplit[0]);
                in.close();
                throw new ImportRecordingException();
            }
            Timestamp timestamp = null;
            String insertstmt = "";
            for (int i = 0; i < linesplit.length; i++) {
                if (i == 0) {
                    timestamp = new Timestamp(sdf.parse(linesplit[i]).getTime());
                } else if (i == linesplit.length - 1) {
                    insertstmt += linesplit[i];
                } else {
                    insertstmt += linesplit[i] + ", ";
                }
            }
            saveData(namesplit[0], insertstmt, timestamp);
        }
        in.close();
    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Validate that the keys of the properties to check match the keys of the
     * template properties
     *
     * @param check Properties that need to be checked
     * @param template Properties that provide the template for checking
     * @throws de.hfts.sensormonitor.exceptions.IllegalConfigurationException
     * Thrown when the keys of the properties to check do not match the keys of
     * the template
     */
    public static void validateProperties(Properties check, Properties template) throws IllegalConfigurationException {
        List<String> checkKeys = new ArrayList<>();
        for (Object o : check.keySet()) {
            checkKeys.add(o.toString());
        }
        List<String> templateKeys = new ArrayList<>();
        for (Object o : template.keySet()) {
            templateKeys.add(o.toString());
        }
        Collections.sort(checkKeys);
        Collections.sort(templateKeys);
        if (!checkKeys.equals(templateKeys)) {
            throw new IllegalConfigurationException();
        }
    }

}
