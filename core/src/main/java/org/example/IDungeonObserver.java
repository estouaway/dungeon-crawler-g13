package org.example;

/**
 * Interface for observing changes in the dungeon
 */
public interface IDungeonObserver {
    /**
     * Called when the dungeon level changes
     * @param newLevel The new dungeon level
     */
    void onLevelUp(int newLevel);

    /**
     * Called when the current room changes
     * @param newRoom The new room the player is in
     */
    void onRoomChange(Room newRoom);
}