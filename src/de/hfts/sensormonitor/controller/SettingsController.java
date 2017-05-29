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
import javafx.scene.control.*;

/**
 * SettingsController --- FXML Controller for settingsWindow.fxml, állows
 * changing basic settings for language, graphs and appearance;
 *
 * @author Polarix IT Solutions
 */
public class SettingsController implements Initializable {

    // -------------- FXML FIELDS ----------------------------------------------
    /**
     * ComboBox listing all available languages
     */
    @FXML
    private ComboBox comboBoxLanguages;
    /**
     * TextField displaying the default timeframe for realtime SensorChart's
     */
    @FXML
    private TextField textFieldTimeFrame;
    /**
     * CheckBox showing if points on the realtime SensorChart's are displayed or not
     */
    @FXML
    private CheckBox checkBoxDisplayPoints;
    /**
     * ComboBox listing all available styles
     */
    @FXML
    private ComboBox comboBoxAppearance;
    /**
     * Label for displaying errors and notices upon applying the settings for graphs
     */
    @FXML
    private Label labelErrorGraphs;
    /**
     * Label for displaying errors and notices upon saving the settings for language
     */
    @FXML
    private Label labelErrorLanguage;
    /**
     * Label for displaying errors and notices upon saving th3 settings for appearance
     */
    @FXML
    private Label labelErrorAppearance;

    // -------------- PRIVATE FIELDS -------------------------------------------
    private IO io;
    private MainController mainController;
    private boolean isLanguageRebootNecessary = false;
    private boolean isAppearanceRebootNecessary = false;

    private String currentLanguage;
    private String currentAppearance;

    // -------------- FXML HANDLERS --------------------------------------------
    /**
     *
     */
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

    /**
     *
     */
    public void handleApplyButtonGraphs() {
        if (Integer.valueOf(textFieldTimeFrame.getText()) > (0 - Integer.valueOf(io.getConfigProp("realtime_xscalemin")))) {
            labelErrorGraphs.setText(io.getLangpackString("timeframe_exceeds_bounds"));
            labelErrorGraphs.setVisible(true);
            textFieldTimeFrame.setText(io.getConfigProp("realtime_timeframe"));
        } else if (!textFieldTimeFrame.getText().equals(io.getConfigProp("realtime_timeframe"))) {
            labelErrorGraphs.setVisible(false);
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

    /**
     *
     */
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

    // -------------- OTHER METHODS --------------------------------------------    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Set the global variable io and apply the data retrieved from it to the
     * ComboBoxes, TextFields and CheckBoxes of the window
     *
     * @param io
     */
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

    /**
     * Check if the changes in the settings require a reboot and execute it if
     * necessary
     */
    public void onClose() {
        if (isAppearanceRebootNecessary || isLanguageRebootNecessary) {
            mainController.rebootProgramm();
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     * @param mc
     */
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }
}
