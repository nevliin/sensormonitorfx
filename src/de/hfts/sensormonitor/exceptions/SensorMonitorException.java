package de.hfts.sensormonitor.exceptions;

import java.util.ResourceBundle;

/**
 * SensorMonitorException --- Parent-exception for all exceptions thrown by the
 * application
 *
 * @author Polarix IT Solutions
 */
public class SensorMonitorException extends RuntimeException {

    /**
     * Needs to be set before throwing any exceptions; provides messages for all
     * exceptions
     */
    public static ResourceBundle langpack;

    /**
     *
     * @param exceptionMessage
     */
    public SensorMonitorException(String exceptionMessage) {
        super(langpack.getString(exceptionMessage));
    }

    /**
     *
     */
    public SensorMonitorException() {
        this("exception_sensormonitor");
    }

}
