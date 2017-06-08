/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.viewelements.SensorChart;
import de.hfts.sensormonitor.misc.*;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.fxml.*;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import org.controlsfx.control.CheckComboBox;

/**
 * RecordingDisplayController --- FXML Controller of recordingDisplay.fxml,
 * connects the View elements with the according Model
 *
 * @author Polarix IT Solutions
 */
public class RecordingDisplayController implements Initializable {

    // -------------- FXML FIELDS ----------------------------------------------
    @FXML
    private SensorChart chartTemperature;
    @FXML
    private SensorChart chartPressure;
    @FXML
    private SensorChart chartRevolutions;
    @FXML
    private CheckComboBox checkComboBoxSensors;
    @FXML
    private Label labelInfo;

    // -------------- PRIVATE FIELDS -------------------------------------------
    private ResourceBundle langpack;
    private List<ChartData> chartDatas = new ArrayList<>();
    private List<SensorChart> sensorCharts = new ArrayList<>();

    // -------------- OTHER METHODS --------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        langpack = resources;
    }

    /**
     * Sets the recording of the tab, adds data to the CheckComboBox, Label and SensorChart's.
     * @param recording
     */
    public void setRecording(Recording recording) {
        labelInfo.setText(recording.getFirsttimestamp().toString() + " - " + recording.getLasttimestamp().toString());
        chartTemperature.setChartData(recording.getChartData(Data.TEMPERATURE), langpack, "sec", "°C", true);
        chartDatas.add(recording.getChartData(Data.TEMPERATURE));
        sensorCharts.add(chartTemperature);
        chartPressure.setChartData(recording.getChartData(Data.PRESSURE), langpack, "sec", "hPa", true);
        chartDatas.add(recording.getChartData(Data.PRESSURE));
        sensorCharts.add(chartPressure);
        chartRevolutions.setChartData(recording.getChartData(Data.REVOLUTIONS), langpack, "sec", "RPM", true);
        chartDatas.add(recording.getChartData(Data.REVOLUTIONS));
        sensorCharts.add(chartRevolutions);
        checkComboBoxSensors.getItems().addAll(recording.getSensors().keySet());
        checkComboBoxSensors.getCheckModel().checkAll();
        checkComboBoxSensors.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                List<String> checkedsensors = new ArrayList<>(checkComboBoxSensors.getCheckModel().getCheckedItems());
                for (long l : recording.getSensors().keySet()) {
                    if (checkedsensors.contains(l)) {
                        for (ChartData cd : chartDatas) {
                            cd.setGraphVisible(l, true);
                        }
                        for (SensorChart sc : sensorCharts) {
                            sc.installTooltips();
                        }
                    } else {
                        for (ChartData cd : chartDatas) {
                            cd.setGraphVisible(l, false);
                        }
                        for (SensorChart sc : sensorCharts) {
                            sc.installTooltips();
                        }
                    }
                }
            }
        });
    }
}
