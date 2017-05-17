package de.hfts.sensormonitor.execute;

import de.hfts.sensormonitor.controller.MainController;
import de.hfts.sensormonitor.exceptions.DatabaseConnectException;
import de.hfts.sensormonitor.exceptions.SensorMonitorException;
import de.hfts.sensormonitor.misc.IO;
import java.net.URL;
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
        SensorMonitorException.langpack = io.getLangpack();
        try {
            io.connectDB();
        } catch (DatabaseConnectException e) {
            isDBConnected = false;
        }
        FXMLLoader loader = new FXMLLoader();
        URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/fxml/main.fxml");
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
    }

    public static void main(String[] args) {
        // Start the application
        launch(args);
    }
}
