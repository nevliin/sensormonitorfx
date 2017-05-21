package de.hfts.sensormonitor.controller;

import de.hft.ss17.cebarround.BaseSensor;
import de.hft.ss17.cebarround.SensorEvent;
import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.exceptions.ImportRecordingException;
import de.hfts.sensormonitor.main.SensorMonitor;
import de.hfts.sensormonitor.misc.ExceptionDialog;
import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.misc.Recorder;
import de.hfts.sensormonitor.misc.Recording;
import de.hfts.sensormonitor.misc.SensorChart;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.model.SensorData;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

/**
 *
 * @author Lord_Roke
 */
public class MainController implements Initializable {

    @FXML
    private Label labelInfo;
    @FXML
    private Button buttonStartRecording;
    @FXML
    private Button buttonStopRecording;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Menu menuRecordings;
    @FXML
    private LineChart chartTemperature;
    @FXML
    private LineChart chartPressure;
    @FXML
    private LineChart chartRevolutions;
    @FXML
    private CheckComboBox checkComboBoxSensors;
    @FXML
    private LineChart chartTemperatureSpecific;
    @FXML
    private TableView tableViewTemperature;
    @FXML
    private LineChart chartPressureSpecific;
    @FXML
    private TableView tableViewPressure;
    @FXML
    private LineChart chartRevolutionsSpecific;
    @FXML
    private TableView tableViewRevolutions;
    @FXML
    private CheckBox checkBoxTemperature;
    @FXML
    private CheckBox checkBoxPressure;
    @FXML
    private CheckBox checkBoxRevolutions;
    private IO io;
    private Stage recordingswindow;
    private Stage settingswindow;
    private HashMap<Data, ChartData> chartData;

    Recorder recorder;

    List<BaseSensor> sensors;

    boolean isDBConnected;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chartData = new HashMap<>();
    }

    /**
     *
     * @param sensors
     */
    public void startDisplay(List<BaseSensor> sensors) {
        SensorData data = new SensorData();
        ChartData dataTemperature = new ChartData(Data.TEMPERATURE, data);
        chartData.put(Data.TEMPERATURE, dataTemperature);
        ChartData dataPressure = new ChartData(Data.PRESSURE, data);
        chartData.put(Data.PRESSURE, dataPressure);
        ChartData dataRevolutions = new ChartData(Data.REVOLUTIONS, data);
        chartData.put(Data.REVOLUTIONS, dataRevolutions);
        for (ChartData cd : chartData.values()) {
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

        SensorChart sensorChartTemperature = new SensorChart(chartTemperature, dataTemperature, io.getLangpack());
        SensorChart sensorChartPressure = new SensorChart(chartPressure, dataPressure, io.getLangpack());
        SensorChart sensorChartRevolutions = new SensorChart(chartRevolutions, dataRevolutions, io.getLangpack());

        ChartData dataTemperatureSpecific = dataTemperature.clone();
        chartData.put(Data.TEMPERATURE, dataTemperatureSpecific);
        ChartData dataPressureSpecific = dataPressure.clone();
        chartData.put(Data.PRESSURE, dataPressureSpecific);
        ChartData dataRevolutionsSpecific = dataRevolutions.clone();
        chartData.put(Data.REVOLUTIONS, dataRevolutionsSpecific);

        SensorChart sensorChartTemperatureSpecific = new SensorChart(chartTemperatureSpecific, dataTemperatureSpecific, io.getLangpack());
        SensorChart sensorChartPressureSpecific = new SensorChart(chartPressureSpecific, dataPressureSpecific, io.getLangpack());
        SensorChart sensorChartRevolutionsSpecific = new SensorChart(chartRevolutionsSpecific, dataRevolutionsSpecific, io.getLangpack());

        for (long l : data.getSensorIDs()) {
            checkComboBoxSensors.getItems().add(Long.toString(l));
        }

        for (BaseSensor b : sensors) {
            b.addListener(data);
            b.addListener(recorder);
            b.stopMeasure();
        }
        for (BaseSensor b : sensors) {
            b.startMeasure();
        }
    }

    /**
     *
     */
    public void handleMenuItemQuit() {
    }

    /**
     *
     */
    public void handleMenuItemReboot() {

    }

    /**
     *
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
     *
     */
    public void handleMenuItemShowAll() {
        if (recordingswindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingslist.fxml"), io.getLangpack());
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
     *
     */
    public void handleMenuItemDeleteAll() {

    }

    /**
     *
     */
    public void handleMenuItemSettings() {

    }

    /**
     *
     */
    public void handleButtonStartRecording() {
        recorder.startRecording();
        buttonStopRecording.setDisable(false);
        buttonStartRecording.setDisable(true);
    }

    /**
     *
     */
    public void handleButtonStopRecording() {
        recorder.stopRecording();
        buttonStopRecording.setDisable(true);
        buttonStartRecording.setDisable(false);
    }

    /**
     *
     */
    public void handleCheckBoxTemperature() {
        if (checkBoxTemperature.isSelected()) {
            recorder.setRecordTemperature(true);
        } else {
            recorder.setRecordTemperature(false);
        }
    }

    /**
     *
     */
    public void handleCheckBoxPressure() {
        if (checkBoxPressure.isSelected()) {
            recorder.setRecordPressure(true);
        } else {
            recorder.setRecordPressure(false);
        }
    }

    /**
     *
     */
    public void handleCheckBoxRevolutions() {
        if (checkBoxRevolutions.isSelected()) {
            recorder.setRecordRevolutions(true);
        } else {
            recorder.setRecordRevolutions(false);
        }
    }

    /**
     *
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
     * Shuts down the program safely by deleting a possibly running recording
     * and closing the database connection.
     */
    public void quitProgramm() {
        //isRecording = false;
        //if (recording != null) {
        //    saveRecording();
        //}
        //io.closeConnection();
        System.exit(0);
    }

    /**
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
     *
     * @param recordingName
     */
    public void displayRecording(String recordingName) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/recordingDisplay.fxml"), io.getLangpack());
            GridPane root = (GridPane) loader.load();
            ((RecordingDisplayController) loader.getController()).setIO(io);
            ((RecordingDisplayController) loader.getController()).setRecording(new Recording(io.loadRecording(recordingName), io));
            Tab tab = new Tab(recordingName, root);
            tab.setClosable(true);
            mainTabPane.getTabs().add(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
