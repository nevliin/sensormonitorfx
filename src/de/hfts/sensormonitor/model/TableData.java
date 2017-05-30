/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Polarix IT Solutions
 */
public class TableData implements SensorDataChangeListener {
    
    private ObservableList<List<Double>> data = FXCollections.observableArrayList();

    @Override
    public void dataChanged(long sensorID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
