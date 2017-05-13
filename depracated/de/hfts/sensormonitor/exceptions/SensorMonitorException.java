package de.hfts.sensormonitor.exceptions;

import java.util.ResourceBundle;
import javafx.scene.control.Alert;

/**
 * Parent-exception for all exceptions thrown by the application
 * @author Polarix IT Solutions
 */
public class SensorMonitorException extends Exception {

    public static ResourceBundle langpack;

    public SensorMonitorException(String exceptionMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText(langpack.getString(exceptionMessage));
        alert.setContentText(null);
        alert.showAndWait();
    }
    
    public SensorMonitorException() {
        this("exception_sensormonitor");
    }

}
