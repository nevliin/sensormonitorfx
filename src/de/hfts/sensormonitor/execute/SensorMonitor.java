package de.hfts.sensormonitor.execute;

import de.hft.ss17.cebarround.BaseSensor;
import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.*;
import de.hfts.sensormonitor.misc.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Polarix IT Solutions
 */
public class SensorMonitor extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        boolean isDBConnected = true;
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
        loadMainWindow(io, stage, sensors, isDBConnected);

    
    }
    
    /**
     * 
     * @param io
     * @param stage
     * @param sensors
     * @param isDBConnected 
     */
    private void loadMainWindow(IO io, Stage stage, List<BaseSensor> sensors, boolean isDBConnected) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/mainWindow.fxml");
            loader.setLocation(url);
            loader.setResources(io.getLangpack());
            BorderPane root = (BorderPane) loader.load();
            
            ((MainController) loader.getController()).setIo(io);
            ((MainController) loader.getController()).setIsDBConnected(isDBConnected);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(io.getStyleSheet("base"), io.getStyleSheet(io.getConfigProp("style")));
            stage.setScene(scene);
            stage.setTitle("CeBarRoundMonitor");
            stage.setMaximized(true);
            stage.setOnCloseRequest(eh -> {
                ((MainController) loader.getController()).quitProgramm();
            });
            stage.show();
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
     *
     */
    public void disableOutput() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // NO-OP
            }
        }));
    }
}
