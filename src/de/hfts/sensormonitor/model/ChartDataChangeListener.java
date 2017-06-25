/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

/**
 * ChartDataChangeListener --- Interface to be implemented by objects who want
 * to be informed about changes in a ChartData
 * 
 * @author Polarix IT Solutions
 */
public interface ChartDataChangeListener {
    
    /**
     * Triggered when the axis bounds in the ChartData changes
     */
    void axisChanged();
    
    /**
     * Triggered when a graph is added or removed
     */
    void graphsChanged();

}
