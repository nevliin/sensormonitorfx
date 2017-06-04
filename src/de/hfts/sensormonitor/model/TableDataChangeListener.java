/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import javafx.collections.ObservableList;

/**
 * ChartDataChangeListener --- Listener for a ChartData
 * @author Polarix IT Solutions
 */
public interface TableDataChangeListener {
    
    /**
     * Triggered when the data in the TableData changes
     */
    void dataChanged();

}
