/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.model.*;
import de.hfts.sensormonitor.model.SensorData.Data;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recording --- Converts a given ResultSet into ChartData's, stores them to be
 * used for SensorCharts
 *
 * @author Polarix IT Solutions
 */
public class Recording {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * First timestamp of the recording
     */
    private Timestamp firsttimestamp;
    /**
     * Last timestamp of the recording
     */
    private Timestamp lasttimestamp; 
    /**
     * Map storing SensorIDs and TypeCodes
     */
    private HashMap<Long, String> sensors = new HashMap<>();
    /**
     * Map storing ChartData's based on SensorData.Data
     */
    private HashMap<Data, ChartData> chartDatas = new HashMap<>();

    // -------------- CONSTRUCTOR ----------------------------------------------
    /**
     * Creates Recording, converts the given ResultSet into multiple ChartData's, sets their attributes and stores them
     * @param recording
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
                if (sensors.get(Long.valueOf(recording.getString("SENSORID"))) == null) {
                    sensors.put(Long.valueOf(recording.getString("SENSORID")), recording.getString("SENSORTYPE"));
                    temperature_points.put(Long.valueOf(recording.getString("SENSORID")), new ArrayList<>());
                    pressure_points.put(Long.valueOf(recording.getString("SENSORID")), new ArrayList<>());
                    revolutions_points.put(Long.valueOf(recording.getString("SENSORID")), new ArrayList<>());
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
                temperature_points.get(Long.valueOf(recording.getString("SENSORID"))).add(temperature);
                pressure_points.get(Long.valueOf(recording.getString("SENSORID"))).add(pressure);
                revolutions_points.get(Long.valueOf(recording.getString("SENSORID"))).add(revolutions);

            }
            recording.close();
        } catch (SQLException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
        }

        chartDatas.put(Data.TEMPERATURE, new ChartData(Data.TEMPERATURE));
        chartDatas.put(Data.PRESSURE, new ChartData(Data.PRESSURE));
        chartDatas.put(Data.REVOLUTIONS, new ChartData(Data.REVOLUTIONS));

        for (Data d : chartDatas.keySet()) {
            chartDatas.get(d).setxMin(0);
            chartDatas.get(d).setxMax((lasttimestamp.getTime() - firsttimestamp.getTime()) / 1000);
            chartDatas.get(d).setxScaleMin(0);
            chartDatas.get(d).setxScaleMax((lasttimestamp.getTime() - firsttimestamp.getTime()) / 1000);
            chartDatas.get(d).setPartTypeCodes(sensors);
        }

        chartDatas.get(Data.TEMPERATURE).setyScaleMax(Double.valueOf(IOUtils.getConfigProp("temperature_yscalemax")));
        chartDatas.get(Data.TEMPERATURE).setyScaleMin(Double.valueOf(IOUtils.getConfigProp("temperature_yscalemin")));
        chartDatas.get(Data.PRESSURE).setyScaleMax(Double.valueOf(IOUtils.getConfigProp("pressure_yscalemax")));
        chartDatas.get(Data.PRESSURE).setyScaleMin(Double.valueOf(IOUtils.getConfigProp("pressure_yscalemin")));
        chartDatas.get(Data.REVOLUTIONS).setyScaleMax(Double.valueOf(IOUtils.getConfigProp("revolutions_yscalemax")));
        chartDatas.get(Data.REVOLUTIONS).setyScaleMin(Double.valueOf(IOUtils.getConfigProp("revolutions_yscalemin")));

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

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     * Returns the first timestamp in the recording
     * @return First timestamp in the recording
     */
    public Timestamp getFirsttimestamp() {
        return firsttimestamp;
    }

    /**
     * Returns the last timestamp in the recording
     * @return Last Timestamp in the recording
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

    /**
     *
     * @param d
     * @return
     */
    public ChartData getChartData(Data d) {
        return chartDatas.get(d);
    }
}
