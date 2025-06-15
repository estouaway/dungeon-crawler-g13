package potatodungeon.screens;

import com.badlogic.gdx.graphics.Color;
import potatodungeon.DungeonCrawler;

public class TitleScreen implements com.badlogic.gdx.Screen {
    private final DungeonCrawler game;
    private com.badlogic.gdx.graphics.g2d.BitmapFont font;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    public TitleScreen(DungeonCrawler game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(2);
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        com.badlogic.gdx.Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        com.badlogic.gdx.Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        // Calculate center positions
        float centerX = DungeonCrawler.WINDOW_WIDTH / 2f;
        float centerY = DungeonCrawler.WINDOW_HEIGHT / 2f;

        // Draw title background
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);

        // Calculate background dimensions
        float bgWidth = 600;
        float bgHeight = 300; // Made taller to accommodate settings option
        float bgX = centerX - (bgWidth / 2);
        float bgY = centerY - (bgHeight / 2);

        shapeRenderer.rect(bgX, bgY, bgWidth, bgHeight);
        shapeRenderer.end();

        // Draw title text
        game.getBatch().begin();
        font.setColor(Color.WHITE);

        // Set font scale for title
        font.getData().setScale(2);

        // Calculate text position for proper centering
        // DUNGEON CRAWLER
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "DUNGEON CRAWLER");
        float titleX = centerX - (titleLayout.width / 2);
        float titleY = centerY + 80; // Moved up to make room for settings
        font.draw(game.getBatch(), titleLayout, titleX, titleY);

        // Start game instruction
        font.getData().setScale(1.5f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout startLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "Press SPACE to start a new game");
        float startX = centerX - (startLayout.width / 2);
        float startY = centerY + 10; // Moved up
        font.draw(game.getBatch(), startLayout, startX, startY);

        // Settings instruction
        com.badlogic.gdx.graphics.g2d.GlyphLayout settingsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "Press S for Settings");
        float settingsX = centerX - (settingsLayout.width / 2);
        float settingsY = centerY - 30; // Below start game
        font.draw(game.getBatch(), settingsLayout, settingsX, settingsY);

        // Controls text
        font.getData().setScale(1);
        com.badlogic.gdx.graphics.g2d.GlyphLayout controlsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "Current Controls: WASD/Arrows: Move, SPACE: Attack");
        float controlsX = centerX - (controlsLayout.width / 2);
        float controlsY = centerY - 80; // Further below
        font.draw(game.getBatch(), controlsLayout, controlsX, controlsY);

        // Current player color indicator
        font.getData().setScale(1);
        String colorText = "Player Color: " + potatodungeon.managers.ConfigurationManager.getInstance().getColorName(potatodungeon.managers.ConfigurationManager.getInstance().getPlayerColor());
        com.badlogic.gdx.graphics.g2d.GlyphLayout colorLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, colorText);
        float colorX = centerX - (colorLayout.width / 2);
        float colorY = centerY - 110; // Below controls
        font.setColor(potatodungeon.managers.ConfigurationManager.getInstance().getPlayerColor());
        font.draw(game.getBatch(), colorLayout, colorX, colorY);

        game.getBatch().end();

        // Check for input to start game
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            game.startNewGame();
        }

        // Check for input to open settings
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.S)) {
            game.setScreen(new SettingsScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}