package potatodungeon.entities;

/**
 * Represents an obstacle in a room
 */
public class Obstacle {
    private float x, y;
    private float radius;

    /**
     * Create a new obstacle
     */
    public Obstacle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Set the position of the obstacle
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
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