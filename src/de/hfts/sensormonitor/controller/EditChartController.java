/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.exceptions.*;
import de.hfts.sensormonitor.misc.*;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.viewelements.SensorChart;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

/**
 * EditChartController --- FXML Controller of editChartWindow.fxml, the window
 * for editing the X- and Y-axis bounds of a SensorChart
 *
 * @author Polarix IT Solutions
 */
public class EditChartController implements Initializable {

    // -------------- PACKAGE PRIVATE FIELDS -----------------------------------
    /**
     * ChartData of the related SensorChart
     */
    ChartData chartData;
    /**
     * SensorChart related to the editChartWindow
     */
    private SensorChart parentChart;

    // -------------- FXML FIELDS ----------------------------------------------
    /**
     * Title of the window, naming the related SensorChart
     */
    @FXML
    private Label labelTitle;
    /**
     * Editable TextField showing the current lower X-Axis bound
     */
    @FXML
    private TextField textFieldXMin;
    /**
     * Editable TextField showing the current upper X-Axis bound
     */
    @FXML
    private TextField textFieldXMax;
    /**
     * Editable TextField showing the current lower Y-Axis bound
     */
    @FXML
    private TextField textFieldYMin;
    /**
     * Editable TextField showing the current upper Y-Axis bound
     */
    @FXML
    private TextField textFieldYMax;
    /**
     * CheckBox for enabling/disabling autoranging on the Y-Axis
     */
    @FXML
    private CheckBox checkBoxAutorange;

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // NO-OP
    }

    /**
     * Sets the ChartData to the Controller and assigns the values of it to the
     * according TextField's
     *
     * @param chartData
     */
    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
        this.labelTitle.setText(labelTitle.getText() + ": " + IOUtils.getLangpackString(chartData.getType().toString().toLowerCase()));
        String xMin = Double.toString(chartData.getxMin());
        String xMax = Double.toString(chartData.getxMax());
        
        String yMin = Double.toString(chartData.getyMin());
        if (chartData.getyMin() == Double.MAX_VALUE) {
            yMin = "AUTO";
            textFieldYMin.setDisable(true);
            checkBoxAutorange.setSelected(true);
        }
        String yMax = Double.toString(chartData.getyMax());
        if (chartData.getyMax() == Double.MAX_VALUE) {
            yMax = "AUTO";
            textFieldYMax.setDisable(true);
        }
        
        textFieldXMin.setText(xMin);
        textFieldXMax.setText(xMax);
        textFieldYMin.setText(yMin);
        textFieldYMax.setText(yMax);
    }

    /**
     * Verifies if the new bounds are valid and assigns them to the ChartData if
     * they are. Throws the according exception if any of the bounds are not
     * valid.
     *
     * @param xmin Lower bound of the X-axis
     * @param xmax Upper bound of the X-axis
     * @param ymin Lower bound of the Y-axis
     * @param ymax Upper bound of the Y-axis
     * @throws IllegalXScaleException
     * @throws IllegalYScaleException
     */
    void updateBounds(String xmin, String xmax, String ymin, String ymax) throws IllegalXScaleException, IllegalYScaleException {
        boolean isXScaleValid = true;
        double valueXMin = 0;
        double valueXMax = 0;
        try {
            valueXMin = Double.valueOf(xmin);
            valueXMax = Double.valueOf(xmax);
            if (valueXMin < chartData.getxScaleMin() || valueXMax > chartData.getxScaleMax() || valueXMax <= valueXMin) {
                isXScaleValid = false;
            }
        } catch (NumberFormatException e) {
            isXScaleValid = false;
        }
        if (isXScaleValid) {
            chartData.setxMin(valueXMin);
            chartData.setxMax(valueXMax);
            chartData.notifyListenersOfAxisChange();
        } else {
            throw new IllegalXScaleException();
        }

        boolean isYScaleValid = true;
        double valueYMin = 0;
        double valueYMax = 0;
        if (ymin.equalsIgnoreCase("AUTO") || ymax.equalsIgnoreCase("AUTO")) {
            valueYMin = Double.MAX_VALUE;
            valueYMax = Double.MAX_VALUE;
        } else {
            try {
                valueYMin = Double.valueOf(ymin);
                valueYMax = Double.valueOf(ymax);
                if (valueYMin < chartData.getyScaleMin() || valueYMax > chartData.getyScaleMax() || valueYMax <= valueYMin) {
                    isYScaleValid = false;
                }
            } catch (NumberFormatException e) {
                isYScaleValid = false;
            }
        }
        if (isYScaleValid) {
            chartData.setyMin(valueYMin);
            chartData.setyMax(valueYMax);
            chartData.notifyListenersOfAxisChange();
        } else {
            throw new IllegalYScaleException();
        }

        if (isXScaleValid && isYScaleValid) {
            textFieldXMax.getScene().getWindow().hide();
        }
    }

    // -------------- FXML HANDLERS --------------------------------------------
    /**
     * Handles the "Cancel" button by hiding the window
     */
    public void handleCancelButton() {
        textFieldXMax.getScene().getWindow().hide();
        parentChart.setEditChartWindow(null);
    }

    /**
     * Handles the "Save" button by updating the bounds with the given values
     * and handling possibly thrown exceptions.
     */
    public void handleSaveButton() {
        try {
            String ymin = textFieldYMin.getText();
            String ymax = textFieldYMax.getText();
            if (checkBoxAutorange.isSelected()) {
                ymin = "AUTO";
                ymax = "AUTO";
            }
            updateBounds(textFieldXMin.getText(), textFieldXMax.getText(), ymin, ymax);
            this.labelTitle.getScene().getWindow().hide();
            parentChart.setEditChartWindow(null);
        } catch (IllegalXScaleException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            new ExceptionDialog(IOUtils.getLangpackString("exception_illegalxscale"), null);
        } catch (IllegalYScaleException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
            new ExceptionDialog(IOUtils.getLangpackString("exception_illegalyscale"), null);
        }
    }

    /**
     * Handles actions on the Autoranging checkbox by enabling/disabling the
     * textfields for manually setting it
     */
    public void handleCheckBoxAutoRanging() {
        if (checkBoxAutorange.isSelected()) {
            textFieldYMin.setDisable(true);
            textFieldYMax.setDisable(true);
        } else {
            textFieldYMin.setDisable(false);
            textFieldYMax.setDisable(false);
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @param sc
     */
    public void setParentChart(SensorChart sc) {
        this.parentChart = sc;
    }
}
