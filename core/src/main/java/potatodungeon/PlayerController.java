package potatodungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import potatodungeon.entities.Player;
import potatodungeon.world.DungeonLevel;

/**
 * Class que contrala o movimento do jogador
 */
public class PlayerController {
    private final Player player;
    private final DungeonLevel level;

    public PlayerController(Player player, DungeonLevel level) {
        this.player = player;
        this.level = level;
    }

    public void update(float delta) {
        boolean moved = false;

        // Handle keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player.moveUp(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.moveDown(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.moveLeft(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.moveRight(delta);
            moved = true;
        }

        if (moved) {
            // Keep player within room boundaries
            level.constrainPlayerToRoom(player);
        }
    }
}