/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

/**
 *
 * @author Polarix IT Solutions
 */
public interface ChartDataChangeListener {

    /**
     * Triggered when the data in a SensorChartData changes
     *
     * @param graphname Sensor ID (name) of the graph which was changed
     */
    void dataChanged(long sensorID);
    
    void axisChanged();

}
