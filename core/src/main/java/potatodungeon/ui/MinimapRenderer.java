package potatodungeon.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import potatodungeon.world.Door;
import potatodungeon.world.DungeonLevel;
import potatodungeon.world.Room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for rendering the minimap
 */
public class MinimapRenderer {
    private static final int WIDTH = 280;
    private static final int HEIGHT = 280;
    private static final int X = 372;
    private static final int Y = 244;
    private static final int PADDING = 10;
    private static final float ROOM_SIZE = 20;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final DungeonLevel level;

    private final Map<Room, Vector2> roomPositions = new HashMap<>();
    private boolean initialized = false;

    public MinimapRenderer(ShapeRenderer shapeRenderer, SpriteBatch batch, DungeonLevel level) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.level = level;
        this.font = new BitmapFont();
    }

    public void initialize() {
        if (!initialized) {
            calculateRoomPositions();
            initialized = true;
        }
    }

    public void invalidate() {
        initialized = false;
    }

    public void render() {
        try {
            List<Room> allRooms = level.getRooms();

            if (allRooms == null || allRooms.isEmpty()) return;

            if (!initialized || roomPositions.size() != allRooms.size()) {
                calculateRoomPositions();
                initialized = true;
            }

            ensureRenderingNotActive();

            renderBackground();

            renderConnections(allRooms);

            renderRooms(allRooms);

            renderLabels(allRooms);

        } catch (Exception e) {
            System.err.println("Minimap error: " + e.getMessage());
            e.printStackTrace();

            ensureRenderingNotActive();
        }
    }

    private void ensureRenderingNotActive() {
        // Encerra shapeRenderer se estiver ativo
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }

        // Encerra batch se estiver ativo
        if (batch.isDrawing()) {
            batch.end();
        }
    }

    private void renderBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(X, Y, WIDTH, HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(X, Y, WIDTH, HEIGHT);
        shapeRenderer.end();
    }

    private void renderConnections(List<Room> allRooms) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.7f);

        boolean[][] drawnConnections = new boolean[allRooms.size()][allRooms.size()];

        for (int i = 0; i < allRooms.size(); i++) {
            Room room = allRooms.get(i);
            Vector2 pos = roomPositions.get(room);

            for (Door door : room.getDoors().values()) {
                Room connected = door.getConnectedRoom();
                int connectedIndex = allRooms.indexOf(connected);

                if (connectedIndex >= 0 && !drawnConnections[i][connectedIndex] &&
                        !drawnConnections[connectedIndex][i]) {

                    Vector2 connPos = roomPositions.get(connected);

                    if (connPos != null) {
                        shapeRenderer.line(
                                X + pos.x, Y + pos.y,
                                X + connPos.x, Y + connPos.y
                        );

                        drawnConnections[i][connectedIndex] = true;
                        drawnConnections[connectedIndex][i] = true;
                    }
                }
            }
        }

        shapeRenderer.end();
    }

    private void renderRooms(List<Room> allRooms) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Room room : allRooms) {
            Vector2 pos = roomPositions.get(room);

            float x = X + pos.x - ROOM_SIZE / 2;
            float y = Y + pos.y - ROOM_SIZE / 2;

            Color roomColor = getRoomColor(room.getRoomType());
            shapeRenderer.setColor(roomColor);
            shapeRenderer.rect(x, y, ROOM_SIZE, ROOM_SIZE);

            // Highlight current room
            if (room == level.getCurrentRoom()) {
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(x - 2, y - 2, ROOM_SIZE + 4, ROOM_SIZE + 4);

                shapeRenderer.setColor(roomColor);
                shapeRenderer.rect(x, y, ROOM_SIZE, ROOM_SIZE);
            }
        }

        shapeRenderer.end();
    }

    private void renderLabels(List<Room> allRooms) {
        batch.begin();
        font.setColor(Color.BLACK);

        for (int i = 0; i < allRooms.size(); i++) {
            Room room = allRooms.get(i);
            Vector2 pos = roomPositions.get(room);

            String number = String.valueOf(i);
            GlyphLayout layout = new GlyphLayout(font, number);
            float textX = X + pos.x - layout.width/2;
            float textY = Y + pos.y + layout.height/2;

            font.draw(batch, number, textX, textY);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Map (F8)", X + 10, Y + HEIGHT - 10);

        batch.end();
    }

    private void calculateRoomPositions() {
        List<Room> rooms = level.getRooms();
        roomPositions.clear();

        // Available space
        float availWidth = WIDTH - PADDING * 2;
        float availHeight = HEIGHT - PADDING * 2;

        int gridSize = (int) Math.ceil(Math.sqrt(rooms.size()));

        float cellWidth = (availWidth) / gridSize;
        float cellHeight = (availHeight) / gridSize;

        float offsetX = (WIDTH - (cellWidth * gridSize)) / 2 + PADDING;
        float offsetY = (HEIGHT - (cellHeight * gridSize)) / 2 + PADDING;

        // Position rooms in grid
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);

            int row = i / gridSize;
            int col = i % gridSize;

            float x = offsetX + col * cellWidth;
            float y = offsetY + row * cellHeight;

            roomPositions.put(room, new Vector2(x, y));
        }

        // Adjust to fit if needed
        adjustPositionsToFit(availWidth, availHeight);
    }

    private void adjustPositionsToFit(float availWidth, float availHeight) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

        // Find bounds
        for (Vector2 pos : roomPositions.values()) {
            minX = Math.min(minX, pos.x);
            minY = Math.min(minY, pos.y);
            maxX = Math.max(maxX, pos.x);
            maxY = Math.max(maxY, pos.y);
        }

        float width = maxX - minX;
        float height = maxY - minY;

        // Scale if needed
        if (width > availWidth || height > availHeight) {
            float scaleX = availWidth / width;
            float scaleY = availHeight / height;
            float scale = Math.min(scaleX, scaleY) * 0.8f;

            for (Vector2 pos : roomPositions.values()) {
                pos.x = (pos.x - minX) * scale + PADDING * 2;
                pos.y = (pos.y - minY) * scale + PADDING * 2;
            }
        }
    }

    private Color getRoomColor(DungeonLevel.RoomType roomType) {
        return switch (roomType) {
            case EMPTY -> Color.LIGHT_GRAY;
            case OBSTACLE_LIGHT -> Color.CYAN;
            case OBSTACLE_MEDIUM -> Color.ORANGE;
            case OBSTACLE_HEAVY -> Color.RED;
            case SPECIAL -> Color.GOLD;
            default -> Color.WHITE;
        };
    }

    public void dispose() {
        font.dispose();
    }
}