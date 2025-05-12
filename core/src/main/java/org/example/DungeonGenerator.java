package org.example;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.awt.Point;

/**
 * Dungeon generator using Binary Space Partitioning (BSP) algorithm
 * Creates rooms with door connections based on BSP tree
 * Ensures all rooms are connected in a single graph
 */
public class DungeonGenerator {
    // Room and connection storage
    private final List<Room> rooms = new ArrayList<>();

    // Direct room connection tracking
    // Key: Room, Value: Map of connected rooms by direction
    private final Map<Room, Map<Room.Direction, Room>> roomConnections = new HashMap<>();

    // BSP tree
    private BSPLeaf rootLeaf;
    private final List<BSPLeaf> leaves = new ArrayList<>();
    private final int TargetLeafCount = 8;

    public DungeonGenerator() {
    }

    /**
     * Generate the dungeon
     * @return List of generated rooms
     */
    public List<Room> generateDungeon() {
        // Clear any existing data
        rooms.clear();
        roomConnections.clear();
        leaves.clear();

        // Create the BSP tree to determine logical room layout
        createBSPTree();

        // Generate rooms based on BSP leaf positions
        generateRooms();

        // Connect rooms based on BSP structure
        connectRooms(rootLeaf);

        // Ensure all rooms are connected
        ensureFullConnectivity();

        // Add a few extra connections for variety (optional)
        addExtraConnections();

        // Log all room connections
        logRoomConnections();

        return rooms;
    }

    /**
     * Create BSP tree by recursively splitting the dungeon area
     */
    private void createBSPTree() {
        // Create root leaf
        rootLeaf = new BSPLeaf(0, 0, DungeonCrawler.WINDOW_WIDTH, DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - DungeonCrawler.BOTTOM_UI_HEIGHT);
        leaves.add(rootLeaf);

        // Recursively split leaves until we have enough
        boolean splitOccurred = true;
        while (splitOccurred && countLeaves() < TargetLeafCount) {
            splitOccurred = false;

            // Make a copy of the current leaves to avoid concurrent modification
            List<BSPLeaf> currentLeaves = new ArrayList<>(leaves);

            // Try to split each leaf
            for (BSPLeaf leaf : currentLeaves) {
                // Skip leaves that have already been split
                if (leaf.leftChild != null || leaf.rightChild != null) {
                    continue;
                }

                // Skip leaves that are too small
                if (leaf.width < DungeonCrawler.WINDOW_WIDTH / 6 || leaf.height < (DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - DungeonCrawler.BOTTOM_UI_HEIGHT) / 6) {
                    continue;
                }

                // Split the leaf
                if (splitLeaf(leaf)) {
                    splitOccurred = true;
                }
            }
        }
    }

    /**
     * Count the number of leaf nodes (rooms) in the BSP tree
     */
    private int countLeaves() {
        int count = 0;
        for (BSPLeaf leaf : leaves) {
            if (leaf.leftChild == null && leaf.rightChild == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Split a leaf into two child leaves
     */
    private boolean splitLeaf(BSPLeaf leaf) {
        // Return if this leaf has already been split
        if (leaf.leftChild != null || leaf.rightChild != null) {
            return false;
        }

        // Determine split direction (horizontal or vertical)
        boolean horizontalSplit;

        // If the leaf is wider than tall, split it vertically
        if (leaf.width > leaf.height * 1.25) {
            horizontalSplit = false;
        }
        // If the leaf is taller than wide, split it horizontally
        else if (leaf.height > leaf.width * 1.25) {
            horizontalSplit = true;
        }
        // Otherwise, choose a random direction
        else {
            horizontalSplit = MathUtils.randomBoolean();
        }

        // Calculate the minimum size for a split
        int minSize = horizontalSplit ?
                (DungeonCrawler.WINDOW_HEIGHT - DungeonCrawler.TOP_UI_HEIGHT - DungeonCrawler.BOTTOM_UI_HEIGHT) / 8 :
                DungeonCrawler.WINDOW_WIDTH / 8;

        // Calculate maximum split position
        int maxSplitPosition = horizontalSplit ?
                leaf.height - minSize :
                leaf.width - minSize;

        // Return false if the leaf is too small to split
        if (maxSplitPosition <= minSize) {
            return false;
        }

        // Determine split position with some randomness
        // Avoid perfectly even splits by using random range between 30% and 70%
        int splitPosition;
        if (horizontalSplit) {
            splitPosition = leaf.y + MathUtils.random((int)(leaf.height * 0.3f), (int)(leaf.height * 0.7f));
        } else {
            splitPosition = leaf.x + MathUtils.random((int)(leaf.width * 0.3f), (int)(leaf.width * 0.7f));
        }

        // Create child leaves
        if (horizontalSplit) {
            // Horizontal split
            leaf.leftChild = new BSPLeaf(leaf.x, leaf.y, leaf.width, splitPosition - leaf.y);
            leaf.rightChild = new BSPLeaf(leaf.x, splitPosition, leaf.width, leaf.height - (splitPosition - leaf.y));
        } else {
            // Vertical split
            leaf.leftChild = new BSPLeaf(leaf.x, leaf.y, splitPosition - leaf.x, leaf.height);
            leaf.rightChild = new BSPLeaf(splitPosition, leaf.y, leaf.width - (splitPosition - leaf.x), leaf.height);
        }

        // Add child leaves to the list
        leaves.add(leaf.leftChild);
        leaves.add(leaf.rightChild);

        return true;
    }

    /**
     * Generate rooms based on the BSP tree
     */
    private void generateRooms() {
        // Count the number of terminal leaves (potential room locations)
        int availableRooms = 0;
        for (BSPLeaf leaf : leaves) {
            if (leaf.leftChild == null && leaf.rightChild == null) {
                availableRooms++;
            }
        }

        // Determine how many rooms to actually create
        int roomsToCreate = Math.min(TargetLeafCount, availableRooms);

        // Find terminal leaves (those without children)
        List<BSPLeaf> terminalLeaves = new ArrayList<>();
        for (BSPLeaf leaf : leaves) {
            if (leaf.leftChild == null && leaf.rightChild == null) {
                terminalLeaves.add(leaf);
            }
        }

        // Sort leaves by distance from center to create a natural progression
        terminalLeaves.sort((a, b) -> {
            // Calculate center points
            Point centerA = new Point(a.x + a.width / 2, a.y + a.height / 2);
            Point centerB = new Point(b.x + b.width / 2, b.y + b.height / 2);

            // Calculate distances from (0,0)
            double distA = Math.sqrt(centerA.x * centerA.x + centerA.y * centerA.y);
            double distB = Math.sqrt(centerB.x * centerB.x + centerB.y * centerB.y);

            // Compare distances
            return Double.compare(distA, distB);
        });

        // Create rooms for the closest N leaves
        for (int i = 0; i < roomsToCreate; i++) {
            BSPLeaf leaf = terminalLeaves.get(i);

            // Create room (using the standard fixed size)
            Room room = new Room(i);

            // Store the BSP position in the leaf
            leaf.roomCenter = new Point(leaf.x + leaf.width / 2, leaf.y + leaf.height / 2);
            leaf.room = room;

            // Assign room type based on distance from start
            assignRoomType(room, i);

            // Generate obstacles
            generateRoomObstacles(room);

            // Add to room list
            rooms.add(room);

            // Initialize connection map for this room
            roomConnections.put(room, new HashMap<>());
        }
    }

    /**
     * Connect rooms based on the BSP tree structure
     * Each connection follows a path through the BSP tree
     */
    private void connectRooms(BSPLeaf leaf) {
        // Skip terminal leaves
        if (leaf.leftChild == null || leaf.rightChild == null) {
            return;
        }

        // Find rooms in both child subtrees
        Room leftRoom = findClosestRoomInSubtree(leaf.leftChild);
        Room rightRoom = findClosestRoomInSubtree(leaf.rightChild);

        // Connect the rooms if both exist
        if (leftRoom != null && rightRoom != null) {
            connectRoomsWithDoors(leftRoom, rightRoom, leaf);
        }

        // Recursively connect rooms in child subtrees
        connectRooms(leaf.leftChild);
        connectRooms(leaf.rightChild);
    }

    /**
     * Find the closest room in a subtree to the split point
     * This helps create more natural connections
     */
    private Room findClosestRoomInSubtree(BSPLeaf leaf) {
        // If this leaf has a room, return it
        if (leaf.room != null) {
            return leaf.room;
        }

        // If no room here, find the closest room from children
        if (leaf.leftChild == null && leaf.rightChild == null) {
            return null; // No children, no room
        }

        Room leftRoom = (leaf.leftChild != null) ? findClosestRoomInSubtree(leaf.leftChild) : null;
        Room rightRoom = (leaf.rightChild != null) ? findClosestRoomInSubtree(leaf.rightChild) : null;

        if (leftRoom == null) return rightRoom;
        if (rightRoom == null) return leftRoom;

        // If both subtrees have rooms, return one randomly
        // For more complex dungeons, you could return the closest one to the split
        return MathUtils.randomBoolean() ? leftRoom : rightRoom;
    }

    /**
     * Connect two rooms with appropriate doors
     */
    private void connectRoomsWithDoors(Room room1, Room room2, BSPLeaf parentLeaf) {
        // Find the leaves containing these rooms
        BSPLeaf leaf1 = findLeafContainingRoom(room1);
        BSPLeaf leaf2 = findLeafContainingRoom(room2);

        if (leaf1 == null || leaf2 == null) {
            return;
        }

        // Determine the logical direction based on the split orientation
        Room.Direction dir1, dir2;

        // Check orientation of the split in the parent
        if (parentLeaf.leftChild.y != parentLeaf.rightChild.y) {
            // Horizontal split (top/bottom)
            if (leaf1.roomCenter.y < leaf2.roomCenter.y) {
                dir1 = Room.Direction.SOUTH;
                dir2 = Room.Direction.NORTH;
            } else {
                dir1 = Room.Direction.NORTH;
                dir2 = Room.Direction.SOUTH;
            }
        } else {
            // Vertical split (left/right)
            if (leaf1.roomCenter.x < leaf2.roomCenter.x) {
                dir1 = Room.Direction.EAST;
                dir2 = Room.Direction.WEST;
            } else {
                dir1 = Room.Direction.WEST;
                dir2 = Room.Direction.EAST;
            }
        }

        // Only add doors if the direction is free
        if (!room1.getDoors().containsKey(dir1) && !room2.getDoors().containsKey(dir2)) {
            // Add doors
            room1.addDoor(dir1, room2);
            room2.addDoor(dir2, room1);

            // Record connections
            roomConnections.get(room1).put(dir1, room2);
            roomConnections.get(room2).put(dir2, room1);
        }
    }

    /**
     * Find the leaf containing a specific room
     */
    private BSPLeaf findLeafContainingRoom(Room room) {
        for (BSPLeaf leaf : leaves) {
            if (leaf.room == room) {
                return leaf;
            }
        }
        return null;
    }

    /**
     * Ensure that all rooms are connected in a single graph
     * This is critical for a properly navigable dungeon
     */
    private void ensureFullConnectivity() {
        // Step 1: Find all disconnected room groups
        List<Set<Room>> roomGroups = findRoomGroups();

        // If we already have a single connected dungeon, we're done
        if (roomGroups.size() <= 1) {
            return;
        }

        // Step 2: Connect the disconnected groups
        // Sort groups by size (descending) to connect larger groups first
        roomGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));

        // Connect each group to the largest group
        Set<Room> mainGroup = roomGroups.getFirst();

        for (int i = 1; i < roomGroups.size(); i++) {
            Set<Room> currentGroup = roomGroups.get(i);

            // Find the best pair of rooms to connect between the two groups
            Room mainGroupRoom = null;
            Room currentGroupRoom = null;
            Room.Direction mainToCurrentDir = null;
            Room.Direction currentToMainDir = null;
            int bestDistance = Integer.MAX_VALUE;

            for (Room roomA : mainGroup) {
                BSPLeaf leafA = findLeafContainingRoom(roomA);
                if (leafA == null) continue;

                for (Room roomB : currentGroup) {
                    BSPLeaf leafB = findLeafContainingRoom(roomB);
                    if (leafB == null) continue;

                    // Calculate distance between room centers
                    int dx = leafA.roomCenter.x - leafB.roomCenter.x;
                    int dy = leafA.roomCenter.y - leafB.roomCenter.y;
                    int distance = dx*dx + dy*dy; // Square distance is enough for comparing

                    // Skip if either room already has 3 doors
                    if (roomConnections.get(roomA).size() >= 3 || roomConnections.get(roomB).size() >= 3) {
                        continue;
                    }

                    // Determine the best direction for connection
                    Room.Direction dirAtoB, dirBtoA;

                    if (Math.abs(dx) > Math.abs(dy)) {
                        // More horizontal distance
                        if (dx > 0) {
                            dirAtoB = Room.Direction.WEST;
                            dirBtoA = Room.Direction.EAST;
                        } else {
                            dirAtoB = Room.Direction.EAST;
                            dirBtoA = Room.Direction.WEST;
                        }
                    } else {
                        // More vertical distance
                        if (dy > 0) {
                            dirAtoB = Room.Direction.NORTH;
                            dirBtoA = Room.Direction.SOUTH;
                        } else {
                            dirAtoB = Room.Direction.SOUTH;
                            dirBtoA = Room.Direction.NORTH;
                        }
                    }

                    // Check if these directions are available
                    if (!roomA.getDoors().containsKey(dirAtoB) && !roomB.getDoors().containsKey(dirBtoA)) {
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            mainGroupRoom = roomA;
                            currentGroupRoom = roomB;
                            mainToCurrentDir = dirAtoB;
                            currentToMainDir = dirBtoA;
                        }
                    }
                }
            }

            // Connect the best pair if found
            if (mainGroupRoom != null && currentGroupRoom != null) {
                mainGroupRoom.addDoor(mainToCurrentDir, currentGroupRoom);
                currentGroupRoom.addDoor(currentToMainDir, mainGroupRoom);

                // Record the connection
                roomConnections.get(mainGroupRoom).put(mainToCurrentDir, currentGroupRoom);
                roomConnections.get(currentGroupRoom).put(currentToMainDir, mainGroupRoom);

                // Add the current group to the main group for future connections
                mainGroup.addAll(currentGroup);
            }
        }
    }

    /**
     * Find all disconnected groups of rooms in the dungeon
     * @return List of sets, where each set contains connected rooms
     */
    private List<Set<Room>> findRoomGroups() {
        List<Set<Room>> roomGroups = new ArrayList<>();
        Set<Room> processedRooms = new HashSet<>();

        // Process each unprocessed room
        for (Room startRoom : rooms) {
            if (processedRooms.contains(startRoom)) {
                continue; // Skip if already in a group
            }

            // Create a new group starting from this room
            Set<Room> currentGroup = new HashSet<>();
            Queue<Room> queue = new LinkedList<>();

            queue.add(startRoom);
            currentGroup.add(startRoom);
            processedRooms.add(startRoom);

            // BFS to find all connected rooms
            while (!queue.isEmpty()) {
                Room currentRoom = queue.poll();

                // Add all connected rooms to the group
                for (Room connectedRoom : roomConnections.get(currentRoom).values()) {
                    if (!processedRooms.contains(connectedRoom)) {
                        queue.add(connectedRoom);
                        currentGroup.add(connectedRoom);
                        processedRooms.add(connectedRoom);
                    }
                }
            }

            // Add the group to our list
            roomGroups.add(currentGroup);
        }

        return roomGroups;
    }

    /**
     * Add a few extra connections between rooms for variety
     * Limited to avoid creating too many doors in a room
     */
    private void addExtraConnections() {
        // Skip for small dungeons
        if (rooms.size() <= 3) return;

        // Number of extra connections to add (limited)
        int extraConnections = Math.min(Math.max(1, rooms.size() / 6), 2);

        for (int i = 0; i < extraConnections; i++) {
            // Pick two random rooms not already connected
            int attempts = 0;
            int maxAttempts = 20;

            while (attempts < maxAttempts) {
                int roomIndex1 = MathUtils.random(rooms.size() - 1);
                int roomIndex2 = MathUtils.random(rooms.size() - 1);

                // Skip if same room
                if (roomIndex1 == roomIndex2) {
                    attempts++;
                    continue;
                }

                Room room1 = rooms.get(roomIndex1);
                Room room2 = rooms.get(roomIndex2);

                // Skip if rooms already have too many doors (max 3 doors per room)
                if (roomConnections.get(room1).size() >= 3 || roomConnections.get(room2).size() >= 3) {
                    attempts++;
                    continue;
                }

                // Check if already connected
                if (isConnected(room1, room2)) {
                    attempts++;
                    continue;
                }

                BSPLeaf leaf1 = findLeafContainingRoom(room1);
                BSPLeaf leaf2 = findLeafContainingRoom(room2);

                if (leaf1 == null || leaf2 == null) {
                    attempts++;
                    continue;
                }

                // Determine directions based on relative positions
                Room.Direction dir1, dir2;

                if (Math.abs(leaf1.roomCenter.x - leaf2.roomCenter.x) >
                        Math.abs(leaf1.roomCenter.y - leaf2.roomCenter.y)) {
                    // More horizontal than vertical distance
                    if (leaf1.roomCenter.x < leaf2.roomCenter.x) {
                        dir1 = Room.Direction.EAST;
                        dir2 = Room.Direction.WEST;
                    } else {
                        dir1 = Room.Direction.WEST;
                        dir2 = Room.Direction.EAST;
                    }
                } else {
                    // More vertical than horizontal distance
                    if (leaf1.roomCenter.y < leaf2.roomCenter.y) {
                        dir1 = Room.Direction.SOUTH;
                        dir2 = Room.Direction.NORTH;
                    } else {
                        dir1 = Room.Direction.NORTH;
                        dir2 = Room.Direction.SOUTH;
                    }
                }

                // Add doors if those directions aren't used yet
                if (!room1.getDoors().containsKey(dir1) && !room2.getDoors().containsKey(dir2)) {
                    room1.addDoor(dir1, room2);
                    room2.addDoor(dir2, room1);

                    // Record connections
                    roomConnections.get(room1).put(dir1, room2);
                    roomConnections.get(room2).put(dir2, room1);
                    break;
                }

                attempts++;
            }
        }
    }

    /**
     * Check if two rooms are already connected
     */
    private boolean isConnected(Room room1, Room room2) {
        // Check if room1 has a connection to room2
        for (Room connectedRoom : roomConnections.get(room1).values()) {
            if (connectedRoom == room2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if all rooms are reachable from room 0
     * Used for debugging to validate the dungeon connectivity
     */
    private boolean validateConnectivity() {
        if (rooms.isEmpty()) {
            return false;
        }

        // Start from room 0
        Room startRoom = rooms.getFirst();
        Set<Room> reachableRooms = new HashSet<>();
        Queue<Room> queue = new LinkedList<>();

        queue.add(startRoom);
        reachableRooms.add(startRoom);

        // BFS to find all reachable rooms
        while (!queue.isEmpty()) {
            Room currentRoom = queue.poll();

            // Add all connected rooms
            for (Room connectedRoom : roomConnections.get(currentRoom).values()) {
                if (!reachableRooms.contains(connectedRoom)) {
                    queue.add(connectedRoom);
                    reachableRooms.add(connectedRoom);
                }
            }
        }

        // All rooms should be reachable
        return reachableRooms.size() == rooms.size();
    }

    /**
     * Log all room connections to the console
     * Using our direct room connection map
     */
    private void logRoomConnections() {
        System.out.println("\n=== ROOM CONNECTIONS (BSP Dungeon) ===");

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            Map<Room.Direction, Room> connections = roomConnections.get(room);

            StringBuilder sb = new StringBuilder();
            sb.append("Room ").append(i);
            sb.append(" (").append(room.getRoomType()).append(")");
            sb.append(" -> Connects to: ");

            if (connections.isEmpty()) {
                sb.append("NO CONNECTIONS!");
            } else {
                List<String> connectionStrings = new ArrayList<>();

                for (Map.Entry<Room.Direction, Room> entry : connections.entrySet()) {
                    Room.Direction direction = entry.getKey();
                    Room connectedRoom = entry.getValue();
                    int connectedRoomIndex = rooms.indexOf(connectedRoom);

                    // Double-check the connection is bidirectional
                    boolean bidirectional = false;
                    Room.Direction oppositeDirection = getOppositeDirection(direction);

                    if (roomConnections.get(connectedRoom).containsKey(oppositeDirection)) {
                        Room backConnectedRoom = roomConnections.get(connectedRoom).get(oppositeDirection);
                        if (backConnectedRoom == room) {
                            bidirectional = true;
                        }
                    }

                    String status = bidirectional ? "" : " (ONE-WAY!)";
                    connectionStrings.add("Room " + connectedRoomIndex + " (" + direction + ")" + status);
                }

                sb.append(String.join(", ", connectionStrings));
            }

            System.out.println(sb);
        }

        // Print door counts and connectivity validation
        System.out.println("\n--- Door counts and Connectivity ---");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int doorCount = room.getDoors().size();
            System.out.println("Room " + i + ": " + doorCount + " doors");
        }

        boolean isFullyConnected = validateConnectivity();
        System.out.println("Dungeon is fully connected: " + isFullyConnected);

        System.out.println("=====================================\n");
    }

    /**
     * Get the opposite direction
     */
    private Room.Direction getOppositeDirection(Room.Direction direction) {
        return switch (direction) {
            case NORTH -> Room.Direction.SOUTH;
            case SOUTH -> Room.Direction.NORTH;
            case EAST -> Room.Direction.WEST;
            case WEST -> Room.Direction.EAST;
            default -> direction;
        };
    }

    /**
     * Assign a room type based on index and with some randomness
     */
    private void assignRoomType(Room room, int index) {
        // First room is always easier
        if (index == 0) {
            room.setRoomType(DungeonLevel.RoomType.EMPTY);
            return;
        }

        // Last room is special
        if (index == TargetLeafCount - 1) {
            room.setRoomType(DungeonLevel.RoomType.SPECIAL);
            return;
        }

        // Others have randomized types with increasing difficulty
        int typeRoll = MathUtils.random(100);
        DungeonLevel.RoomType type = getRoomType((float) index, typeRoll);

        room.setRoomType(type);
    }

    private DungeonLevel.RoomType getRoomType(float index, int typeRoll) {
        DungeonLevel.RoomType type;

        // Adjust probabilities based on room index (later rooms more difficult)
        float progressFactor = index / TargetLeafCount;

        if (typeRoll < 20 - progressFactor * 15) {
            type = DungeonLevel.RoomType.EMPTY;
        } else if (typeRoll < 50 - progressFactor * 20) {
            type = DungeonLevel.RoomType.OBSTACLE_LIGHT;
        } else if (typeRoll < 80 - progressFactor * 10) {
            type = DungeonLevel.RoomType.OBSTACLE_MEDIUM;
        } else if (typeRoll < 95) {
            type = DungeonLevel.RoomType.OBSTACLE_HEAVY;
        } else {
            type = DungeonLevel.RoomType.SPECIAL;
        }
        return type;
    }

    /**
     * Generate obstacles inside a room
     */
    private void generateRoomObstacles(Room room) {
        DungeonLevel.RoomType type = room.getRoomType();

        int minObstacles = 0;
        int maxObstacles = 0;

        switch (type) {
            case EMPTY -> maxObstacles = 1;
            case OBSTACLE_LIGHT -> {
                minObstacles = 2;
                maxObstacles = 4;
            }
            case OBSTACLE_MEDIUM -> {
                minObstacles = 4;
                maxObstacles = 7;
            }
            case OBSTACLE_HEAVY -> {
                minObstacles = 7;
                maxObstacles = 12;
            }
            case SPECIAL -> {
                minObstacles = 5;
                maxObstacles = 8;
            }
        }

        // Generate obstacles
        room.generateObstacles(minObstacles, maxObstacles);
    }

    /**
     * BSP Leaf class (represents a node in the BSP tree)
     */
    private static class BSPLeaf {
        int x, y, width, height;
        BSPLeaf leftChild, rightChild;
        Room room;
        Point roomCenter;

        public BSPLeaf(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}