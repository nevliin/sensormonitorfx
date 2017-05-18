package de.hfts.sensormonitor.execute;

import de.hft.ss17.cebarround.BaseSensor;
import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.IllegalSensorAmountException;
import de.hfts.sensormonitor.exceptions.SensorMonitorException;
import de.hfts.sensormonitor.misc.ExceptionDialog;
import de.hfts.sensormonitor.misc.IO;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SensorMonitor extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        boolean isDBConnected = true;
        IO io = new IO();
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
        FXMLLoader loader = new FXMLLoader();
        URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/main.fxml");
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

    }

    public static void main(String[] args) {
        // Start the application
        launch(args);
    }
    
    public void disableOutput() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // NO-OP
            }
        }));
    }
}
