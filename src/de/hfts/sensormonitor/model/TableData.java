/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import com.sun.javafx.collections.ObservableListWrapper;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * TableData --- Converts data from the SensorData to Lists and stores it,
 * provides the model for TableViews
 *
 * @author Polarix IT Solutions
 */
public class TableData implements SensorDataChangeListener {

    /**
     * Utility for rounding numbers to a certain amount of places
     *
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * TableDataPoint --- Stores time as a double relative to the last
     * SensorEvent and the value of the SensorEvent
     */
    class TableDataPoint {

        public double time;
        public double value;

        public TableDataPoint(double time, double value) {
            this.time = time;
            this.value = value;
        }

        public TableDataPoint() {

        }

    }

    private int maxColumn = 0;
    private double minTime;

    private LinkedHashMap<Long, Integer> columnIDs = new LinkedHashMap<>();
    private ObservableList<ObservableList<Double>> data = new ObservableListWrapper<>(new ArrayList<>());
    private Data type;
    private SensorData sensorData;

    /**
     * Standard Constructor
     *
     * @param type
     * @param sensorData
     */
    public TableData(Data type, SensorData sensorData) {
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    /**
     *
     * @param sensorID
     */
    public void addSensor(long sensorID) {
        columnIDs.put(sensorID, maxColumn);
        maxColumn += 2;
    }

    @Override
    public void dataChanged(long sensorID) {
        setPointsToData(sensorData.getPoints(type, sensorID), data, columnIDs.get(sensorID));
    }

    /**
     *
     * @param points
     * @param observList
     * @param columnId
     */
    public void setPointsToData(List<SensorDataPoint> points, List<ObservableList<Double>> observList, int columnId) {
        double lastTime = 0;
        Date lastPoint = null;
        for (int i = 0; i < points.size(); i++) {
            SensorDataPoint p = points.get(i);

            double time = 0;
            if (lastPoint != null) {
                time = (lastPoint.getTime() - p.time.getTime());
                time = lastTime - (time / 1000);
                time = round(time, 3);
            }
            try {
                observList.get(i).set(columnId, time);
                observList.get(i).set(columnId + 1, p.value);
            } catch (IndexOutOfBoundsException e) {
                observList.add(i, new ObservableListWrapper<>(new ArrayList<Double>()));
                for (int j = 0; j < maxColumn; j++) {
                    observList.get(i).add(null);
                }
                observList.get(i).add(columnId, time);
                observList.get(i).add(columnId + 1, p.value);
            }
            lastPoint = p.time;
            lastTime = time;
        }
    }

    /**
     *
     * @return
     */
    public ObservableList<ObservableList<Double>> getData() {
        return data;
    }

    /**
     *
     * @return
     */
    public double getMinTime() {
        return minTime;
    }

    /**
     *
     * @param minTime
     */
    public void setMinTime(double minTime) {
        this.minTime = minTime;
    }

}
