/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.misc.Recording;
import de.hfts.sensormonitor.misc.SensorChart;
import de.hfts.sensormonitor.model.SensorData;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;

/**
 *
 * @author Polarix IT Solutions
 */
public class RecordingDisplayController implements Initializable {

    Recording recording;
    IO io;

    @FXML
    private LineChart chartTemperature;
    @FXML
    private LineChart chartPressure;
    @FXML
    private LineChart chartRevolutions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setIO(IO io) {
        this.io = io;
    }

    public void setRecording(Recording recording) {
        SensorChart sensorChartTemperature = new SensorChart(chartTemperature, recording.getChartData(Data.TEMPERATURE), io.getLangpack());
        SensorChart sensorChartPressure = new SensorChart(chartPressure, recording.getChartData(Data.PRESSURE), io.getLangpack());
        SensorChart sensorChartRevolutions = new SensorChart(chartRevolutions, recording.getChartData(Data.REVOLUTIONS), io.getLangpack());
    }

}
