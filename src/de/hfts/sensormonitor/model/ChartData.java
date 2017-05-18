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

    private ObservableList<XYChart.Series> chartSeries;
    private SensorData sensorData;
    private Data type;
    private double xScaleMin;
    private double xScaleMax;
    private double yScaleMin;
    private double yScaleMax;
    private double xMin = -50;
    private double xMax;
    private double yMin;
    private double yMax;

    public ChartData(Data type, SensorData sensorData) {
        this.chartSeries = new ObservableListWrapper<XYChart.Series>(new ArrayList<XYChart.Series>());
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    public ObservableList<XYChart.Series> getChartSeries() {
        return chartSeries;
    }

    public void setChartSeries(ObservableList<XYChart.Series> chartSeries) {
        this.chartSeries = chartSeries;
    }

    @Override
    public void dataChanged(long sensorID) {
        ArrayList<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
        if (containsSensorID(sensorID)) {
            XYChart.Series series = new XYChart.Series();
            series.setName(Long.toString(sensorID));
            chartSeries.add(series);
        }
        if (points != null) {
            setPointsToSeries(getSeries(sensorID), points);
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
                time = (time / 1000.0);
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

    public boolean containsSensorID(long sensorID) {
        for (XYChart.Series s : chartSeries) {
            if (s.getName().equalsIgnoreCase(Long.toString(sensorID))) {
                return true;
            }
        }
        return false;
    }

    public XYChart.Series getSeries(long sensorID) {
        for (XYChart.Series s : chartSeries) {
            if (s.getName().equalsIgnoreCase(Long.toString(sensorID))) {
                return s;
            }
        }
        return null;
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
