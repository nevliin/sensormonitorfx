/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hft.ss17.cebarround.*;
import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.logging.*;
import javafx.scene.control.TextInputDialog;

/**
 * Recorder --- Handles recording the received SensorEvents if a recording is
 * running
 *
 * @author Polarix IT Solutions
 */
public class Recorder implements CeBarRoundObserver<SensorEvent> {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Current recording; null if none is running
     */
    private LiveRecording recording;

    boolean recordTemperature = true;
    boolean recordPressure = true;
    boolean recordRevolutions = true;

    /**
     * Inner class, represents one recording session
     */
    public class LiveRecording {

        // -------------- PRIVATE FIELDS ---------------------------------------
        private int rowid;
        /**
         * Generic name of the table in the database before finalizing it
         */
        private String genericName;

        // -------------- CONSTRUCTOR ------------------------------------------
        /**
         * Creates new LiveRecording
         *
         */
        public LiveRecording() {
            genericName = IOUtils.createGenericTable();
            this.rowid = 0;
            LogHandler.LOGGER.info(LogHandler.getLangpackString("recording_started"));
        }

        /**
         * Records received SensorEvent by transforming it into a String and
         * passing it to IOUtils.saveData()
         *
         * @param cbre
         */
        public void recordData(SensorEvent cbre) {
            String insertstmt = rowid + ", " + Long.toString(cbre.getUniqueSensorIdentifier()) + ", '" + cbre.getSensorTypeCode() + "'";
            if (recordTemperature) {
                insertstmt += ", " + Double.toString(cbre.getTemperature());
            } else {
                insertstmt += ", null";
            }
            if (recordPressure) {
                insertstmt += ", " + Double.toString(cbre.getPressure());
            } else {
                insertstmt += ", null";
            }
            if (recordRevolutions) {
                insertstmt += ", " + Integer.toString(cbre.getRevolutions());
            } else {
                insertstmt += ", null";
            }

            IOUtils.saveData(genericName, insertstmt, new Timestamp(cbre.getDate().getTime()));
            rowid++;
        }

        /**
         * Shows dialog for chosing a name for the recording; ensures the given
         * name matches the conventions.
         */
        public void saveRecording() {
            recording = null;
            if (rowid == 0) {
                IOUtils.dropTable(genericName);
            } else {
                boolean deleteRecording = false;
                boolean isNameInvalid = true;
                Optional<String> newname = null;
                boolean secondtry = false;
                do {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle(IOUtils.getLangpackString("save_recording_title"));
                    dialog.setContentText(IOUtils.getLangpackString("save_recording"));
                    if (secondtry) {
                        dialog.setHeaderText(IOUtils.getLangpackString("exception_illegaltablename"));
                    } else {
                        dialog.setHeaderText(IOUtils.getLangpackString("save_recording_title"));
                    }
                    newname = dialog.showAndWait();

                    if (!newname.isPresent()) {
                        isNameInvalid = false;
                        deleteRecording = true;
                    } else if (newname.get().length() != 0) {
                        try {
                            IOUtils.renameTable(genericName, newname.get());
                            LogHandler.LOGGER.info(LogHandler.getLangpackString("recording_finished") + ": " + newname.get());
                            isNameInvalid = false;
                        } catch (IllegalTableNameException ex) {
                            LogHandler.LOGGER.log(Level.WARNING, null, ex);
                            isNameInvalid = true;
                        }
                    }
                    secondtry = true;
                } while (isNameInvalid);
                if (deleteRecording) {
                    IOUtils.dropTable(genericName);
                }
            }
        }

        /**
         * Returns the generic name of the table related to the recording
         *
         * @return Generic name as String
         */
        public String getGenericName() {
            return genericName;
        }

        /**
         * Returns the current rowID in the table
         *
         * @return Current rowID
         */
        public int getRowid() {
            return rowid;
        }

    }

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * Standard constructor
     *
     */
    public Recorder() {
    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     *
     * @param cbre
     */
    @Override
    public void sensorDataEventListener(SensorEvent cbre) {
        if (recording != null) {
            recording.recordData(cbre);
        }
    }

    /**
     * Starts recording the received SensorEvents
     */
    public void startRecording() {
        recording = new LiveRecording();
    }

    /**
     * Stops recording the received SensorEvents
     */
    public void stopRecording() {
        recording.saveRecording();
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @return
     */
    public LiveRecording getRecording() {
        return recording;
    }

    /**
     *
     * @param recording
     */
    public void setRecording(LiveRecording recording) {
        this.recording = recording;
    }

    /**
     *
     * @return Boolean indicating if pressure should be recorded
     */
    public boolean isRecordTemperature() {
        return recordTemperature;
    }

    /**
     * Set the boolean indicating if temperature should be recorded
     *
     * @param recordTemperature Boolean indicating if temperature should be
     * recorded
     */
    public void setRecordTemperature(boolean recordTemperature) {
        this.recordTemperature = recordTemperature;
    }

    /**
     * Get the boolean indicating if pressure should be recorded
     *
     * @return Boolean indicating if pressure should be recorded
     */
    public boolean isRecordPressure() {
        return recordPressure;
    }

    /**
     * Set the boolean indicating if pressure should be recorded
     *
     * @param recordPressure Boolean indicating if pressure should be recorded
     */
    public void setRecordPressure(boolean recordPressure) {
        this.recordPressure = recordPressure;
    }

    /**
     * Get the boolean indicating if revolutions should be recorded
     *
     * @return Boolean indicating if revolutions should be recorded
     */
    public boolean isRecordRevolutions() {
        return recordRevolutions;
    }

    /**
     * Set the boolean indicating if revolutions should be recorded
     *
     * @param recordRevolutions Boolean indicating if revolutions should be
     * recorded
     */
    public void setRecordRevolutions(boolean recordRevolutions) {
        this.recordRevolutions = recordRevolutions;
    }

}
