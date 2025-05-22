package potatodungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import potatodungeon.entities.Player;
import potatodungeon.world.DungeonLevel;
import potatodungeon.world.Room;

/**
 * Class responsible for rendering UI elements
 */
public class GameUI {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final DungeonLevel level;
    private final Player player;

    public GameUI(SpriteBatch batch, DungeonLevel level, Player player) {
        this.batch = batch;
        this.level = level;
        this.player = player;

        this.font = new BitmapFont();
        this.font.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );
    }

    public void render(boolean debugMode) {
        batch.begin();

        // Draw text in top UI bar
        font.setColor(Color.WHITE);
        font.draw(batch, "Room " + level.getCurrentRoom().getId() + " Type: " + level.getCurrentRoom().getRoomType(),
                10, DungeonCrawler.WINDOW_HEIGHT - 15);
        font.draw(batch, "Press ESC for title screen",
                DungeonCrawler.WINDOW_WIDTH - 200, DungeonCrawler.WINDOW_HEIGHT - 15);
        font.draw(batch, "F8: Toggle Map",
                DungeonCrawler.WINDOW_WIDTH - 380, DungeonCrawler.WINDOW_HEIGHT - 15);

        // Draw debug info if enabled
        if (debugMode) {
            renderDebugInfo();
        }

        batch.end();
    }

    private void renderDebugInfo() {
        font.setColor(Color.YELLOW);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(),
                10, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - 10);
        font.draw(batch, "Player: (" + (int)player.getX() + ", " + (int)player.getY() + ")",
                10, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - 30);

        // Show door info
        int doorCount = level.getCurrentRoom().getDoors().size();
        font.draw(batch, "Doors: " + doorCount,
                10, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - 50);

        // List door directions
        StringBuilder doorDirs = new StringBuilder("Directions: ");
        for (Room.Direction dir : level.getCurrentRoom().getDoors().keySet()) {
            doorDirs.append(dir).append(" ");
        }
        font.draw(batch, doorDirs.toString(),
                10, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - 70);

        font.draw(batch, "F1: Toggle debug",
                10, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - 90);
    }

    public void dispose() {
        font.dispose();
    }
}