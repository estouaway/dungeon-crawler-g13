package potatodungeon.states;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.managers.TransitionManager;
import potatodungeon.screens.GameScreen;

public class TransitionState implements IGameState {
    private final TransitionManager transitionManager;
    private final GameScreen gameScreen;

    public TransitionState(TransitionManager transitionManager, GameScreen gameScreen) {
        this.transitionManager = transitionManager;
        this.gameScreen = gameScreen;
    }

    @Override
    public void update(float delta) {
        // Atualiza transição
        boolean transitionCompleted = transitionManager.update(delta);

        // Se transição terminou, volta para playing
        if (transitionCompleted) {
            gameScreen.setState(gameScreen.getPlayingState());
        }
    }

    @Override
    public void render(ShapeRenderer renderer) {
        // TransitionManager faz o rendering (com fade)
        transitionManager.render(renderer);
    }

    @Override
    public void onEnter() {
        System.out.println("Entered Transitioning State");
    }

    @Override
    public void onExit() {
        System.out.println("Exited Transitioning State - Transition completed!");
    }
}

