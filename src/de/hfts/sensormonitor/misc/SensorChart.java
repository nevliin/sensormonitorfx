package de.hfts.sensormonitor.misc;

import de.hfts.sensormonitor.chart.*;
import de.hfts.sensormonitor.model.ChartData;
import java.io.IOException;
import java.util.*;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
public class SensorChart extends LineChart {

    private HashMap<String, XYChart.Series> seriesdata = new HashMap<>();
    private ContextMenu popupmenu;
    private SensorChartData chartdata;
    private Stage editChartWindow;

    /**
     * Constructor for a SensorChart, creates a new SensorChartData based on the
     * given arguments
     *
     * @param xmin Lower bound of the X-axis
     * @param xmax Upper bound of the X-axis
     * @param ymin Lower bound of the Y-axis
     * @param ymax Upper bound of the Y-axis
     * @param xunit Unit of the X-axis
     * @param yunit Unit of the Y-axis
     * @param langpack ResourceBundle containing the configured language pack
     * @param chartname Name of the SensorChart
     */
    /*public SensorChart(double xmin, double xmax, double ymin, double ymax, String xunit, String yunit, ResourceBundle langpack, String chartname) {
        super(new NumberAxis(xunit, xmin, xmax, (xmax - xmin) / 10), new NumberAxis(yunit, ymin, ymax, (ymax - ymin) / 10));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("custom_control.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setAnimated(false);
        this.setCreateSymbols(true);
        this.setTitle(chartname);
        this.chartdata = new SensorChartData(xmin, xmax, ymin, ymax, xunit, yunit, langpack, chartname);
        chartdata.addListener(this);
        popupmenu = initContextMenu();
        this.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            popupmenu.show(SensorChart.this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            popupmenu.hide();
        });

    }*/
    public SensorChart() {
        super(new NumberAxis(), new NumberAxis());
        ((NumberAxis) this.getXAxis()).setLabel("sec");
        this.setAnimated(false);
    }

    /**
     * Constructor for a SensorChart, uses the given SensorChartData
     *
     * @param chartData SensorChartData defining the content and attributes of
     * the SensorChart
     */
    /*public SensorChart(SensorChartData chartData) {
        this(chartData.getXMin(), chartData.getXMax(), chartData.getYMin(), chartData.getYMax(), chartData.getXUnit(), chartData.getYUnit(), chartData.getLangpack(), chartData.getChartname());
        this.setChartData(chartData);
        chartData.addListener(this);
    }*/
// <--- Window operations --->
    /**
     * Add tooltips displaying detailed information to each XY-point on the
     * LineChart. Call this after the nodes have been displayed, only to be used
     * on static data.
     */
    public void installTooltips() {
        for (XYChart.Series s : seriesdata.values()) {
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
        MenuItem menuitem_help = new MenuItem(chartdata.getLangpack().getString("help"));
        MenuItem menuitem_edit = new MenuItem(chartdata.getLangpack().getString("edit"));
        contextMenu.getItems().addAll(menuitem_help, menuitem_edit);

        menuitem_help.setOnAction(eh -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText(chartdata.getLangpack().getString("help_graph"));
            alert.showAndWait();
        });

        menuitem_edit.setOnAction(eh -> {
            if (editChartWindow == null) {
                initEditChartWindow();
            } else {
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
        // Create the stage, fill it and show itw
        editChartWindow = new Stage();
        editChartWindow.setOnCloseRequest(eh -> {
            editChartWindow = null;
        });

        editChartWindow.setTitle(chartdata.getLangpack().getString("edit_graph"));

        GridPane gridPane_EditGraph = new GridPane();
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        gridPane_EditGraph.getColumnConstraints().addAll(column, column2, column, column2);
        gridPane_EditGraph.setId("edit-graph-grid");

        Label labelTitle = new Label(chartdata.getLangpack().getString("edit_graph") + ": " + this.chartdata.getChartname());
        gridPane_EditGraph.add(labelTitle, 0, 0, 4, 1);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        gridPane_EditGraph.add(separator, 0, 1, 4, 1);

        Label labelXMin = new Label(chartdata.getLangpack().getString("x_minimum"));
        gridPane_EditGraph.add(labelXMin, 0, 2);

        TextField textfieldXMin = new TextField(Double.toString(chartdata.getXMin()));
        gridPane_EditGraph.add(textfieldXMin, 1, 2);

        Label labelXMax = new Label(chartdata.getLangpack().getString("x_maximum"));
        gridPane_EditGraph.add(labelXMax, 2, 2);

        TextField textfieldXMax = new TextField(Double.toString(chartdata.getXMax()));
        gridPane_EditGraph.add(textfieldXMax, 3, 2);

        Label labelYMin = new Label(chartdata.getLangpack().getString("y_minimum"));
        gridPane_EditGraph.add(labelYMin, 0, 3);

        TextField textfieldYMin = new TextField(Double.toString(chartdata.getYMin()));
        gridPane_EditGraph.add(textfieldYMin, 1, 3);

        Label labelYMax = new Label(chartdata.getLangpack().getString("y_maximum"));
        gridPane_EditGraph.add(labelYMax, 2, 3);

        TextField textfieldYMax = new TextField(Double.toString(chartdata.getYMax()));
        gridPane_EditGraph.add(textfieldYMax, 3, 3);

        separator = new Separator(Orientation.HORIZONTAL);
        gridPane_EditGraph.add(separator, 0, 4, 4, 1);

        Button buttonCancel = new Button(chartdata.getLangpack().getString("cancel"));
        gridPane_EditGraph.add(buttonCancel, 0, 5, 2, 1);

        Button buttonSave = new Button(chartdata.getLangpack().getString("save"));
        gridPane_EditGraph.add(buttonSave, 2, 5, 2, 1);

        Scene editChartScene = new Scene(gridPane_EditGraph);
        editChartScene.getStylesheets().addAll(this.getScene().getStylesheets());

        editChartWindow.setScene(editChartScene);
        editChartWindow.sizeToScene();

        editChartWindow.show();

        // Add button actions
        buttonCancel.setOnAction(eh -> {
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
        });
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
        for (String s : graphs) {
            this.chartdata.put(s, new GraphSeries(s));
            XYChart.Series series = new XYChart.Series();
            series.setName(s);
            seriesdata.put(s, series);
            this.getData().add(series);
        }
    }

    /**
     * Add a graph to the LineChart. Only to be used for static recordings,
     * won't work with dynamic data.
     *
     * @param points List of GraphPoints, containing the graph to be displayed
     * @param graphname Sensor ID (name) of the graph
     */
    public void addGraph(List<GraphPoint> points, String graphname) {
        chartdata.put(graphname, new GraphSeries(graphname, points));
        XYChart.Series series = new XYChart.Series();
        setPointsToSeries(series, points);
        series.setName(graphname);
        seriesdata.put(graphname, series);
        this.getData().add(series);
    }

    /**
     * Sets the specified graphs visibility
     *
     * @param graphname Sensor ID (name) of the graph
     * @param isVisible Visibility status
     */
    private void setGraphVisible(String graphname, boolean isVisible) {
        if (isVisible) {
            setPointsToSeries(seriesdata.get(graphname), chartdata.get(graphname).getPoints());
        } else {
            seriesdata.get(graphname).getData().clear();
        }
        installTooltips();
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
        List<GraphPoint> points = chartdata.get(graphname).getPoints();
        List<Double> values = new ArrayList<>();
        for (GraphPoint p : points) {
            values.add(p.y);
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
        ((NumberAxis) this.getXAxis()).setLowerBound(chartdata.getXMin());
        ((NumberAxis) this.getXAxis()).setUpperBound(chartdata.getXMax());
        ((NumberAxis) this.getXAxis()).setTickUnit((chartdata.getXMax() - chartdata.getXMin()) / 10);

        ((NumberAxis) this.getYAxis()).setLowerBound(chartdata.getYMin());
        ((NumberAxis) this.getYAxis()).setUpperBound(chartdata.getYMax());
        ((NumberAxis) this.getYAxis()).setTickUnit((chartdata.getYMax() - chartdata.getYMin()) / 10);
    }

    /**
     * Clear the Series and add the GraphPoint's in the List to it
     *
     * @param series
     * @param points
     */
    private void setPointsToSeries(XYChart.Series series, List<GraphPoint> points) {
        try {
            series.getData().clear();
        } catch (NullPointerException e) {
        }
        for (GraphPoint p : points) {
            if (!p.isEmpty()) {
                series.getData().add(new XYChart.Data(p.x, p.y));
            }
        }
    }

// <--- Getters & Setters --->    
    /**
     * Set the SensorChartData of the SensorChart
     *
     * @param data
     */
    public void setChartData(ChartData data) {
        this.setData(data.getChartSeries());
        NumberAxis xAxis = (NumberAxis) this.getXAxis();
        xAxis.setUpperBound(data.getxMax());
        xAxis.setLowerBound(data.getxMin());
    }

    /**
     * Set the maximum and minimum of the X-scale
     *
     * @param xscalemin Minimum lower bound of the X-axis
     * @param xscalemax Maximum upper bound of the X-axis
     */
    public void setXScaleBounds(double xscalemin, double xscalemax) {
        chartdata.setXScaleMin(xscalemin);
        chartdata.setXScaleMax(xscalemax);
    }

    /**
     * Set the maximum and minimum of the Y-scale
     *
     * @param yscalemin Minimum lower bound of the Y-axis
     * @param yscalemax Maximum upper bound of the Y-axis
     */
    public void setYScaleBounds(double yscalemin, double yscalemax) {
        chartdata.setYScaleMin(yscalemin);
        chartdata.setYScaleMax(yscalemax);
    }

    /**
     *
     * @return
     */
    public ResourceBundle getLangpack() {
        return chartdata.getLangpack();
    }

    /**
     *
     * @return
     */
    public SensorChartData getChartData() {
        return chartdata;
    }

}
