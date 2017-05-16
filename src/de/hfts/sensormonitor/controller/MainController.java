package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.IO;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainController implements Initializable {

    @FXML private Label labelInfo;
    private IO io;
    private Stage recordingswindow;
    private Stage settingswindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void manageMenuItemQuit() {
    }

    public void manageMenuItemReboot() {

    }

    public void manageMenuItemShowAll() {
        if (recordingswindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/fxml/recordingslist.fxml"), io.getLangpack());
                BorderPane root = (BorderPane) loader.load();
                ((RecordingsListController) loader.getController()).setListItems(io.getTables());
                recordingswindow = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(labelInfo.getScene().getStylesheets());
                recordingswindow.setScene(scene);
                recordingswindow.sizeToScene();
                recordingswindow.show();
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            recordingswindow.toFront();
        }
    }

    public void manageMenuItemDeleteAll() {

    }

    public void manageMenuItemSettings() {

    }

    public void setIo(IO io) {
        this.io = io;
    }
    
    

    /**
     * Shuts down the program safely by deleting a possibly running recording
     * and closing the database connection.
     */
    public void quitProgramm() {
        //isRecording = false;
        //if (recording != null) {
        //    saveRecording();
        //}
        //io.closeConnection();
        System.exit(0);
    }

}
