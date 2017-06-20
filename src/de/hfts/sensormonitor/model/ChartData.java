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
 * ChartData --- Converts data from the SensorData to XYChart.Series and stores
 * it, provides the model for the SensorChart
 *
 * @author Polarix IT Solutions
 */
public class ChartData implements SensorDataChangeListener {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * List of listeners for changes in the axis-bounds
     */
    private List<ChartDataChangeListener> listeners = new ArrayList<>();
    /**
     * Data model for SensorCharts
     */
    private ObservableList<XYChart.Series<Double, Double>> lineChartModel = new ObservableListWrapper<>(new ArrayList<XYChart.Series<Double, Double>>());
    /**
     * Map storing the Series with the SensorID's as keys for easy access
     */
    private Map<Long, XYChart.Series<Double, Double>> chartGraphs = new HashMap<>();
    /**
     * Map of part type codes with the SensorIDs as key
     */
    private HashMap<Long, String> partTypeCodes = new HashMap<>();
    /**
     * Related SensorData; null if the ChartData was created from a recording
     */
    private SensorData sensorData;
    /**
     * Data type displayed in the SensorChart
     */
    private SensorData.Data type;
    /**
     * Minimal value of the X-axis
     */
    private double xScaleMin;
    /**
     * Maximal value of the X-axis
     */
    private double xScaleMax;
    /**
     * Minimal value of the Y-axis
     */
    private double yScaleMin;
    /**
     * Maximal value of the Y-axis
     */
    private double yScaleMax;
    /**
     * Lower bound of the X-axis
     */
    private double xMin;
    /**
     * Upper bound of the X-axis
     */
    private double xMax;
    /**
     * Lower bound of the Y-axis; Double.MAX_VALUE if the Y-axis is set to
     * autoranging
     */
    private double yMin = Double.MAX_VALUE;
    /**
     * Upper bound of the Y-axis; Double.MAX_VALUE if the Y-axis is set to
     * autoranging
     */
    private double yMax = Double.MAX_VALUE;

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * Creates new ChartData, sets type and starts listening to the SensorData
     * for new data
     *
     * @param type Type of data saved in the ChartData
     * @param sensorData SensorData providing sensor data to the ChartData
     */
    public ChartData(Data type, SensorData sensorData) {
        listeners = new ArrayList<>();
        this.chartGraphs = new HashMap<>();
        this.type = type;
        this.sensorData = sensorData;
        sensorData.addListener(this);
    }

    /**
     * Creates new ChartData and sets type
     *
     * @param type Type of data saved in the ChartData
     */
    public ChartData(Data type) {
        this.type = type;
    }

    // -------------- LISTENER METHODS -----------------------------------------
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
     * Notifies all listeners of a change in the graph axis
     */
    public void notifyListenersOfAxisChange() {
        for (ChartDataChangeListener dcl : listeners) {
            dcl.axisChanged();
        }
    }

    /**
     * Notifies all listeners of a change in the graph axis
     */
    public void notifyListenersOfGraphChange() {
        for (ChartDataChangeListener dcl : listeners) {
            dcl.graphsChanged();
        }
    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Adds a List of SensorDataPoint's to the ChartData
     *
     * @param sensorID Sensor ID of the sensors the data was received from
     * @param points List of SensorDataPoint's with received data
     */
    public void addGraphToChart(long sensorID, List<SensorDataPoint> points) {
        if (chartGraphs.get(sensorID) == null) {
            XYChart.Series<Double, Double> series = new XYChart.Series<>();
            series.setName(Long.toString(sensorID));
            chartGraphs.put(sensorID, series);
            Platform.runLater(() -> {
                lineChartModel.add(series);
                notifyListenersOfGraphChange();
            });
        }
        setPointsToSeries(chartGraphs.get(sensorID), points);
    }

    /**
     * Clears the series and adds the given SensorDataPoint's to it
     *
     * @param series Series of the SensorChart
     * @param points List of SensorDataPoint's with received data
     */
    private void setPointsToSeries(XYChart.Series series, List<SensorDataPoint> points) {
        List<XYChart.Data> data = new ArrayList<>();
        double lastTime = 0;
        Date lastPoint = null;
        for (SensorDataPoint p : points) {
            double time = 0;
            if (lastPoint != null) {
                time = lastPoint.getTime() - p.time.getTime();
                time = lastTime - (time / 1000.0);
            }
            if (time >= this.getxMin() - 1) {
                if (!p.isEmpty()) {
                    final double currentTime = time;
                    data.add(new XYChart.Data(currentTime, p.value));
                }
            }
            lastPoint = p.time;
            lastTime = time;
        }
        Platform.runLater(() -> {
            try {
                series.getData().clear();
            } catch (NullPointerException e) {
                // NO-OP - Catching NullPointerException if the series doesn't have any data yet
            }
        });

        // Add all new data at once to prevent lag in the rendering of the data
        Platform.runLater(() -> {
            for (XYChart.Data d : data) {
                series.getData().add(d);
            }
        });
    }

    /**
     * Listener for changes in the connected SensorData
     *
     * @param sensorID SensorID of the sensor which data changed
     */
    @Override
    public void dataChanged(long sensorID) {
        List<SensorDataPoint> points = sensorData.getPoints(type, sensorID);
        if (chartGraphs.get(sensorID) == null) {
            partTypeCodes.put(sensorID, sensorData.getTypeCode(sensorID));
        }
        addGraphToChart(sensorID, points);
    }

    /**
     * Returns a new ChartData with the attributes of this ChartData
     *
     * @return New ChartData with similar variable values
     */
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
     * Changes the visibility of a specific graph
     *
     * @param sensorID SensorID of the sensors whose graphs visibility should be
     * changed
     * @param isVisible Boolean indicating the visibility
     */
    public void setGraphVisible(long sensorID, boolean isVisible) {
        if (isVisible) {
            if (!lineChartModel.contains(chartGraphs.get(sensorID))) {
                lineChartModel.add(chartGraphs.get(sensorID));
                notifyListenersOfGraphChange();
            }
        } else {
            try {
                lineChartModel.remove(chartGraphs.get(sensorID));
                notifyListenersOfGraphChange();
            } catch (NullPointerException ex) {
                // NO-OP
            }
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     * Returns a series from the ChartData based on the SensorID
     *
     * @param sensorID SensorID of the sensor
     * @return Series of the SensorChart
     */
    public XYChart.Series<Double, Double> getSeries(long sensorID) {
        return chartGraphs.get(sensorID);
    }

    /**
     * Returns all series of the ChartData
     *
     * @return Collection of all series
     */
    public Collection<XYChart.Series<Double, Double>> getSeries() {
        return chartGraphs.values();
    }

    /**
     *
     * @return ObservableList of the Series displayed in the related SensorChart
     */
    public ObservableList<XYChart.Series<Double, Double>> getObservableList() {
        return lineChartModel;
    }

    /**
     *
     * @return Minimal value of the lower bound of the X-axis
     */
    public double getxScaleMin() {
        return xScaleMin;
    }

    /**
     *
     * @param xScaleMin Minimal value of the lower bound of the X-axis
     */
    public void setxScaleMin(double xScaleMin) {
        this.xScaleMin = xScaleMin;
    }

    /**
     *
     * @return Maximal value of the upper bound of the X-axis
     */
    public double getxScaleMax() {
        return xScaleMax;
    }

    /**
     *
     * @param xScaleMax Maximal value of the upper bound of the X-axis
     */
    public void setxScaleMax(double xScaleMax) {
        this.xScaleMax = xScaleMax;
    }

    /**
     *
     * @return Minimal value of the lower bound of the Y-axis
     */
    public double getyScaleMin() {
        return yScaleMin;
    }

    /**
     *
     * @param yScaleMin Minimal value of the lower bound of the Y-axis
     */
    public void setyScaleMin(double yScaleMin) {
        this.yScaleMin = yScaleMin;
    }

    /**
     *
     * @return Maximal value of the upper bound of the Y-axis
     */
    public double getyScaleMax() {
        return yScaleMax;
    }

    /**
     *
     * @param yScaleMax Maximal value of the upper bound of the Y-axis
     */
    public void setyScaleMax(double yScaleMax) {
        this.yScaleMax = yScaleMax;
    }

    /**
     *
     * @return Lower bound of the X-axis
     */
    public double getxMin() {
        return xMin;
    }

    /**
     *
     * @param xMin Lower bound of the X-axis
     */
    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    /**
     *
     * @return Upper bound of the X-axis
     */
    public double getxMax() {
        return xMax;
    }

    /**
     *
     * @param xMax Upper bound of the X-axis
     */
    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    /**
     *
     * @return Lower bound of the Y-axis
     */
    public double getyMin() {
        return yMin;
    }

    /**
     *
     * @param yMin Lower bound of the Y-axis
     */
    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    /**
     *
     * @return Upper bound of the Y-axis
     */
    public double getyMax() {
        return yMax;
    }

    /**
     *
     * @param yMax Upper bound of the Y-axis
     */
    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    /**
     *
     * @return Type of data saved in the ChartData
     */
    public Data getType() {
        return type;
    }

    /**
     *
     * @param type Type of data saved in the ChartData
     */
    public void setType(Data type) {
        this.type = type;
    }

    /**
     *
     * @return Map of part type codes with SensorID's as keys
     */
    public Map<Long, String> getPartTypeCodes() {
        return partTypeCodes;
    }

    /**
     *
     * @param partTypeCodes Map of part type codes with SensorID's as keys
     */
    public void setPartTypeCodes(HashMap<Long, String> partTypeCodes) {
        this.partTypeCodes = partTypeCodes;
    }

}
