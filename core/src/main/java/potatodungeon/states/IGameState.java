package potatodungeon.states;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IGameState {
    void update(float delta);
    void render(ShapeRenderer renderer);
    void onEnter();  // quando entra no estado
    void onExit();   // quando sai do estado
}