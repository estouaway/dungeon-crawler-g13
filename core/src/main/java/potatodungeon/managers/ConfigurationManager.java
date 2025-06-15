package potatodungeon.managers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

public class ConfigurationManager {
    private static ConfigurationManager instance;

    private int upKey = Input.Keys.W;
    private int downKey = Input.Keys.S;
    private int leftKey = Input.Keys.A;
    private int rightKey = Input.Keys.D;

    private int actionKey = Input.Keys.SPACE;

    private Color playerColor = Color.BLUE;

    private final Color[] availableColors = {
            Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW,
            Color.PURPLE, Color.ORANGE, Color.CYAN, Color.PINK
    };

    private final String[] colorNames = {
            "Blue", "Red", "Green", "Yellow",
            "Purple", "Orange", "Cyan", "Pink"
    };

    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public int getUpKey() { return upKey; }
    public int getDownKey() { return downKey; }
    public int getLeftKey() { return leftKey; }
    public int getRightKey() { return rightKey; }
    public int getActionKey() { return actionKey; }
    public Color getPlayerColor() { return playerColor; }
    public Color[] getAvailableColors() { return availableColors; }
    public String[] getColorNames() { return colorNames; }

    public void setUpKey(int key) { this.upKey = key; }
    public void setDownKey(int key) { this.downKey = key; }
    public void setLeftKey(int key) { this.leftKey = key; }
    public void setRightKey(int key) { this.rightKey = key; }
    public void setActionKey(int key) { this.actionKey = key; }
    public void setPlayerColor(Color color) { this.playerColor = color; }

    public String getKeyName(int keyCode) {
        return Input.Keys.toString(keyCode);
    }

    public String getColorName(Color color) {
        for (int i = 0; i < availableColors.length; i++) {
            if (availableColors[i].equals(color)) {
                return colorNames[i];
            }
        }
        return "Unknown";
    }

    public int getColorIndex(Color color) {
        for (int i = 0; i < availableColors.length; i++) {
            if (availableColors[i].equals(color)) {
                return i;
            }
        }
        return 0;
    }

    public boolean isKeyAssigned(int keyCode, String excludeAction) {
        if (!excludeAction.equals("up") && upKey == keyCode) return true;
        if (!excludeAction.equals("down") && downKey == keyCode) return true;
        if (!excludeAction.equals("left") && leftKey == keyCode) return true;
        if (!excludeAction.equals("right") && rightKey == keyCode) return true;
        if (!excludeAction.equals("action") && actionKey == keyCode) return true;
        return false;
    }
}