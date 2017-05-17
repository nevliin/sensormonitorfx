package de.hfts.sensormonitor.exceptions;

import java.util.ResourceBundle;
import javafx.scene.control.Alert;

/**
 * Parent-exception for all exceptions thrown by the application
 * @author Polarix IT Solutions
 */
public class SensorMonitorException extends Exception {

    public SensorMonitorException(String exceptionMessage) {
    }
    
    public SensorMonitorException() {
        this("exception_sensormonitor");
    }

}