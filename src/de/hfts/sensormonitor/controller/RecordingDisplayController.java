/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.*;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.*;
import javafx.scene.chart.LineChart;

/**
 * RecordingDisplayController --- FXML Controller of recordingDisplay.fxml, connects the View elements with the according Model
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
    
    // -------------- PRIVATE FIELDS -------------------------------------------
    private Recording recording;
    private ResourceBundle langpack;

    // -------------- OTHER METHODS --------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        langpack = resources;
    }

    // -------------- GETTERS & SETTERS ----------------------------------------

    /**
     *
     * @param recording
     */
    public void setRecording(Recording recording) {
        chartTemperature.setChartData(recording.getChartData(Data.TEMPERATURE), langpack, "sec", "°C");
        chartPressure.setChartData(recording.getChartData(Data.PRESSURE), langpack, "sec", "hPa");
        chartRevolutions.setChartData(recording.getChartData(Data.REVOLUTIONS), langpack, "sec", "RPM");
    }

}
