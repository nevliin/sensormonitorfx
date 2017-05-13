package de.hfts.sensormonitor.main;

import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Class setting up the main JavaFX stage and defining the close action
 *
 * @author Polarix IT Solutions
 */
public class MainStage {

    public MainStage(Stage stage) {
        BorderPane pane = new BorderPane();
        SensorMonitor scene = new SensorMonitor(pane, stage);
        stage.setTitle("SensorMonitor");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                scene.quitProgramm();
                System.exit(0);
            }
        });
    }

}
