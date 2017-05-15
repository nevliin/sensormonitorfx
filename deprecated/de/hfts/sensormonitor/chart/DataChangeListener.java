package de.hfts.sensormonitor.chart;

/**
 * Interface to be implemented by objects who want to be informed about changes
 * in a SensorChartData
 *
 * @author Polarix IT Solutions
 */
public interface DataChangeListener {

    /**
     * Triggered when the data in a SensorChartData changes
     *
     * @param graphname Sensor ID (name) of the graph which was changed
     */
    void dataChanged(String graphname);

    /**
     * Triggered when the visibility of a GraphSeries in a SensorChartData
     * changes
     *
     * @param graphname Sensor ID (name) of the graph which visibility changed
     * @param isVisible
     */
    void visibilityChanged(String graphname, boolean isVisible);

    /**
     * Triggered when the upper or lower bound of one of the axis' changes
     */
    void axisChanged();
}
