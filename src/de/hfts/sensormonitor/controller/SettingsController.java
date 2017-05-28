/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.IO;
import de.hfts.sensormonitor.misc.SensorChart;
import de.hfts.sensormonitor.model.ChartData;
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
    private MainController mainController;
    private boolean isLanguageRebootNecessary = false;
    private boolean isAppearanceRebootNecessary = false;

    private String currentLanguage;
    private String currentAppearance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void handleSaveButtonLanguage() {
        if (((String) comboBoxLanguages.getValue()).equals(currentLanguage)) {
            labelErrorLanguage.setVisible(false);
            isLanguageRebootNecessary = false;
            io.setConfigProp("lang", io.getLanguages().get(((String) comboBoxLanguages.getValue())));
            io.saveConfigProperties();
        } else {
            labelErrorLanguage.setText(io.getLangpackString("applied_upon_closing"));
            labelErrorLanguage.setVisible(true);
            isLanguageRebootNecessary = true;
            io.setConfigProp("lang", io.getLanguages().get(((String) comboBoxLanguages.getValue())));
            io.saveConfigProperties();
        }
    }

    public void handleApplyButtonGraphs() {
        if (!textFieldTimeFrame.getText().equals(io.getConfigProp("realtime_timeframe"))) {
            for (ChartData cd : mainController.getChartDatas()) {
                cd.setxMin(0 - Integer.valueOf(textFieldTimeFrame.getText()));
            }
            io.setConfigProp("realtime_timeframe", textFieldTimeFrame.getText());
            io.saveConfigProperties();
        }
        if (checkBoxDisplayPoints.isSelected() != Boolean.valueOf(io.getConfigProp("displayPointSymbols"))) {
            for (SensorChart sc : mainController.getSensorCharts()) {
                sc.setCreateSymbols(checkBoxDisplayPoints.isSelected());
            }
            io.setConfigProp("displayPointSymbols", Boolean.toString(checkBoxDisplayPoints.isSelected()));
            io.saveConfigProperties();
        }
    }

    public void handleSaveButtonAppearance() {
        if (((String) comboBoxAppearance.getValue()).equals(currentAppearance)) {
            labelErrorAppearance.setVisible(false);
            isAppearanceRebootNecessary = false;
            io.setConfigProp("style", ((String) comboBoxAppearance.getValue()));
            io.saveConfigProperties();
        } else {
            labelErrorAppearance.setText(io.getLangpackString("applied_upon_closing"));
            labelErrorAppearance.setVisible(true);
            isAppearanceRebootNecessary = true;
            io.setConfigProp("style", ((String) comboBoxAppearance.getValue()));
            io.saveConfigProperties();
        }
    }

    public void setIO(IO io) {
        this.io = io;
        comboBoxLanguages.setItems(FXCollections.observableArrayList(io.getLanguages().keySet()));
        currentLanguage = new Locale(io.getConfigProp("lang")).getDisplayLanguage(new Locale(io.getConfigProp("lang")));
        comboBoxLanguages.setValue(currentLanguage);
        textFieldTimeFrame.setText(io.getConfigProp("realtime_timeframe"));
        checkBoxDisplayPoints.setSelected(Boolean.valueOf(io.getConfigProp("displayPointSymbols")));
        io.loadAvailableSkins();
        comboBoxAppearance.setItems(FXCollections.observableArrayList(io.getStyles()));
        currentAppearance = io.getConfigProp("style");
        comboBoxAppearance.setValue(currentAppearance);
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void onClose() {
        if (isAppearanceRebootNecessary || isLanguageRebootNecessary) {
            mainController.rebootProgramm();
        }
    }
}
