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
     * X-value of the point
     */
    public double value;

    /**
     * Y-value of the point; time in seconds relative to the current time or to
     * the start of the recording
     */
    public Date time;

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Indicates if this SensorDataPoint contains no data
     */
    private boolean isEmpty = false;

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     *
     * @param value Y-value of the point
     * @param time X-value of the point
     * @param isEmpty Indicates if this SensorDataPoint contains no data
     */
    public SensorDataPoint(double value, Date time, boolean isEmpty) {
        this.value = value;
        this.time = time;
        this.isEmpty = isEmpty;
    }

    /**
     *
     * @param value Y-value of the point
     * @param time X-value of the point
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
     * @return Indicates if this SensorDataPoint contains no data
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     *
     * @param isEmpty Indicates if this SensorDataPoint contains no data
     */
    public void isEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

}
