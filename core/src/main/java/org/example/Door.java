package org.example;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a door between rooms
 */
public class Door {
    private int x, y;
    private int width, height;
    private final Room.Direction direction;
    private final Room connectedRoom;
    private Color color;

    /**
     * Create a new door
     */
    public Door(int room_x, int room_y, int width, int height, Room.Direction direction, Room connectedRoom) {
        this.direction = direction;
        this.connectedRoom = connectedRoom;

        final int DOOR_SIZE = 60;

        switch (direction) {
            case NORTH:
                this.x = room_x + width / 2 - DOOR_SIZE / 2;
                this.y = room_y + height - 5;
                this.width = DOOR_SIZE;
                this.height = 10;
                break;
            case EAST:
                this.x = room_x + width - 5;
                this.y = room_y + height / 2 - DOOR_SIZE / 2;
                this.width = 10;
                this.height = DOOR_SIZE;
                break;
            case SOUTH:
                this.x = room_x + width / 2 - DOOR_SIZE / 2;
                this.y = room_y - 5;
                this.width = DOOR_SIZE;
                this.height = 10;
                break;
            case WEST:
                this.x = room_x - 5;
                this.y = room_y + height / 2 - DOOR_SIZE / 2;
                this.width = 10;
                this.height = DOOR_SIZE;
                break;
        }

        // Assign color based on direction
        //maybe remove this
        setColorByDirection();
    }

    /**
     * Set the door color based on its direction
     */
    private void setColorByDirection() {
        switch (direction) {
            case NORTH:
                color = Color.BLUE;
                break;
            case EAST:
                color = Color.GREEN;
                break;
            case SOUTH:
                color = Color.ORANGE;
                break;
            case WEST:
                color = Color.PINK;
                break;
            default:
                color = Color.GRAY;
        }
    }

    /**
     * Get the bounds of the door for collision detection
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Get the door color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the door direction
     */
    public Room.Direction getDirection() {
        return direction;
    }

    /**
     * Get the room this door connects to
     */
    public Room getConnectedRoom() {
        return connectedRoom;
    }
}