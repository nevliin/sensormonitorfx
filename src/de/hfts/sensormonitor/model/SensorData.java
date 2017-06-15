/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import com.sun.javafx.collections.ObservableListWrapper;
import de.hft.ss17.cebarround.*;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * SensorData --- Receives and saves SensorEvent's from the BaseSensor's it
 * listens to
 *
 * @author Polarix IT Solutions
 */
public class SensorData implements CeBarRoundObserver<SensorEvent> {
    
    long oldTime = 0;

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
    private List<SensorDataChangeListener> listeners = new ArrayList<>();
    private ObservableList<String> sensorIDs = new ObservableListWrapper<>(new ArrayList<String>());
    private Map<Long, String> partTypeCodes = new LinkedHashMap<>();
    private Map<Data, Map<Long, ArrayList<SensorDataPoint>>> graphs = new LinkedHashMap<>();

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
     * @param cbre
     */
    @Override
    public void sensorDataEventListener(SensorEvent cbre) {
        Platform.runLater(() -> {            
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
     *
     * @param sensorID
     * @param typeCode
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
     * @param type
     * @param sensorID
     * @return
     */
    public ArrayList<SensorDataPoint> getPoints(Data type, long sensorID) {
        return graphs.get(type).get(sensorID);
    }

    /**
     *
     * @param sensorID
     * @return
     */
    public String getTypeCode(long sensorID) {
        return partTypeCodes.get(sensorID);
    }

    /**
     *
     * @return
     */
    public ObservableList<String> getSensorIDs() {
        return sensorIDs;
    }

    /**
     *
     * @return
     */
    public Map<Long, String> getPartTypeCodes() {
        return partTypeCodes;
    }

    public Map<Data, Map<Long, ArrayList<SensorDataPoint>>> getGraphs() {
        return graphs;
    }
    
        

}
