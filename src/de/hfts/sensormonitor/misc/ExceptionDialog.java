/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import javafx.scene.control.Alert;

/**
 * ExceptionDialog --- Used to display errors and exceptions to the user
 *
 * @author Polarix IT Solutions
 */
public class ExceptionDialog {

    /**
     * Creates a JavaFX error alert with the given parameters and displays it
     * @param text
     * @param subtext
     */
    public ExceptionDialog(String text, String subtext) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText(text);
        alert.setContentText(subtext);
        alert.showAndWait();
    }

}
