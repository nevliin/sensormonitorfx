/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.controller;

import de.hfts.sensormonitor.exceptions.IllegalXScaleException;
import de.hfts.sensormonitor.exceptions.IllegalYScaleException;
import de.hfts.sensormonitor.exceptions.SensorMonitorException;
import de.hfts.sensormonitor.misc.LogHandler;
import de.hfts.sensormonitor.model.ChartData;
import de.hfts.sensormonitor.model.SensorData;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Polarix IT Solutions
 */
public class EditChartControllerTest {

    static EditChartController ecc;

    @BeforeClass
    public static void setUpClass() {
        ecc = new EditChartController();
        ChartData cd = new ChartData(SensorData.Data.TEMPERATURE);
        cd.setxScaleMin(-50);
        cd.setxScaleMax(0);
        cd.setyScaleMin(0);
        cd.setyScaleMax(100);
        ecc.chartData = cd;
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        LogHandler.langpack = ResourceBundle.getBundle("lang.lang", new Locale("en"));
    }

    @After
    public void tearDown() {
    }

    @Test(expected = IllegalXScaleException.class)
    public void testXAxisUpdateWithOutOfRangeValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-60", "0", "50", "70");
    }

    @Test(expected = IllegalXScaleException.class)
    public void testXAxisUpdateWithSameValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-40", "-40", "50", "70");
    }

    @Test(expected = IllegalXScaleException.class)
    public void testXAxisUpdateWithMinHigherThanMaxValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-20", "-40", "50", "70");
    }

    @Test(expected = IllegalYScaleException.class)
    public void testYAxisUpdateWithOutOfRangeValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-40", "0", "-10", "70");
    }

    @Test(expected = IllegalYScaleException.class)
    public void testYAxisUpdateWithSameValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-40", "0", "70", "70");
    }

    @Test(expected = IllegalYScaleException.class)
    public void testYAxisUpdateWithMinHigherThanMaxValues() throws IllegalXScaleException, IllegalYScaleException {
        ecc.updateBounds("-50", "-40", "70", "50");
    }

}
