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
 *
 * @author Polarix IT Solutions
 */
public class RecordingDisplayController implements Initializable {

    Recording recording;
    IO io;

    @FXML
    private SensorChart chartTemperature;
    @FXML
    private SensorChart chartPressure;
    @FXML
    private SensorChart chartRevolutions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     *
     * @param io
     */
    public void setIO(IO io) {
        this.io = io;
    }

    /**
     *
     * @param recording
     */
    public void setRecording(Recording recording) {
        chartTemperature.setChartData(recording.getChartData(Data.TEMPERATURE), io.getLangpack(), "sec", "°C");
        chartPressure.setChartData(recording.getChartData(Data.PRESSURE), io.getLangpack(), "sec", "hPa");
        chartRevolutions.setChartData(recording.getChartData(Data.REVOLUTIONS), io.getLangpack(), "sec", "RPM");
    }

}
