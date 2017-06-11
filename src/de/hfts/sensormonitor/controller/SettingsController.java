/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.misc.IOUtils;
import de.hfts.sensormonitor.viewelements.SensorChart;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.model.TableData;
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
     * TabPane containing all settings tabs
     */
    @FXML
    private TabPane Settings;
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
     * CheckBox showing if points on the realtime SensorChart's are displayed or
     * not
     */
    @FXML
    private CheckBox checkBoxDisplayPoints;
    /**
     * ComboBox listing all available styles
     */
    @FXML
    private ComboBox comboBoxAppearance;
    /**
     * Label for displaying errors and notices upon applying the settings for
     * graphs
     */
    @FXML
    private Label labelErrorGraphs;
    /**
     * Label for displaying errors and notices upon saving the settings for
     * language
     */
    @FXML
    private Label labelErrorLanguage;
    /**
     * Label for displaying errors and notices upon saving the settings for
     * appearance
     */
    @FXML
    private Label labelErrorAppearance;

    // -------------- PRIVATE FIELDS -------------------------------------------
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
            IOUtils.setConfigProp("lang", IOUtils.getLanguages().get(((String) comboBoxLanguages.getValue())));
            IOUtils.saveConfigProperties();
        } else {
            labelErrorLanguage.setText(IOUtils.getLangpackString("applied_upon_closing"));
            labelErrorLanguage.setVisible(true);
            isLanguageRebootNecessary = true;
            IOUtils.setConfigProp("lang", IOUtils.getLanguages().get(((String) comboBoxLanguages.getValue())));
            IOUtils.saveConfigProperties();
        }
    }

    /**
     *
     */
    public void handleApplyButtonGraphs() {
        if (Integer.valueOf(textFieldTimeFrame.getText()) > (0 - Integer.valueOf(IOUtils.getConfigProp("realtime_xscalemin")))) {
            labelErrorGraphs.setText(IOUtils.getLangpackString("timeframe_exceeds_bounds"));
            labelErrorGraphs.setVisible(true);
            textFieldTimeFrame.setText(IOUtils.getConfigProp("realtime_timeframe"));
        } else if (!textFieldTimeFrame.getText().equals(IOUtils.getConfigProp("realtime_timeframe"))) {
            labelErrorGraphs.setVisible(false);
            for (ChartData cd : mainController.getChartDatas()) {
                cd.setxMin(0 - Integer.valueOf(textFieldTimeFrame.getText()));
                cd.notifyListenersOfAxisChange();
            }
            for (TableData td : mainController.getTableDatas().values()) {
                td.setMinTime(0 - Integer.valueOf(textFieldTimeFrame.getText()));
            }
            IOUtils.setConfigProp("realtime_timeframe", textFieldTimeFrame.getText());
            IOUtils.saveConfigProperties();
        }
        if (checkBoxDisplayPoints.isSelected() != Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols"))) {
            for (SensorChart sc : mainController.getSensorCharts()) {
                sc.setCreateSymbols(checkBoxDisplayPoints.isSelected());
            }
            IOUtils.setConfigProp("displayPointSymbols", Boolean.toString(checkBoxDisplayPoints.isSelected()));
            IOUtils.saveConfigProperties();
        }
    }

    /**
     *
     */
    public void handleSaveButtonAppearance() {
        if (((String) comboBoxAppearance.getValue()).equals(currentAppearance)) {
            labelErrorAppearance.setVisible(false);
            isAppearanceRebootNecessary = false;
            IOUtils.setConfigProp("style", ((String) comboBoxAppearance.getValue()));
            IOUtils.saveConfigProperties();
        } else {
            labelErrorAppearance.setText(IOUtils.getLangpackString("applied_upon_closing"));
            labelErrorAppearance.setVisible(true);
            isAppearanceRebootNecessary = true;
            IOUtils.setConfigProp("style", ((String) comboBoxAppearance.getValue()));
            IOUtils.saveConfigProperties();
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
     */
    public void setUpData() {
        IOUtils.loadAvailableLanguages();
        comboBoxLanguages.setItems(FXCollections.observableArrayList(IOUtils.getLanguages().keySet()));
        currentLanguage = new Locale(IOUtils.getConfigProp("lang")).getDisplayLanguage(new Locale(IOUtils.getConfigProp("lang")));
        comboBoxLanguages.setValue(currentLanguage);
        textFieldTimeFrame.setText(IOUtils.getConfigProp("realtime_timeframe"));
        checkBoxDisplayPoints.setSelected(Boolean.valueOf(IOUtils.getConfigProp("displayPointSymbols")));
        IOUtils.loadAvailableStyles();
        comboBoxAppearance.setItems(FXCollections.observableArrayList(IOUtils.getStyles()));
        currentAppearance = IOUtils.getConfigProp("style");
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
