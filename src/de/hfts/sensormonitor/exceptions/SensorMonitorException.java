package de.hfts.sensormonitor.exceptions;

import de.hfts.sensormonitor.misc.LogHandler;
import java.util.ResourceBundle;

/**
 * SensorMonitorException --- Parent-exception for all exceptions thrown by the
 * application
 *
 * @author Polarix IT Solutions
 */
public class SensorMonitorException extends RuntimeException {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * Key of the corresponding exception message; for internationalization
     * purposes
     */
    private String exceptionKey;

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * Creates an exception with the corresponding text from the langpack
     * 
     * @param exceptionKey
     */
    public SensorMonitorException(String exceptionKey) {
        super(LogHandler.getLangpackString(exceptionKey));
        this.exceptionKey = exceptionKey;
    }

    /**
     * Creates a SensorMonitorException
     */
    public SensorMonitorException() {
        this("exception_sensormonitor");
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     * 
     * @return 
     */
    public String getExceptionKey() {
        return exceptionKey;
    }

}
