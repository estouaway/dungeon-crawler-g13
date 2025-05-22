package potatodungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.screens.GameScreen;
import potatodungeon.screens.TitleScreen;

/**
 * Main game class
 */
public class DungeonCrawler extends Game {
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final int TOP_UI_HEIGHT = 50;
    public static final int BOTTOM_UI_HEIGHT = 20;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        setScreen(new TitleScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();

        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    /**
     * Start a new game
     */
    public void startNewGame() {
        setScreen(new GameScreen(this));
    }

    /**
     * Return to the title screen
     */
    public void returnToTitle() {
        setScreen(new TitleScreen(this));
    }

    /**
     * Get the SpriteBatch for rendering
     */
    public SpriteBatch getBatch() {
        return batch;
    }
}