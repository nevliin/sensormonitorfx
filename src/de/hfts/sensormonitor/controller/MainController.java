package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.main.SensorMonitor;
import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.sensor.DataSensor;
import de.hfts.sensormonitor.sensor.SensorData;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainController implements Initializable {

    @FXML
    private Label labelInfo;
    @FXML
    private Button buttonStartRecording;
    @FXML
    private Button buttonStopRecording;
    private IO io;
    private Stage recordingswindow;
    private Stage settingswindow;

    LiveRecording recording;

    boolean recordTemperature = true;
    boolean recordPressure = true;
    boolean recordRevolutions = true;

    public class LiveRecording {

        private int rowid;
        private String genericName;
        private IO io;

        /**
         *
         * @param sensor
         * @param io
         */
        public LiveRecording(DataSensor sensor, IO io) {
            genericName = io.createGenericTable();
            this.io = io;
            this.rowid = 0;
        }

        /**
         *
         * @param name
         * @throws de.hfts.sensormonitor.exceptions.IllegalTableNameException
         */
        public void finalizeName(String name) throws IllegalTableNameException {
            io.renameTable(genericName, name);
        }

        /**
         *
         * @param data
         * @param sensor
         */
        public void recordData(SensorData data, DataSensor sensor) {
            String insertstmt = rowid + ", " + Integer.toString((int) sensor.getUniquesensoridentifier()) + ", " + Integer.toString((int) sensor.getSensortypecode());
            if (recordTemperature) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Temperature"));
            } else {
                insertstmt += ", null";
            }
            if (recordPressure) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Pressure"));
            } else {
                insertstmt += ", null";
            }
            if (recordRevolutions) {
                insertstmt += ", " + Integer.toString((int) (double) sensor.getData().getData().get("Revolutions"));
            } else {
                insertstmt += ", null";
            }

            io.saveData(genericName, insertstmt, data.getTime());
            rowid++;
        }

        public void saveRecording() {
            if (recording.getRowid() == 0) {
                io.dropTable(recording.getGenericName());
            } else {
                boolean deleteRecording = false;
                boolean isNameInvalid = true;
                Optional<String> newname = null;
                boolean secondtry = false;
                do {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle(io.getLangpack().getString("save_recording_title"));
                    dialog.setContentText(io.getLangpack().getString("save_recording"));
                    if (secondtry) {
                        dialog.setHeaderText(io.getLangpack().getString("exception_tablenameexception"));
                    } else {
                        dialog.setHeaderText(io.getLangpack().getString("save_recording_title"));
                    }
                    newname = dialog.showAndWait();

                    if (!newname.isPresent()) {
                        isNameInvalid = false;
                        deleteRecording = true;
                    } else if (newname.get().length() != 0) {
                        try {
                            recording.finalizeName(newname.get());
                            isNameInvalid = false;
                        } catch (IllegalTableNameException ex) {
                            Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    secondtry = true;
                } while (isNameInvalid);
                if (deleteRecording) {
                    io.dropTable(recording.getGenericName());
                }
            }
            recording = null;
        }

        /**
         *
         * @return
         */
        public String getGenericName() {
            return genericName;
        }

        /**
         *
         * @return
         */
        public int getRowid() {
            return rowid;
        }

    }

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

    public void handleButtonStartRecording() {
        recording = new LiveRecording(new DataSensor(), io);
        buttonStopRecording.setDisable(false);
        buttonStartRecording.setDisable(true);
    }

    public void handleButtonStopRecording() {
        recording.saveRecording();
        buttonStopRecording.setDisable(true);
        buttonStartRecording.setDisable(false);
    }
    
    public void handleCheckBoxTemperature() {
        
    }
    
    public void handleCheckBoxPressure() {
        
    }
    
    public void handleCheckBoxRevolutions() {
        
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
