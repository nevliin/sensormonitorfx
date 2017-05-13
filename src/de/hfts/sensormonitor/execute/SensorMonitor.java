package de.hfts.sensormonitor.execute;

import de.hfts.sensormonitor.controller.MainController;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javafx.application.Application; 
import javafx.fxml.FXMLLoader; 
import javafx.scene.Parent; 
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; 

public class SensorMonitor extends Application { 
     
    @Override 
    public void start(Stage stage) throws Exception{ 
    	FXMLLoader loader = new FXMLLoader();
    	InputStream stream = this.getClass().getClassLoader().getResourceAsStream("de/hfts/sensormonitor/fxml/main_fxml.fxml");
    	
    	BorderPane root = (BorderPane) loader.load(stream);
    	
    	Scene scene = new Scene(root);
    	stage.setScene(scene);
    	stage.setTitle("A simple FXML Example");
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
