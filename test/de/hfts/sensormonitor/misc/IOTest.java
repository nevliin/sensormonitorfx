/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.exceptions.IllegalTableNameException;
import de.hfts.sensormonitor.exceptions.IllegalXScaleException;
import de.hfts.sensormonitor.exceptions.IllegalYScaleException;
import de.hfts.sensormonitor.exceptions.ImportRecordingException;
import de.hfts.sensormonitor.exceptions.SensorMonitorException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Locale;
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

    IO io;
    String oldname;

    public IOTest() {
        SensorMonitorException.langpack = ResourceBundle.getBundle("lang.lang", new Locale("en"));
        io = new IO();
        try {
            io.connectDB();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        //new File("test.csv").delete();
        io.dropTable("oldname");
    }

    @Test(expected = IllegalTableNameException.class)
    public void testRenamingTableWithIllegalName() throws IllegalTableNameException {
        oldname = io.createGenericTable();
        io.renameTable(oldname, "2");
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
            io.importRecording(new File("a0c1b1f7cfe1e904368.csv"));
        } catch (IOException | IllegalTableNameException | ParseException ex) {
            Logger.getLogger(IOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
