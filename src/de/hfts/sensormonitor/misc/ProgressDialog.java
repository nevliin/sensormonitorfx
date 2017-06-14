/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hfts.sensormonitor.misc;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Polarix IT Solutions
 */
public class ProgressDialog {

    private Scene scene;
    private Stage stage;
    private ProgressBar progressBar;
    private int stages;

    public ProgressDialog(int stages, String message, String title) {
        this.stages = stages;
        stage = new Stage();
        stage.setTitle(title);

        GridPane gp = new GridPane();
        gp.getStyleClass().add("progressDialog");

        Label label = new Label(message);
        gp.add(label, 0, 0);

        progressBar = new ProgressBar(0);
        gp.add(progressBar, 0, 1);

        scene = new Scene(gp);
        stage.setScene(scene);
        stage.show();
    }

    public void hide() {
        Platform.runLater(() -> {
            stage.hide();
        });
    }

    public void progress() {
        Platform.runLater(() -> {
            double d = progressBar.getProgress();
            progressBar.setProgress(d + (1.0 / stages));
        });
    }

    public Scene getScene() {
        return scene;
    }

}
