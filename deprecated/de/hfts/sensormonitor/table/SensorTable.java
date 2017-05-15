/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.table;

import de.hfts.sensormonitor.chart.DataChangeListener;
import de.hfts.sensormonitor.chart.GraphPoint;
import de.hfts.sensormonitor.chart.SensorChartData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorTable implements DataChangeListener {

    private SensorChartData data;
    private ResourceBundle langpack;
    private TableView<ObservableList<Double>> table;

    /**
     * Inner class representing a row of the table
     */
    public class SensorDataRow implements Comparable {

        private double time;
        private List<Double> sensordata;

        /**
         * Standard constructor for SensorDataRow
         * @param time Key value for the SensorDataRow
         */
        public SensorDataRow(double time) {
            this.time = time;
            sensordata = new ArrayList<>();
        }

        /**
         *
         * @return
         */
        public double getTime() {
            return time;
        }

        /**
         *
         * @param time
         */
        public void setTime(double time) {
            this.time = time;
        }

        /**
         *
         * @return
         */
        public List<Double> getSensordata() {
            return sensordata;
        }

        /**
         *
         * @param sensordata
         */
        public void setSensordata(List<Double> sensordata) {
            this.sensordata = sensordata;
        }

        /**
         * Convert the SensorDataRow to a List following the scheme [time, sensordata...]
         * @return
         */
        public List<Double> toList() {
            List<Double> result = new ArrayList<>();
            result.add(this.time);
            for (double d : sensordata) {
                result.add(d);
            }
            return result;
        }

        @Override
        public int compareTo(Object o) {
            SensorDataRow r = (SensorDataRow) o;
            if (r.getTime() > this.time) {
                return 1;
            } else if (r.getTime() < this.time) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    /**
     * Standard constructor for SensorTable
     * @param data SensorChartData to be displayed by the table
     * @param langpack
     */
    public SensorTable(SensorChartData data, ResourceBundle langpack) {
        table = new TableView<>();
        this.data = data;
        this.langpack = langpack;
        data.addListener(this);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ObservableList<Double>, Double> tablecol_time = new TableColumn(langpack.getString("time") + " (" + data.getXUnit() + ")");
        tablecol_time.setCellValueFactory(param -> {
            Double d = null;
            try {
                d = param.getValue().get(0);
            } catch (IndexOutOfBoundsException e) {
                d = Double.MAX_VALUE;
            }
            return new ReadOnlyObjectWrapper<>(d);
        }
        );

        TableColumn tablecol_sensors = new TableColumn(langpack.getString("sensors") + " (" + data.getYUnit() + ")");
        int counter = 1;
        for (String s : data.getGraphs().keySet()) {
            TableColumn<ObservableList<Double>, Double> tablecol_sensor = new TableColumn<>(s);
            tablecol_sensors.getColumns().add(tablecol_sensor);
            final int counterFixed = counter;
            tablecol_sensor.setCellValueFactory(param -> {
                Double d = null;
                try {
                    d = param.getValue().get(counterFixed);
                } catch (IndexOutOfBoundsException e) {
                    d = Double.MAX_VALUE;
                }
                return new ReadOnlyObjectWrapper<>(d);
            });
            counter++;
        }
        table.getColumns().addAll(tablecol_time, tablecol_sensors);
    }

    /**
     * Get the related graphs from the SensorChartData, convert them and display them in the TableView
     */
    private void updateData() {
        LinkedHashMap<Double, SensorDataRow> rows = new LinkedHashMap<>();
        for (String s : data.getGraphs().keySet()) {
            for (GraphPoint p : data.getGraphs().get(s).getPoints()) {
                if (!rows.keySet().contains(p.x)) {
                    rows.put(p.x, new SensorDataRow(p.x));
                }
                rows.get(p.x).getSensordata().add(p.y);
            }
        }
        List<SensorDataRow> arrayList_rows = new ArrayList<>(rows.values());
        Collections.sort(arrayList_rows);
        table.getItems().clear();
        for (SensorDataRow sdr : arrayList_rows) {
            List al = sdr.toList();
            table.getItems().add(FXCollections.observableArrayList(al));
        }
    }

    @Override
    public void dataChanged(String graphname) {
        updateData();
    }

    @Override
    public void visibilityChanged(String graphname, boolean isVisible) {

    }

    @Override
    public void axisChanged() {

    }

    /**
     * Returns the TableView associated with the SensorTable
     * @return
     */
    public TableView<ObservableList<Double>> getTable() {
        return table;
    }

}
