package potatodungeon.managers;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.entities.Player;
import potatodungeon.DungeonCrawler;
import potatodungeon.world.Door;
import potatodungeon.world.DungeonLevel;
import potatodungeon.world.Room;

/**
 * Classe para gerir as transições de sala
 */
public class TransitionManager {
    private static final float TRANSITION_DURATION = 0.5f;

    private final DungeonLevel level;
    private final Player player;

    private boolean isTransitioning = false;
    private Room transitionFromRoom;
    private Room.Direction transitionDirection;
    private float transitionTimer = 0;

    public TransitionManager(DungeonLevel level, Player player) {
        this.level = level;
        this.player = player;
    }

    /**
     * Tenta iniciar uma transição se o jogador estiver numa porta
     */
    public void checkAndStartTransition() {
        if (isTransitioning) return;

        Door collidingDoor = level.checkDoorCollision(player);
        if (collidingDoor != null) {
            startTransition(collidingDoor, level.getCurrentRoom());
        }
    }

    private void startTransition(Door door, Room fromRoom) {
        this.isTransitioning = true;
        this.transitionTimer = 0;
        this.transitionDirection = door.getDirection();
        this.transitionFromRoom = fromRoom;

        System.out.println("Starting transition to new room via " + transitionDirection + " door");
    }

    /**
     * Atualiza a transição
     */
    public boolean update(float delta) {
        if (!isTransitioning) return false;

        transitionTimer += delta;

        if (transitionTimer >= TRANSITION_DURATION) {
            completeTransition();
            return true;
        }
        return false;
    }

    private void completeTransition() {
        Door door = transitionFromRoom.getDoors().get(transitionDirection);
        level.transitionToRoom(door, player);

        this.isTransitioning = false;
        this.transitionTimer = 0;
        this.transitionFromRoom = null;
        this.transitionDirection = null;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!isTransitioning) return;

        // calcula tempo de transicao e usa para fazer um fade
        float progress = Math.min(transitionTimer / TRANSITION_DURATION, 1.0f);

        level.render(shapeRenderer);
        player.render(shapeRenderer);

        // desenha o fade
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, progress);
        shapeRenderer.rect(0, 0, DungeonCrawler.WINDOW_WIDTH, DungeonCrawler.WINDOW_HEIGHT);
        shapeRenderer.end();
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }
}