package de.hfts.sensormonitor.model;


/**
 * Interface to be implemented by objects who want to be informed about changes
 * in a SensorChartData
 *
 * @author Polarix IT Solutions
 */
public interface SensorDataChangeListener {

    /**
     * Triggered when the data in a SensorChartData changes
     *
     * @param graphname Sensor ID (name) of the graph which was changed
     */
    void dataChanged(long sensorID);

}
