package potatodungeon.managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import potatodungeon.world.Room;
import potatodungeon.world.DungeonLevel;
import potatodungeon.generation.RoomHelper;
import potatodungeon.entities.Enemy;

import java.util.ArrayList;
import java.util.List;

public class EnemyManager {
    private List<Enemy> enemies;

    public EnemyManager() {
        enemies = new ArrayList<>();
    }

    public void generateEnemiesForRoom(Room room) {
        DungeonLevel.RoomType roomType = room.getRoomType();

        int enemyCount = switch (roomType) {
            case EMPTY -> MathUtils.random(0, 1);
            case OBSTACLE_LIGHT -> MathUtils.random(1, 2);
            case OBSTACLE_MEDIUM -> MathUtils.random(2, 3);
            case OBSTACLE_HEAVY -> MathUtils.random(3, 4);
            case SPECIAL -> MathUtils.random(4, 5);
        };

        // Usar RoomHelper para posicionamento sem sobreposições
        RoomHelper roomHelper = new RoomHelper(room);

        for (int i = 0; i < enemyCount; i++) {
            Enemy.EnemyType enemyType = selectRandomEnemyType();

            // Usar RoomHelper para encontrar posição válida
            float[] position = roomHelper.findValidPosition(15f); // 15f = raio do inimigo

            if (position != null) {
                Enemy enemy = new Enemy(position[0], position[1], enemyType);
                enemies.add(enemy);
            } else {
                // Se não conseguir posicionar, não adiciona inimigo
                System.out.println("Warning: Could not place enemy in room - no valid position found");
            }
        }
    }

    private Enemy.EnemyType selectRandomEnemyType() {
        Enemy.EnemyType[] types = Enemy.EnemyType.values();
        return types[MathUtils.random(types.length - 1)];
    }

    public void update(float deltaTime, Vector2 playerPosition) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, playerPosition);
        }

        // Remover inimigos mortos
        enemies.removeIf(enemy -> !enemy.isAlive());
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public void clearEnemies() {
        enemies.clear();
    }

    public int getAliveEnemyCount() {
        return (int) enemies.stream().filter(Enemy::isAlive).count();
    }
}