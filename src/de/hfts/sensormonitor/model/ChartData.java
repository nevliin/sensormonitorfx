/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import de.hfts.sensormonitor.chart.GraphPoint;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Polarix IT Solutions
 */
public class ChartData implements DataChangeListener {

    SensorData sensorData;
    Data type;
    ArrayList<XYChart.Series> data;

    public ChartData(Data type, SensorData sensorData) {
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    @Override
    public void dataChanged(long sensorID) {
        ArrayList<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
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
        }
        /*for (GraphPoint p : points) {
            if (!p.isEmpty()) {
                series.getData().add(new XYChart.Data(p.x, p.y));
            }
        }*/
    }

}
