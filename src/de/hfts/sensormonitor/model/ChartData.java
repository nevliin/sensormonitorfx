/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import de.hfts.sensormonitor.model.SensorData.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Polarix IT Solutions
 */
public class ChartData implements DataChangeListener {

    private SensorData sensorData;
    private Data type;
    private HashMap<Long, XYChart.Series> data;
    private double xScaleMin;
    private double xScaleMax;
    private double yScaleMin;
    private double yScaleMax;
    private double xMin = -50;
    private double xMax;
    private double yMin;
    private double yMax;

    public ChartData(Data type, SensorData sensorData) {
        this.type = type;
        this.sensorData = sensorData;
        data = new HashMap<>();
        sensorData.addListener(this);
    }

    @Override
    public void dataChanged(long sensorID) {
        ArrayList<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
        if (!sensorData.getSensorIDs().contains(sensorID)) {
            data.put(sensorID, new XYChart.Series());
        }
        if (points != null) {
            setPointsToSeries(data.get(sensorID), points);
        }
    }

    /**
     * Clear the Series and add the GraphPoint's in the List to it
     *
     * @param series
     * @param points
     */
     private void setPointsToSeries(XYChart.Series series, List<SensorDataPoint> points) {
        try {
            series.getData().clear();
        } catch (NullPointerException e) {
            // Catching NullPointerException if the series doesn't have any data yet
        }
        Date lastPoint = null;
        for (SensorDataPoint p : points) {
            double time = 0;
            if (lastPoint != null) {
                time = lastPoint.getTime() - p.time.getTime();
            }
            if (time > xMin) {
                if (!p.isEmpty()) {
                    try {
                    series.getData().add(new XYChart.Data(time, p.value));
                    } catch (NullPointerException e) {
                        
                    }
                }
            }
            lastPoint = p.time;
        }
    }

    public ObservableList<XYChart.Series> toObservableList() {
        return FXCollections.observableArrayList(new ArrayList(data.values()));
    }

}
