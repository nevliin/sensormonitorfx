package de.hfts.sensormonitor.main;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class starting the application
 *
 * @author Polarix IT Solutions
 */
public class CeBarRoundMonitor extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainStage main = new MainStage(primaryStage);
    }

}
