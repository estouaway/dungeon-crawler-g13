package org.example;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the overall dungeon structure
 */
public class DungeonLevel {
    private List<Room> rooms;
    private Room currentRoom;

    // Change may to enemy, no_enemy and boss?
    public enum RoomType {
        EMPTY, OBSTACLE_LIGHT, OBSTACLE_MEDIUM, OBSTACLE_HEAVY, SPECIAL
    }

    private final List<DungeonObserver> observers = new ArrayList<>();

    /**
     * Creates a new dungeon level
     */
    public DungeonLevel() {
        rooms = new ArrayList<>();
    }

    /**
     * Add an observer to be notified of dungeon events
     */
    public void addObserver(DungeonObserver observer) {
        observers.add(observer);
    }

    /**
     * Generate a new dungeon
     */
    public void generate() {
        rooms.clear();

        // BSP generator
        DungeonGenerator generator = new DungeonGenerator();

        // Generate the rooms
        rooms = generator.generateDungeon();

        // Log the generation results for debugging
        System.out.println("Generated " + rooms.size() + " rooms");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            System.out.println("Room " + i + ": Type: " + room.getRoomType() +
                    " - Doors: " + room.getDoors().size());
        }

        // Set current room to the first room
        if (!rooms.isEmpty()) {
            currentRoom = rooms.getFirst();

            // Notify observers of new room
            for (DungeonObserver observer : observers) {
                observer.onRoomChange(currentRoom);
            }
        }
    }

    /**
     * Render the current room
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (currentRoom != null) {
            currentRoom.render(shapeRenderer);
        }
    }

    /**
     * Constrain player to current room boundaries
     */
    public void constrainPlayerToRoom(Player player) {
        if (currentRoom != null) {
            currentRoom.constrainPlayer(player);
        }
    }

    /**
     * Check if player is colliding with a door
     */
    public Door checkDoorCollision(Player player) {
        if (currentRoom == null) {
            return null;
        }

        // Check each door in the current room
        for (Door door : currentRoom.getDoors().values()) {
            Rectangle doorBounds = door.getBounds();
            float playerX = player.getX();
            float playerY = player.getY();

            // Expanded door bounds for better detection
            Rectangle expandedBounds = new Rectangle(
                    doorBounds.x - 10,
                    doorBounds.y - 10,
                    doorBounds.width + 20,
                    doorBounds.height + 20
            );

            if (expandedBounds.contains(playerX, playerY)) {
                return door;
            }
        }

        return null;
    }

    /**
     * Transition player to a new room through a door
     */
    public void transitionToRoom(Door door, Player player) {
        Room nextRoom = door.getConnectedRoom();

        if (nextRoom != null) {
            // Get opposite direction door
            Room.Direction oppositeDirection = getOppositeDirection(door.getDirection());
            Door targetDoor = nextRoom.getDoors().get(oppositeDirection);

            // Position player near the target door
            if (targetDoor != null) {
                Rectangle targetBounds = targetDoor.getBounds();
                float newX = targetBounds.x + targetBounds.width / 2;
                float newY = targetBounds.y + targetBounds.height / 2;

                // Move player away from door slightly
                switch (oppositeDirection) {
                    case NORTH:
                        newY -= player.getRadius() * 3;
                        break;
                    case EAST:
                        newX -= player.getRadius() * 3;
                        break;
                    case SOUTH:
                        newY += player.getRadius() * 3;
                        break;
                    case WEST:
                        newX += player.getRadius() * 3;
                        break;
                }

                // Set player position
                player.setPosition(newX, newY);

                // Update current room
                currentRoom = nextRoom;

                // Notify observers
                for (DungeonObserver observer : observers) {
                    observer.onRoomChange(currentRoom);
                }

                System.out.println("Transitioned to room type: " + currentRoom.getRoomType());
            }
        }
    }

    /**
     * Get the opposite direction
     */
    private Room.Direction getOppositeDirection(Room.Direction direction) {
        return switch (direction) {
            case NORTH -> Room.Direction.SOUTH;
            case EAST -> Room.Direction.WEST;
            case SOUTH -> Room.Direction.NORTH;
            case WEST -> Room.Direction.EAST;
        };
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public List<Room> getRooms() {
        return rooms;
    }
}