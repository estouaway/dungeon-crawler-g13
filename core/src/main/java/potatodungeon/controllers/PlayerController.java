package potatodungeon.controllers;

import com.badlogic.gdx.Gdx;
import potatodungeon.entities.Player;
import potatodungeon.world.DungeonLevel;
import potatodungeon.managers.ConfigurationManager;

/**
 * Class que contrala o movimento do jogador
 */
public class PlayerController {
    private final Player player;
    private final DungeonLevel level;
    private final ConfigurationManager config;

    public PlayerController(Player player, DungeonLevel level) {
        this.player = player;
        this.level = level;
        this.config = ConfigurationManager.getInstance();
    }

    public void update(float delta) {
        boolean moved = false;

        // inputs de tecladåo usando as teclas configuradas
        if (Gdx.input.isKeyPressed(config.getUpKey())) {
            player.moveUp(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(config.getDownKey())) {
            player.moveDown(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(config.getLeftKey())) {
            player.moveLeft(delta);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(config.getRightKey())) {
            player.moveRight(delta);
        }

        if (moved) {
            // matenm jogador dentro dos limites da sala
            level.constrainPlayerToRoom(player);
        }
    }
}