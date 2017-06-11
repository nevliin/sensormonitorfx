/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * LogHandler --- Contains the Logger and a ResourceBundle for the logging
 * messages. Handles logging output and formats it.
 *
 * @author Polarix IT Solutions
 */
public class LogHandler {

    // -------------- PUBLIC FIELDS --------------------------------------------
    /**
     * Needs to be set before throwing any exceptions; provides messages for all
     * exceptions as well as logging messages
     */
    public static ResourceBundle langpack;

    /**
     * Logger for all information and exceptions in the application
     */
    public static Logger LOGGER;

    // -------------- INNER CLASSES --------------------------------------------
    /**
     * Formats the LogRecords for the FileHandler
     */
    static class LogFormatter extends java.util.logging.Formatter {

        @Override
        public String format(LogRecord record) {
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();
                return "[" + IOUtils.sdf.format(new Date(record.getMillis())) + "] " + record.getLevel() + ": " + stacktrace + "\n";
            } else {
                return "[" + IOUtils.sdf.format(new Date(record.getMillis())) + "] " + record.getLevel() + ": " + record.getMessage() + "\n";
            }
        }

    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Create a logger, add a FileHandler
     */
    public static void createLogger() {
        try {
            LOGGER = Logger.getLogger("");
            GregorianCalendar cal = new GregorianCalendar();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
            String logfilename = format.format(cal.getTime()) + ".txt";
            FileHandler fh = new FileHandler(System.getProperty("user.home") + File.separator + ".sensormonitor" + File.separator + "logs" + File.separator + logfilename);
            fh.setFormatter(new LogFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     *
     */
    public static String getLangpackString(String key) {
        return langpack.getString(key);
    }

}
