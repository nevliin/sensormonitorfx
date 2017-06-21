/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import com.sun.javafx.collections.ObservableListWrapper;
import de.hft.ss17.cebarround.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * SensorData --- Receives and saves SensorEvent's from the BaseSensor's it
 * listens to
 *
 * @author Polarix IT Solutions
 */
public class SensorData implements CeBarRoundObserver<SensorEvent> {

    // -------------- PUBLIC ENUMERATIONS --------------------------------------
    /**
     * Enumeration listing all information provided by the sensors
     */
    public enum Data {

        /**
         * Type for data provided by SensorEvent.getTemperature()
         */
        TEMPERATURE,
        /**
         * Type for data provided by SensorEvent.getPressure()
         */
        PRESSURE,
        /**
         * Type for data provided by SensorEvent.getRevolutions()
         */
        REVOLUTIONS
    }

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Executor for processing incoming sensor data IMPORTANT:
     * Platform.runlater() must be used when adding processed data to the UI
     */
    private Executor dataExecutor = Executors.newSingleThreadExecutor();
    /**
     * List of SensorDataChangeListeners added to this instance of SensorData
     */
    private List<SensorDataChangeListener> listeners = new ArrayList<>();
    /**
     * List of SensorIDs, provides the model for the ComboCheckBox on the main
     * page
     */
    private ObservableList<String> sensorIDs = new ObservableListWrapper<>(new ArrayList<String>());
    /**
     * Map of Part type codes with the SensorIDs as key
     */
    private Map<Long, String> partTypeCodes = new LinkedHashMap<>();
    /**
     * Map of received sensor data, stored separated by type of data and
     * SensorID
     */
    private Map<Data, Map<Long, List<SensorDataPoint>>> graphs = new LinkedHashMap<>();

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * Adds HashMap for all Data values
     */
    public SensorData() {
        for (Data d : Data.values()) {
            graphs.put(d, new HashMap<>());
        }
    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Receives SensorEvent's from the BaseSensor's, reads the information and
     * saves it. Notifies SensorDataChangeListener's of the change in data
     *
     * @param cbre SensorEvent containing detailed sensor data
     */
    @Override
    public void sensorDataEventListener(SensorEvent cbre) {
        dataExecutor.execute(() -> {
            // Create new ArrayList's and save the TypeCode if the SensorID is unknown
            if (!partTypeCodes.keySet().contains(cbre.getUniqueSensorIdentifier())) {
                partTypeCodes.put(cbre.getUniqueSensorIdentifier(), cbre.getSensorTypeCode());
                sensorIDs.add(Long.toString(cbre.getUniqueSensorIdentifier()));
                graphs.get(Data.TEMPERATURE).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
                graphs.get(Data.PRESSURE).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
                graphs.get(Data.REVOLUTIONS).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
            }
            // Put the data into the according ArrayList's
            graphs.get(Data.TEMPERATURE).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getTemperature(), cbre.getDate()));
            graphs.get(Data.PRESSURE).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getPressure(), cbre.getDate()));
            graphs.get(Data.REVOLUTIONS).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getRevolutions(), cbre.getDate()));

            notifyListenersOfDataChange(cbre.getUniqueSensorIdentifier());
        });
    }

    /**
     * Adds a sensor by adding it to the corresponding Lists and Maps as well as
     * notifying listeners
     *
     * @param sensorID Unique ID of the sensor
     * @param typeCode Part type code of the sensor
     */
    public void addSensor(long sensorID, String typeCode) {
        partTypeCodes.put(sensorID, typeCode);
        sensorIDs.add(Long.toString(sensorID));
        graphs.get(Data.TEMPERATURE).put(sensorID, new ArrayList<>());
        graphs.get(Data.PRESSURE).put(sensorID, new ArrayList<>());
        graphs.get(Data.REVOLUTIONS).put(sensorID, new ArrayList<>());
        notifyListenersOfDataChange(sensorID);
    }

    // -------------- LISTENER METHODS -----------------------------------------
    /**
     * Add listener implementing DataChangeListener to the SensorChartData
     *
     * @param toAdd Object to be notified of changes
     */
    public void addListener(SensorDataChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Remove listener from the list
     *
     * @param toRemove Object to remove from the listeners
     */
    public void removeListener(SensorDataChangeListener toRemove) {
        listeners.remove(toRemove);
    }

    /**
     * Notifies all listeners of a change in the graph data
     *
     * @param sensorID Sensor ID (name) of the graph that was changed
     */
    public void notifyListenersOfDataChange(long sensorID) {
        for (SensorDataChangeListener dcl : listeners) {
            dcl.dataChanged(sensorID);
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @param type Type of data to be retrieved
     * @param sensorID ID of the sensor of which data should be retrieved
     * @return List of SensorDataPoint's of a single type of Data and received from a single sensor
     */
    public List<SensorDataPoint> getPoints(Data type, long sensorID) {
        return graphs.get(type).get(sensorID);
    }

    /**
     *
     * @param sensorID ID of the sensor
     * @return Part type code of the sensor
     */
    public String getTypeCode(long sensorID) {
        return partTypeCodes.get(sensorID);
    }

    /**
     *
     * @return List of SensorIDs of all connected sensors
     */
    public ObservableList<String> getSensorIDs() {
        return sensorIDs;
    }

    /**
     *
     * @return Map of part type codes with SensorID's as keys
     */
    public Map<Long, String> getPartTypeCodes() {
        return partTypeCodes;
    }

    public Map<Data, Map<Long, List<SensorDataPoint>>> getGraphs() {
        return graphs;
    }

}
