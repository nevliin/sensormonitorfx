/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import java.io.File;
import java.net.URL;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author Polarix IT Solutions
 */
public class RecordingsListController implements Initializable {
    
    @FXML
    private ListView recordingsList;
    private MainController parentController;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recordingsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    /**
     *
     */
    public void handleButtonDisplayRecording() {
        List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
        for (String recording : selectedrecordings) {
            parentController.displayRecording(recording);            
        }        
    }
    
    /**
     *
     */
    public void handleButtonDeleteRecording() {
        List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
        for (String recording : selectedrecordings) {
            parentController.getIo().dropTable(recording);            
        }
        parentController.closeTab(selectedrecordings);
        recordingsList.setItems(FXCollections.observableArrayList(parentController.getIo().getTables()));
    }
    
    /**
     *
     */
    public void handleButtonExportRecording() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(parentController.getIo().getLangpack().getString("select_export_directory"));
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File dir = directoryChooser.showDialog(null);
        
        if (dir != null) {
            List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
            for (String recording : selectedrecordings) {
                parentController.getIo().exportRecording(recording, dir.getAbsolutePath());
            }
        }
    }
    
    /**
     *
     * @param tables
     */
    public void setListItems(List<String> tables) {
        recordingsList.setItems(FXCollections.observableArrayList(tables));
    }
    
    /**
     *
     * @param mc
     */
    public void setParentController(MainController mc) {
        this.parentController = mc;
    }
    
}
