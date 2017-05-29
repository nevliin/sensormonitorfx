/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.model;

import java.util.Date;

/**
 * SensorDataPoint --- Saves the time and value of one type of information from
 * a SensorEvent
 *
 * @author Polarix IT Solutions
 */
public class SensorDataPoint {

    // -------------- PUBLIC FIELDS --------------------------------------------

    /**
     *
     */
    public double value;

    /**
     *
     */
    public Date time;
    
    // -------------- PRIVATE FIELDS -------------------------------------------
    private boolean isEmpty = false;

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * 
     * @param value
     * @param time
     * @param isEmpty
     */
    public SensorDataPoint(double value, Date time, boolean isEmpty) {
        this.value = value;
        this.time = time;
        this.isEmpty = isEmpty;
    }

    /**
     *
     * @param value
     * @param time
     */
    public SensorDataPoint(double value, Date time) {
        this.value = value;
        this.time = time;
    }

    /**
     *
     */
    public SensorDataPoint() {
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     *
     * @param isEmpty
     */
    public void isEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

}
