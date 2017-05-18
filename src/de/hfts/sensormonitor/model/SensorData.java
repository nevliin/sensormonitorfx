/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import de.hft.ss17.cebarround.CeBarRoundEvent;
import de.hft.ss17.cebarround.CeBarRoundObserver;
import de.hft.ss17.cebarround.SensorEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorData implements CeBarRoundObserver<SensorEvent> {

    public enum Data {
        TEMPERATURE, PRESSURE, REVOLUTIONS
    }

    private List<DataChangeListener> listeners = new ArrayList<>();
    Map<Long, String> mapIDTypeCode;
    Map<Data, Map<Long, ArrayList<SensorDataPoint>>> graphs;

    public SensorData() {
        graphs = new HashMap<>();
        mapIDTypeCode = new HashMap<>();
        for (Data d : Data.values()) {
            graphs.put(d, new HashMap<>());
        }
    }

    @Override
    public void sensorDataEventListener(SensorEvent cbre) {
        if (!mapIDTypeCode.keySet().contains(cbre.getUniqueSensorIdentifier())) {
            mapIDTypeCode.put(cbre.getUniqueSensorIdentifier(), cbre.getSensorTypeCode());
            graphs.get(Data.TEMPERATURE).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
            graphs.get(Data.PRESSURE).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
            graphs.get(Data.REVOLUTIONS).put(cbre.getUniqueSensorIdentifier(), new ArrayList<>());
        }
        graphs.get(Data.TEMPERATURE).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getTemperature(), cbre.getDate()));
        graphs.get(Data.PRESSURE).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getPressure(), cbre.getDate()));
        graphs.get(Data.REVOLUTIONS).get(cbre.getUniqueSensorIdentifier()).add(0, new SensorDataPoint(cbre.getRevolutions(), cbre.getDate()));
        notifyListenersOfDataChange(cbre.getUniqueSensorIdentifier());
    }

    // <--- Listener operations --->
    /**
     * Add listener implementing DataChangeListener to the SensorChartData
     *
     * @param toAdd Object to be notified of changes
     */
    public void addListener(DataChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Remove listener from the list
     *
     * @param toRemove Object to remove from the listeners
     */
    public void removeListener(DataChangeListener toRemove) {
        listeners.remove(toRemove);
    }

    /**
     * Notifies all listeners of a change in the graph data
     *
     * @param graphname Sensor ID (name) of the graph that was changed
     */
    public void notifyListenersOfDataChange(long sensorID) {
        for (DataChangeListener dcl : listeners) {
            dcl.dataChanged(sensorID);
        }
    }

    public ArrayList<SensorDataPoint> getPoints(Data type, long sensorID) {
        return graphs.get(type).get(sensorID);
    }

    public String getTypeCode(long sensorID) {
        return mapIDTypeCode.get(sensorID);
    }

    public Set<Long> getSensorIDs() {
        return mapIDTypeCode.keySet();
    }

}
