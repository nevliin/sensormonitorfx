/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.viewelements;

import de.hfts.sensormonitor.model.TableDataChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * SensorTable --- Class inherited from JavaFX' TableView. Displays the data
 * saved in a TableData and reacts to changes of the data.
 * 
 * @author Polarix IT Solutions
 */
public class SensorTable extends TableView implements TableDataChangeListener {

    @Override
    public void dataChanged() {
        ((TableColumn) this.getColumns().get(0)).setVisible(false);
        ((TableColumn) this.getColumns().get(0)).setVisible(true);
    }

}
