package de.hfts.sensormonitor.execute;

import de.hft.ss17.cebarround.BaseSensor;
import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.*;
import de.hfts.sensormonitor.misc.*;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * SensorMonitor --- Starts the applications by loading Properties and the
 * mainWindow
 *
 * @author Polarix IT Solutions
 */
public class SensorMonitor extends Application {

    static PrintStream originalOut;

    /**
     * Creates the IO and loads resources
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        originalOut = System.out;
        boolean isDBConnected = true;
        // Create the IO, connect to the DB and load the sensors
        IO io = new IO();
        SensorMonitorException.langpack = io.getLangpack();
        try {
            io.connectDB();
        } catch (ClassNotFoundException | SQLException e) {
            isDBConnected = false;
            new ExceptionDialog(io.getLangpackString("exception_databaseconnect"), null);
        }
        List<BaseSensor> sensors = null;
        try {
            sensors = io.loadSensors();
            disableOutput();
        } catch (IllegalSensorAmountException e) {
            new ExceptionDialog(io.getLangpackString("exception_illegalsensoramount"), null);
            System.exit(0);
        }
        // Set the icon of the application
        URL url = getClass().getClassLoader().getResource("images/logo_rose_marble.png");
        Image image = new Image(url.toExternalForm());
        stage.getIcons().add(image);

        loadMainWindow(io, stage, sensors, isDBConnected);
    }

    /**
     * Load mainWindow.fxml and pass parameters to the MainWindowController
     *
     * @param io
     * @param stage
     * @param sensors
     * @param isDBConnected
     */
    private void loadMainWindow(IO io, Stage stage, List<BaseSensor> sensors, boolean isDBConnected) {
        try {
            // Load the FXML file and save it as root
            FXMLLoader loader = new FXMLLoader();
            URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/mainWindow.fxml");
            loader.setLocation(url);
            loader.setResources(io.getLangpack());
            BorderPane root = (BorderPane) loader.load();

            // Pass parameters to the controller
            ((MainController) loader.getController()).setIo(io);
            ((MainController) loader.getController()).setIsDBConnected(isDBConnected);

            // Create the scene and add it to the stage
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(io.getStyleSheet("base"), io.getStyleSheet(io.getConfigProp("style")));
            stage.setScene(scene);
            stage.setTitle("CeBarRoundMonitor");
            stage.setMaximized(true);
            stage.setOnCloseRequest(eh -> {
                ((MainController) loader.getController()).quitProgramm();
                System.exit(0);
            });
            stage.show();

            // Start displaying sensor data
            ((MainController) loader.getController()).startDisplay(sensors);
        } catch (IOException ex) {
            Logger.getLogger(SensorMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        // Start the application
        launch(args);
    }

    /**
     * Disables System.out
     */
    public static void disableOutput() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // NO-OP
            }
        }));
    }

    public static void enableOutput() {
        System.setOut(originalOut);
    }
}
