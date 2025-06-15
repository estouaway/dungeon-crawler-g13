package potatodungeon.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import potatodungeon.entities.Obstacle;
import potatodungeon.entities.Player;
import potatodungeon.DungeonCrawler;
import potatodungeon.generation.RoomHelper;
import potatodungeon.entities.Enemy;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Room class representing a single dungeon room
public class Room {

    private static final int ROOM_WIDTH = DungeonCrawler.WINDOW_WIDTH - 40; // 20px margin on each side
    private static final int ROOM_HEIGHT = DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - DungeonCrawler.BOTTOM_UI_HEIGHT - 40;

    // Room position (centered in window)
    private static final int ROOM_X = (DungeonCrawler.WINDOW_WIDTH - ROOM_WIDTH) / 2;
    private static final int ROOM_Y = DungeonCrawler.BOTTOM_UI_HEIGHT + 20;

    private int id;
    private Rectangle bounds;
    private List<Obstacle> obstacles;
    private List<Enemy> enemies = new ArrayList<>();
    private Map<Direction, Door> doors;
    private DungeonLevel.RoomType roomType;
    private Color floorColor;

    // Enum for door directions
    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    public Room(int id) {
        this.id = id;
        this.bounds = new Rectangle(ROOM_X, ROOM_Y, ROOM_WIDTH, ROOM_HEIGHT);
        this.obstacles = new ArrayList<>();
        this.doors = new HashMap<>();
        this.roomType = DungeonLevel.RoomType.EMPTY;

        // maybe floor colors for each type of room?
        this.floorColor = new Color(0.15f, 0.15f, 0.3f, 1f);
    }

    public void addDoor(Direction direction, Room connectedRoom) {
        Door door = new Door(ROOM_X, ROOM_Y, ROOM_WIDTH, ROOM_HEIGHT, direction, connectedRoom);
        doors.put(direction, door);
    }

    // Generate random obstacles in the room
    public void generateObstacles(int minObstacles, int maxObstacles) {
        obstacles.clear();

        RoomHelper helper = new RoomHelper(this);

        int numObstacles = MathUtils.random(minObstacles, maxObstacles);

        for (int i = 0; i < numObstacles; i++) {
            Obstacle obstacle = createObstacleForRoomType();

            if (!helper.positionObstacle(obstacle)) {
                break;
            }

            obstacles.add(obstacle);
        }
    }

    public void generateEnemies(int minEnemies, int maxEnemies) {
        enemies.clear();

        int enemyCount = MathUtils.random(minEnemies, maxEnemies);
        RoomHelper roomHelper = new RoomHelper(this);

        for (int i = 0; i < enemyCount; i++) {
            Enemy.EnemyType enemyType = selectRandomEnemyType();

            // Usar RoomHelper para encontrar posição válida
            float[] position = roomHelper.findValidPosition(15f); // 15f = raio do inimigo

            if (position != null) {
                Enemy enemy = new Enemy(position[0], position[1], enemyType);
                enemies.add(enemy);
            } else {
                System.out.println("Warning: Could not place enemy in room - no valid position found");
            }
        }
    }

    private Enemy.EnemyType selectRandomEnemyType() {
        Enemy.EnemyType[] types = Enemy.EnemyType.values();
        return types[MathUtils.random(types.length - 1)];
    }


    // Helper method to create obstacles based on room type
    // example logic for enemies
    private Obstacle createObstacleForRoomType() {
        int minSize = 20;
        int maxSize = 40;

        if (roomType == DungeonLevel.RoomType.OBSTACLE_HEAVY) {
            minSize = 25;
            maxSize = 45;
        } else if (roomType == DungeonLevel.RoomType.OBSTACLE_LIGHT) {
            minSize = 15;
            maxSize = 30;
        }

        int obstacleSize = MathUtils.random(minSize, maxSize);

        // Create the obstacle (without position yet)
        return new Obstacle(0, 0, obstacleSize);
    }

    // Render the room and its contents
    public void render(ShapeRenderer shapeRenderer) {
        renderFloor(shapeRenderer);
        renderBorders(shapeRenderer);
        renderDoors(shapeRenderer);
        renderObstacles(shapeRenderer);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                Vector2 pos = enemy.getPosition();

                // Cor baseada no tipo
                switch (enemy.getType()) {
                    case FAST_SHOOTER:
                        shapeRenderer.setColor(1f, 0f, 0f, 1f); // Vermelho
                        break;
                    case NORMAL_SHOOTER:
                        shapeRenderer.setColor(1f, 0.5f, 0f, 1f); // Laranja
                        break;
                    case SLOW_SHOOTER:
                        shapeRenderer.setColor(0.8f, 0f, 0.8f, 1f); // Roxo
                        break;
                }

                shapeRenderer.circle(pos.x, pos.y, 15f);
            }
        }
        shapeRenderer.end();
    }

    private void renderFloor(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(floorColor);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();
    }

    private void renderBorders(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Color borderColor = getRoomBorderColor();
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();
    }

    private Color getRoomBorderColor() {
        return switch (roomType) {
            case EMPTY -> new Color(0.6f, 0.6f, 0.7f, 1);
            case OBSTACLE_LIGHT -> new Color(0.6f, 0.7f, 0.6f, 1);
            case OBSTACLE_MEDIUM -> new Color(0.7f, 0.6f, 0.5f, 1);
            case OBSTACLE_HEAVY -> new Color(0.7f, 0.5f, 0.5f, 1);
            case SPECIAL -> new Color(0.8f, 0.7f, 0.2f, 1);
        };
    }

    private void renderDoors(ShapeRenderer shapeRenderer) {
        // draw door fill
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Door door : doors.values()) {
            shapeRenderer.setColor(door.getColor());
            Rectangle doorBounds = door.getBounds();
            shapeRenderer.rect(doorBounds.x, doorBounds.y, doorBounds.width, doorBounds.height);
        }
        shapeRenderer.end();

        // draw door borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (Door door : doors.values()) {
            Rectangle doorBounds = door.getBounds();
            shapeRenderer.rect(doorBounds.x, doorBounds.y, doorBounds.width, doorBounds.height);
        }
        shapeRenderer.end();
    }

    private void renderObstacles(ShapeRenderer shapeRenderer) {
        // draw obstacle filling
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Obstacle obstacle : obstacles) {
            shapeRenderer.setColor(getObstacleColor());
            drawObstacle(shapeRenderer, obstacle, false);
        }
        shapeRenderer.end();

        // draw borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        for (Obstacle obstacle : obstacles) {
            drawObstacle(shapeRenderer, obstacle, true);
        }
        shapeRenderer.end();
    }

    private Color getObstacleColor() {
        return switch (roomType) {
            case EMPTY, OBSTACLE_LIGHT -> new Color(0.4f, 0.4f, 0.5f, 1);
            case OBSTACLE_MEDIUM -> new Color(0.45f, 0.4f, 0.35f, 1);
            case OBSTACLE_HEAVY -> new Color(0.5f, 0.3f, 0.3f, 1);
            case SPECIAL -> new Color(0.6f, 0.5f, 0.2f, 1);
            default -> new Color(0.5f, 0.5f, 0.5f, 1);
        };
    }

    private void drawObstacle(ShapeRenderer shapeRenderer, Obstacle obstacle, boolean isOutline) {
        float halfSize = obstacle.getRadius();
        shapeRenderer.rect(obstacle.getX() - halfSize, obstacle.getY() - halfSize,
                halfSize * 2, halfSize * 2);
    }

    public void constrainPlayer(Player player) {
        float playerX = player.getX();
        float playerY = player.getY();
        float playerRadius = player.getRadius();

        // verifica se jogador esta numa porta, so restringe movimento caso contrario
        boolean inDoor = isPlayerInDoor(playerX, playerY);

        if (!inDoor) {
            constrainToWalls(player, playerX, playerY, playerRadius);
        }

        constrainToObstacles(player, playerX, playerY, playerRadius);
    }

    private boolean isPlayerInDoor(float playerX, float playerY) {
        for (Door door : doors.values()) {
            Rectangle doorBounds = door.getBounds();
            // expande ligeiramente a detecao da porta
            Rectangle expandedDoorBounds = new Rectangle(
                    doorBounds.x - 10,
                    doorBounds.y - 10,
                    doorBounds.width + 20,
                    doorBounds.height + 20
            );

            if (expandedDoorBounds.contains(playerX, playerY)) {
                return true;
            }
        }
        return false;
    }

    private void constrainToWalls(Player player, float playerX, float playerY, float playerRadius) {
        float newX = playerX;
        float newY = playerY;

        if (playerX - playerRadius < bounds.x) {
            newX = bounds.x + playerRadius;
        }
        if (playerX + playerRadius > bounds.x + bounds.width) {
            newX = bounds.x + bounds.width - playerRadius;
        }
        if (playerY - playerRadius < bounds.y) {
            newY = bounds.y + playerRadius;
        }
        if (playerY + playerRadius > bounds.y + bounds.height) {
            newY = bounds.y + bounds.height - playerRadius;
        }

        if (newX != playerX || newY != playerY) {
            player.setPosition(newX, newY);
        }
    }

    private void constrainToObstacles(Player player, float playerX, float playerY, float playerRadius) {
        for (Obstacle obstacle : obstacles) {
            float halfSize = obstacle.getRadius();
            float obstacleLeft = obstacle.getX() - halfSize;
            float obstacleRight = obstacle.getX() + halfSize;
            float obstacleBottom = obstacle.getY() - halfSize;
            float obstacleTop = obstacle.getY() + halfSize;

            // Encontrar o ponto mais próximo do quadrado ao centro do jogador
            float closestX = Math.max(obstacleLeft, Math.min(playerX, obstacleRight));
            float closestY = Math.max(obstacleBottom, Math.min(playerY, obstacleTop));

            // Calcular distância do centro do jogador ao ponto mais próximo
            float dx = playerX - closestX;
            float dy = playerY - closestY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // Se a distância for menor que o raio do jogador, há colisão
            if (distance < playerRadius) {
                // Empurrar jogador para fora
                if (distance > 0) {
                    float pushX = (dx / distance) * (playerRadius - distance);
                    float pushY = (dy / distance) * (playerRadius - distance);
                    player.setPosition(playerX + pushX, playerY + pushY);
                }
            }
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getId() {
        return id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Map<Direction, Door> getDoors() {
        return doors;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public DungeonLevel.RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(DungeonLevel.RoomType type) {
        this.roomType = type;
    }
}