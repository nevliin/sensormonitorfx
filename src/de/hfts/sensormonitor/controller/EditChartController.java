/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.model.ChartData;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Polarix IT Solutions
 */
public class EditChartController implements Initializable {

    ChartData chartData;
    @FXML private TextField textFieldXMin;    
    @FXML private TextField textFieldXMax;    
    @FXML private TextField textFieldYMin;    
    @FXML private TextField textFieldYMax;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // NO-OP
    }    

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
        textFieldXMin.setText(Double.toString(chartData.getxMin()));
        textFieldXMax.setText(Double.toString(chartData.getxMax()));
        textFieldYMin.setText(Double.toString(chartData.getyMin()));
        textFieldYMax.setText(Double.toString(chartData.getyMax()));
    }
    
    
    
}
