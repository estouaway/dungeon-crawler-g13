package org.example;

// Player class
public class Player {
    private float x, y;
    private float radius;
    private float speed; // maybe get some items for speed
    // add more properties.. strength, health...

    public Player(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speed = 200.0f; // Base speed in pixels per second
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getSpeed() { return speed; }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void moveX(float amount) {
        x += amount;
    }

    public void moveY(float amount) {
        y += amount;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}