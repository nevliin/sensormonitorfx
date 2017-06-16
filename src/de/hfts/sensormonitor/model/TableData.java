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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;

/**
 * TableData --- Converts data from the SensorData to Lists and stores it,
 * provides the model for TableViews
 *
 * @author Polarix IT Solutions
 */
public class TableData implements SensorDataChangeListener {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Count of columns in the Table
     */
    private int maxColumn = 0;
    /**
     * Minimal time to be displayed in the Table
     */
    private double minTime;

    /**
     * List of listeners
     */
    private List<TableDataChangeListener> listeners = new ArrayList<>();

    /**
     * Number of the column related to each SensorID, stored in a map with the
     * SensorID's as key
     */
    private Map<Long, Integer> columnIDs = new LinkedHashMap<>();
    /**
     * Data model of the SensorTable
     */
    private ObservableList<ObservableList<Double>> data = new ObservableListWrapper<>(new ArrayList<>());
    /**
     * Data type of the TableData
     */
    private SensorData.Data type;
    /**
     * Related SensorData
     */
    private SensorData sensorData;

    // -------------- CONSTRUCTOR ----------------------------------------------
    /**
     * Standard Constructor
     *
     * @param type Data type of the TableData
     * @param sensorData SensorData related to the TableData
     */
    public TableData(Data type, SensorData sensorData) {
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
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

    // -------------- OTHER METHODS -----------------------------------------
    /**
     * Save the column number of the sensor
     *
     * @param sensorID SensorID
     */
    public void addSensor(long sensorID) {
        columnIDs.put(sensorID, maxColumn);
        maxColumn += 2;
    }

    @Override
    public void dataChanged(long sensorID) {
        setPointsToData(sensorData.getPoints(type, sensorID), data, columnIDs.get(sensorID));
        notifyListenersOfDataChange();
    }

    /**
     * Sets the values of the SensorDataPoints to the specified columns of the
     * data model
     *
     * @param points List of SensorDataPoints
     * @param observList Data model of the SensorTable
     * @param columnId Number of the column related to the SensorID
     */
    public void setPointsToData(List<SensorDataPoint> points, List<ObservableList<Double>> observList, int columnId) {
        double lastTime = 0;
        Date lastPoint = null;
        for (int i = 0; i < points.size(); i++) {
            SensorDataPoint point = points.get(i);

            double time = 0;
            if (lastPoint != null) {
                time = (lastPoint.getTime() - point.time.getTime());
                time = lastTime - (time / 1000);
                time = round(time, 3);
            }
            if (time >= minTime) {
                try {
                    observList.get(i).set(columnId, time);
                    observList.get(i).set(columnId + 1, point.value);
                } catch (IndexOutOfBoundsException e) {
                    observList.add(i, new ObservableListWrapper<>(new ArrayList<Double>()));
                    for (int j = 0; j < maxColumn - 2; j++) {
                        observList.get(i).add(null);
                    }
                    observList.get(i).add(columnId, time);
                    observList.get(i).add(columnId + 1, point.value);
                }
            } else {
                try {
                    observList.remove(i);
                } catch (IndexOutOfBoundsException ex) {
                    // NO-OP
                }
            }
            lastPoint = point.time;
            lastTime = time;
        }
    }

    /**
     * Utility for rounding numbers to a certain amount of places
     *
     * @param value Number
     * @param places Decimal places
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

    // -------------- LISTENER METHODS -----------------------------------------
    /**
     * Add listener implementing DataChangeListener to the SensorChartData
     *
     * @param toAdd Object to be notified of changes
     */
    public void addListener(TableDataChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Remove listener from the list
     *
     * @param toRemove Object to remove from the listeners
     */
    public void removeListener(TableDataChangeListener toRemove) {
        listeners.remove(toRemove);
    }

    /**
     * Notifies all listeners of a change in the graph axis
     *
     *
     */
    public void notifyListenersOfDataChange() {
        for (TableDataChangeListener dcl : listeners) {
            dcl.dataChanged();
        }
    }

}
