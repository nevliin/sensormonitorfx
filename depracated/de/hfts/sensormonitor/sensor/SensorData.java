/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.sensor;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorData {
    
    HashMap<String, Double> data;
    Timestamp time;
    
    public SensorData() {
        Random random = new Random();
        data = new HashMap<>();
        double temperature = random.nextInt(50)+25;
        data.put("Temperature", temperature);
        double pressure = random.nextInt(10000)/100+950;
        data.put("Pressure", pressure);
        double rpm = random.nextInt(5000);
        data.put("Revolutions", rpm);
        time = new Timestamp(new GregorianCalendar().getTimeInMillis());
    }

    public HashMap<String, Double> getData() {
        return data;
    }

    public void setData(HashMap<String, Double> data) {
        this.data = data;
    }

    public Timestamp getTime() {
        return time;
    }
    
    
    
    
    
}
