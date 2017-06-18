package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.viewelements.SensorTable;
import de.hfts.sensormonitor.viewelements.SensorChart;
import de.hft.ss17.cebarround.BaseSensor;
import de.hfts.sensormonitor.exceptions.*;
import de.hfts.sensormonitor.execute.SensorMonitor;
import de.hfts.sensormonitor.misc.*;
import de.hfts.sensormonitor.model.*;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

/**
 * MainController --- FXML Controller of mainWindow.fxml, handles all Menu,
 * Button and CheckBox actions and connects View elements with the according
 * Model
 *
 * @author Polarix IT Solutions
 */
public class MainController implements Initializable {

    // -------------- FXML FIELDS ----------------------------------------------
    /**
     * Button for starting/stopping measuring the sensor data
     */
    @FXML
    private Button buttonMeasuring;
    /**
     * Button for starting a recording through the associated Recorder
     */
    @FXML
    private Button buttonStartRecording;
    /**
     * Button for stopping a recording through the associated Recorder
     */
    @FXML
    private Button buttonStopRecording;
    /**
     * Main TabPane including Tab's for realtime and recordings
     */
    @FXML
    private TabPane mainTabPane;
    /**
     * Menu containing MenuItems for recording actions
     */
    @FXML
    private Menu menuRecordings;
    /**
     * SensorChart in the Overview Tab displaying temperature info of the
     * sensors
     */
    @FXML
    private SensorChart chartTemperature;
    /**
     * SensorChart in the Overview Tab displaying pressure info of the sensors
     */
    @FXML
    private SensorChart chartPressure;
    /**
     * SensorChart in the Overview Tab displaying revolutions info of the
     * sensors
     */
    @FXML
    private SensorChart chartRevolutions;
    /**
     * CheckComboBox listing the sensors and providing the option to hide them
     */
    @FXML
    private CheckComboBox checkComboBoxSensors;
    /**
     * SensorChart in the specific Tab displaying temperature info of the
     * sensors
     */
    @FXML
    private SensorChart chartTemperatureSpecific;
    /**
     * TableView in the specific Tab displaying the exact values of the Sensors
     */
    @FXML
    private SensorTable tableViewTemperature;
    /**
     * SensorChart in the specific Tab displaying temperature info of the
     * sensors
     */
    @FXML
    private SensorChart chartPressureSpecific;
    /**
     * TableView in the specific Tab displaying the exact values of the Sensors
     */
    @FXML
    private SensorTable tableViewPressure;
    /**
     * SensorChart in the specific Tab displaying temperature info of the
     * sensors
     */
    @FXML
    private SensorChart chartRevolutionsSpecific;
    /**
     * TableView in the specific Tab displaying the exact values of the Sensors
     */
    @FXML
    private SensorTable tableViewRevolutions;
    /**
     * CheckBox for toggling the recording of the temperature data of the
     * sensors
     */
    @FXML
    private CheckBox checkBoxTemperature;
    /**
     * CheckBox for toggling the recording of the pressure data of the sensors
     */
    @FXML
    private CheckBox checkBoxPressure;
    /**
     * CheckBox for toggling the recording of the revolutions data of the
     * sensors
     */
    @FXML
    private CheckBox checkBoxRevolutions;

    // -------------- PACKAGE PRIVATE FIELDS -----------------------------------
    /**
     * Stage containing the scene loaded from recordingsListWindow.fxml
     */
    Stage recordingsListWindow;
    /**
     * Stage containing the scene loaded from settingsWindow.fxml
     */
    Stage settingsWindow;

    /**
     * Indicates if the application is connected to the H2 database
     */
    boolean isDBConnected;
    /**
     * Indicates if the sensors are currently being measured
     */
    boolean isMeasuring = false;

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Instance of Recorder, to start/stop recording the incoming sensor data
     */
    private Recorder recorder;

    /**
     * List of all ChartData's
     */
    private ArrayList<ChartData> chartDatas = new ArrayList<>();
    /**
     * List of all SensorChart's
     */
    private ArrayList<SensorChart> sensorCharts = new ArrayList<>();
    /**
     * Map of all SensorTable's with the data type as key
     */
    private HashMap<Data, SensorTable> tableViews = new HashMap<>();
    /**
     * Map of all TableData's with the data type as key
     */
    private HashMap<Data, TableData> tableDatas = new HashMap<>();

    /**
     * List of all sensors connected to the application
     */
    private List<BaseSensor> sensors;

    // -------------- FXML HANDLERS -------------------------------------------------    
    /**
     * Handle clicking the MenuItem "Quit", part of the Menu "File"
     */
    public void handleMenuItemQuit() {
        quitProgramm();
        System.exit(0);
    }

    /**
     * Handle clicking the MenuItem "Reboot", part of the Menu "File"
     */
    public void handleMenuItemReboot() {
        rebootProgramm();
    }

    /**
     * Handle clicking the MenuItem "Import recording", part of the Menu
     * "Recordings"
     */
    public void handleMenuItemImportRecording() {
        FileChooser fc = new FileChooser();
        fc.setTitle(IOUtils.getLangpackString("select_csv_file"));
        fc.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fc.showOpenDialog(null);
        if (file != null) {
            Thread t = new Thread(() -> {
                try {
                    IOUtils.importRecording(file);
                    displayRecording(file.getName().split("\\.")[0].toUpperCase());
                } catch (IOException | ParseException ex) {
                    Platform.runLater(() -> {
                        new ExceptionDialog(ex.getMessage(), null);
                    });
                    LogHandler.LOGGER.log(Level.SEVERE, null, ex);
                } catch (ImportRecordingException | IllegalTableNameException ex) {
                    Platform.runLater(() -> {
                        new ExceptionDialog(IOUtils.getLangpackString(ex.getExceptionKey()), null);
                    });
                    LogHandler.LOGGER.log(Level.SEVERE, null, ex);
                }
                LogHandler.LOGGER.info(LogHandler.getLangpackString("recording_imported") + ": " + file.getName().split("\\.")[0]);
            });
            t.start();
        }
    }

    /**
     * Handle clicking the MenuItem "Show all", part of the Menu "Recordings"
     */
    public void handleMenuItemShowAll() {
        if (recordingsListWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingsListWindow.fxml"), IOUtils.getLangpack());
                BorderPane root = (BorderPane) loader.load();
                ((RecordingsListController) loader.getController()).setListItems(IOUtils.getTables());
                ((RecordingsListController) loader.getController()).setParentController(this);
                recordingsListWindow = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(checkComboBoxSensors.getScene().getStylesheets());
                recordingsListWindow.setScene(scene);
                recordingsListWindow.sizeToScene();
                recordingsListWindow.setOnCloseRequest(eh -> {
                    recordingsListWindow = null;
                });
                recordingsListWindow.show();
            } catch (IOException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            recordingsListWindow.toFront();
        }
    }

    /**
     * Handle clicking the MenuItem "Delete all", part of the Menu "Recordings"
     */
    public void handleMenuItemDeleteAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(IOUtils.getLangpackString("delete_all"));
        alert.setHeaderText("");
        alert.setContentText(IOUtils.getLangpackString("confirm_delete_all_recordings"));

        Optional<ButtonType> pressedButton = alert.showAndWait();
        if (pressedButton.get() == ButtonType.OK) {
            IOUtils.dropAllTables();
        }
    }

    /**
     * Handle clicking the MenuItem "Settings", part of the Menu "Help"
     */
    public void handleMenuItemSettings() {
        if (settingsWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/settingsWindow.fxml"), IOUtils.getLangpack());
                TabPane root = (TabPane) loader.load();
                settingsWindow = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(checkComboBoxSensors.getScene().getStylesheets());
                ((SettingsController) loader.getController()).setMainController(this);
                settingsWindow.setScene(scene);
                settingsWindow.sizeToScene();
                settingsWindow.setOnCloseRequest(eh -> {
                    settingsWindow = null;
                    ((SettingsController) loader.getController()).onClose();
                });
                settingsWindow.show();
                ((SettingsController) loader.getController()).setUpData();
            } catch (IOException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            settingsWindow.toFront();
        }
    }

    /**
     * Handle clicking the MenuItem "About", part of the Menu "Help"
     */
    public void handleMenuItemAbout() {
    }

    /**
     * Handle clicking the Button "Start recording"
     */
    public void handleButtonStartRecording() {
        recorder.startRecording();
        buttonStopRecording.setDisable(false);
        buttonStartRecording.setDisable(true);
    }

    /**
     * Handle clicking the Button "Stop recording"
     */
    public void handleButtonStopRecording() {
        recorder.stopRecording();
        buttonStopRecording.setDisable(true);
        buttonStartRecording.setDisable(false);
    }

    /**
     * Handle clicking the CheckBox "Temperature"
     */
    public void handleCheckBoxTemperature() {
        if (checkBoxTemperature.isSelected()) {
            recorder.setRecordTemperature(true);
        } else {
            recorder.setRecordTemperature(false);
        }
    }

    /**
     * Handle clicking the CheckBox "Pressure"
     */
    public void handleCheckBoxPressure() {
        if (checkBoxPressure.isSelected()) {
            recorder.setRecordPressure(true);
        } else {
            recorder.setRecordPressure(false);
        }
    }

    /**
     * Handle clicking the CheckBox "Revolutions"
     */
    public void handleCheckBoxRevolutions() {
        if (checkBoxRevolutions.isSelected()) {
            recorder.setRecordRevolutions(true);
        } else {
            recorder.setRecordRevolutions(false);
        }
    }

    /**
     * Handle clicking the Button "Start measuring"/"Stop measuring"
     */
    public void handleButtonMeasuring() {
        if (isMeasuring) {
            for (BaseSensor bs : sensors) {
                bs.stopMeasure();
            }
            buttonMeasuring.setText(IOUtils.getLangpackString("start_measuring"));
            isMeasuring = false;
            LogHandler.LOGGER.info(LogHandler.getLangpackString("measuring_stopped"));
        } else {
            for (BaseSensor bs : sensors) {
                bs.startMeasure();
            }
            buttonMeasuring.setText(IOUtils.getLangpackString("stop_measuring"));
            isMeasuring = true;
            LogHandler.LOGGER.info(LogHandler.getLangpackString("measuring_started"));
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @return Boolean indicating if the application is connected to the database
     */
    public boolean isDBConnected() {
        return isDBConnected;
    }

    /**
     *
     * @param isDBConnected Boolean indicating if the application is connected to the database
     */
    public void setDBConnected(boolean isDBConnected) {
        this.isDBConnected = isDBConnected;
        if (isDBConnected) {
            menuRecordings.setDisable(false);
            buttonStartRecording.setDisable(false);
        } else {
            menuRecordings.setDisable(true);
            buttonStartRecording.setDisable(true);
        }
    }

    /**     
     * @return List of all ChartData's
     */
    public List<ChartData> getChartDatas() {
        return chartDatas;
    }

    /**
     *
     * @return List of ChartData's 
     */
    public List<SensorChart> getSensorCharts() {
        return sensorCharts;
    }

    /**
     *
     * @return Map of TableData's with the data type as key
     */
    public Map<Data, TableData> getTableDatas() {
        return tableDatas;
    }

    // -------------- OTHER METHODS --------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Creates SensorData and retrieves the SensorIDs from the sensors. Calls
     * setUpCharts() and setUpTables(). Starts receiving data from the sensors
     * when done.
     *
     * @param sensors List of sensors connected to the application
     */
    public void startDisplay(List<BaseSensor> sensors) {
        this.sensors = sensors;
        recorder = new Recorder();
        SensorData data = new SensorData();
        for (BaseSensor b : sensors) {
            try {
                Field fieldID = b.getClass().getSuperclass().getDeclaredField("uniqueId");
                fieldID.setAccessible(true);
                long sensorID = (long) fieldID.get(b);
                Field fieldTypeCode = b.getClass().getSuperclass().getDeclaredField("partTypeCode");
                fieldTypeCode.setAccessible(true);
                String typeCode = (String) fieldTypeCode.get(b);
                data.addSensor(sensorID, typeCode);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        setUpCharts(data);
        setUpTables(data);

        for (BaseSensor b : sensors) {
            b.addListener(data);
            b.addListener(recorder);
            b.stopMeasure();
        }

        checkComboBoxSensors.getItems().setAll(data.getSensorIDs());
        checkComboBoxSensors.getCheckModel().checkAll();

        checkComboBoxSensors.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                List<String> checkedsensors = new ArrayList<>(checkComboBoxSensors.getCheckModel().getCheckedItems());
                for (long l : data.getPartTypeCodes().keySet()) {
                    if (checkedsensors.contains(Long.toString(l))) {
                        for (ChartData cd : chartDatas) {
                            cd.setGraphVisible(l, true);
                        }
                    } else {
                        for (ChartData cd : chartDatas) {
                            cd.setGraphVisible(l, false);
                        }
                    }
                }
            }
        });

        for (BaseSensor b : sensors) {
            b.startMeasure();
        }
        LogHandler.LOGGER.info(LogHandler.getLangpackString("measuring_started"));
        isMeasuring = true;
    }

    /**
     * Sets up the ChartDatas and SensorCharts with configuration from IOUtils
     * and connects them.
     *
     * @param data SensorData connected to the sensors
     */
    public void setUpCharts(SensorData sensorData) {
        ChartData dataTemperature = new ChartData(Data.TEMPERATURE, sensorData);
        chartDatas.add(dataTemperature);
        ChartData dataPressure = new ChartData(Data.PRESSURE, sensorData);
        chartDatas.add(dataPressure);
        ChartData dataRevolutions = new ChartData(Data.REVOLUTIONS, sensorData);
        chartDatas.add(dataRevolutions);
        for (ChartData cd : chartDatas) {
            cd.setxMin(0 - Double.valueOf(IOUtils.getConfigProp("realtime_timeframe")));
            cd.setxMax(0);
            cd.setxScaleMin(Double.valueOf(IOUtils.getConfigProp("realtime_xscalemin")));
            cd.setxScaleMax(Double.valueOf(IOUtils.getConfigProp("realtime_xscalemax")));
        }
        dataTemperature.setyScaleMax(Double.valueOf(IOUtils.getConfigProp("temperature_yscalemax")));
        dataTemperature.setyScaleMin(Double.valueOf(IOUtils.getConfigProp("temperature_yscalemin")));
        dataPressure.setyScaleMax(Double.valueOf(IOUtils.getConfigProp("pressure_yscalemax")));
        dataPressure.setyScaleMin(Double.valueOf(IOUtils.getConfigProp("pressure_yscalemin")));
        dataRevolutions.setyScaleMax(Double.valueOf(IOUtils.getConfigProp("revolutions_yscalemax")));
        dataRevolutions.setyScaleMin(Double.valueOf(IOUtils.getConfigProp("revolutions_yscalemin")));

        chartTemperature.setChartData(dataTemperature, IOUtils.getLangpack(), "sec", "°C", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        chartPressure.setChartData(dataPressure, IOUtils.getLangpack(), "sec", "hPa", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        chartRevolutions.setChartData(dataRevolutions, IOUtils.getLangpack(), "sec", "RPM", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        sensorCharts.add(chartTemperature);
        sensorCharts.add(chartPressure);
        sensorCharts.add(chartRevolutions);

        ChartData dataTemperatureSpecific = dataTemperature.clone();
        chartDatas.add(dataTemperatureSpecific);
        ChartData dataPressureSpecific = dataPressure.clone();
        chartDatas.add(dataPressureSpecific);
        ChartData dataRevolutionsSpecific = dataRevolutions.clone();
        chartDatas.add(dataRevolutionsSpecific);

        chartTemperatureSpecific.setChartData(dataTemperatureSpecific, IOUtils.getLangpack(), "sec", "°C", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        chartPressureSpecific.setChartData(dataPressureSpecific, IOUtils.getLangpack(), "sec", "hPa", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        chartRevolutionsSpecific.setChartData(dataRevolutionsSpecific, IOUtils.getLangpack(), "sec", "RPM", Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        sensorCharts.add(chartTemperatureSpecific);
        sensorCharts.add(chartPressureSpecific);
        sensorCharts.add(chartRevolutionsSpecific);
    }

    /**
     * Sets up the TableViews and TableDatas with configuration from IOUtils and
     * connects them.
     *
     * @param sensorData SensorData connected to the sensors
     */
    public void setUpTables(SensorData sensorData) {
        tableViews.put(Data.TEMPERATURE, tableViewTemperature);
        tableViews.put(Data.PRESSURE, tableViewPressure);
        tableViews.put(Data.REVOLUTIONS, tableViewRevolutions);
        tableDatas.put(Data.TEMPERATURE, new TableData(Data.TEMPERATURE, sensorData));
        tableDatas.put(Data.PRESSURE, new TableData(Data.PRESSURE, sensorData));
        tableDatas.put(Data.REVOLUTIONS, new TableData(Data.REVOLUTIONS, sensorData));
        for (Data data : tableViews.keySet()) {
            tableViews.get(data).setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tableDatas.get(data).setMinTime(0 - Double.valueOf(IOUtils.getConfigProp("realtime_timeframe")));
            tableDatas.get(data).addListener(tableViews.get(data));
            int counter = 0;
            for (long sensorID : sensorData.getPartTypeCodes().keySet()) {
                TableColumn tc = new TableColumn(Long.toString(sensorID));
                TableColumn<ObservableList<Double>, Double> tc_time = new TableColumn(IOUtils.getLangpackString("time"));
                TableColumn<ObservableList<Double>, Double> tc_value = new TableColumn(IOUtils.getLangpackString("value"));
                final int counterFixed = counter;
                tc_time.setCellValueFactory(param -> {
                    Double d = null;
                    try {
                        d = param.getValue().get(counterFixed);
                    } catch (IndexOutOfBoundsException e) {
                        d = Double.MAX_VALUE;
                    }
                    return new ReadOnlyObjectWrapper<>(d);
                });
                tc_value.setCellValueFactory(param -> {
                    Double d = null;
                    try {
                        d = param.getValue().get(counterFixed + 1);
                    } catch (IndexOutOfBoundsException e) {
                        d = Double.MAX_VALUE;
                    }
                    return new ReadOnlyObjectWrapper<>(d);
                });
                counter += 2;
                tc.getColumns().addAll(tc_time, tc_value);
                tableViews.get(data).getColumns().add(tc);
                tableDatas.get(data).addSensor(sensorID);
            }
            tableViews.get(data).setItems(tableDatas.get(data).getData());
        }
    }

    /**
     * Shuts down the program safely by deleting a possibly running recording
     * and closing the database connection.
     */
    public void quitProgramm() {
        if (recorder.getRecording() != null) {
            recorder.stopRecording();
        }
        checkComboBoxSensors.getScene().getWindow().hide();
        IOUtils.closeConnection();
        LogHandler.LOGGER.info(LogHandler.getLangpackString("program_stopped"));
    }

    /**
     * Restarts the program
     */
    public void rebootProgramm() {
        quitProgramm();
        LogHandler.LOGGER.info(LogHandler.getLangpackString("program_rebooting"));
        SensorMonitor sm = new SensorMonitor();
        sm.isReboot = true;
        try {
            sm.start(new Stage());
        } catch (Exception ex) {
            // NO-OP - catch any exception preventing the program from rebooting
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes tabs of the mainTabPane based on the name of the Tab
     *
     * @param name Title of the tab
     */
    public void closeTab(String name) {
        ObservableList<Tab> tabs = mainTabPane.getTabs();
        for (Tab t : tabs) {
            if (t.getText().equalsIgnoreCase(name)) {
                mainTabPane.getTabs().remove(t.getId());
            }
        }
    }

    /**
     * Closes tabs of the mainTabPane based on the names of the Tab's
     *
     * @param names List of the titles of the tabs
     */
    public void closeTab(List<String> names) {
        ObservableList<Tab> tabs = mainTabPane.getTabs();
        for (Tab t : tabs) {
            if (names.contains(t.getText())) {
                mainTabPane.getTabs().remove(t.getId());
            }
        }
    }

    /**
     * Creates a Tab and loads the according FXML file for displaying a
     * recording from the Embedded H2 Database in SensorChart's
     *
     * @param recordingName Name of the recording to be displayed
     */
    public void displayRecording(String recordingName) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingDisplay.fxml"), IOUtils.getLangpack());
            GridPane root = (GridPane) loader.load();
            ((RecordingDisplayController) loader.getController()).setRecording(new Recording(IOUtils.loadRecording(recordingName)));
            Tab tab = new Tab(recordingName, root);
            tab.setClosable(true);
            Platform.runLater(() -> {
                mainTabPane.getTabs().add(tab);
            });
        } catch (IOException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
