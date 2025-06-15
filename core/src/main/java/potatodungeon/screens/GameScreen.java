package potatodungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.*;
import potatodungeon.controllers.PlayerController;
import potatodungeon.entities.Player;
import potatodungeon.entities.PlayerFactory;
import potatodungeon.managers.TransitionManager;
import potatodungeon.observers.IDungeonObserver;
import potatodungeon.states.IGameState;
import potatodungeon.states.PauseState;
import potatodungeon.states.PlayState;
import potatodungeon.states.TransitionState;
import potatodungeon.ui.GameUI;
import potatodungeon.ui.MinimapRenderer;
import potatodungeon.world.DungeonLevel;
import potatodungeon.world.Room;
import potatodungeon.managers.EnemyManager;
import potatodungeon.managers.BulletManager;
import potatodungeon.entities.Enemy;
import potatodungeon.entities.Bullet;
import potatodungeon.entities.Obstacle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game screen handling rendering and game logic
 */
public class GameScreen implements Screen, IDungeonObserver {
    private final DungeonCrawler game;

    private EnemyManager enemyManager;
    private BulletManager bulletManager;
    private IGameState currentState;
    private PlayState playingState;
    private TransitionState transitionState;
    private PauseState pauseState;
    private ShapeRenderer shapeRenderer;
    private Player player;
    private DungeonLevel currentLevel;

    // Components
    private GameUI gameUI;
    private MinimapRenderer minimapRenderer;
    private TransitionManager transitionManager;
    private PlayerController playerController;

    // UI states
    private boolean debugMode = false;
    private boolean showMinimap = false;
    private boolean pauseKeyPressed = false;

    public GameScreen(DungeonCrawler game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        player = PlayerFactory.INSTANCE.createPlayer(
                DungeonCrawler.WINDOW_WIDTH / 2,
                DungeonCrawler.WINDOW_HEIGHT / 2, 15);

        currentLevel = new DungeonLevel();
        currentLevel.addObserver(this);

        initializeComponents();
        currentLevel.generate();
        initializeStates();
    }

    private void initializeComponents() {
        gameUI = new GameUI(game.getBatch(), currentLevel, player);
        minimapRenderer = new MinimapRenderer(shapeRenderer, game.getBatch(), currentLevel);
        transitionManager = new TransitionManager(currentLevel, player);
        playerController = new PlayerController(player, currentLevel);
        bulletManager = BulletManager.getInstance();
    }

    private void initializeStates() {
        playingState = new PlayState(playerController, transitionManager, this);
        transitionState = new TransitionState(transitionManager, this);
        pauseState = new PauseState(this);

        // Começar no estado Playing
        currentState = playingState;
        currentState.onEnter();
    }

    @Override
    public void render(float delta) {
        handleGlobalInput(); // F1, F8, etc.

        updateEnemiesAndBullets(delta);
        currentState.update(delta);

        clearScreen();
        currentState.render(shapeRenderer);

        renderBullets();

        // UI sempre visível
        gameUI.render(debugMode);
        if (showMinimap) {
            minimapRenderer.render();
        }
    }

    private void handleGlobalInput() {
        // Input que funciona em qualquer estado
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            showMinimap = !showMinimap;
            if (showMinimap) {
                minimapRenderer.initialize();
            }
        }

        // Return to title screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToTitle();
        }

        // Pause só funciona se estiver no play state
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !pauseKeyPressed) {
            pauseKeyPressed = true;

            if (currentState == playingState) {
                setState(pauseState);           // Pausar
            } else if (currentState == pauseState) {
                setState(playingState);          // ✅ DESPAUSAR!
            }
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.P)) {
            pauseKeyPressed = false;
        }
    }

    public void setState(IGameState newState) {
        if (currentState != null) {
            currentState.onExit();
        }
        currentState = newState;
        currentState.onEnter();
    }
    private void clearScreen() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void renderGame() {
        currentLevel.render(shapeRenderer);
        player.render(shapeRenderer);
    }

    private void updateEnemiesAndBullets(float delta) {
        if (currentState != playingState) {
            return;
        }

        Vector2 playerPosition = new Vector2(player.getX(), player.getY());

        // Atualizar jogador COM lista de inimigos
        List<Enemy> currentEnemies = currentLevel.getCurrentRoom().getEnemies();
        player.update(delta);

        currentLevel.checkPlayerAttack(player);

        // Atualizar inimigos
        currentLevel.updateEnemies(delta, playerPosition);

        // Obter obstáculos da sala atual
        List<Vector2> obstaclePositions = getCurrentRoomObstacles();

        // Atualizar balas
        bulletManager.update(
                delta,
                playerPosition,
                obstaclePositions,
                currentLevel.getCurrentRoom().getBounds().width,
                currentLevel.getCurrentRoom().getBounds().height
        );
    }

    private List<Vector2> getCurrentRoomObstacles() {
        List<Vector2> positions = new ArrayList<>();

        Room currentRoom = currentLevel.getCurrentRoom();
        if (currentRoom != null) {
            for (Obstacle obstacle : currentRoom.getObstacles()) {
                positions.add(new Vector2(obstacle.getX(), obstacle.getY()));
            }
        }

        return positions;
    }

    private void renderBullets() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 0f, 1f); // Amarelo para balas

        for (Bullet bullet : bulletManager.getBullets()) {
            if (bullet.isActive()) {
                Vector2 pos = bullet.getPosition();
                shapeRenderer.circle(pos.x, pos.y, 3f); // Balas pequenas
            }
        }

        shapeRenderer.end();
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
        bulletManager.clearBullets();
    }

    public DungeonCrawler getGame() { return game; }
    public PlayState getPlayingState() { return playingState; }
    public TransitionState getTransitioningState() { return transitionState; }
    public PauseState getPausedState() { return pauseState; }
}
