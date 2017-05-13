package de.hfts.sensormonitor.exceptions;

/**
 * Exception thrown when more than 10 sensors are connected to the application
 * @author Polarix IT Solutions
 */
public class IllegalSensorAmountException extends SensorMonitorException {
    
    public IllegalSensorAmountException() {
        super("exception_illegalsensoramount");
    }
    
}
