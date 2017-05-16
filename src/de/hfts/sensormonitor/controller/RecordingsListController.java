/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Polarix IT Solutions
 */
public class RecordingsListController implements Initializable {

    @FXML private ListView recordingsList;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    public void handleButtonDisplayRecording() {

    }

    public void handleButtonDeleteRecording() {

    }

    public void handleButtonExportRecording() {

    }
    
    public void setListItems(List<String> tables) {
        recordingsList.setItems(FXCollections.observableArrayList(tables));
    }

}
