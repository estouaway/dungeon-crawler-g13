package org.example;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dungeon Potato");
        config.setWindowedMode(DungeonCrawler.WINDOW_WIDTH, DungeonCrawler.WINDOW_HEIGHT); // Use constants from DungeonCrawler
        new Lwjgl3Application(new DungeonCrawler(), config);
    }
}