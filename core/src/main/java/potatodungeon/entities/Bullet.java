package potatodungeon.entities;

import com.badlogic.gdx.math.Vector2;

public class Bullet {
    private Vector2 position;
    private Vector2 velocity;
    private float speed;
    private boolean active;

    public Bullet(float x, float y, Vector2 direction, float speed) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(direction).nor().scl(speed);
        this.speed = speed;
        this.active = true;
    }

    public void update(float deltaTime) {
        if (!active) return;

        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
    }

    public void destroy() {
        active = false;
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public boolean isActive() { return active; }
}