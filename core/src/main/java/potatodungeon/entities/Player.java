package potatodungeon.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.managers.ConfigurationManager;

/**
 * Representa o player
 */
public class Player {

    private ConfigurationManager config;
    private float x, y;
    private float radius;
    private float speed; // maybe get some items for speed
    // add more properties.. strength, health...

    // só é chamado dentro do proprio package, neste caso, pela factory
    Player(float x, float y, float radius) {
        this.config = ConfigurationManager.getInstance();
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speed = 200.0f;
    }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getSpeed() { return speed; }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(this.config.getPlayerColor()); // Red
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();
    }

    public void moveLeft(float delta) {
        x -= speed * delta;
    }

    public void moveRight(float delta) {
        x += speed * delta;
    }

    public void moveUp(float delta) {
        y += speed * delta;
    }

    public void moveDown(float delta) {
        y -= speed * delta;
    }

    // por implementar
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}