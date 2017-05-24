package de.hfts.sensormonitor.exceptions;

/**
 * IllegalTableNameException --- Exception thrown when a table name is illegal, either because it doesn't
 * match the naming conventions for SQL tables or because the table already
 * exists
 *
 * @author Polarix IT Solutions
 */
public class IllegalTableNameException extends SensorMonitorException {

    /**
     *
     */
    public IllegalTableNameException() {
        super("exception_illegaltablename");
    }

}
