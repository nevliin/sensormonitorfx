package de.hfts.sensormonitor.exceptions;

/**
 * IllegalXScaleException --- Exception thrown when the values of the X-axis are illegal, either because
 * the lower bound is higher than the upper bound, the bounds are the same or
 * the bounds exceed the maximum and/or minimum scale values
 *
 * @author Polarix IT Solutions
 */
public class IllegalXScaleException extends SensorMonitorException {

    /**
     *
     */
    public IllegalXScaleException() {
        super("exception_illegalxscale");
    }

}
