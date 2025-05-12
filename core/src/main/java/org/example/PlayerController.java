package org.example;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Class for handling player input and movement
 */
public class PlayerController {
    private final Player player;
    private final DungeonLevel level;

    public PlayerController(Player player, DungeonLevel level) {
        this.player = player;
        this.level = level;
    }

    public void update(float delta) {
        float speed = 200.0f * delta;
        boolean moved = false;

        // Handle keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player.moveY(speed);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.moveY(-speed);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.moveX(-speed);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.moveX(speed);
            moved = true;
        }

        if (moved) {
            // Keep player within room boundaries
            level.constrainPlayerToRoom(player);
        }
    }
}