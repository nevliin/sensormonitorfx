/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.ExceptionDialog;
import de.hfts.sensormonitor.misc.IOUtils;
import de.hfts.sensormonitor.misc.LogHandler;
import de.hfts.sensormonitor.misc.ProgressDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
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
    /**
     * ListView displaying a list of all available recordings
     */
    @FXML
    private ListView recordingsList;

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Controller of the main window
     */
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
        ProgressDialog pd = new ProgressDialog(selectedrecordings.size(), IOUtils.getLangpackString("loading_recordings"), IOUtils.getLangpackString("progress_bar"));
        pd.getScene().getStylesheets().addAll(recordingsList.getStylesheets());
        Thread t = new Thread(() -> {
            for (String recording : selectedrecordings) {
                parentController.displayRecording(recording);
                pd.progress();
            }
            pd.hide();
            LogHandler.LOGGER.info(LogHandler.getLangpackString("recordings_displayed") + ": " + String.join(", ", selectedrecordings));
            Platform.runLater(() -> {
                parentController.recordingsListWindow.hide();
                parentController.recordingsListWindow = null;
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
            IOUtils.dropTable(recording);
        }
        LogHandler.LOGGER.info(LogHandler.getLangpackString("recordings_deleted") + ": " + String.join(", ", selectedrecordings));
        parentController.closeTab(selectedrecordings);
        recordingsList.setItems(FXCollections.observableArrayList(IOUtils.getTables()));
    }

    /**
     * Handle clicking the button "Export recording"
     */
    public void handleButtonExportRecording() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(IOUtils.getLangpack().getString("select_export_directory"));
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir = directoryChooser.showDialog(null);

        if (dir != null) {
            List<String> selectedrecordings = recordingsList.getSelectionModel().getSelectedItems();
            ProgressDialog pd = new ProgressDialog(selectedrecordings.size(), IOUtils.getLangpackString("exporting_recordings"), IOUtils.getLangpackString("progress_bar"));
            pd.getScene().getStylesheets().addAll(recordingsList.getStylesheets());
            Thread t = new Thread(() -> {
                for (String recording : selectedrecordings) {
                    try {
                        IOUtils.exportRecording(recording, dir.getAbsolutePath());
                        Platform.runLater(() -> {
                            pd.progress();
                        });
                    } catch (IOException | SQLException ex) {
                        LogHandler.LOGGER.log(Level.SEVERE, null, ex);
                        pd.hide();
                        new ExceptionDialog(IOUtils.getLangpackString("error_exportrecording") + ": " + recording, null);
                    }
                }
                pd.hide();
                LogHandler.LOGGER.info(LogHandler.getLangpackString("recordings_exported") + ": " + String.join(", ", selectedrecordings));
            });
            t.start();
        }
    }

    // -------------- GETTERS & SETTERS
    /**
     *
     * @param tables List of database tables/recordings
     */
    public void setListItems(List<String> tables) {
        recordingsList.setItems(FXCollections.observableArrayList(tables));
    }

    /**
     *
     * @param mc Controller of the main window
     */
    public void setParentController(MainController mc) {
        this.parentController = mc;
    }

}
