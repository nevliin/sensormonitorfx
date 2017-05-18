package de.hfts.sensormonitor.main;

import de.hft.ss17.cebarround.BaseSensor;
import de.hft.ss17.cebarround.CeBarRoundEvent;
import de.hft.ss17.cebarround.CeBarRoundObserver;
import de.hfts.sensormonitor.chart.SensorChart;
import de.hfts.sensormonitor.chart.GraphPoint;
import de.hfts.sensormonitor.chart.SensorChartData;
import de.hfts.sensormonitor.exceptions.DatabaseConnectException;
import de.hfts.sensormonitor.exceptions.IllegalSensorAmountException;
import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.exceptions.SensorMonitorException;
import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.table.SensorTable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.*;
import javafx.animation.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;

/**
 * Central class defining the structure of the main window and the actions
 *
 * @author Polarix IT Solutions
 */
public class SensorMonitor extends Scene implements CeBarRoundObserver<CeBarRoundEvent> {

    TabPane tabpane;   // TabbedPane in the center
    HashMap<String, SensorChartData> chartdata = new HashMap<>();

    LiveRecording recording; // Current recording (null if none is running)
    List<BaseSensor> sensors; // Sensor monitored in real time
    IO io; // Instance of IO, handles loading/saving from properties and the DB
    ResourceBundle langpack; // LanguagePack in the language saved in config.properties
    HashMap<String, Locale> availableLangs = new HashMap<>();
    List<String> availableSkins = new ArrayList<>();

    Stage parentStage;

    Stage recordingswindow;
    Stage settingswindow;

    boolean isRecording = false;
    boolean recordTemperature = true;
    boolean recordPressure = true;
    boolean recordRevolutions = true;

    public void sensorDataEventListener(CeBarRoundEvent cbre) {
    }

    /**
     * Inner class for a recording of the data from the real time sensor
     */
    public class LiveRecording {

        private int rowid;
        private String genericName;
        private IO io;

        /**
         *
         * @param sensor
         * @param io
         */
        public LiveRecording(BaseSensor sensor, IO io) {
            genericName = io.createGenericTable();
            this.io = io;
            this.rowid = 0;
        }

        /**
         *
         * @param name
         * @throws de.hfts.sensormonitor.exceptions.IllegalTableNameException
         */
        public void finalizeName(String name) throws IllegalTableNameException {
            io.renameTable(genericName, name);
        }

        /**
         *
         * @param data
         * @param sensor
         */
        public void recordData(SensorData data, BaseSensor sensor) {
            String insertstmt = rowid + ", " + Integer.toString((int) sensor.getUniquesensoridentifier()) + ", " + Integer.toString((int) sensor.getSensortypecode());
            if (recordTemperature) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Temperature"));
            } else {
                insertstmt += ", null";
            }
            if (recordPressure) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Pressure"));
            } else {
                insertstmt += ", null";
            }
            if (recordRevolutions) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Revolutions"));
            } else {
                insertstmt += ", null";
            }

            io.saveData(genericName, insertstmt, data.getTime());
            rowid++;
        }

        /**
         *
         * @return
         */
        public String getGenericName() {
            return genericName;
        }

        /**
         *
         * @return
         */
        public int getRowid() {
            return rowid;
        }

    }

    /**
     * Inner class for a recording loaded from the database
     */
    public class Recording {

        private Timestamp firsttimestamp; // First timestamp of the recording
        private Timestamp lasttimestamp; // Last timestamp of the recording
        private HashMap<String, String> sensors = new HashMap<>(); // A map with the sensorids as key and the corresponding sensortypes as value
        private HashMap<String, List<GraphPoint>> temperature_points = new HashMap<>(); // A map with the sensorids as key and the corresponding GraphPoints as value
        private HashMap<String, List<GraphPoint>> pressure_points = new HashMap<>(); // ^
        private HashMap<String, List<GraphPoint>> revolutions_points = new HashMap<>(); // ^

        /**
         *
         * @param name
         */
        public Recording(String name) {
            ResultSet recording = io.loadRecording(name);

            try {
                while (recording.next()) {
                    if (firsttimestamp == null) {
                        firsttimestamp = recording.getTimestamp("TIME");
                    }
                    if (sensors.get(recording.getString("SENSORID")) == null) {
                        sensors.put(recording.getString("SENSORID"), recording.getString("SENSORTYPE"));
                        temperature_points.put(recording.getString("SENSORID"), new ArrayList<>());
                        pressure_points.put(recording.getString("SENSORID"), new ArrayList<>());
                        revolutions_points.put(recording.getString("SENSORID"), new ArrayList<>());
                    }

                    lasttimestamp = recording.getTimestamp("TIME");

                    GraphPoint temperature = new GraphPoint((lasttimestamp.getTime() - firsttimestamp.getTime()) / 1000.0, 0);
                    try {
                        temperature.y = Double.valueOf(recording.getString("TEMPERATURE"));
                    } catch (NullPointerException e) {
                        temperature.setIsEmpty(true);
                    }

                    GraphPoint pressure = new GraphPoint((lasttimestamp.getTime() - firsttimestamp.getTime()) / 1000.0, 0);
                    try {
                        pressure.y = Double.valueOf(recording.getString("PRESSURE"));
                    } catch (NullPointerException e) {
                        pressure.setIsEmpty(true);
                    }

                    GraphPoint revolutions = new GraphPoint((lasttimestamp.getTime() - firsttimestamp.getTime()) / 1000.0, 0);
                    try {
                        revolutions.y = Double.valueOf(recording.getString("REVOLUTIONS"));
                    } catch (NullPointerException e) {
                        revolutions.setIsEmpty(true);
                    }

                    temperature_points.get(recording.getString("SENSORID")).add(temperature);
                    pressure_points.get(recording.getString("SENSORID")).add(pressure);
                    revolutions_points.get(recording.getString("SENSORID")).add(revolutions);

                }
            } catch (SQLException ex) {
                Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         *
         * @return
         */
        public Timestamp getFirsttimestamp() {
            return firsttimestamp;
        }

        /**
         *
         * @return
         */
        public Timestamp getLasttimestamp() {
            return lasttimestamp;
        }

        /**
         *
         * @return
         */
        public HashMap<String, String> getSensors() {
            return sensors;
        }

        /**
         *
         * @return
         */
        public HashMap<String, List<GraphPoint>> getTemperature_points() {
            return temperature_points;
        }

        /**
         *
         * @return
         */
        public HashMap<String, List<GraphPoint>> getPressure_points() {
            return pressure_points;
        }

        /**
         *
         * @return
         */
        public HashMap<String, List<GraphPoint>> getRevolutions_points() {
            return revolutions_points;
        }

    }

    /**
     * Popup window for displaying all recordings saved in the database
     */
    public class TableList extends BorderPane {

        List<String> tables; // List of all tables in the connected Embedded Database
        ListView<String> list; // JavaFX ListView displaying the tables

        ObservableList<String> items;

        /**
         *
         * @param tables
         */
        public TableList(List<String> tables) {
            super();
            this.tables = tables;
            this.setId("table-list");
            list = new ListView<String>();
            items = FXCollections.observableArrayList(tables);
            list.setItems(items);
            list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            GridPane grid_buttons = new GridPane();
            grid_buttons.setHgap(5);
            grid_buttons.setPadding(new Insets(5, 5, 5, 5));

            Button display = new Button(langpack.getString("display_recording"));
            Button delete = new Button(langpack.getString("delete_recording"));
            Button export = new Button(langpack.getString("export"));
            grid_buttons.add(display, 0, 0);
            grid_buttons.add(delete, 1, 0);
            grid_buttons.add(export, 2, 0);

            display.setOnAction(eh -> {
                displayRecordings();
            });
            delete.setOnAction(eh -> {
                deleteRecordings();
            });
            export.setOnAction(eh -> {
                exportRecordings();
            });

            this.setCenter(list);
            this.setBottom(grid_buttons);
        }

        private void deleteRecordings() {
            List<String> selectedrecordings = list.getSelectionModel().getSelectedItems();
            for (String recording : selectedrecordings) {
                io.dropTable(recording);
                try {
                    removeTab(recording);
                } catch (IndexOutOfBoundsException e) {
                    // catches the Exception if the tab is not open
                }
            }
            TableList.this.tables = io.getTables();
            items = FXCollections.observableArrayList(tables);
            list.setItems(items);
        }

        private void displayRecordings() {
            List<String> selectedrecordings = list.getSelectionModel().getSelectedItems();
            for (String recording : selectedrecordings) {
                SensorMonitor.this.displayRecording(recording);
            }
        }

        private void exportRecordings() {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Database Directory");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File dir = directoryChooser.showDialog(null);

            if (dir != null) {
                List<String> selectedrecordings = list.getSelectionModel().getSelectedItems();
                for (String recording : selectedrecordings) {
                    io.exportRecording(recording, dir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Tab with standard components to be displayed in the SettingsWindow
     */
    public abstract class SettingsTab extends Tab {

        private boolean isRebootNecessary = false;
        GridPane gridpane;
        Button button_save;
        Label label_error;

        /**
         *
         * @param name
         * @param requiresReboot
         * @param errorMessage
         * @param rowsfilled
         */
        public SettingsTab(String name, boolean requiresReboot, String errorMessage, int rowsfilled) {
            super();

            // Rotate the tab text
            Label l = new Label(name);
            l.setRotate(90);
            StackPane stp = new StackPane(new Group(l));
            this.setGraphic(stp);

            gridpane = new GridPane();
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setPercentWidth(50);
            gridpane.getColumnConstraints().addAll(column, column);
            gridpane.setId("grid-pane");

            this.label_error = new Label(errorMessage);
            label_error.setId("label-small-font");

            this.fillPane(gridpane);
            this.addSaveOption(rowsfilled);
            this.setContent(gridpane);
        }

        // Abstract method defining the content of the Tab
        /**
         *
         * @param gridpane
         */
        public abstract void fillPane(GridPane gridpane);

        // Abstract method defining the actions when the save button is pressed
        /**
         *
         */
        public abstract void save();

        // Add standard components to the Tab
        private void addSaveOption(int rowsfilled) {
            for (int i = 0; i < 5 - rowsfilled; i++) {
                Label filler = new Label("");
                gridpane.add(filler, 0, i + rowsfilled);
            }

            button_save = new Button(langpack.getString("save"));
            button_save.setMaxWidth(Double.MAX_VALUE);
            gridpane.add(button_save, 1, 5);
            gridpane.add(label_error, 0, 6, 2, 1);

            button_save.setOnAction(eh -> {
                save();
            });
        }

        // Method to hide the error label
        /**
         *
         */
        public void hideError() {
            this.label_error.setVisible(false);
        }

        public boolean isRebootNecessary() {
            return isRebootNecessary;
        }

        public void setIsRebootNecessary(boolean isRebootNecessary) {
            this.isRebootNecessary = isRebootNecessary;
        }

    }

    /**
     * Constructs the windows, instantiates the Sensor and connects to the
     * database and properties.
     *
     * @param pane
     * @param parentStage
     */
    public SensorMonitor(BorderPane pane, Stage parentStage) {
        super(pane);
        this.tabpane = new TabPane();
        this.parentStage = parentStage;
        pane.setCenter(tabpane);
        loadResources();
        try {
            io.connectDB();
            sensors = io.loadSensors();
            initWindow();
        } catch (DatabaseConnectException e) {
            System.exit(0);
        } catch (IllegalSensorAmountException ex) {
            System.exit(0);
        }
    }

    /**
     * Creates IO and loads the Language Pack
     */
    private void loadResources() {
        langpack = ResourceBundle.getBundle("lang.lang", new Locale("en"));
        SensorMonitorException.langpack = this.langpack;
        io = new IO();
        availableLangs = io.getAvailableLanguages();
        availableSkins = io.getAvailableSkins();

        // Load the base stylesheet and the set skin
        String base = io.getStyleSheet("base");
        String skin = io.getStyleSheet(io.getConfigProp("style"));
        this.getStylesheets().addAll(base, skin);

        langpack = ResourceBundle.getBundle("lang.lang", new Locale(io.getConfigProp("lang")));
        SensorMonitorException.langpack = this.langpack;
    }

// <--- Main window parts --->
    /**
     * Initiates and fills the main window
     */
    private void initWindow() {
        URL url = getClass().getClassLoader().getResource("images/logo_rose_marble.png");
        Image image = new Image(url.toExternalForm());
        parentStage.getIcons().add(image);

        // Add the menubar
        ((BorderPane) this.getRoot()).setTop(initMenuBar());

        GridPane sensormonitor = initSensorDataDisplay(true, null);

        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        TabPane mainpane = new TabPane();
        mainpane.getStyleClass().add("real-time-pane");

        Tab tab_overview = new Tab(langpack.getString("overview"), sensormonitor);
        tab_overview.setClosable(false);

        mainpane.getTabs().add(tab_overview);

        createChartSpecificTabs(mainpane, chartdata);

        Tab realtime = new Tab("SensorMonitor", mainpane);
        mainpane.getStyleClass().add("real-time-tab");
        realtime.setClosable(false);
        tabpane.getTabs().add(realtime);

    }

    /**
     * Initiates and returns the MenuBar
     *
     * @return
     */
    private MenuBar initMenuBar() {

        MenuItem menu_file_quit = new MenuItem(langpack.getString("quit"));
        menu_file_quit.setOnAction(eh -> {
            quitProgramm();
        });

        MenuItem menu_file_reboot = new MenuItem("Reboot");
        menu_file_reboot.setOnAction(eh -> {
            rebootWindow();
        });

        Menu menu_file = new Menu(langpack.getString("file"));
        menu_file.getItems().addAll(menu_file_quit, menu_file_reboot);

        MenuItem menu_recordings_showall = new MenuItem(langpack.getString("show_all"));
        menu_recordings_showall.setOnAction(eh -> {
            if (recordingswindow == null) {
                initRecordingsListWindow();
            } else {
                recordingswindow.toFront();
            }
        });

        MenuItem menu_recordings_import = new MenuItem(langpack.getString("import_recording"));
        menu_recordings_import.setOnAction(eh -> {
            FileChooser fs = new FileChooser();
            fs.setTitle("select_csv_file");
            fs.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fs.showOpenDialog(null);
            if (file != null) {
                try {
                    io.importRecording(file);
                    displayRecording(file.getName().split("\\.")[0].toUpperCase());
                } catch (IllegalTableNameException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(langpack.getString("exception_tablenameexception"));
                    alert.showAndWait();
                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                } catch (ParseException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        MenuItem menu_recordings_deleteall = new MenuItem(langpack.getString("delete_all"));
        menu_recordings_deleteall.setOnAction(eh -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(langpack.getString("delete_all"));
            alert.setHeaderText("");
            alert.setContentText(langpack.getString("confirm_delete_all_recordings"));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                io.dropAllTables();
            }
        });

        Menu menu_recordings = new Menu(langpack.getString("recordings"));
        menu_recordings.getItems().addAll(menu_recordings_showall, menu_recordings_import, menu_recordings_deleteall);

        MenuItem menu_help_settings = new MenuItem(langpack.getString("settings"));
        menu_help_settings.setOnAction(eh -> {
            if (settingswindow == null) {
                initSettingsWindow();
            } else {
                settingswindow.toFront();
            }
        });

        Menu menu_help = new Menu(langpack.getString("help"));
        menu_help.getItems().addAll(menu_help_settings);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menu_file, menu_recordings, menu_help);

        return menuBar;
    }

    /**
     * Initiates and returns a GridPane display of data in real time (isRealtime
     * = true) or a recording from the database (isRealtime = false). Set
     * recordingname = null for real time displays.
     *
     * @param isRealtime
     * @param record
     * @return
     */
    private GridPane initSensorDataDisplay(boolean isRealtime, Recording record) {
        GridPane gridPane_OverView = new GridPane();
        gridPane_OverView.setId("grid-pane");

        RowConstraints row1 = new RowConstraints(25);
        row1.setVgrow(Priority.NEVER);
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        gridPane_OverView.getRowConstraints().addAll(row1, row1, row1, row2, row1, row1);

        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        gridPane_OverView.getColumnConstraints().addAll(column, column, column);

        List<String> sensorids = new ArrayList<>();
        for (BaseSensor s : sensors) {
            sensorids.add(Integer.toString((int) s.uniqueId()));
        }
        CheckComboBox combobox_sensorid = new CheckComboBox(FXCollections.observableArrayList(sensorids));
        gridPane_OverView.add(combobox_sensorid, 0, 0);

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        gridPane_OverView.add(separator, 0, 1, 3, 1);

        double realtime_timeframe = 0 - Double.valueOf(io.getConfigProp("realtime_timeframe"));
        SensorChart graph_temperature = new SensorChart(realtime_timeframe, 0, 0, 100, "sec", "°C", langpack, langpack.getString("temperature"));
        graph_temperature.setYScaleBounds(Double.valueOf(io.getConfigProp("temperature_yscalemin")), Double.valueOf(io.getConfigProp("temperature_yscalemax")));
        gridPane_OverView.add(graph_temperature, 0, 3);

        SensorChart graph_pressure = new SensorChart(realtime_timeframe, 0, 900, 1100, "sec", "hPa", langpack, langpack.getString("pressure"));
        graph_pressure.setYScaleBounds(Double.valueOf(io.getConfigProp("pressure_yscalemin")), Double.valueOf(io.getConfigProp("pressure_yscalemax")));
        gridPane_OverView.add(graph_pressure, 1, 3);

        SensorChart graph_revolution = new SensorChart(realtime_timeframe, 0, -1000, 7500, "sec", "RPM", langpack, langpack.getString("revolutions"));
        graph_revolution.setYScaleBounds(Double.valueOf(io.getConfigProp("revolutions_yscalemin")), Double.valueOf(io.getConfigProp("revolutions_yscalemax")));
        gridPane_OverView.add(graph_revolution, 2, 3);

        List<String> arraylist_sensors = null;
        if (isRealtime) {
            chartdata.put(langpack.getString("temperature"), graph_temperature.getChartData());
            chartdata.put(langpack.getString("pressure"), graph_pressure.getChartData());
            chartdata.put(langpack.getString("revolutions"), graph_revolution.getChartData());
            String[] s = new String[sensorids.size()];
            graph_temperature.setXScaleBounds(Double.valueOf(io.getConfigProp("realtime_xscalemin")), Double.valueOf(io.getConfigProp("realtime_xscalemax")));
            graph_temperature.setUpGraphs(sensorids.toArray(s));
            graph_temperature.setCreateSymbols(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
            graph_pressure.setXScaleBounds(Double.valueOf(io.getConfigProp("realtime_xscalemin")), Double.valueOf(io.getConfigProp("realtime_xscalemax")));
            graph_pressure.setUpGraphs(sensorids.toArray(s));
            graph_pressure.setCreateSymbols(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
            graph_revolution.setXScaleBounds(Double.valueOf(io.getConfigProp("realtime_xscalemin")), Double.valueOf(io.getConfigProp("realtime_xscalemax")));
            graph_revolution.setUpGraphs(sensorids.toArray(s));
            graph_revolution.setCreateSymbols(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        } else {
            double timeframe = (record.getLasttimestamp().getTime() - record.getFirsttimestamp().getTime()) / 1000.0;
            timeframe = Math.round(timeframe);
            graph_temperature.getChartData().setXMin(0);
            graph_temperature.getChartData().setXMax(timeframe);
            graph_temperature.setXScaleBounds(0, timeframe);

            graph_pressure.getChartData().setXMin(0);
            graph_pressure.getChartData().setXMax(timeframe);
            graph_pressure.setXScaleBounds(0, timeframe);

            graph_revolution.getChartData().setXMin(0);
            graph_revolution.getChartData().setXMax(timeframe);
            graph_revolution.setXScaleBounds(0, timeframe);

            arraylist_sensors = new ArrayList<>(record.getSensors().keySet());
            for (String sensor : arraylist_sensors) {
                graph_temperature.addGraph(record.getTemperature_points().get(sensor), sensor);
                graph_pressure.addGraph(record.getPressure_points().get(sensor), sensor);
                graph_revolution.addGraph(record.getRevolutions_points().get(sensor), sensor);
            }
        }

        /*Label label_temp_avg = new Label(langpack.getString("average") + " " + graph_temperature.getYAverage(null) + " " + graph_temperature.getChartData().getYUnit());
        label_temp_avg.setMaxWidth(Double.MAX_VALUE);
        label_temp_avg.setAlignment(Pos.CENTER);
        gridPane_OverView.add(label_temp_avg, 0, 4);

        Label label_pressure_avg = new Label(langpack.getString("average") + " " + graph_pressure.getYAverage(null) + " " + graph_pressure.getChartData().getYUnit());
        label_pressure_avg.setMaxWidth(Double.MAX_VALUE);
        label_pressure_avg.setAlignment(Pos.CENTER);
        gridPane_OverView.add(label_pressure_avg, 1, 4);

        Label label_revolution_avg = new Label(langpack.getString("average") + " " + graph_revolution.getYAverage(null) + " " + graph_revolution.getChartData().getYUnit());
        label_revolution_avg.setMaxWidth(Double.MAX_VALUE);
        label_revolution_avg.setAlignment(Pos.CENTER);
        gridPane_OverView.add(label_revolution_avg, 2, 4);*/

        if (!isRealtime) {
            // Change the sensors in the CheckComboBox to the sensors of the recording
            combobox_sensorid.getItems().setAll(FXCollections.observableArrayList(record.getSensors().keySet()));

            Label label_timeframe = new Label(record.getFirsttimestamp().toString() + " - " + record.getLasttimestamp().toString());
            gridPane_OverView.add(label_timeframe, 2, 0);
        }

        combobox_sensorid.getCheckModel().checkAll();

        if (isRealtime) {
            combobox_sensorid.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    List<String> checkedsensors = new ArrayList<>(combobox_sensorid.getCheckModel().getCheckedItems());
                    for (String s : sensorids) {
                        if (checkedsensors.contains(s)) {
                            for (SensorChartData data : chartdata.values()) {
                                data.setGraphVisible(s, true);
                            }
                        } else {
                            for (SensorChartData data : chartdata.values()) {
                                data.setGraphVisible(s, false);
                            }
                        }
                    }
                }
            });

            separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            gridPane_OverView.add(separator, 0, 5, 3, 1);
            gridPane_OverView.add(initRecordingControlsPanel(), 0, 6, 3, 1);
        } else {
            final List<String> arraylist_sensorids = arraylist_sensors;
            combobox_sensorid.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    List<String> checkedsensors = new ArrayList<>(combobox_sensorid.getCheckModel().getCheckedItems());
                    for (String s : arraylist_sensorids) {
                        if (checkedsensors.contains(s)) {
                            graph_temperature.getChartData().setGraphVisible(s, true);
                            graph_pressure.getChartData().setGraphVisible(s, true);
                            graph_revolution.getChartData().setGraphVisible(s, true);
                        } else {
                            graph_temperature.getChartData().setGraphVisible(s, false);
                            graph_pressure.getChartData().setGraphVisible(s, false);
                            graph_revolution.getChartData().setGraphVisible(s, false);
                        }
                    }
                }
            });
        }

        // Create a timeline to constantly update the SensorChartData (will be replaced with a Listener)
        if (isRealtime) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), ae -> {
                for (BaseSensor s : sensors) {
                    s.createNewData();
                    if (isRecording) {
                        recording.recordData(s.getData(), s);
                    }
                    for (SensorChartData data : chartdata.values()) {
                        if (data.getChartname().equalsIgnoreCase(langpack.getString("temperature"))) {
                            data.moveLeftSpecific(1, s.getData().getData().get("Temperature"), Integer.toString((int) s.getUniquesensoridentifier()));
                        }
                        if (data.getChartname().equalsIgnoreCase(langpack.getString("pressure"))) {
                            data.moveLeftSpecific(1, s.getData().getData().get("Pressure"), Integer.toString((int) s.getUniquesensoridentifier()));
                        }
                        if (data.getChartname().equalsIgnoreCase(langpack.getString("revolutions"))) {
                            data.moveLeftSpecific(1, s.getData().getData().get("Revolutions"), Integer.toString((int) s.getUniquesensoridentifier()));
                        }
                    }
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
        return gridPane_OverView;
    }

    /**
     * Initiates and returns a GridPane with all controls for starting, stopping
     * and configuring a recording.
     *
     * @return
     */
    private GridPane initRecordingControlsPanel() {
        GridPane panel_recordings = new GridPane();
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        panel_recordings.getColumnConstraints().addAll(column, column, column, column, column, column);
        panel_recordings.setId("recording-pane");

        Button button_startrecording = new Button(langpack.getString("start_recording"));
        panel_recordings.add(button_startrecording, 0, 0);

        Button button_stoprecording = new Button(langpack.getString("stop_recording"));
        button_stoprecording.setDisable(true);
        panel_recordings.add(button_stoprecording, 1, 0);

        Label label_record = new Label(langpack.getString("recording_selection") + "     ");
        panel_recordings.add(label_record, 2, 0);

        CheckBox checkbox_temperature = new CheckBox(langpack.getString("temperature"));
        checkbox_temperature.setSelected(true);
        panel_recordings.add(checkbox_temperature, 3, 0);

        CheckBox checkbox_pressure = new CheckBox(langpack.getString("pressure"));
        checkbox_pressure.setSelected(true);
        panel_recordings.add(checkbox_pressure, 4, 0);

        CheckBox checkbox_revolutions = new CheckBox(langpack.getString("revolutions"));
        checkbox_revolutions.setSelected(true);
        panel_recordings.add(checkbox_revolutions, 5, 0);

        button_startrecording.setOnAction(al -> {
            recording = new LiveRecording(new BaseSensor(), io);
            isRecording = true;
            button_stoprecording.setDisable(false);
            button_startrecording.setDisable(true);
        });

        button_stoprecording.setOnAction(al -> {
            isRecording = false;
            saveRecording();
            button_stoprecording.setDisable(true);
            button_startrecording.setDisable(false);
        });

        checkbox_temperature.setOnAction(cl -> {
            recordTemperature = checkbox_temperature.isSelected() ? true : false;
        });

        checkbox_pressure.setOnAction(cl -> {
            recordPressure = checkbox_pressure.isSelected() ? true : false;
        });

        checkbox_revolutions.setOnAction(cl -> {
            recordRevolutions = checkbox_revolutions.isSelected() ? true : false;
        });

        return panel_recordings;
    }

    /**
     * Create additional tabs for the realtime TabPane to display each
     * SensorChartData as a LineChart and TableView
     *
     * @param mainpane
     * @param chartdata
     */
    private void createChartSpecificTabs(TabPane mainpane, HashMap<String, SensorChartData> chartdata) {
        for (SensorChartData data : chartdata.values()) {
            GridPane gridPane = new GridPane();
            gridPane.setId("grid-pane");

            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setPercentWidth(50);
            gridPane.getColumnConstraints().addAll(column, column);

            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            RowConstraints row2 = new RowConstraints();
            row2.setVgrow(Priority.NEVER);
            gridPane.getRowConstraints().addAll(row2, row2, row);

            HBox infoBar = new HBox();

            Label label_title = new Label(data.getChartname());
            infoBar.getChildren().add(label_title);

            gridPane.add(infoBar, 0, 0, 2, 1);

            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);

            gridPane.add(separator, 0, 1, 2, 1);

            String[] sensors_string = new String[sensors.size()];
            for (int i = 0; i < sensors_string.length; i++) {
                sensors_string[i] = (Integer.toString((int) sensors.get(i).getUniquesensoridentifier()));
            }
            SensorChart chartduplicate = new SensorChart(data);
            chartduplicate.setUpGraphs(sensors_string);
            gridPane.add(chartduplicate, 0, 2);

            SensorTable tableSensorChartData = new SensorTable(data, langpack);
            tableSensorChartData.getTable().setEditable(false);

            gridPane.add(tableSensorChartData.getTable(), 1, 2);

            Tab tab = new Tab(data.getChartname(), gridPane);
            tab.setClosable(false);

            mainpane.getTabs().add(tab);
        }
    }

    /**
     * Initiates a Tab displaying the data of the specified recording and adds
     * it to the TabPane.
     *
     * @param recordingname
     */
    public void displayRecording(String recordingname) {
        Recording record = new Recording(recordingname);
        GridPane gp = initSensorDataDisplay(false, record);
        Tab tab_recording = new Tab(recordingname, gp);
        tabpane.getTabs().add(tab_recording);
        for (Node n : gp.getChildren()) {
            if (n instanceof SensorChart) {
                ((SensorChart) n).installTooltips();
            }
        }
    }

// <--- Additional windows --->
    /**
     * Initiates a new window displaying a list of all recordings found in the
     * database. Includes options to delete and display recordings.
     */
    private void initRecordingsListWindow() {
        recordingswindow = new Stage();
        recordingswindow.setOnCloseRequest(eh -> {
            recordingswindow = null;
        });
        recordingswindow.setTitle(langpack.getString("settings"));

        TableList recordings = new TableList(io.getTables());

        Scene scene = new Scene(recordings);
        scene.getStylesheets().addAll(this.getStylesheets());

        recordingswindow.setScene(scene);
        recordingswindow.sizeToScene();

        recordingswindow.show();
    }

    /**
     * Initiates a new window displaying a tabpane with various settings that
     * can be adjusted
     */
    private void initSettingsWindow() {

        settingswindow = new Stage();

        settingswindow.setTitle(langpack.getString("settings"));

        TabPane settingtabs = new TabPane();
        settingtabs.setId("settings-tabpane");
        settingtabs.setSide(Side.LEFT);
        settingtabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        settingtabs.setRotateGraphic(true);
        settingtabs.setTabMinHeight(100);
        settingtabs.setTabMaxHeight(100);

        SettingsTab tab_language = new SettingsTab(langpack.getString("language"), true, langpack.getString("applied_upon_closing"), 1) {

            ComboBox combobox_language;

            @Override
            public void fillPane(GridPane gridpane) {
                Label label_language = new Label(langpack.getString("language") + ": ");
                gridpane.add(label_language, 0, 0);

                List<String> displaylangs = new ArrayList<>();
                for (String s : availableLangs.keySet()) {
                    displaylangs.add(s);
                }
                combobox_language = new ComboBox(FXCollections.observableArrayList(displaylangs));
                combobox_language.setEditable(false);
                combobox_language.setValue(new Locale(io.getConfigProp("lang")).getDisplayLanguage(new Locale(io.getConfigProp("lang"))));
                //combobox_language.setFont(popupfont);
                gridpane.add(combobox_language, 1, 0);

            }

            @Override
            public void save() {
                if (!availableLangs.get(combobox_language.getValue()).getLanguage().equalsIgnoreCase(new Locale(io.getConfigProp("lang")).getLanguage())) {
                    io.setConfigProp("lang", availableLangs.get(combobox_language.getValue()).getLanguage());
                    setIsRebootNecessary(true);
                    label_error.setVisible(true);
                } else {
                    io.setConfigProp("lang", availableLangs.get(combobox_language.getValue()).getLanguage());
                    setIsRebootNecessary(false);
                    label_error.setVisible(false);
                }
            }
        };
        SettingsTab tab_graphs = new SettingsTab(langpack.getString("graphs"), false, langpack.getString("error_timeframe_invalid"), 1) {

            TextField textfield_timeframe;
            CheckBox checkBox_displaySymbols;

            @Override
            public void fillPane(GridPane gridpane) {
                Label label_timeframe = new Label(langpack.getString("displayedtimeframe") + ": ");
                gridpane.add(label_timeframe, 0, 0);

                textfield_timeframe = new TextField(io.getConfigProp("realtime_timeframe"));
                gridpane.add(textfield_timeframe, 1, 0);

                checkBox_displaySymbols = new CheckBox(langpack.getString("display_point_symbols"));
                gridpane.add(checkBox_displaySymbols, 0, 1, 2, 1);
                checkBox_displaySymbols.setSelected(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
            }

            @Override
            public void save() {
                if (Integer.valueOf(textfield_timeframe.getText()) != Integer.valueOf(io.getConfigProp("realtime_timeframe"))) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(langpack.getString("error_timeframe_invalid"));
                    try {
                        if (Double.valueOf(textfield_timeframe.getText()) > (Double.valueOf(io.getConfigProp("realtime_xscalemax")) - Double.valueOf(io.getConfigProp("realtime_xscalemin"))) || Double.valueOf(textfield_timeframe.getText()) <= 0) {
                            alert.showAndWait();
                        } else {
                            io.setConfigProp("realtime_timeframe", textfield_timeframe.getText());
                            io.saveConfigProperties();
                            for (SensorChartData s : chartdata.values()) {
                                s.setXMin(0 - Double.valueOf(textfield_timeframe.getText()));
                            }
                        }
                    } catch (NumberFormatException e) {
                        alert.showAndWait();
                    }
                }
                if (checkBox_displaySymbols.isSelected() != Boolean.valueOf(io.getConfigProp("displayPointSymbols"))) {
                    io.setConfigProp("displayPointSymbols", Boolean.toString(checkBox_displaySymbols.isSelected()));
                    io.saveConfigProperties();
                    setIsRebootNecessary(true);
                    label_error.setVisible(true);
                } else {
                    setIsRebootNecessary(false);
                    label_error.setVisible(false);
                }
            }
        };
        SettingsTab tab_appearance = new SettingsTab(langpack.getString("appearance"), true, langpack.getString("applied_upon_closing"), 1) {

            ComboBox combobox_appearance;

            @Override
            public void fillPane(GridPane gridpane) {
                Label label_appearance = new Label(langpack.getString("appearance") + ": ");
                gridpane.add(label_appearance, 0, 0);

                combobox_appearance = new ComboBox(FXCollections.observableArrayList(availableSkins));
                combobox_appearance.setEditable(false);
                combobox_appearance.setValue(io.getConfigProp("style"));
                //combobox_language.setFont(popupfont);
                gridpane.add(combobox_appearance, 1, 0);
            }

            @Override
            public void save() {
                if (combobox_appearance.getValue().equals(io.getConfigProp("style"))) {
                    io.setConfigProp("style", (String) combobox_appearance.getValue());
                    setIsRebootNecessary(false);
                    label_error.setVisible(false);
                } else {
                    io.setConfigProp("style", (String) combobox_appearance.getValue());
                    setIsRebootNecessary(true);
                    label_error.setVisible(true);
                }
            }
        };

        settingtabs.getTabs().addAll(tab_language, tab_graphs, tab_appearance);

        Scene scene = new Scene(settingtabs);
        scene.getStylesheets().addAll(this.getStylesheets());

        settingswindow.setScene(scene);
        settingswindow.sizeToScene();
        settingswindow.show();

        for (Tab tab : settingtabs.getTabs()) {
            SettingsTab settingstab = (SettingsTab) tab;
            settingstab.hideError();
        }

        // Center the window
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        settingswindow.setX((primScreenBounds.getWidth() - settingswindow.getWidth()) / 2);
        settingswindow.setY((primScreenBounds.getHeight() - settingswindow.getHeight()) / 2);

        settingswindow.setOnCloseRequest(eh -> {
            for (Tab tab : settingtabs.getTabs()) {
                SettingsTab settingstab = (SettingsTab) tab;
                if (settingstab.isRebootNecessary) {
                    io.saveConfigProperties();
                    SensorMonitor.this.rebootWindow();
                }
            }
            settingswindow.hide();
            settingswindow = null;
        });

    }

// <--- Actions triggered by the user --->
    /**
     * Save the current recording
     */
    private void saveRecording() {
        if (recording.getRowid() == 0) {
            io.dropTable(recording.getGenericName());
        } else {
            boolean deleteRecording = false;
            boolean isNameInvalid = true;
            Optional<String> newname = null;
            boolean secondtry = false;
            do {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle(langpack.getString("save_recording_title"));
                dialog.setContentText(langpack.getString("save_recording"));
                if (secondtry) {
                    dialog.setHeaderText(langpack.getString("exception_tablenameexception"));
                } else {
                    dialog.setHeaderText(langpack.getString("save_recording_title"));
                }
                newname = dialog.showAndWait();

                if (!newname.isPresent()) {
                    isNameInvalid = false;
                    deleteRecording = true;
                } else if (newname.get().length() != 0) {
                    try {
                        recording.finalizeName(newname.get());
                        isNameInvalid = false;
                    } catch (IllegalTableNameException ex) {
                        Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                secondtry = true;
            } while (isNameInvalid);
            if (deleteRecording) {
                io.dropTable(recording.getGenericName());
            }
        }
        recording = null;
    }

    /**
     * Shuts down the program safely by deleting a possibly running recording
     * and closing the database connection.
     */
    public void quitProgramm() {
        isRecording = false;
        if (recording != null) {
            saveRecording();
        }
        io.closeConnection();
        System.exit(0);
    }

    /**
     * Closes the current window safely and instantiates a new one. Used to
     * apply changes in the settings.
     */
    private void rebootWindow() {
        parentStage.close();
        io.closeConnection();
        io.saveConfigProperties();
        MainStage main = new MainStage(new Stage());
    }

    /**
     * Remove a tab from the main tabpane based on the title of the tab     *
     * @param name
     */
    private void removeTab(String name) {
        ObservableList<Tab> tabs = tabpane.getTabs();
        for (Tab t : tabs) {
            if (t.getText().equalsIgnoreCase(name)) {
                tabpane.getTabs().remove(t.getId());
            }
        }
    }

}
