package potatodungeon.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import potatodungeon.managers.ConfigurationManager;

import java.util.List;

/**
 * Representa o player
 */
public class Player {

    private ConfigurationManager config;
    private float x, y;
    private float radius;
    private float speed; // maybe get some items for speed

    private float shockwaveRadius = 0f;
    private boolean isAttacking = false;
    private float timeSinceLastAttack = 999f;
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
        // Renderizar onda de choque
        if (isAttacking && shockwaveRadius > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Onda amarela que fica transparente
            float alpha = 1f - (shockwaveRadius / 40f);
            shapeRenderer.setColor(1f, 1f, 0f, alpha);
            shapeRenderer.circle(x, y, shockwaveRadius);

            shapeRenderer.end();
        }
    }

    public void update(float deltaTime) {
        // Atualizar cooldown
        timeSinceLastAttack += deltaTime;

        // Verificar input de ataque
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) &&
                timeSinceLastAttack >= 0.5f && !isAttacking) {

            // Começar ataque
            isAttacking = true;
            shockwaveRadius = 0f;
            timeSinceLastAttack = 0f;
            System.out.println("Player attacks!");
        }

        // Atualizar onda de choque
        if (isAttacking) {
            shockwaveRadius += 100f * deltaTime; // 30/0.3 = 100
            if (shockwaveRadius >= 30f) {
                isAttacking = false;
                shockwaveRadius = 0f;
            }
        }
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


    public boolean isAttacking() { return isAttacking; }
    public float getShockwaveRadius() { return shockwaveRadius; }

    // por implementar
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}