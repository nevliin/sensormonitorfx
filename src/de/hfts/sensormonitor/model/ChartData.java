/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import com.sun.javafx.collections.ObservableListWrapper;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Polarix IT Solutions
 */
public class ChartData implements DataChangeListener {

    private ArrayList<ChartDataChangeListener> listeners;
    private HashMap<Long, ArrayList<SensorDataPoint>> chartGraphs;
    private SensorData sensorData;
    private Data type;
    private double xScaleMin;
    private double xScaleMax;
    private double yScaleMin;
    private double yScaleMax;
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    public ChartData(Data type, SensorData sensorData) {
        listeners = new ArrayList<>();
        this.chartGraphs = new HashMap<>();
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }
    
        // <--- Listener operations --->
    /**
     * Add listener implementing DataChangeListener to the SensorChartData
     *
     * @param toAdd Object to be notified of changes
     */
    public void addListener(ChartDataChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Remove listener from the list
     *
     * @param toRemove Object to remove from the listeners
     */
    public void removeListener(ChartDataChangeListener toRemove) {
        listeners.remove(toRemove);
    }

    /**
     * Notifies all listeners of a change in the graph data
     *
     * @param graphname Sensor ID (name) of the graph that was changed
     */
    public void notifyListenersOfDataChange(long sensorID) {
        for (ChartDataChangeListener dcl : listeners) {
            dcl.dataChanged(sensorID);
        }
    }

    public ArrayList<SensorDataPoint> getPoints(long sensorID) {
        return chartGraphs.get(sensorID);
    }

    @Override
    public void dataChanged(long sensorID) {
        ArrayList<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
        if (chartGraphs.get(sensorID) == null) {
            chartGraphs.put(sensorID, new ArrayList<>());
        }
        chartGraphs.put(sensorID, points);
        notifyListenersOfDataChange(sensorID);
    }

    public double getxScaleMin() {
        return xScaleMin;
    }

    public void setxScaleMin(double xScaleMin) {
        this.xScaleMin = xScaleMin;
    }

    public double getxScaleMax() {
        return xScaleMax;
    }

    public void setxScaleMax(double xScaleMax) {
        this.xScaleMax = xScaleMax;
    }

    public double getyScaleMin() {
        return yScaleMin;
    }

    public void setyScaleMin(double yScaleMin) {
        this.yScaleMin = yScaleMin;
    }

    public double getyScaleMax() {
        return yScaleMax;
    }

    public void setyScaleMax(double yScaleMax) {
        this.yScaleMax = yScaleMax;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

}
