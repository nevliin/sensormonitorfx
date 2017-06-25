/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

/**
 * TableDataChangeListener --- Interface to be implemented by objects who want
 * to be informed about changes in a TableData
 * @author Polarix IT Solutions
 */
public interface TableDataChangeListener {
    
    /**
     * Triggered when the data in the TableData changes
     */
    void dataChanged();

}
