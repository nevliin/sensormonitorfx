package de.hfts.sensormonitor.chart;

/**
 * GraphPoint --- XY-point with a special boolean indicating if the point is empty
 * @author Polarix IT Solutions
 */
public class GraphPoint {
    
    private boolean isEmpty = false; 

    /**
     * X-value of the GraphPoint
     */
    public double x;

    /**
     * Y-value of the GraphPoint
     */
    public double y;
    
    /**
     * 
     * @param x
     * @param y
     */
    public GraphPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     *
     */
    public GraphPoint() {
        
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     *
     * @param isEmpty
     */
    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }   
    
}
