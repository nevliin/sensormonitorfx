package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.chart.*;
import de.hfts.sensormonitor.controller.EditChartController;
import de.hfts.sensormonitor.model.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * SensorChart --- Class inherited from JavaFX' LineChart. Displays the data
 * saved in a SensorChartData and reacts to changes of the data.
 *
 * @author Polarix IT Solutions
 */
public class SensorChart implements ChartDataChangeListener {

    private LineChart lineChart;

    private ContextMenu contextMenu;
    private ChartData chartdata;
    private Stage editChartWindow;

    private ResourceBundle langpack;

    /**
     *
     * @param lineChart
     * @param chartData
     * @param langpack
     */
    public SensorChart(LineChart lineChart, ChartData chartData, ResourceBundle langpack) {
        this.chartdata = chartData;
        this.langpack = langpack;
        this.lineChart = lineChart;

        chartData.addListener(this);

        Platform.runLater(() -> {
            lineChart.setData(chartdata.getObservableList());
            updateAxis();
        });

        contextMenu = initContextMenu();
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            contextMenu.show(lineChart, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        lineChart.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            contextMenu.hide();
        });
    }

// <--- Window operations --->
    /**
     * Add tooltips displaying detailed information to each XY-point on the
     * LineChart. Call this after the nodes have been displayed, only to be used
     * on static data.
     */
    public void installTooltips() {
        /*for (XYChart.Series s : seriesdata.values()) {
            for (Object d : s.getData()) {
                XYChart.Data data = (XYChart.Data) d;
                Tooltip tooltip = new Tooltip(chartdata.getLangpack().getString("sensor_id") + ": " + s.getName() + "\n"
                        + chartdata.getLangpack().getString("average") + ": " + this.getYAverage(s.getName()) + " " + chartdata.getYUnit() + "\n"
                        + chartdata.getLangpack().getString("x_value") + ": " + Math.round(Double.valueOf(data.getXValue().toString()) * 100) / 100.0 + " " + chartdata.getXUnit() + "\n"
                        + chartdata.getLangpack().getString("y_value") + ": " + data.getYValue() + " " + chartdata.getYUnit());
                try {
                    //Adding class on hover
                    data.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            data.getNode().getStyleClass().add("onHover");
                            tooltip.show(data.getNode().getScene().getWindow(), data.getNode().getScene().getX() + event.getScreenX() + 10, data.getNode().getScene().getX() + event.getScreenY() + 10);
                        }

                    });

                    //Removing class on exit
                    data.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            data.getNode().getStyleClass().remove("onHover");
                            tooltip.hide();
                        }
                    });
                } catch (NullPointerException ex) {

                }
            }
        }*/
    }

    /**
     * Initialise and return a ContextMenu with options regarding the
     * SensorChart
     *
     * @return
     */
    private ContextMenu initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuitem_help = new MenuItem(langpack.getString("help"));
        MenuItem menuitem_edit = new MenuItem(langpack.getString("edit"));
        contextMenu.getItems().addAll(menuitem_help, menuitem_edit);

        menuitem_help.setOnAction(eh -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText(langpack.getString("help_graph"));
            alert.showAndWait();
        });

        menuitem_edit.setOnAction(eh -> {
            if (editChartWindow == null) {
                initEditChartWindow();
            } else {
                editChartWindow.show();
                editChartWindow.toFront();
            }
        });
        return contextMenu;
    }

    /**
     * Initialises and shows a new stage with options to edit the X- and Y-scale
     * of the SensorChart
     */
    private void initEditChartWindow() {
        try {
            // Create the stage, fill it and show it
            FXMLLoader loader = new FXMLLoader();
            URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/editChart.fxml");
            loader.setLocation(url);
            loader.setResources(langpack);
            GridPane root = (GridPane) loader.load();

            editChartWindow = new Stage();
            editChartWindow.setOnCloseRequest(eh -> {
                editChartWindow = null;
            });

            ((EditChartController) loader.getController()).setChartData(chartdata);

            Scene editChartScene = new Scene(root);
            editChartScene.getStylesheets().addAll(lineChart.getScene().getStylesheets());

            editChartWindow.setScene(editChartScene);
            editChartWindow.sizeToScene();

            editChartWindow.show();

            // Add button actions
            /*buttonCancel.setOnAction(eh -> {
            editChartWindow.hide();
            });
            
            buttonSave.setOnAction(eh -> {
            boolean isXScaleValid = false;
            try {
            double tempxmin = Double.valueOf(textfieldXMin.getText());
            double tempxmax = Double.valueOf(textfieldXMax.getText());
            if (tempxmin < tempxmax && tempxmax <= chartdata.getXScaleMax() && tempxmin >= chartdata.getXScaleMin()) {
            isXScaleValid = true;
            }
            } catch (NumberFormatException e) {
            isXScaleValid = false;
            }

            if (isXScaleValid) {
            chartdata.setXMin(Double.valueOf(textfieldXMin.getText()));
            chartdata.setXMax(Double.valueOf(textfieldXMax.getText()));
            } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(chartdata.getLangpack().getString("error_xscale_invalid"));
            alert.showAndWait();
            }

            boolean isYScaleValid = false;

            try {
            double tempymin = Double.valueOf(textfieldYMin.getText());
            double tempymax = Double.valueOf(textfieldYMax.getText());
            if (tempymin < tempymax && tempymax <= chartdata.getYScaleMax() && tempymin >= chartdata.getYScaleMin()) {
            isYScaleValid = true;
            }
            } catch (NumberFormatException e) {
            isYScaleValid = false;
            }

            if (isYScaleValid) {
            chartdata.setYMin(Double.valueOf(textfieldYMin.getText()));
            chartdata.setYMax(Double.valueOf(textfieldYMax.getText()));
            } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(chartdata.getLangpack().getString("error_yscale_invalid"));
            alert.showAndWait();
            }
            if (isYScaleValid && isXScaleValid) {
            updateAxis();
            editChartWindow.hide();
            }
            });*/
        } catch (IOException ex) {
            Logger.getLogger(SensorChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

// <--- Graph operations --->
    /**
     * Sets up XY-Series for each String in the parameter. Required for realtime
     * SensorCharts with dynamic data.
     *
     * @param graphs String array containing the Sensor IDs (names) of the
     * graphs to be display
     */
    public void setUpGraphs(String[] graphs) {
    }

    /**
     * Add a graph to the LineChart. Only to be used for static recordings,
     * won't work with dynamic data.
     *
     * @param points List of GraphPoints, containing the graph to be displayed
     * @param graphname Sensor ID (name) of the graph
     */
    public void addGraph(List<GraphPoint> points, String graphname) {
    }

    /**
     * Sets the specified graphs visibility
     *
     * @param graphname Sensor ID (name) of the graph
     * @param isVisible Visibility status
     */
    private void setGraphVisible(String graphname, boolean isVisible) {
        /*if (isVisible) {
            setPointsToSeries(seriesdata.get(graphname), chartdata.getPoints(Long.valueOf(graphname)));
        } else {
            seriesdata.get(graphname).getData().clear();
        }
        installTooltips();*/
    }

    /**
     * Get the average of all Y-values of the specified series. Returns "NaN" if
     * the parameter is null.
     *
     * @param graphname Sensor ID (name) of the graph the average should be
     * retrieved from
     * @return
     */
    public String getYAverage(String graphname) {
        if (graphname == null) {
            return "NaN";
        }
        XYChart.Series<Double, Double> points = chartdata.getSeries(Long.valueOf(graphname));
        List<Double> values = new ArrayList<>();
        for (XYChart.Data<Double, Double> d : points.getData()) {
            values.add(d.getYValue());
        }
        double total = 0;
        for (double d : values) {
            total += d;
        }
        String result = String.format("%.2f", total / values.size());
        return result;
    }

    /**
     * Updates the X- and Y-axis of the LineChart if the bounds changed
     */
    public void updateAxis() {
        Platform.runLater(() -> {
            ((NumberAxis) lineChart.getXAxis()).setLowerBound(chartdata.getxMin());
            ((NumberAxis) lineChart.getXAxis()).setUpperBound(chartdata.getxMax());
            ((NumberAxis) lineChart.getXAxis()).setTickUnit((chartdata.getxMax() - chartdata.getxMin()) / 10);

            if (chartdata.getyMin() == Double.MAX_VALUE && chartdata.getyMax() == Double.MAX_VALUE) {
                lineChart.getYAxis().setAutoRanging(true);
            } else {
                lineChart.getYAxis().setAutoRanging(false);
                ((NumberAxis) lineChart.getYAxis()).setLowerBound(chartdata.getyMin());
                ((NumberAxis) lineChart.getYAxis()).setUpperBound(chartdata.getyMax());
                ((NumberAxis) lineChart.getYAxis()).setTickUnit((chartdata.getyMax() - chartdata.getyMin()) / 10);
            }
        });
    }

    /**
     * Clear the Series and add the GraphPoint's in the List to it
     *
     * @param series
     * @param points
     */
    private void setPointsToSeries(XYChart.Series series, List<SensorDataPoint> points) {
        try {
            series.getData().clear();
        } catch (NullPointerException e) {
            // Catching NullPointerException if the series doesn't have any data yet
        }
        double lastTime = 0;
        Date lastPoint = null;
        for (SensorDataPoint p : points) {
            double time = 0;
            if (lastPoint != null) {
                time = lastPoint.getTime() - p.time.getTime();
                time = lastTime - (time / 1000.0);
            }
            if (time > chartdata.getxMin()) {
                if (!p.isEmpty()) {
                    try {
                        series.getData().add(new XYChart.Data(time, p.value));
                    } catch (NullPointerException e) {

                    }
                }
            }
            lastPoint = p.time;
            lastTime = time;
        }
    }

// <--- Getters & Setters --->    
    /**
     * Set the SensorChartData of the SensorChart
     *
     * @param data
     */
    public void setChartData(ChartData data) {
        chartdata = data;
        data.addListener(this);
        updateAxis();
    }

    /**
     * Set the maximum and minimum of the X-scale
     *
     * @param xscalemin Minimum lower bound of the X-axis
     * @param xscalemax Maximum upper bound of the X-axis
     */
    public void setXScaleBounds(double xscalemin, double xscalemax) {
        chartdata.setxScaleMin(xscalemin);
        chartdata.setxScaleMax(xscalemax);
    }

    /**
     * Set the maximum and minimum of the Y-scale
     *
     * @param yscalemin Minimum lower bound of the Y-axis
     * @param yscalemax Maximum upper bound of the Y-axis
     */
    public void setYScaleBounds(double yscalemin, double yscalemax) {
        chartdata.setxScaleMin(yscalemin);
        chartdata.setxScaleMax(yscalemax);
    }

    /**
     *
     * @return
     */
    public ChartData getChartData() {
        return chartdata;
    }

    @Override
    public void dataChanged(long sensorID) {
        lineChart.setData(chartdata.getObservableList());
    }

    /**
     *
     */
    @Override
    public void axisChanged() {
        updateAxis();
    }

}
