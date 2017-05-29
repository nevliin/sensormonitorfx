package de.hfts.sensormonitor.misc;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import de.hfts.sensormonitor.controller.EditChartController;
import de.hfts.sensormonitor.model.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * SensorChart --- Class inherited from JavaFX' LineChart. Displays the data
 * saved in a ChartData and reacts to changes of the data.
 *
 * @author Polarix IT Solutions
 */
public class SensorChart extends LineChart implements ChartDataChangeListener {

    // -------------- PRIVATE FIELDS -------------------------------------------
    /**
     * ContextMenu shown when right-clicking on the SensorChart
     */
    private ContextMenu contextMenu;
    /**
     * Data model of the SensorChart
     */
    private ChartData chartdata;
    /**
     * Window for editing the bounds of the SensorChart
     */
    private Stage editChartWindow;

    private ResourceBundle langpack;

    // -------------- CONSTRUCTORS ---------------------------------------------
    /**
     * Default constructor for the FXMLLoader
     */
    public SensorChart() {
        super(new NumberAxis(), new NumberAxis());
    }

    // -------------- GETTERS & SETTERS ----------------------------------------
    /**
     * Set the Data model of the SensorChart as well as the language
     * ResourceBundle and the axis titles.
     *
     * @param chartData
     * @param xAxisTitle
     * @param yAxisTitle
     * @param langpack
     * @param createSymbols
     */
    public void setChartData(ChartData chartData, ResourceBundle langpack, String xAxisTitle, String yAxisTitle, boolean createSymbols) {
        this.chartdata = chartData;
        this.langpack = langpack;

        chartData.addListener(this);

        Platform.runLater(() -> {
            this.setData(chartdata.getObservableList());
            updateAxis();
        });

        // Set up the ContextMenu
        contextMenu = initContextMenu();
        this.setAnimated(false);
        this.setCreateSymbols(createSymbols);
        this.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            contextMenu.hide();
        });

        // Set the axis titles
        this.getXAxis().setLabel(xAxisTitle);
        this.getYAxis().setLabel(yAxisTitle);
    }

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
     *
     * @return
     */
    public ChartData getChartData() {
        return chartdata;
    }

    // -------------- OTHER METHODS --------------------------------------------
    /**
     * Add tooltips displaying detailed information to each XY-point on the
     * LineChart. Call this after the nodes have been displayed, only to be used
     * on static data.
     */
    public void installTooltips() {
        for (XYChart.Series s : chartdata.getSeries()) {
            for (Object d : s.getData()) {
                XYChart.Data data = (XYChart.Data) d;
                Tooltip tooltip = new Tooltip(langpack.getString("sensor_id") + ": " + s.getName() + "\n"
                        + langpack.getString("partTypeCode") + ": " + chartdata.getPartTypeCodes().get(Long.valueOf(s.getName())) + "\n"
                        + langpack.getString("average") + ": " + this.getYAverage(s.getName()) + " " + this.getYAxis().getLabel() + "\n"
                        + langpack.getString("x_value") + ": " + Math.round(Double.valueOf(data.getXValue().toString()) * 100) / 100.0 + " " + this.getXAxis().getLabel() + "\n"
                        + langpack.getString("y_value") + ": " + data.getYValue() + " " + this.getYAxis().getLabel());
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
                    // NO-OP --- catch NullPointerException if SensorChart is not visible yet
                }
            }
        }
    }

    /**
     * Add tooltips displaying additional information when hovering over the
     * label identifying the series.
     */
    public void installLabelTooltips() {
        for (LegendItem li : ((Legend) this.getLegend()).getItems()) {
            Tooltip tooltip = new Tooltip(langpack.getString("sensor_id") + ": " + li.getText() + "\n"
                    + langpack.getString("average") + ": " + this.getYAverage(li.getText()) + " " + this.getYAxis().getLabel());
            Tooltip.install(li.getSymbol(), tooltip);
            /*try {
                //Adding class on hover
                
                li.getSymbol().setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        tooltip.show(li.getSymbol().getScene().getWindow(), li.getSymbol().getScene().getX() + event.getScreenX() + 10, li.getSymbol().getScene().getX() + event.getScreenY() + 10);
                    }

                });

                //Removing class on exit
                li.getSymbol().setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        tooltip.hide();
                    }
                });
            } catch (NullPointerException ex) {
                // NO-OP --- catch NullPointerException if SensorChart is not visible yet
            }*/
        }
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
            URL url = this.getClass().getClassLoader().getResource("de/hfts/sensormonitor/view/editChartWindow.fxml");
            loader.setLocation(url);
            loader.setResources(langpack);
            GridPane root = (GridPane) loader.load();

            editChartWindow = new Stage();
            editChartWindow.setOnCloseRequest(eh -> {
                editChartWindow = null;
            });

            ((EditChartController) loader.getController()).setChartData(chartdata);

            Scene editChartScene = new Scene(root);
            editChartScene.getStylesheets().addAll(this.getScene().getStylesheets());

            editChartWindow.setScene(editChartScene);
            editChartWindow.sizeToScene();

            editChartWindow.show();
        } catch (IOException ex) {
            Logger.getLogger(SensorChart.class.getName()).log(Level.SEVERE, null, ex);
        }
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
     * Updates the X- and Y-axis of the SensorChart if the bounds changed
     */
    public void updateAxis() {
        Platform.runLater(() -> {
            this.getXAxis().setAutoRanging(false);
            ((NumberAxis) this.getXAxis()).setLowerBound(chartdata.getxMin());
            ((NumberAxis) this.getXAxis()).setUpperBound(chartdata.getxMax());
            ((NumberAxis) this.getXAxis()).setTickUnit((chartdata.getxMax() - chartdata.getxMin()) / 10);

            if (chartdata.getyMin() == Double.MAX_VALUE && chartdata.getyMax() == Double.MAX_VALUE) {
                this.getYAxis().setAutoRanging(true);
            } else {
                this.getYAxis().setAutoRanging(false);
                ((NumberAxis) this.getYAxis()).setLowerBound(chartdata.getyMin());
                ((NumberAxis) this.getYAxis()).setUpperBound(chartdata.getyMax());
                ((NumberAxis) this.getYAxis()).setTickUnit((chartdata.getyMax() - chartdata.getyMin()) / 10);
            }
            installTooltips();
        });
    }

    @Override
    public void axisChanged() {
        updateAxis();
    }

}
