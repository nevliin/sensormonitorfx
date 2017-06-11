/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.exceptions;

/**
 * ImportRecordingException --- Exception thrown when the found
 * config.properties does not match the required configuration
 *
 * @author Polarix IT Solutions
 */
public class IllegalConfigurationException extends SensorMonitorException {

    /**
     *
     */
    public IllegalConfigurationException() {
        super("exception_illegalconfiguration");
    }

}
