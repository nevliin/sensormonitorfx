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
            genericName = IO.createGenericTable();
            this.rowid = 0;
        }

        /**
         * Renames the table related to the recording
         *
         * @param name
         * @throws de.hfts.sensormonitor.exceptions.IllegalTableNameException
         */
        public void finalizeName(String name) throws IllegalTableNameException {
            IO.renameTable(genericName, name);
        }

        /**
         * Records received SensorEvent by transforming it into a String and
         * passing it to the instance of IO
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

            IO.saveData(genericName, insertstmt, new Timestamp(cbre.getDate().getTime()));
            rowid++;
        }

        /**
         * Shows dialog for chosing a name for the recording; ensures the given
         * name matches the conventions.
         */
        public void saveRecording() {
            if (recording.getRowid() == 0) {
                IO.dropTable(recording.getGenericName());
            } else {
                boolean deleteRecording = false;
                boolean isNameInvalid = true;
                Optional<String> newname = null;
                boolean secondtry = false;
                do {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle(IO.getLangpackString("save_recording_title"));
                    dialog.setContentText(IO.getLangpackString("save_recording"));
                    if (secondtry) {
                        dialog.setHeaderText(IO.getLangpackString("exception_illegaltablename"));
                    } else {
                        dialog.setHeaderText(IO.getLangpackString("save_recording_title"));
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
                            Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    secondtry = true;
                } while (isNameInvalid);
                if (deleteRecording) {
                    IO.dropTable(recording.getGenericName());
                }
            }
            recording = null;
        }

        /**
         * Returns the generic name of the table related to the recording
         * @return
         */
        public String getGenericName() {
            return genericName;
        }

        /**
         * Returns the current rowID in the table
         * @return
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
        recording = null;
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
     * @return
     */
    public boolean isRecordTemperature() {
        return recordTemperature;
    }

    /**
     *
     * @param recordTemperature
     */
    public void setRecordTemperature(boolean recordTemperature) {
        this.recordTemperature = recordTemperature;
    }

    /**
     *
     * @return
     */
    public boolean isRecordPressure() {
        return recordPressure;
    }

    /**
     *
     * @param recordPressure
     */
    public void setRecordPressure(boolean recordPressure) {
        this.recordPressure = recordPressure;
    }

    /**
     *
     * @return
     */
    public boolean isRecordRevolutions() {
        return recordRevolutions;
    }

    /**
     *
     * @param recordRevolutions
     */
    public void setRecordRevolutions(boolean recordRevolutions) {
        this.recordRevolutions = recordRevolutions;
    }

}
