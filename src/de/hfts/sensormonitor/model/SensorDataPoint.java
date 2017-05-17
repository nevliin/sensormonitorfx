/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorDataPoint {
    
    public double value;
    public Date time;

    public SensorDataPoint(double value, Date time) {
        this.value = value;
        this.time = time;
    }

    public SensorDataPoint() {
    }
    
    
    
}
