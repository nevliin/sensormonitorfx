/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import com.sun.javafx.collections.ObservableListWrapper;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Polarix IT Solutions
 */
public class ChartData implements DataChangeListener {

    private List<ChartDataChangeListener> listeners;
    private ObservableList<XYChart.Series<Double, Double>> lineChartModel = new ObservableListWrapper<>(new ArrayList<XYChart.Series<Double, Double>>());
    private HashMap<Long, XYChart.Series<Double, Double>> chartGraphs;
    private SensorData sensorData;
    private Data type;
    private double xScaleMin;
    private double xScaleMax;
    private double yScaleMin;
    private double yScaleMax;
    private double xMin;
    private double xMax;
    private double yMin = Double.MAX_VALUE;
    private double yMax = Double.MAX_VALUE;

    /**
     *
     * @param type
     * @param sensorData
     */
    public ChartData(Data type, SensorData sensorData) {
        listeners = new ArrayList<>();
        this.chartGraphs = new HashMap<>();
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    /**
     *
     */
    public ChartData() {
        listeners = new ArrayList<>();
        this.chartGraphs = new HashMap<>();
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
     * @param sensorID Sensor ID (name) of the graph that was changed
     */
    public void notifyListenersOfDataChange(long sensorID) {
        for (ChartDataChangeListener dcl : listeners) {
            dcl.dataChanged(sensorID);
        }
    }

    /**
     * Notifies all listeners of a change in the graph axis
     *
     *
     */
    public void notifyListenersOfAxisChange() {
        for (ChartDataChangeListener dcl : listeners) {
            dcl.axisChanged();
        }
    }

    /**
     *
     * @param sensorID
     * @return
     */
    public XYChart.Series<Double, Double> getSeries(long sensorID) {
        return chartGraphs.get(sensorID);
    }

    /**
     *
     * @param sensorID
     * @param points
     */
    public void addGraphToChart(long sensorID, List<SensorDataPoint> points) {
        if (chartGraphs.get(sensorID) == null) {
            XYChart.Series<Double, Double> series = new XYChart.Series<>();
            series.setName(Long.toString(sensorID));
            chartGraphs.put(sensorID, series);
            Platform.runLater(() -> {
                lineChartModel.add(series);
            });
        }
        setPointsToSeries(chartGraphs.get(sensorID), points);
        notifyListenersOfDataChange(sensorID);
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
        double lastTime = 0;
        Date lastPoint = null;
        for (SensorDataPoint p : points) {
            double time = 0;
            if (lastPoint != null) {
                time = lastPoint.getTime() - p.time.getTime();
                time = lastTime - (time / 1000.0);
            }
            if (time >= this.getxMin()-1) {
                if (!p.isEmpty()) {
                    try {
                        series.getData().add(new XYChart.Data(time, p.value));
                    } catch (NullPointerException e) {

                    }
                }
            }
            lastPoint = p.time;
            lastTime = time;
        }
    }

    @Override
    public void dataChanged(long sensorID) {
        ArrayList<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
        if (chartGraphs.get(sensorID) == null) {
            XYChart.Series<Double, Double> series = new XYChart.Series<>();
            series.setName(Long.toString(sensorID));
            chartGraphs.put(sensorID, series);
            Platform.runLater(() -> {
                lineChartModel.add(series);
            });
        }
        setPointsToSeries(chartGraphs.get(sensorID), points);
        notifyListenersOfDataChange(sensorID);
    }

    /**
     *
     * @return
     */
    public ObservableList<XYChart.Series<Double, Double>> getObservableList() {
        return lineChartModel;
    }

    public ChartData clone() {
        ChartData result = new ChartData(this.type, this.sensorData);
        sensorData.addListener(this);
        result.setxMax(xMax);
        result.setxMin(xMin);
        result.setyMax(yMax);
        result.setyMin(yMin);
        result.setxScaleMax(xScaleMax);
        result.setxScaleMin(yScaleMin);
        result.setyScaleMax(yScaleMax);
        result.setyScaleMin(yScaleMin);
        return result;
    }

    /**
     *
     * @return
     */
    public double getxScaleMin() {
        return xScaleMin;
    }

    /**
     *
     * @param xScaleMin
     */
    public void setxScaleMin(double xScaleMin) {
        this.xScaleMin = xScaleMin;
    }

    /**
     *
     * @return
     */
    public double getxScaleMax() {
        return xScaleMax;
    }

    /**
     *
     * @param xScaleMax
     */
    public void setxScaleMax(double xScaleMax) {
        this.xScaleMax = xScaleMax;
    }

    /**
     *
     * @return
     */
    public double getyScaleMin() {
        return yScaleMin;
    }

    /**
     *
     * @param yScaleMin
     */
    public void setyScaleMin(double yScaleMin) {
        this.yScaleMin = yScaleMin;
    }

    /**
     *
     * @return
     */
    public double getyScaleMax() {
        return yScaleMax;
    }

    /**
     *
     * @param yScaleMax
     */
    public void setyScaleMax(double yScaleMax) {
        this.yScaleMax = yScaleMax;
    }

    /**
     *
     * @return
     */
    public double getxMin() {
        return xMin;
    }

    /**
     *
     * @param xMin
     */
    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    /**
     *
     * @return
     */
    public double getxMax() {
        return xMax;
    }

    /**
     *
     * @param xMax
     */
    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    /**
     *
     * @return
     */
    public double getyMin() {
        return yMin;
    }

    /**
     *
     * @param yMin
     */
    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    /**
     *
     * @return
     */
    public double getyMax() {
        return yMax;
    }

    /**
     *
     * @param yMax
     */
    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    /**
     *
     * @return
     */
    public Data getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(Data type) {
        this.type = type;
    }
    
    

}
