package de.hfts.sensormonitor.exceptions;

/**
 * Exception thrown when a connection to the database can not be established
 * @author Polarix IT Solutions
 */
public class DatabaseConnectException extends SensorMonitorException {
    
    public DatabaseConnectException() {
        super("exception_databaseconnect");
    }
    
}
