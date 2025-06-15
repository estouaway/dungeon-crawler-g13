package potatodungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import potatodungeon.DungeonCrawler;
import potatodungeon.managers.ConfigurationManager;

public class SettingsScreen implements Screen {
    private final DungeonCrawler game;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private ConfigurationManager config;

    private int selectedOption = 0;
    private final String[] menuOptions = {
            "Up Key", "Down Key", "Left Key", "Right Key", "Action Key", "Player Color", "Back"
    };

    private boolean waitingForKey = false;
    private String keyToSet = "";

    private int currentColorIndex = 0;

    public SettingsScreen(DungeonCrawler game) {
        this.game = game;
        this.config = ConfigurationManager.getInstance();
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        shapeRenderer = new ShapeRenderer();
        currentColorIndex = config.getColorIndex(config.getPlayerColor());
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float centerX = DungeonCrawler.WINDOW_WIDTH / 2f;
        float startY = DungeonCrawler.WINDOW_HEIGHT - 100;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(50, 50, DungeonCrawler.WINDOW_WIDTH - 100, DungeonCrawler.WINDOW_HEIGHT - 100);
        shapeRenderer.end();

        game.getBatch().begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        GlyphLayout titleLayout = new GlyphLayout(font, "SETTINGS");
        font.draw(game.getBatch(), titleLayout, centerX - titleLayout.width / 2, startY);

        font.getData().setScale(1.5f);
        float optionY = startY - 80;

        for (int i = 0; i < menuOptions.length; i++) {
            String optionText = menuOptions[i];
            String valueText = "";

            switch (menuOptions[i]) {
                case "Up Key":
                    valueText = ": " + config.getKeyName(config.getUpKey());
                    break;
                case "Down Key":
                    valueText = ": " + config.getKeyName(config.getDownKey());
                    break;
                case "Left Key":
                    valueText = ": " + config.getKeyName(config.getLeftKey());
                    break;
                case "Right Key":
                    valueText = ": " + config.getKeyName(config.getRightKey());
                    break;
                case "Action Key":
                    valueText = ": " + config.getKeyName(config.getActionKey());
                    break;
                case "Player Color":
                    valueText = ": " + config.getColorName(config.getPlayerColor());
                    break;
            }

            if (i == selectedOption) {
                font.setColor(Color.YELLOW);
                if (waitingForKey && i < menuOptions.length - 2) {
                    optionText += " - Press any key...";
                    valueText = "";
                }
            } else {
                font.setColor(Color.WHITE);
            }

            String fullText = optionText + valueText;
            GlyphLayout layout = new GlyphLayout(font, fullText);
            font.draw(game.getBatch(), layout, centerX - layout.width / 2, optionY);

            optionY -= 60;
        }

        font.getData().setScale(1f);
        font.setColor(Color.LIGHT_GRAY);
        String instructions = waitingForKey ? "Press ESC to cancel" :
                "UP/DOWN: Navigate, ENTER: Select, ESC: Back";
        GlyphLayout instrLayout = new GlyphLayout(font, instructions);
        font.draw(game.getBatch(), instrLayout, centerX - instrLayout.width / 2, 80);

        game.getBatch().end();
    }

    private void handleInput() {
        if (waitingForKey) {
            handleKeyBinding();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % menuOptions.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            selectOption();
        }

        if (selectedOption == 5) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                currentColorIndex = (currentColorIndex - 1 + config.getAvailableColors().length) % config.getAvailableColors().length;
                config.setPlayerColor(config.getAvailableColors()[currentColorIndex]);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                currentColorIndex = (currentColorIndex + 1) % config.getAvailableColors().length;
                config.setPlayerColor(config.getAvailableColors()[currentColorIndex]);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
        }
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0:
                startKeyBinding("up");
                break;
            case 1:
                startKeyBinding("down");
                break;
            case 2:
                startKeyBinding("left");
                break;
            case 3:
                startKeyBinding("right");
                break;
            case 4:
                startKeyBinding("action");
                break;
            case 5:
                break;
            case 6:
                game.setScreen(new TitleScreen(game));
                break;
        }
    }

    private void startKeyBinding(String key) {
        waitingForKey = true;
        keyToSet = key;
    }

    private void handleKeyBinding() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            waitingForKey = false;
            keyToSet = "";
            return;
        }


        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                if (i == Input.Keys.ESCAPE || i == Input.Keys.ENTER) {
                    continue;
                }

                if (config.isKeyAssigned(i, keyToSet)) {
                    continue;
                }

                switch (keyToSet) {
                    case "up":
                        config.setUpKey(i);
                        break;
                    case "down":
                        config.setDownKey(i);
                        break;
                    case "left":
                        config.setLeftKey(i);
                        break;
                    case "right":
                        config.setRightKey(i);
                        break;
                    case "action":
                        config.setActionKey(i);
                        break;
                }

                waitingForKey = false;
                keyToSet = "";
                break;
            }
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}