/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.chart.GraphPoint;
import de.hfts.sensormonitor.main.SensorMonitor;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.model.SensorData.Data;
import de.hfts.sensormonitor.model.SensorDataPoint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inner class for a recording loaded from the database
 *
 * @author Polarix IT Solutions
 */
public class Recording {

    private Timestamp firsttimestamp; // First timestamp of the recording
    private Timestamp lasttimestamp; // Last timestamp of the recording
    private HashMap<Long, String> sensors = new HashMap<>(); // A map with the sensorids as key and the corresponding sensortypes as value
    private HashMap<Data, ChartData> chartDatas = new HashMap<>();

    /**
     *
     * @param name
     */
    public Recording(ResultSet recording) {
        HashMap<Long, List<SensorDataPoint>> temperature_points = new HashMap<>();
        HashMap<Long, List<SensorDataPoint>> pressure_points = new HashMap<>();
        HashMap<Long, List<SensorDataPoint>> revolutions_points = new HashMap<>();
        try {
            while (recording.next()) {
                if (firsttimestamp == null) {
                    firsttimestamp = recording.getTimestamp("TIME");
                }
                if (sensors.get(recording.getLong("SENSORID")) == null) {
                    sensors.put(recording.getLong("SENSORID"), recording.getString("SENSORTYPE"));
                    temperature_points.put(recording.getLong("SENSORID"), new ArrayList<>());
                    pressure_points.put(recording.getLong("SENSORID"), new ArrayList<>());
                    revolutions_points.put(recording.getLong("SENSORID"), new ArrayList<>());
                }

                lasttimestamp = recording.getTimestamp("TIME");

                SensorDataPoint temperature = new SensorDataPoint();
                temperature.time = lasttimestamp;
                try {
                    temperature.value = Double.valueOf(recording.getString("TEMPERATURE"));
                } catch (NullPointerException e) {
                    temperature.isEmpty(true);
                }

                SensorDataPoint pressure = new SensorDataPoint();
                pressure.time = lasttimestamp;
                try {
                    pressure.value = Double.valueOf(recording.getString("PRESSURE"));
                } catch (NullPointerException e) {
                    pressure.isEmpty(true);
                }

                SensorDataPoint revolutions = new SensorDataPoint();
                revolutions.time = lasttimestamp;
                try {
                    revolutions.value = Double.valueOf(recording.getString("REVOLUTIONS"));
                } catch (NullPointerException e) {
                    revolutions.isEmpty(true);
                }
                temperature_points.get(recording.getLong("SENSORID")).add(temperature);
                pressure_points.get(recording.getLong("SENSORID")).add(pressure);
                revolutions_points.get(recording.getLong("SENSORID")).add(revolutions);

            }
        } catch (SQLException ex) {
            Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        chartDatas.put(Data.TEMPERATURE, new ChartData());
        chartDatas.put(Data.PRESSURE, new ChartData());
        chartDatas.put(Data.REVOLUTIONS, new ChartData());
        
        for(Data d : chartDatas.keySet()) {
            chartDatas.get(d).setxMin(0);
            chartDatas.get(d).setxMax((lasttimestamp.getTime() - firsttimestamp.getTime())/1000);
        }
        
        for (long l : temperature_points.keySet()) {
            chartDatas.get(Data.TEMPERATURE).addGraphToChart(l, temperature_points.get(l));
        }        
        for (long l : pressure_points.keySet()) {
            chartDatas.get(Data.PRESSURE).addGraphToChart(l, pressure_points.get(l));
        }
        for (long l : revolutions_points.keySet()) {
            chartDatas.get(Data.REVOLUTIONS).addGraphToChart(l, revolutions_points.get(l));
        }

    }

    /**
     *
     * @return
     */
    public Timestamp getFirsttimestamp() {
        return firsttimestamp;
    }

    /**
     *
     * @return
     */
    public Timestamp getLasttimestamp() {
        return lasttimestamp;
    }

    /**
     *
     * @return
     */
    public HashMap<Long, String> getSensors() {
        return sensors;
    }
    
    public ChartData getChartData(Data d) {
        return chartDatas.get(d);
    }
}
