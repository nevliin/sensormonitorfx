package de.hfts.sensormonitor.chart;

import java.util.*;

/**
 * Saves a single graph with additional information about the name and the visibility of the graph
 * @author Polarix IT Solutions
 */
public class GraphSeries {

    private List<GraphPoint> points = new ArrayList<>(); // Points of the graph
    private boolean isVisible = true; 
    private String name;

    /**
     * Constructor for an empty GraphSeries
     * @param name
     */
    public GraphSeries(String name) {
        this.name = name;

    }

    /**
     * Constructor for a filled GraphSeries
     * @param name
     * @param points
     */
    public GraphSeries(String name, List<GraphPoint> points) {
        this.name = name;
        this.points = points;
    }

    /**
     * Get the XY-points of the GraphSeries
     * @return 
     */
    public List<GraphPoint> getPoints() {
        return points;
    }

    /**
     * Set the XY-points of the GraphSeries
     * @param points
     */
    public void setPoints(List<GraphPoint> points) {
        this.points = points;
    }
    
    /**
     * Get the visibility of the GraphSeries
     * @return
     */
    public boolean isVisible() {
        return this.isVisible;
    }
    
    /**
     * Set the visibility of the GraphSeries
     * @param isVisible
     */
    public void isVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
