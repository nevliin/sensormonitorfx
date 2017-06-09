package de.hfts.sensormonitor.execute;

import de.hft.ss17.cebarround.BaseSensor;
import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.*;
import de.hfts.sensormonitor.misc.*;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
        IO.loadConfiguration();
        LogHandler.createLogger();        
        LogHandler.langpack = ResourceBundle.getBundle("lang.logging", new Locale("en"));
        LogHandler.LOGGER.info(LogHandler.getLangpackString("program_started"));
        LogHandler.LOGGER.info(LogHandler.getLangpackString("program_language") + ": " + IO.getConfigProp("lang"));
        try {
            IO.connectDB();
            LogHandler.LOGGER.info(LogHandler.getLangpackString("database_connected"));
        } catch (ClassNotFoundException | SQLException e) {
            isDBConnected = false;
            LogHandler.LOGGER.log(Level.SEVERE, null, e);
            LogHandler.LOGGER.warning(LogHandler.getLangpackString("exception_databaseconnect"));
            new ExceptionDialog(IO.getLangpackString("db_connect_error"), null);
        }
        List<BaseSensor> sensors = null;
        try {
            sensors = IO.loadSensors();
            LogHandler.LOGGER.info(LogHandler.getLangpackString("sensors_loaded"));
            disableOutput();
        } catch (IllegalSensorAmountException e) {
            LogHandler.LOGGER.severe(e.getMessage());
            new ExceptionDialog(IO.getLangpackString(e.getExceptionKey()), null);
            System.exit(0);
        }
        // Set the icon of the application
        URL url = getClass().getClassLoader().getResource("images/logo_rose_marble.png");
        Image image = new Image(url.toExternalForm());
        stage.getIcons().add(image);

        loadMainWindow(stage, sensors, isDBConnected);
    }

    /**
     * Load mainWindow.fxml and pass parameters to the MainWindowController
     *
     * @param stage
     * @param sensors
     * @param isDBConnected
     */
    private void loadMainWindow(Stage stage, List<BaseSensor> sensors, boolean isDBConnected) {
        try {
            // Load the FXML file and save it as root
            FXMLLoader loader = new FXMLLoader();
            URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/mainWindow.fxml");
            loader.setLocation(url);
            loader.setResources(IO.getLangpack());
            BorderPane root = (BorderPane) loader.load();

            // Pass parameters to the controller
            ((MainController) loader.getController()).setDBConnected(isDBConnected);

            // Create the scene and add it to the stage
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(IO.getStyleSheet("base"), IO.getStyleSheet(IO.getConfigProp("style")));
            stage.setScene(scene);
            stage.setTitle("CeBarRoundMonitor");
            stage.setMaximized(true);
            stage.setOnCloseRequest(eh -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("");
                alert.setContentText(IO.getLangpackString("quit_confirmation"));

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        ((MainController) loader.getController()).quitProgramm();
                    } catch (Exception ex) {
                        // NO-OP - prevents that the application can not be closed due to any kind of exception
                        LogHandler.LOGGER.log(Level.SEVERE, null, ex);
                    }
                    System.exit(0);
                } else {
                    eh.consume();
                }
            });
            stage.show();
            LogHandler.LOGGER.info(LogHandler.getLangpackString("application_window_shown"));

            // Start displaying sensor data
            ((MainController) loader.getController()).startDisplay(sensors);
        } catch (IOException ex) {
            LogHandler.LOGGER.log(Level.SEVERE, null, ex);
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

    /**
     * Enables System.out
     */
    public static void enableOutput() {
        System.setOut(originalOut);
    }
}
