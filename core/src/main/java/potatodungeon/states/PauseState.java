package potatodungeon.states;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.DungeonCrawler;
import potatodungeon.screens.GameScreen;


// Como fazer quando tiver inimigos a mexerem-se sozinhos?
// estados geram tudo?
// um manager centralizado que faz update dos inimigos
// flag global?
public class PauseState implements IGameState {
    private final GameScreen gameScreen;

    public PauseState(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(ShapeRenderer renderer) {
        // Renderiza jogo por baixo (frozen)
        gameScreen.renderGame();

        // Overlay escuro de pause
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0, 0, 0, 0.5f); // Preto com transparência
        renderer.rect(0, 0, DungeonCrawler.WINDOW_WIDTH, DungeonCrawler.WINDOW_HEIGHT);
        renderer.end();

        gameScreen.getGame().getBatch().begin();
        gameScreen.getGame().getBatch().end();
        // adicionar texto
    }

    @Override
    public void onEnter() {
        System.out.println("Game Paused - Press P or ESC to resume");
    }

    @Override
    public void onExit() {
        System.out.println("Game Resumed");
    }
}