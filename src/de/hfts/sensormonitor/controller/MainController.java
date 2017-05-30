package de.hfts.sensormonitor.controller;

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
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
     *
     */
    @FXML
    private Label labelInfo;
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
    private TableView tableViewTemperature;
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
    private TableView tableViewPressure;
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
    private TableView tableViewRevolutions;
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

    // -------------- PRIVATE FIELDS -------------------------------------------
    private IO io;
    private Recorder recorder;

    private Stage recordingswindow;
    private Stage settingswindow;

    private ArrayList<ChartData> chartDatas = new ArrayList<>();
    private ArrayList<SensorChart> sensorCharts = new ArrayList<>();
    private List<BaseSensor> sensors;

    boolean isDBConnected;

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
     * Handle clicking the MenuItem "Import recording", part of the Menu "Recordings"
     */
    public void handleMenuItemImportRecording() {
        FileChooser fs = new FileChooser();
        fs.setTitle(io.getLangpackString("select_csv_file"));
        fs.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fs.showOpenDialog(null);
        if (file != null) {
            try {
                io.importRecording(file);
                displayRecording(file.getName().split("\\.")[0].toUpperCase());
            } catch (IOException | ParseException | ImportRecordingException | IllegalTableNameException ex) {
                new ExceptionDialog(ex.getMessage(), null);
            }
        }
    }

    /**
     * Handle clicking the MenuItem "Show all", part of the Menu "Recordings"
     */
    public void handleMenuItemShowAll() {
        if (recordingswindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingsListWindow.fxml"), io.getLangpack());
                BorderPane root = (BorderPane) loader.load();
                ((RecordingsListController) loader.getController()).setListItems(io.getTables());
                ((RecordingsListController) loader.getController()).setParentController(this);
                recordingswindow = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(labelInfo.getScene().getStylesheets());
                recordingswindow.setScene(scene);
                recordingswindow.sizeToScene();
                recordingswindow.setOnCloseRequest(eh -> {
                    recordingswindow = null;
                });
                recordingswindow.show();
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            recordingswindow.toFront();
        }
    }

    /**
     * Handle clicking the MenuItem "Delete all", part of the Menu "Recordings"
     */
    public void handleMenuItemDeleteAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(io.getLangpackString("delete_all"));
        alert.setHeaderText("");
        alert.setContentText(io.getLangpackString("confirm_delete_all_recordings"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            io.dropAllTables();
        }
    }

    /**
     * Handle clicking the MenuItem "Settings", part of the Menu "Help"
     */
    public void handleMenuItemSettings() {
        io.loadAvailableLanguages();
        if (settingswindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/settingsWindow.fxml"), io.getLangpack());
                TabPane root = (TabPane) loader.load();
                settingswindow = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(labelInfo.getScene().getStylesheets());
                ((SettingsController) loader.getController()).setIO(io);
                ((SettingsController) loader.getController()).setMainController(this);
                settingswindow.setScene(scene);
                settingswindow.sizeToScene();
                settingswindow.setOnCloseRequest(eh -> {
                    settingswindow = null;
                    ((SettingsController) loader.getController()).onClose();
                });
                settingswindow.show();
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            settingswindow.toFront();
        }
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

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @return
     */
    public boolean isDBConnected() {
        return isDBConnected;
    }

    /**
     *
     * @param isDBConnected
     */
    public void setIsDBConnected(boolean isDBConnected) {
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
     * @param io
     */
    public void setIo(IO io) {
        this.io = io;
        recorder = new Recorder(io);
    }

    /**
     *
     * @return
     */
    public IO getIo() {
        return this.io;
    }

    /**
     *
     * @return
     */
    public ArrayList<ChartData> getChartDatas() {
        return chartDatas;
    }

    /**
     *
     * @return
     */
    public ArrayList<SensorChart> getSensorCharts() {
        return sensorCharts;
    }

    // -------------- OTHER METHODS --------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Creates SensorData and connects ChartData, SensorChart and the sensors to
     * it. Starts receiving data from the sensors when done.
     *
     * @param sensors
     */
    public void startDisplay(List<BaseSensor> sensors) {
        SensorData data = new SensorData();
        setUpCharts(data);
        setUpTables(data);

        for (BaseSensor b : sensors) {
            try {
                Field fieldID = b.getClass().getSuperclass().getDeclaredField("uniqueId");
                fieldID.setAccessible(true);
                long sensorID = (long) fieldID.get(b);
                Field fieldTypeCode = b.getClass().getSuperclass().getDeclaredField("partTypeCode");
                fieldTypeCode.setAccessible(true);
                String typeCode = (String) fieldTypeCode.get(b);
                data.addSensor(sensorID, typeCode);
                b.addListener(data);
                b.addListener(recorder);
                b.stopMeasure();
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        checkComboBoxSensors.getItems().setAll(data.getSensorIDs());
        checkComboBoxSensors.getCheckModel().checkAll();

        checkComboBoxSensors.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                List<String> checkedsensors = new ArrayList<>(checkComboBoxSensors.getCheckModel().getCheckedItems());
                for (long l : data.getMapIDTypeCode().keySet()) {
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
    }
    
    /**
     * 
     */
    public void setUpCharts(SensorData data) {
                ChartData dataTemperature = new ChartData(Data.TEMPERATURE, data);
        chartDatas.add(dataTemperature);
        ChartData dataPressure = new ChartData(Data.PRESSURE, data);
        chartDatas.add(dataPressure);
        ChartData dataRevolutions = new ChartData(Data.REVOLUTIONS, data);
        chartDatas.add(dataRevolutions);
        for (ChartData cd : chartDatas) {
            cd.setxMin(0 - Double.valueOf(io.getConfigProp("realtime_timeframe")));
            cd.setxMax(0);
            cd.setxScaleMin(Double.valueOf(io.getConfigProp("realtime_xscalemin")));
            cd.setxScaleMax(Double.valueOf(io.getConfigProp("realtime_xscalemax")));
        }
        dataTemperature.setyScaleMax(Double.valueOf(io.getConfigProp("temperature_yscalemax")));
        dataTemperature.setyScaleMin(Double.valueOf(io.getConfigProp("temperature_yscalemin")));
        dataPressure.setyScaleMax(Double.valueOf(io.getConfigProp("pressure_yscalemax")));
        dataPressure.setyScaleMin(Double.valueOf(io.getConfigProp("pressure_yscalemin")));
        dataRevolutions.setyScaleMax(Double.valueOf(io.getConfigProp("revolutions_yscalemax")));
        dataRevolutions.setyScaleMin(Double.valueOf(io.getConfigProp("revolutions_yscalemin")));

        chartTemperature.setChartData(dataTemperature, io.getLangpack(), "sec", "°C", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        chartPressure.setChartData(dataPressure, io.getLangpack(), "sec", "hPa", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        chartRevolutions.setChartData(dataRevolutions, io.getLangpack(), "sec", "RPM", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        sensorCharts.add(chartTemperature);
        sensorCharts.add(chartPressure);
        sensorCharts.add(chartRevolutions);

        ChartData dataTemperatureSpecific = dataTemperature.clone();
        chartDatas.add(dataTemperatureSpecific);
        ChartData dataPressureSpecific = dataPressure.clone();
        chartDatas.add(dataPressureSpecific);
        ChartData dataRevolutionsSpecific = dataRevolutions.clone();
        chartDatas.add(dataRevolutionsSpecific);

        chartTemperatureSpecific.setChartData(dataTemperatureSpecific, io.getLangpack(), "sec", "°C", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        chartPressureSpecific.setChartData(dataPressureSpecific, io.getLangpack(), "sec", "hPa", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        chartRevolutionsSpecific.setChartData(dataRevolutionsSpecific, io.getLangpack(), "sec", "RPM", Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        sensorCharts.add(chartTemperatureSpecific);
        sensorCharts.add(chartPressureSpecific);
        sensorCharts.add(chartRevolutionsSpecific);
    }
    
    public void setUpTables(SensorData data) {
        
    }

    /**
     * Shuts down the program safely by deleting a possibly running recording
     * and closing the database connection.
     */
    public void quitProgramm() {
        if (recorder.getRecording() != null) {
            recorder.stopRecording();
        }
        labelInfo.getScene().getWindow().hide();
        io.closeConnection();
    }

    /**
     * Restarts the program
     */
    public void rebootProgramm() {
        quitProgramm();
        SensorMonitor sm = new SensorMonitor();
        try {
            sm.start(new Stage());
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes tabs of the mainTabPane based on the name of the Tab
     *
     * @param name
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
     * @param names
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
     * @param recordingName
     */
    public void displayRecording(String recordingName) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingDisplay.fxml"), io.getLangpack());
            GridPane root = (GridPane) loader.load();
            ((RecordingDisplayController) loader.getController()).setRecording(new Recording(io.loadRecording(recordingName), io));
            Tab tab = new Tab(recordingName, root);
            tab.setClosable(true);
            mainTabPane.getTabs().add(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
