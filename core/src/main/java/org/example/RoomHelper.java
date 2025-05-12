package org.example;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for placing entities in rooms
 * Ensures entities don't overlap with doors, walls, or each other
 */
public class RoomHelper {
    // Buffer distances
    private static final float WALL_BUFFER = 20f;
    private static final float DOOR_BUFFER = 100f;
    private static final float ENTITY_BUFFER = 15f;

    // Room reference
    private final Room room;

    // List to track occupied spaces
    private final List<Rectangle> occupiedSpaces = new ArrayList<>();

    /**
     * Create a new room helper for a specific room
     * @param room The room to manage
     */
    public RoomHelper(Room room) {
        this.room = room;
        initializeOccupiedSpaces();
    }

    /**
     * Initialize the helper with existing occupied spaces
     * Creates "no spawn zones" around doors and existing obstacles
     */
    private void initializeOccupiedSpaces() {
        // Add door areas to occupied spaces with larger buffer
        for (Door door : room.getDoors().values()) {
            Rectangle doorBounds = door.getBounds();

            Rectangle doorArea;

            doorArea = switch (door.getDirection()) {
                case NORTH -> new Rectangle(
                        doorBounds.x - DOOR_BUFFER / 2,
                        doorBounds.y - DOOR_BUFFER,
                        doorBounds.width + DOOR_BUFFER,
                        doorBounds.height + DOOR_BUFFER
                );
                case SOUTH -> new Rectangle(
                        doorBounds.x - DOOR_BUFFER / 2,
                        doorBounds.y - DOOR_BUFFER / 4,
                        doorBounds.width + DOOR_BUFFER,
                        doorBounds.height + DOOR_BUFFER
                );
                case EAST -> new Rectangle(
                        doorBounds.x - DOOR_BUFFER,
                        doorBounds.y - DOOR_BUFFER / 2,
                        doorBounds.width + DOOR_BUFFER / 4,
                        doorBounds.height + DOOR_BUFFER
                );
                case WEST -> new Rectangle(
                        doorBounds.x - DOOR_BUFFER / 4,
                        doorBounds.y - DOOR_BUFFER / 2,
                        doorBounds.width + DOOR_BUFFER,
                        doorBounds.height + DOOR_BUFFER
                );
            };

            occupiedSpaces.add(doorArea);
        }

        // Add obstacles to occupied spaces
        for (Obstacle obstacle : room.getObstacles()) {
            float halfSize = obstacle.getRadius();
            addOccupiedSpace(
                    obstacle.getX() - halfSize - ENTITY_BUFFER,
                    obstacle.getY() - halfSize - ENTITY_BUFFER,
                    (halfSize + ENTITY_BUFFER) * 2,
                    (halfSize + ENTITY_BUFFER) * 2
            );
        }
    }

    /**
     * Add a rectangular space to the occupied spaces list
     */
    private void addOccupiedSpace(float x, float y, float width, float height) {
        occupiedSpaces.add(new Rectangle(x, y, width, height));
    }

    /**
     * Find a valid position for an entity with the given size
     * @param entityRadius Entity radius/size
     * @return float array of valid position, or null if no valid position found
     */
    public float[] findValidPosition(float entityRadius) {
        return findBiasedPosition(0f, entityRadius); // No bias
    }

    /**
     * Find a valid position with directional bias
     * @param centerBias Bias toward center (positive) or edges (negative), 0 for uniform
     * @param entityRadius Entity radius/size
     * @return float array of valid position, or null if no valid position found
     */
    public float[] findBiasedPosition(float centerBias, float entityRadius) {
        Rectangle roomBounds = room.getBounds();
        int maxAttempts = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate biased coordinates
            float x, y;

            if (centerBias > 0) {
                // Bias toward center
                float roomCenterX = roomBounds.x + roomBounds.width / 2;
                float roomCenterY = roomBounds.y + roomBounds.height / 2;
                float maxDist = Math.min(roomBounds.width, roomBounds.height) / 2 - WALL_BUFFER - entityRadius;

                // Generate distance from center (biased closer to center)
                float distance = maxDist * (1 - MathUtils.random(0, 1) * centerBias);
                float angle = MathUtils.random(0, MathUtils.PI2);

                x = roomCenterX + (float)Math.cos(angle) * distance;
                y = roomCenterY + (float)Math.sin(angle) * distance;
            } else if (centerBias < 0) {
                // Bias toward edges/corners
                float edgeBias = -centerBias; // Convert to positive for calculation

                // Generate coordinates based on edge bias
                if (MathUtils.randomBoolean()) {
                    // Bias toward X edges
                    x = MathUtils.randomBoolean() ?
                            roomBounds.x + WALL_BUFFER + entityRadius + MathUtils.random(0, roomBounds.width / 3) * edgeBias :
                            roomBounds.x + roomBounds.width - WALL_BUFFER - entityRadius - MathUtils.random(0, roomBounds.width / 3) * edgeBias;
                    y = roomBounds.y + WALL_BUFFER + entityRadius + MathUtils.random(roomBounds.height - (WALL_BUFFER * 2) - (entityRadius * 2));
                } else {
                    // Bias toward Y edges
                    x = roomBounds.x + WALL_BUFFER + entityRadius + MathUtils.random(roomBounds.width - (WALL_BUFFER * 2) - (entityRadius * 2));
                    y = MathUtils.randomBoolean() ?
                            roomBounds.y + WALL_BUFFER + entityRadius + MathUtils.random(0, roomBounds.height / 3) * edgeBias :
                            roomBounds.y + roomBounds.height - WALL_BUFFER - entityRadius - MathUtils.random(0, roomBounds.height / 3) * edgeBias;
                }
            } else {
                // No bias (uniform distribution)
                x = roomBounds.x + WALL_BUFFER + entityRadius + MathUtils.random(roomBounds.width - (WALL_BUFFER * 2) - (entityRadius * 2));
                y = roomBounds.y + WALL_BUFFER + entityRadius + MathUtils.random(roomBounds.height - (WALL_BUFFER * 2) - (entityRadius * 2));
            }

            // Check if the position is valid
            if (isPositionValid(x, y, entityRadius)) {
                // Mark this position as occupied
                markPositionOccupied(x, y, entityRadius);
                // Return the valid position
                return new float[]{x, y};
            }
        }

        // Couldn't find a valid position
        return null;
    }

    /**
     * Check if a position is valid, doesn't overlap with existing entities
     * @param x X coordinate
     * @param y Y coordinate
     * @param radius Entity radius
     * @return boolean true if position is valid
     */
    public boolean isPositionValid(float x, float y, float radius) {
        // Create bounds for checking overlap
        Rectangle entityBounds = new Rectangle(
                x - radius - ENTITY_BUFFER,
                y - radius - ENTITY_BUFFER,
                (radius + ENTITY_BUFFER) * 2,
                (radius + ENTITY_BUFFER) * 2
        );

        // Check if the position overlaps with any occupied space
        for (Rectangle occupied : occupiedSpaces) {
            if (occupied.overlaps(entityBounds)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Mark a position as occupied
     * @param x X coordinate
     * @param y Y coordinate
     * @param radius Entity radius
     */
    public void markPositionOccupied(float x, float y, float radius) {
        Rectangle entityBounds = new Rectangle(
                x - radius - ENTITY_BUFFER,
                y - radius - ENTITY_BUFFER,
                (radius + ENTITY_BUFFER) * 2,
                (radius + ENTITY_BUFFER) * 2
        );
        occupiedSpaces.add(entityBounds);
    }

    public boolean positionObstacle(Obstacle obstacle) {
        float[] position = findValidPosition(obstacle.getRadius());

        if (position != null) {
            // Set the obstacle position
            obstacle.setPosition(position[0], position[1]);

            // Mark this position as occupied
            markPositionOccupied(position[0], position[1], obstacle.getRadius());
            return true;
        }

        return false; // Failed to position
    }
}