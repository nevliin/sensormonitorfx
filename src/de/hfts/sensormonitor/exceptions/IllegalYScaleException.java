package de.hfts.sensormonitor.exceptions;

/**
 * IllegalYScaleException --- Exception thrown when the values of the Y-axis are illegal, either because
 * the lower bound is higher than the upper bound, the bounds are the same or
 * the bounds exceed the maximum and/or minimum scale values
 *
 * @author Polarix IT Solutions
 */
public class IllegalYScaleException extends SensorMonitorException {

    /**
     *
     */
    public IllegalYScaleException() {
        super("exception_illegalyscale");
    }

}
