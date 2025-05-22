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
        float bgHeight = 200;
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
        float titleY = centerY + 50; // Slightly above center
        font.draw(game.getBatch(), titleLayout, titleX, titleY);

        // Start game instruction
        font.getData().setScale(1.5f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout startLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "Press SPACE to start a new game");
        float startX = centerX - (startLayout.width / 2);
        float startY = centerY - 50; // Slightly below center
        font.draw(game.getBatch(), startLayout, startX, startY);

        // Controls text
        font.getData().setScale(1);
        com.badlogic.gdx.graphics.g2d.GlyphLayout controlsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, "WASD/Arrows: Move, SPACE/Left Click: Attack, E: Interact");
        float controlsX = centerX - (controlsLayout.width / 2);
        float controlsY = centerY - 150; // Further below center
        font.draw(game.getBatch(), controlsLayout, controlsX, controlsY);

        game.getBatch().end();

        // Check for input to start game
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            game.startNewGame();
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