package potatodungeon.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import potatodungeon.managers.BulletManager;

public class Enemy {
    public enum EnemyType {
        FAST_SHOOTER,   // Atira rápido, balas lentas
        NORMAL_SHOOTER, // Velocidade normal
        SLOW_SHOOTER    // Atira devagar, balas rápidas
    }

    private Vector2 position;
    private EnemyType type;
    private float shootCooldown;
    private float timeSinceLastShot;
    private float bulletSpeed;
    private boolean alive;

    public Enemy(float x, float y, EnemyType type) {
        this.position = new Vector2(x, y);
        this.type = type;
        this.alive = true;
        this.timeSinceLastShot = 0;

        // Configurar stats baseados no tipo
        switch (type) {
            case FAST_SHOOTER:
                shootCooldown = 0.8f;  // Atira a cada 0.8s
                bulletSpeed = 120f;    // Balas lentas
                break;
            case NORMAL_SHOOTER:
                shootCooldown = 1.5f;  // Atira a cada 1.5s
                bulletSpeed = 180f;    // Balas normais
                break;
            case SLOW_SHOOTER:
                shootCooldown = 2.5f;  // Atira a cada 2.5s
                bulletSpeed = 250f;    // Balas rápidas
                break;
        }
    }

    public void update(float deltaTime, Vector2 playerPosition) {
        if (!alive) return;

        timeSinceLastShot += deltaTime;

        // Sempre atira se cooldown passou
        if (timeSinceLastShot >= shootCooldown) {
            shoot(playerPosition);
            timeSinceLastShot = 0;
        }
    }

    private void shoot(Vector2 playerPosition) {
        // Calcular direção para o jogador
        Vector2 direction = new Vector2(playerPosition).sub(position).nor();

        // Criar bala (será gerida pelo BulletManager)
        BulletManager.getInstance().createEnemyBullet(position.x, position.y, direction, bulletSpeed);
    }

    public void kill() {
        alive = false;
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public EnemyType getType() { return type; }
    public boolean isAlive() { return alive; }
}