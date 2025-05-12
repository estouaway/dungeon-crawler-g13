package org.example;

/**
 * Represents an obstacle in a room
 */
public class Obstacle {
    private float x, y;
    private float radius;
    private boolean square;

    /**
     * Create a new obstacle
     */
    public Obstacle(float x, float y, float radius, boolean square) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.square = square;
    }

    /**
     * Set the position of the obstacle
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Check if the obstacle is a square
     */
    public boolean isSquare() {
        return square;
    }

    /**
     * Get the X position
     */
    public float getX() {
        return x;
    }

    /**
     * Get the Y position
     */
    public float getY() {
        return y;
    }

    /**
     * Get the radius (size)
     */
    public float getRadius() {
        return radius;
    }
}