package de.hfts.sensormonitor.table;

import de.hfts.sensormonitor.chart.DataChangeListener;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author K
 */
public class SensorTableModel implements DataChangeListener {

    private ObservableList<List<Double>> data = FXCollections.observableArrayList();

    public SensorTableModel() {

    }
    
    public void addToStart(List<Double> list) {
        data.add(0, list);
    }
    
    public void add(List<Double> list) {
        data.add(list);
    }
    
    public List get(int index) {
        return data.get(index);
    }
    
    public ObservableList<List<Double>> get() {
        return data;
    }
    
    public void clear() {
        data.clear();
    }
    
    public void remove(int index) {
        data.remove(index);
    }

    @Override
    public void dataChanged(String graphname) {
        
    }

    @Override
    public void visibilityChanged(String graphname, boolean isVisible) {
    }

    @Override
    public void axisChanged() {
        
    }

}
