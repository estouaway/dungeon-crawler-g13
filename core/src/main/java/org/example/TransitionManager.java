package org.example;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Class for managing room transitions
 */
public class TransitionManager {
    private static final float TRANSITION_DURATION = 0.5f;

    private final DungeonLevel level;
    private final Player player;

    private Room transitionFromRoom;
    private Room.Direction transitionDirection;
    private float transitionTimer = 0;

    /**
     * Create a new room helper for a specific room
     * @param level Dungeon level
     * @param player The player
     */
    public TransitionManager(DungeonLevel level, Player player) {
        this.level = level;
        this.player = player;
    }

    public void startTransition(Door door, Room fromRoom) {
        this.transitionTimer = 0;
        this.transitionDirection = door.getDirection();
        this.transitionFromRoom = fromRoom;

        System.out.println("Starting transition to new room via " + transitionDirection + " door");
    }

    /**
     * Update transition state
     * @return true if transition is complete
     */
    public boolean update(float delta) {
        transitionTimer += delta;

        if (transitionTimer >= TRANSITION_DURATION) {
            // Move to new room
            Door door = transitionFromRoom.getDoors().get(transitionDirection);
            level.transitionToRoom(door, player);
            return true;
        }

        return false;
    }

    public void render(ShapeRenderer shapeRenderer) {
        // Calculate transition progress
        float progress = Math.min(transitionTimer / TRANSITION_DURATION, 1.0f);

        // Draw current level
        level.render(shapeRenderer);

        // Draw player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(player.getX(), player.getY(), player.getRadius());
        shapeRenderer.end();

        // Draw fade overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, progress);
        shapeRenderer.rect(0, 0, DungeonCrawler.WINDOW_WIDTH, DungeonCrawler.WINDOW_HEIGHT);
        shapeRenderer.end();
    }
}