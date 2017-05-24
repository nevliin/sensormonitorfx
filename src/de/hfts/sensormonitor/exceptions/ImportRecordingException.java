/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.exceptions;

/**
 * ImportRecordingException --- Exception thrown when an imported CSV-file doesn't match the requirements for
 * exported recordings, such as the amount of columns.
 *
 * @author Polarix IT Solutions
 */
public class ImportRecordingException extends SensorMonitorException {

    /**
     *
     */
    public ImportRecordingException() {
        super("exception_importrecording");
    }

}
