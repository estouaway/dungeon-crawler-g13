package potatodungeon.managers;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import potatodungeon.entities.Bullet;

public class BulletManager {
    private static BulletManager instance;
    private List<Bullet> bullets;

    private BulletManager() {
        bullets = new ArrayList<>();
    }

    public static BulletManager getInstance() {
        if (instance == null) {
            instance = new BulletManager();
        }
        return instance;
    }

    public void createEnemyBullet(float x, float y, Vector2 direction, float speed) {
        bullets.add(new Bullet(x, y, direction, speed));
    }

    public void update(float deltaTime, Vector2 playerPosition, List<Vector2> obstacles, float roomWidth, float roomHeight) {
        Iterator<Bullet> iterator = bullets.iterator();

        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();

            if (!bullet.isActive()) {
                iterator.remove();
                continue;
            }

            bullet.update(deltaTime);
            Vector2 bulletPos = bullet.getPosition();

            // Verificar colisão com paredes da sala
            if (bulletPos.x < 0 || bulletPos.x > roomWidth ||
                    bulletPos.y < 0 || bulletPos.y > roomHeight) {
                bullet.destroy();
                iterator.remove();
                continue;
            }

            // Verificar colisão com jogador (assumindo raio de 15 pixels)
            if (bulletPos.dst(playerPosition) < 15f) {
                bullet.destroy();
                iterator.remove();
                // Aqui podes adicionar lógica de dano ao jogador
                System.out.println("Player hit by bullet!");
                continue;
            }

            // Verificar colisão com obstáculos
            boolean hitObstacle = false;
            for (Vector2 obstacle : obstacles) {
                if (bulletPos.dst(obstacle) < 20f) { // Assumindo obstáculos com raio de 20
                    bullet.destroy();
                    hitObstacle = true;
                    break;
                }
            }

            if (hitObstacle) {
                iterator.remove();
            }
        }
    }

    public List<Bullet> getBullets() {
        return new ArrayList<>(bullets);
    }

    public void clearBullets() {
        bullets.clear();
    }
}