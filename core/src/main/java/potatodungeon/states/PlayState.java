package potatodungeon.states;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.PlayerController;
import potatodungeon.managers.TransitionManager;
import potatodungeon.screens.GameScreen;

public class PlayState implements IGameState {
    private final PlayerController playerController;
    private final TransitionManager transitionManager;
    private final GameScreen gameScreen;

    public PlayState(PlayerController playerController,
                        TransitionManager transitionManager,
                        GameScreen gameScreen) {
        this.playerController = playerController;
        this.transitionManager = transitionManager;
        this.gameScreen = gameScreen;
    }

    @Override
    public void update(float delta) {
        // Atualiza jogador
        playerController.update(delta);

        // Verifica se deve começar transição
        transitionManager.checkAndStartTransition();

        // Se detectou porta, muda para estado de transição
        if (transitionManager.isTransitioning()) {
            gameScreen.setState(gameScreen.getTransitioningState());
        }
    }

    @Override
    public void render(ShapeRenderer renderer) {
        // Rendering normal do jogo
        gameScreen.renderGame();
    }

    @Override
    public void onEnter() {
        System.out.println("Entered Playing State");
    }

    @Override
    public void onExit() {
        System.out.println("Exited Playing State");
    }
}