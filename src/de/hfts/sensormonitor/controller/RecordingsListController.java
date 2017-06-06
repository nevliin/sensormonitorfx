/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.ExceptionDialog;
import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.misc.ProgressDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

/**
 * RecordingListController --- FXML Controller of recordingListWindow, allows
 * setting the ListView items and handles all Button actions
 *
 * @author Polarix IT Solutions
 */
public class RecordingsListController implements Initializable {

    // -------------- FXML FIELDS ----------------------------------------------
    @FXML
    private ListView recordingsList;

    // -------------- PRIVATE FIELDS -------------------------------------------
    private MainController parentController;

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recordingsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // -------------- FXML HANDLERS --------------------------------------------
    /**
     * Handle clicking the button "Display recording"
     */
    public void handleButtonDisplayRecording() {
        List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
        Thread t = new Thread(() -> {
            Platform.runLater(() -> {
                ProgressDialog pd = new ProgressDialog(selectedrecordings.size(), IO.getLangpackString("loading_recordings"), IO.getLangpackString("progress_bar"));
                pd.getScene().getStylesheets().addAll(recordingsList.getStylesheets());
                for (String recording : selectedrecordings) {
                    parentController.displayRecording(recording);
                    pd.progress();
                }
                pd.hide();
            });
        });
        t.start();
    }

    /**
     * Handle clicking the button "Delete recording"
     */
    public void handleButtonDeleteRecording() {
        List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
        for (String recording : selectedrecordings) {
            IO.dropTable(recording);
        }
        parentController.closeTab(selectedrecordings);
        recordingsList.setItems(FXCollections.observableArrayList(IO.getTables()));
    }

    /**
     * Handle clicking the button "Export recording"
     */
    public void handleButtonExportRecording() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(IO.getLangpack().getString("select_export_directory"));
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir = directoryChooser.showDialog(null);

        if (dir != null) {
            Thread t = new Thread(() -> {
                Platform.runLater(() -> {
                    List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
                    ProgressDialog pd = new ProgressDialog(selectedrecordings.size(), IO.getLangpackString("exporting_recordings"), IO.getLangpackString("progress_bar"));
                    pd.getScene().getStylesheets().addAll(recordingsList.getStylesheets());
                    for (String recording : selectedrecordings) {
                        try {
                            IO.exportRecording(recording, dir.getAbsolutePath());
                            pd.progress();
                        } catch (IOException | SQLException ex) {
                            new ExceptionDialog(IO.getLangpackString("error_exportrecording") + ": " + recording, null);
                            pd.hide();
                        }
                    }
                    pd.hide();
                });
            });
            t.start();
        }
    }

    // -------------- GETTERS & SETTERS
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
