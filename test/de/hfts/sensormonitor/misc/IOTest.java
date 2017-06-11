/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.exceptions.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class IOTest {

    static String oldname;
    static String testTableName;

    @BeforeClass
    public static void setUpClass() {
        IOUtils.loadConfiguration();
        try {
            IOUtils.connectDB();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        new File("a0c1b1f7cfe1e904368.csv").delete();
        IOUtils.dropTable(oldname);
        IOUtils.dropTable(testTableName);
    }

    @Before
    public void setUp() {
        LogHandler.langpack = ResourceBundle.getBundle("lang.lang", new Locale("en"));
    }

    @After
    public void tearDown() {
    }

    @Test(expected = IllegalTableNameException.class)
    public void testRenamingTableWithIllegalName() throws IllegalTableNameException {
        oldname = IOUtils.createGenericTable();
        IOUtils.renameTable(oldname, "2");
    }

    @Test(expected = ImportRecordingException.class)
    public void testImportingCSVWithWrongFormat() throws ImportRecordingException {
        try {
            FileWriter fw = new FileWriter("a0c1b1f7cfe1e904368.csv");
            StringBuilder sb = new StringBuilder();
            sb.append("2017-05-21 22:50:10.348;0;925800402;'CeBarRound-5.1.3';63;21;4997; \n");
            sb.append("2017-05-21 22:50:10.348;0;925800402;'CeBarRound-5.1.3';21;4997; \n");
            fw.write(sb.toString());
            fw.close();
            IOUtils.importRecording(new File("a0c1b1f7cfe1e904368.csv"));
        } catch (IOException | IllegalTableNameException | ParseException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test(expected = IllegalConfigurationException.class)
    public void testValidatingPropertiesWithDifferentKeys() throws IllegalConfigurationException {
        Properties check = new Properties();
        check.setProperty("1", "test");
        check.setProperty("2", "test");
        Properties template = new Properties();
        template.setProperty("1", "test");
        template.setProperty("3", "test");
        IOUtils.validateProperties(check, template);
    }

    @Test(expected = IllegalConfigurationException.class)
    public void testValidatingPropertiesWithDifferentAmountsOfKeys() throws IllegalConfigurationException {
        Properties check = new Properties();
        check.setProperty("1", "test");
        check.setProperty("2", "test");
        Properties template = new Properties();
        template.setProperty("1", "test");
        template.setProperty("2", "test");
        template.setProperty("3", "test");
        IOUtils.validateProperties(check, template);
    }

    @Test
    public void testValidatingPropertiesWithSimilarKeys() throws IllegalConfigurationException {
        Properties check = new Properties();
        check.setProperty("1", "test");
        check.setProperty("2", "test");
        Properties template = new Properties();
        template.setProperty("1", "test");
        template.setProperty("2", "test");
        IOUtils.validateProperties(check, template);
    }
    
    @Test 
    public void testCreatingGenericTable() {
        testTableName = IOUtils.createGenericTable();
        ResultSet rs = IOUtils.loadRecording(testTableName);
        if(rs == null) {
            fail();
        }
    }

}
