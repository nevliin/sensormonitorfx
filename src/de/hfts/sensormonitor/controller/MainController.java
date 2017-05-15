package de.hfts.sensormonitor.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class MainController implements Initializable {

    @FXML private MenuBarController menubar;
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //menubar.setIo(io);
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
