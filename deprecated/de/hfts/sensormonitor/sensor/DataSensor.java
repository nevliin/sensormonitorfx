package de.hfts.sensormonitor.sensor;

import java.util.Random;

/**
 *
 * @author Polarix IT Solutions
 */
public class DataSensor {
    
    private double sensortypecode;
    private double uniquesensoridentifier;
    private SensorData data;
    
    public DataSensor() {
        Random rand = new Random();
        sensortypecode = rand.nextInt(10000);
        uniquesensoridentifier = rand.nextInt(1000000);
    }
    
    public void createNewData() {
        data = new SensorData();
    }

    public double getSensortypecode() {
        return sensortypecode;
    }

    public void setSensortypecode(double sensortypecode) {
        this.sensortypecode = sensortypecode;
    }

    public double getUniquesensoridentifier() {
        return uniquesensoridentifier;
    }

    public void setUniquesensoridentifier(double uniquesensoridentifier) {
        this.uniquesensoridentifier = uniquesensoridentifier;
    }

    public SensorData getData() {
        return data;
    }

    public void setData(SensorData data) {
        this.data = data;
    }
    
    
    
}
