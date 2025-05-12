package org.example;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Room class representing a single dungeon room
public class Room {

    // Fixed room dimensions to fit window with UI margins
    private static final int ROOM_WIDTH = DungeonCrawler.WINDOW_WIDTH - 40; // 20px margin on each side
    private static final int ROOM_HEIGHT = DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - DungeonCrawler.BOTTOM_UI_HEIGHT - 40;

    // Room position (centered in window)
    private static final int ROOM_X = (DungeonCrawler.WINDOW_WIDTH - ROOM_WIDTH) / 2;
    private static final int ROOM_Y = DungeonCrawler.BOTTOM_UI_HEIGHT + 20;

    private int id;
    private Rectangle bounds; // Room boundaries
    private List<Obstacle> obstacles; // Obstacles like pillars
    private Map<Direction, Door> doors; // Doors on each side
    private DungeonLevel.RoomType roomType; // Room type for variety
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

        // Generate a slightly randomized floor color
        this.floorColor = new Color(
                MathUtils.random(0.1f, 0.2f),
                MathUtils.random(0.1f, 0.2f),
                MathUtils.random(0.25f, 0.35f),
                1.0f
        );
    }

    // Add a door in specified direction
    public void addDoor(Direction direction, Room connectedRoom) {
        Door door = new Door(ROOM_X, ROOM_Y, ROOM_WIDTH, ROOM_HEIGHT, direction, connectedRoom);
        doors.put(direction, door);
    }

    // Generate random obstacles in the room
    public void generateObstacles(int minObstacles, int maxObstacles) {
        // Clear existing obstacles
        obstacles.clear();

        // Create a helper for this room
        RoomHelper helper = new RoomHelper(this);

        // Random number of obstacles
        int numObstacles = MathUtils.random(minObstacles, maxObstacles);

        // Delegate obstacle creation to the helper
        for (int i = 0; i < numObstacles; i++) {
            Obstacle obstacle = createObstacleForRoomType();

            // Let helper find position and add the obstacle
            if (!helper.positionObstacle(obstacle)) {
                // If positioning failed, stop adding more
                break;
            }

            // Add to our list
            obstacles.add(obstacle);
        }
    }

    // Helper method to create obstacles based on room type
    private Obstacle createObstacleForRoomType() {
        // Determine size based on room type
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

        // Determine shape based on room type
        boolean isSquare = MathUtils.randomBoolean(0.7f); // 70% chance of squares

        if (roomType == DungeonLevel.RoomType.SPECIAL) {
            isSquare = MathUtils.randomBoolean(0.3f); // 30% chance of squares in special rooms
        }

        // Create the obstacle (without position yet)
        return new Obstacle(0, 0, obstacleSize, isSquare);
    }

    // Render the room and its contents
    public void render(ShapeRenderer shapeRenderer) {
        renderFloor(shapeRenderer);
        renderBorders(shapeRenderer);
        renderDoors(shapeRenderer);
        renderObstacles(shapeRenderer);
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

    // review this
    private void drawObstacle(ShapeRenderer shapeRenderer, Obstacle obstacle, boolean isOutline) {
        if (obstacle.isSquare()) {
            // square
            float halfSize = obstacle.getRadius();
            shapeRenderer.rect(obstacle.getX() - halfSize, obstacle.getY() - halfSize,
                    halfSize * 2, halfSize * 2);
        } else {
            // circle
            shapeRenderer.circle(obstacle.getX(), obstacle.getY(), obstacle.getRadius());
        }
    }

    // Check collisions with room boundaries CHANGE THIS TO PLAYER RESPONSABILITY
    public void constrainPlayer(Player player) {
        float playerX = player.getX();
        float playerY = player.getY();
        float playerRadius = player.getRadius();

        // Check if player is in any door first
        boolean inDoor = false;
        for (Door door : doors.values()) {
            Rectangle doorBounds = door.getBounds();
            // Use slightly expanded bounds for door detection
            Rectangle expandedDoorBounds = new Rectangle(
                    doorBounds.x - 10,
                    doorBounds.y - 10,
                    doorBounds.width + 20,
                    doorBounds.height + 20
            );

            if (expandedDoorBounds.contains(playerX, playerY)) {
                inDoor = true;
                break;
            }
        }

        if (!inDoor) {
            // Left wall
            if (playerX - playerRadius < bounds.x) {
                player.setPosition(bounds.x + playerRadius, playerY);
            }
            // Right wall
            if (playerX + playerRadius > bounds.x + bounds.width) {
                player.setPosition(bounds.x + bounds.width - playerRadius, playerY);
            }
            // Bottom wall
            if (playerY - playerRadius < bounds.y) {
                player.setPosition(playerX, bounds.y + playerRadius);
            }
            // Top wall
            if (playerY + playerRadius > bounds.y + bounds.height) {
                player.setPosition(playerX, bounds.y + bounds.height - playerRadius);
            }
        }

        // Check obstacle collisions
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isSquare()) {
                // Square collision
                float halfSize = obstacle.getRadius();
                float obstacleLeft = obstacle.getX() - halfSize;
                float obstacleRight = obstacle.getX() + halfSize;
                float obstacleBottom = obstacle.getY() - halfSize;
                float obstacleTop = obstacle.getY() + halfSize;

                // Expanded rectangle for collision (includes player radius)
                Rectangle expandedObstacle = new Rectangle(
                        obstacleLeft - playerRadius,
                        obstacleBottom - playerRadius,
                        halfSize * 2 + playerRadius * 2,
                        halfSize * 2 + playerRadius * 2
                );

                if (expandedObstacle.contains(playerX, playerY)) {
                    // Find the closest edge to push player out
                    float leftDist = Math.abs(playerX - obstacleLeft);
                    float rightDist = Math.abs(playerX - obstacleRight);
                    float topDist = Math.abs(playerY - obstacleTop);
                    float bottomDist = Math.abs(playerY - obstacleBottom);

                    // Find minimum distance
                    float minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));

                    // Push player in direction of minimum distance
                    if (minDist == leftDist) {
                        player.setPosition(obstacleLeft - playerRadius, playerY);
                    } else if (minDist == rightDist) {
                        player.setPosition(obstacleRight + playerRadius, playerY);
                    } else if (minDist == topDist) {
                        player.setPosition(playerX, obstacleTop + playerRadius);
                    } else if (minDist == bottomDist) {
                        player.setPosition(playerX, obstacleBottom - playerRadius);
                    }
                }
            } else {
                // Circle collision
                float dx = playerX - obstacle.getX();
                float dy = playerY - obstacle.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float minDistance = playerRadius + obstacle.getRadius();

                if (distance < minDistance) {
                    // Push player away from obstacle
                    float angle = (float) Math.atan2(dy, dx);
                    float pushX = obstacle.getX() + (float) Math.cos(angle) * minDistance;
                    float pushY = obstacle.getY() + (float) Math.sin(angle) * minDistance;
                    player.setPosition(pushX, pushY);
                }
            }
        }

    }

    public int getId() { return id; }
    public Rectangle getBounds() { return bounds; }
    public Map<Direction, Door> getDoors() { return doors; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public DungeonLevel.RoomType getRoomType() { return roomType; }

    public void setRoomType(DungeonLevel.RoomType type) {
        this.roomType = type;
    }
}