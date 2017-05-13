/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorChartData {

    private LinkedHashMap<String, GraphSeries> graphs = new LinkedHashMap<>();
    private List<DataChangeListener> listeners = new ArrayList<>();
    private String chartName;
    private double xScaleMin;
    private double xScaleMax;
    private double yScaleMin;
    private double yScaleMax;
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private String xUnit;
    private String yUnit;
    private ResourceBundle langpack;

    /**
     *
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     * @param xUnit
     * @param yUnit
     * @param langpack
     * @param chartName
     */
    public SensorChartData(double xMin, double xMax, double yMin, double yMax, String xUnit, String yUnit, ResourceBundle langpack, String chartName) {
        this.chartName = chartName;
        this.langpack = langpack;
        this.yUnit = yUnit;
        this.xUnit = xUnit;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

// <--- Listener operations --->
    /**
     * Add listener implementing DataChangeListener to the SensorChartData
     * @param toAdd Object to be notified of changes
     */
    public void addListener(DataChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Remove listener from the list
     * @param toRemove Object to remove from the listeners
     */
    public void removeListener(DataChangeListener toRemove) {
        listeners.remove(toRemove);
    }

    /**
     * Notifies all listeners of a change in the graph data
     * @param graphname Sensor ID (name) of the graph that was changed
     */
    public void notifyListenersOfDataChange(String graphname) {
        for (DataChangeListener dcl : listeners) {
            dcl.dataChanged(graphname);
        }
    }

    /**
     * Notifies all listeners of a visibility change of a graph
     * @param graphname Sensor ID (name) of the graph which visibility changed
     * @param isVisible Visibility status of the graph
     */
    public void notifyListenersOfVisibilityChange(String graphname, boolean isVisible) {
        for (DataChangeListener dcl : listeners) {
            dcl.visibilityChanged(graphname, isVisible);
        }
    }

    /**
     * Notifies all listeners of a change in the X- or Y-Axis
     */
    public void notifyListenersOfAxisChange() {
        for (DataChangeListener dcl : listeners) {
            dcl.axisChanged();
        }
    }

// <--- Graph operations --->
    /**
     * Returns a hashmap with all graphs in the SensorChartData
     * @return HashMap with Sensor ID's as key and GraphSeries' as values
     */
    public LinkedHashMap<String, GraphSeries> getGraphs() {
        return graphs;
    }

    /**
     * Set the graphs of the SensorChartData
     * @param graphs LinkedHashMap containing the Sensor ID's as keys and the corresponding GraphSeries' as values
     */
    public void setGraphs(LinkedHashMap<String, GraphSeries> graphs) {
        this.graphs = graphs;
        notifyListenersOfDataChange(null);
    }

    /**
     * Move all points in a GraphSeries by the specified value and add a newValue at 0.
     * @param move Move value
     * @param newValue Value of the point to be added at 0
     * @param graphname Sensor ID (name) of the GraphSeries
     */
    public void moveLeftSpecific(double move, double newValue, String graphname) {
        GraphPoint newPoint = new GraphPoint(0, newValue);
        List<GraphPoint> points = graphs.get(graphname).getPoints();

        if (points.size() != 0) {
            for (int i = 0; i < points.size(); i++) {
                points.get(i).x -= move;
            }
        }
        points.add(newPoint);
        notifyListenersOfDataChange(graphname);
    }
    
    /**
     * Change the visibility of the specified graph
     * @param graphname Sensor ID (name) of the graph
     * @param isVisible Visibility status of the graph
     */
    public void setGraphVisible(String graphname, boolean isVisible) {
        graphs.get(graphname).isVisible(isVisible);
        notifyListenersOfVisibilityChange(graphname, isVisible);
    }

// <--- Getters & Setters --->
    /**
     * Return a GraphSeries based on the LinkedHashMap graphs and the given key
     * @param key Key value for the LinkedHashMap graphs
     * @return 
     */
    public GraphSeries get(String key) {
        return graphs.get(key);
    }

    /**
     * Put a new GraphSeries with the specified key into the LinkedHashMap graphs
     * @param key Key value for the LinkedHashMap graphs
     * @param series New value for the LinkedHashMap graphs
     */
    public void put(String key, GraphSeries series) {
        graphs.put(key, series);
    }

    /**
     *
     * @return
     */
    public String getChartname() {
        return chartName;
    }

    /**
     *
     * @param chartName
     */
    public void setChartname(String chartName) {
        this.chartName = chartName;
    }

    /**
     *
     * @return
     */
    public double getXScaleMin() {
        return xScaleMin;
    }

    /**
     *
     * @param xScaleMin
     */
    public void setXScaleMin(double xScaleMin) {
        this.xScaleMin = xScaleMin;
    }

    /**
     *
     * @return
     */
    public double getXScaleMax() {
        return xScaleMax;
    }

    /**
     *
     * @param xScaleMax
     */
    public void setXScaleMax(double xScaleMax) {
        this.xScaleMax = xScaleMax;
    }

    /**
     *
     * @return
     */
    public double getYScaleMin() {
        return yScaleMin;
    }

    /**
     *
     * @param yScaleMin
     */
    public void setYScaleMin(double yScaleMin) {
        this.yScaleMin = yScaleMin;
    }

    /**
     *
     * @return
     */
    public double getYScaleMax() {
        return yScaleMax;
    }

    /**
     *
     * @param yScaleMax
     */
    public void setYScaleMax(double yScaleMax) {
        this.yScaleMax = yScaleMax;
    }

    /**
     *
     * @return
     */
    public double getXMin() {
        return xMin;
    }

    /**
     *
     * @param xMin
     */
    public void setXMin(double xMin) {
        this.xMin = xMin;
        notifyListenersOfAxisChange();
    }

    /**
     *
     * @return
     */
    public double getXMax() {
        return xMax;
    }

    /**
     *
     * @param xMax
     */
    public void setXMax(double xMax) {
        this.xMax = xMax;
        notifyListenersOfAxisChange();
    }

    /**
     *
     * @return
     */
    public double getYMin() {
        return yMin;
    }

    /**
     *
     * @param yMin
     */
    public void setYMin(double yMin) {
        this.yMin = yMin;
        notifyListenersOfAxisChange();
    }

    /**
     *
     * @return
     */
    public double getYMax() {
        return yMax;
    }

    /**
     *
     * @param yMax
     */
    public void setYMax(double yMax) {
        this.yMax = yMax;
        notifyListenersOfAxisChange();
    }

    /**
     *
     * @return
     */
    public String getXUnit() {
        return xUnit;
    }

    /**
     *
     * @param xUnit
     */
    public void setXUnit(String xUnit) {
        this.xUnit = xUnit;
    }

    /**
     *
     * @return
     */
    public String getYUnit() {
        return yUnit;
    }

    /**
     *
     * @param yUnit
     */
    public void setYUnit(String yUnit) {
        this.yUnit = yUnit;
    }

    /**
     *
     * @return
     */
    public ResourceBundle getLangpack() {
        return langpack;
    }

    /**
     *
     * @param langpack
     */
    public void setLangpack(ResourceBundle langpack) {
        this.langpack = langpack;
    }

}
