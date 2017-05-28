/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.IO;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Polarix IT Solutions
 */
public class SettingsController implements Initializable {

    @FXML
    private ComboBox comboBoxLanguages;
    @FXML
    private TextField textFieldTimeFrame;
    @FXML
    private CheckBox checkBoxDisplayPoints;
    @FXML
    private ComboBox comboBoxAppearance;
    @FXML
    private Label labelErrorGraphs;
    @FXML
    private Label labelErrorLanguage;
    @FXML
    private Label labelErrorAppearance;
    private IO io;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void handleSaveButtonLanguage() {

    }

    public void handleSaveButtonGraphs() {

    }

    public void handleSaveButtonAppearance() {

    }

    public void setIO(IO io) {
        this.io = io;
        comboBoxLanguages.setItems(FXCollections.observableArrayList(io.getLanguages()));
        comboBoxLanguages.setValue(new Locale(io.getConfigProp("lang")).getDisplayLanguage(new Locale(io.getConfigProp("lang"))));
        textFieldTimeFrame.setText(io.getConfigProp("realtime_timeframe"));
        checkBoxDisplayPoints.setSelected(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        io.loadAvailableSkins();
        comboBoxAppearance.setItems(FXCollections.observableArrayList(io.getStyles()));
        comboBoxAppearance.setValue(io.getConfigProp("style"));        
    }
}
