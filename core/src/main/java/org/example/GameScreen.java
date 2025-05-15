package org.example;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Main game screen handling rendering and game logic
 */
public class GameScreen implements Screen, IDungeonObserver {
    // Game state enum
    public enum GameState {
        PLAYING, TRANSITIONING
    }

    private final DungeonCrawler game;
    private ShapeRenderer shapeRenderer;
    private Player player;
    private DungeonLevel currentLevel;

    // Game state
    private GameState gameState = GameState.PLAYING;

    // Components
    private GameUI gameUI;
    private MinimapRenderer minimapRenderer;
    private TransitionManager transitionManager;
    private PlayerController playerController;

    // UI states
    private boolean debugMode = false;
    private boolean showMinimap = false;

    /**
     * Constructor
     */
    public GameScreen(DungeonCrawler game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        player = new Player(
                DungeonCrawler.WINDOW_WIDTH / 2,
                DungeonCrawler.WINDOW_HEIGHT / 2, 15);

        currentLevel = new DungeonLevel();
        currentLevel.addObserver(this);

        initializeComponents();

        currentLevel.generate();
    }

    private void initializeComponents() {
        gameUI = new GameUI(game.getBatch(), currentLevel, player);
        minimapRenderer = new MinimapRenderer(shapeRenderer, game.getBatch(), currentLevel);
        transitionManager = new TransitionManager(currentLevel, player);
        playerController = new PlayerController(player, currentLevel);
    }

    @Override
    public void render(float delta) {
        // Update game logic
        update(delta);

        // Clear the screen
        clearScreen();

        // Render game elements based on current state
        if (gameState == GameState.TRANSITIONING) {
            transitionManager.render(shapeRenderer);
        } else {
            renderGame();
        }

        // Draw UI elements
        gameUI.render(debugMode);

        // Show minimap if toggled
        if (showMinimap) {
            minimapRenderer.render();
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderGame() {
        // Render dungeon
        currentLevel.render(shapeRenderer);

        // Render player
        renderPlayer();
    }

    private void renderPlayer() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1); // Red
        shapeRenderer.circle(player.getX(), player.getY(), player.getRadius());
        shapeRenderer.end();
    }

    private void update(float delta) {
        handleKeyboardInput();

        if (gameState == GameState.TRANSITIONING) {
            boolean transitionComplete = transitionManager.update(delta);
            if (transitionComplete) {
                gameState = GameState.PLAYING;
            }
            return;
        }

        playerController.update(delta);
        checkRoomTransition();
    }

    private void handleKeyboardInput() {
        // Toggle debug mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        // Return to title screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToTitle();
        }

        // Toggle minimap
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            showMinimap = !showMinimap;
            if (showMinimap) {
                minimapRenderer.initialize();
            }
        }
    }

    private void checkRoomTransition() {
        Door collidingDoor = currentLevel.checkDoorCollision(player);

        if (collidingDoor != null) {
            startRoomTransition(collidingDoor);
        }
    }

    private void startRoomTransition(Door door) {
        if (gameState != GameState.TRANSITIONING) {
            gameState = GameState.TRANSITIONING;
            transitionManager.startTransition(door, currentLevel.getCurrentRoom());
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
        shapeRenderer.dispose();
        gameUI.dispose();
    }

    @Override
    public void onLevelUp(int newLevel) {
        System.out.println("Dungeon level up to: " + newLevel);
    }

    @Override
    public void onRoomChange(Room newRoom) {
        System.out.println("Room changed to type: " + newRoom.getRoomType());
        minimapRenderer.invalidate();
    }
}