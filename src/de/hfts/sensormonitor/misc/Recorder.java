/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hft.ss17.cebarround.CeBarRoundObserver;
import de.hft.ss17.cebarround.SensorEvent;
import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.main.SensorMonitor;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextInputDialog;

/**
 *
 * @author Polarix IT Solutions
 */
public class Recorder implements CeBarRoundObserver<SensorEvent>{
    
    IO io;
    LiveRecording recording;

    boolean recordTemperature = true;
    boolean recordPressure = true;
    boolean recordRevolutions = true;

    @Override
    public void sensorDataEventListener(SensorEvent cbre) {
        if(recording != null) {
            recording.recordData(cbre);
        }
    }
    
    public class LiveRecording {

        private int rowid;
        private String genericName;
        private IO io;
        
        

        /**
         *
         * @param sensor
         * @param io
         */
        public LiveRecording(IO io) {
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
        public void recordData(SensorEvent cbre) {
            String insertstmt = rowid + ", " + Integer.toString((int) cbre.getUniqueSensorIdentifier()) + ", '" + cbre.getSensorTypeCode() + "'";
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

            io.saveData(genericName, insertstmt, new Timestamp(cbre.getDate().getTime()));
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
    
    public Recorder(IO io) {
        this.io = io;
    }
    
    public void startRecording() {
        recording = new LiveRecording(io);
    }
    
    public void stopRecording() {
        recording.saveRecording();
    }

    public LiveRecording getRecording() {
        return recording;
    }

    public void setRecording(LiveRecording recording) {
        this.recording = recording;
    }

    public boolean isRecordTemperature() {
        return recordTemperature;
    }

    public void setRecordTemperature(boolean recordTemperature) {
        this.recordTemperature = recordTemperature;
    }

    public boolean isRecordPressure() {
        return recordPressure;
    }

    public void setRecordPressure(boolean recordPressure) {
        this.recordPressure = recordPressure;
    }

    public boolean isRecordRevolutions() {
        return recordRevolutions;
    }

    public void setRecordRevolutions(boolean recordRevolutions) {
        this.recordRevolutions = recordRevolutions;
    }
    
    
    
}
