/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import de.hfts.sensormonitor.model.SensorData.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Polarix IT Solutions
 */
public class TableData implements SensorDataChangeListener {
    
    class TableDataPoint {
        public double time;
        public double value;
        
        public TableDataPoint(double time, double value) {
            this.time = time;
            this.value = value;
        }
        
        public TableDataPoint() {
            
        }
        
    }
    
    private HashMap<Long, ArrayList<TableDataPoint>> points;
    private ObservableList<List<Double>> data = FXCollections.observableArrayList();
    private Data type;
    private Date lastDate;

    public TableData(Data type) {
        this.type = type;
    }
    
    @Override
    public void dataChanged(long sensorID) {
        
    }
    
}
