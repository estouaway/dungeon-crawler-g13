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
     * Simplified BSP tree creation - only keeps division orientation
     */
    private void createBSPTree() {
        // Criar raiz com coordenadas lógicas (0,0)
        rootLeaf = new BSPLeaf(0, 0);
        leaves.add(rootLeaf);

        // Dividir recursivamente até ter salas suficientes
        boolean splitOccurred = true;
        while (splitOccurred && countLeaves() < TargetLeafCount) {
            splitOccurred = false;

            // Copiar lista atual para evitar modificação concorrente
            List<BSPLeaf> currentLeaves = new ArrayList<>(leaves);

            // Tentar dividir cada folha
            for (BSPLeaf leaf : currentLeaves) {
                // Pular folhas já divididas
                if (leaf.leftChild != null || leaf.rightChild != null) {
                    continue;
                }

                // Dividir folha
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
     * Extremely simplified split - just creates children with division orientation
     */
    private boolean splitLeaf(BSPLeaf leaf) {
        // Verificar se já foi dividida
        if (leaf.leftChild != null || leaf.rightChild != null) {
            return false;
        }

        // Decidir direção da divisão aleatoriamente ou baseado em alguma lógica simples
        boolean horizontalSplit = MathUtils.randomBoolean();
        leaf.horizontalSplit = horizontalSplit;

        // Criar filhos com coordenadas lógicas relativas
        if (horizontalSplit) {
            // Divisão horizontal (cima/baixo)
            leaf.leftChild = new BSPLeaf(leaf.x, leaf.y - 1); // Filho acima
            leaf.rightChild = new BSPLeaf(leaf.x, leaf.y + 1); // Filho abaixo
        } else {
            // Divisão vertical (esquerda/direita)
            leaf.leftChild = new BSPLeaf(leaf.x - 1, leaf.y); // Filho à esquerda
            leaf.rightChild = new BSPLeaf(leaf.x + 1, leaf.y); // Filho à direita
        }

        // Adicionar à lista
        leaves.add(leaf.leftChild);
        leaves.add(leaf.rightChild);

        return true;
    }

    /**
     * Generate rooms based on the BSP tree
     */
    private void generateRooms() {
        List<BSPLeaf> terminalLeaves = new ArrayList<>();
        for (BSPLeaf leaf : leaves) {
            if (leaf.leftChild == null && leaf.rightChild == null) {
                terminalLeaves.add(leaf);
            }
        }

        // Ordenar folhas por "distância" do centro lógico (0,0)
        terminalLeaves.sort((a, b) -> {
            double distA = Math.sqrt(a.x * a.x + a.y * a.y);
            double distB = Math.sqrt(b.x * b.x + b.y * b.y);
            return Double.compare(distA, distB);
        });

        // Criar salas
        int roomsToCreate = Math.min(TargetLeafCount, terminalLeaves.size());
        for (int i = 0; i < roomsToCreate; i++) {
            BSPLeaf leaf = terminalLeaves.get(i);

            // Criar sala
            Room room = new Room(i);

            // Armazenar centro para cálculos de direção
            leaf.roomCenter = new Point(leaf.x, leaf.y);
            leaf.room = room;

            // Atribuir tipo de sala baseado no índice
            assignRoomType(room, i);

            // Gerar obstáculos
            generateRoomObstacles(room);

            // Adicionar à lista
            rooms.add(room);

            // Inicializar mapa de conexões
            roomConnections.put(room, new HashMap<>());
        }

    }

    /**
     * Connect rooms based on the BSP tree structure
     * Each connection follows a path through the BSP tree
     */
    private void connectRooms(BSPLeaf leaf) {
        // folhas terminais sai do metodo
        // so nos interessa os nós internos q sao esses q geram ligacoes
        if (leaf.leftChild == null || leaf.rightChild == null) {
            return;
        }

        // encontrar as duas salas de cada lado da divisao
        Room leftRoom = findClosestRoomInSubtree(leaf.leftChild);
        Room rightRoom = findClosestRoomInSubtree(leaf.rightChild);

        // se ambos existem, criar ligacao
        if (leftRoom != null && rightRoom != null) {
            connectRoomsWithDoors(leftRoom, rightRoom, leaf);
        }

        // chamada recursiva para todos os nós filho para garantir q todas as salas fiquem ligadas
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
        BSPLeaf leaf1 = findLeafContainingRoom(room1);
        BSPLeaf leaf2 = findLeafContainingRoom(room2);

        if (leaf1 == null || leaf2 == null) {
            return;
        }

        // Determinar direções baseadas na orientação da divisão
        Room.Direction dir1, dir2;

        if (parentLeaf.horizontalSplit) {
            // Divisão horizontal - salas estão acima/abaixo uma da outra
            if (leaf1.y < leaf2.y) {
                dir1 = Room.Direction.SOUTH;
                dir2 = Room.Direction.NORTH;
            } else {
                dir1 = Room.Direction.NORTH;
                dir2 = Room.Direction.SOUTH;
            }
        } else {
            // Divisão vertical - salas estão à esquerda/direita uma da outra
            if (leaf1.x < leaf2.x) {
                dir1 = Room.Direction.EAST;
                dir2 = Room.Direction.WEST;
            } else {
                dir1 = Room.Direction.WEST;
                dir2 = Room.Direction.EAST;
            }
        }

        // Adicionar portas se as direções estiverem livres
        if (!room1.getDoors().containsKey(dir1) && !room2.getDoors().containsKey(dir2)) {
            room1.addDoor(dir1, room2);
            room2.addDoor(dir2, room1);

            // Registrar conexões
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
        // vai buscar todos os grupos de salas que estao desconectadas umas das outras
        List<Set<Room>> roomGroups = findRoomGroups();

        // se so houver um grupo, dungeon esta completa, todas as salas tao ligadas.
        if (roomGroups.size() <= 1) {
            return;
        }

        // ordenar os grupos de sala do maior para o menor
        roomGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));

        // colocamos o grupo maior cmo principal
        Set<Room> mainGroup = roomGroups.getFirst();

        // iniciamos o loop sobre os grupos restantes
        for (int i = 1; i < roomGroups.size(); i++) {
            Set<Room> currentGroup = roomGroups.get(i);

            Room mainGroupRoom = null;
            Room currentGroupRoom = null;
            Room.Direction mainToCurrentDir = null;
            Room.Direction currentToMainDir = null;
            int bestDistance = Integer.MAX_VALUE;


            // para cada sala dos grupos, vms buscar a leaf para ter as coordenadas do roomcenter
            for (Room roomA : mainGroup) {
                BSPLeaf leafA = findLeafContainingRoom(roomA);
                if (leafA == null) continue;

                for (Room roomB : currentGroup) {
                    // skip se as salas ja tem 3 portas
                    if (roomConnections.get(roomA).size() >= 3 || roomConnections.get(roomB).size() >= 3) {
                        continue;
                    }

                    BSPLeaf leafB = findLeafContainingRoom(roomB);
                    if (leafB == null) continue;

                    // calcula a distancia entre as duas salas
                    int dx = leafA.roomCenter.x - leafB.roomCenter.x;
                    int dy = leafA.roomCenter.y - leafB.roomCenter.y;
                    int distance = dx*dx + dy*dy; //  a distancia quadratica é suficiente para comparacao

                    // Determine the best direction for connection
                    Room.Direction dirAtoB, dirBtoA;

                    if (Math.abs(dx) > Math.abs(dy)) {
                        // mais distancia horizontal
                        if (dx > 0) {
                            dirAtoB = Room.Direction.WEST;
                            dirBtoA = Room.Direction.EAST;
                        } else {
                            dirAtoB = Room.Direction.EAST;
                            dirBtoA = Room.Direction.WEST;
                        }
                    } else {
                        // mais distancia vertical
                        if (dy > 0) {
                            dirAtoB = Room.Direction.NORTH;
                            dirBtoA = Room.Direction.SOUTH;
                        } else {
                            dirAtoB = Room.Direction.SOUTH;
                            dirBtoA = Room.Direction.NORTH;
                        }
                    }
                    // verifica se as direcoes estao disponiveis
                    if (!roomA.getDoors().containsKey(dirAtoB) && !roomB.getDoors().containsKey(dirBtoA)) {
                        if (distance < bestDistance) {
                            // se a distancia entre as salas for menos que a melhor distancia encontrada
                            // esta passa a ser a melhor distancia
                            bestDistance = distance;
                            mainGroupRoom = roomA;
                            currentGroupRoom = roomB;
                            mainToCurrentDir = dirAtoB;
                            currentToMainDir = dirBtoA;
                        }
                    }
                }
            }

            // ligar o melhor par
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

        for (Room startRoom : rooms) {
            if (processedRooms.contains(startRoom)) {
                continue; // skip se sala ja foi processada
            }

            Set<Room> currentGroup = new HashSet<>();
            Queue<Room> queue = new LinkedList<>();

            queue.add(startRoom);
            currentGroup.add(startRoom);
            processedRooms.add(startRoom);

            // BFS busca em largura
            // ciclo enquanto houver salas na queue.
            while (!queue.isEmpty()) {
                // vai buscar a proxima sala na queue e remove-a da queue
                Room currentRoom = queue.poll();

                // verifica se a sala ja tem ligacao e retorna-as
                for (Room connectedRoom : roomConnections.get(currentRoom).values()) {
                    // verifica as q foram processadas e adiciona-as a queue
                    if (!processedRooms.contains(connectedRoom)) {
                        queue.add(connectedRoom);
                        currentGroup.add(connectedRoom);
                        processedRooms.add(connectedRoom);
                    }
                }
            }

            // qd a busca termina o grupo devera conter todas as salas ligadas entre si
            // o  loop principal vai continuar para encontrar outros grupos , se houverem
            roomGroups.add(currentGroup);
        }

        // no fim retorna todos os grupos ligados
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
        if (index == 0) {
            room.setRoomType(DungeonLevel.RoomType.EMPTY);
            return;
        }

        // Last room is special
        if (index == TargetLeafCount - 1) {
            room.setRoomType(DungeonLevel.RoomType.SPECIAL);
            return;
        }

        // Calculate progression factor (0.0 to 1.0)
        float progressFactor = (float)index / TargetLeafCount;

        // Random roll with adjusted probabilities based on progression
        int typeRoll = MathUtils.random(100);

        // Determine room type based on roll and progression
        DungeonLevel.RoomType type;

        // TODO Change obstacles to enemie density for example
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

        room.setRoomType(type);
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
        int x, y; // Apenas para posição lógica, não precisamos de coordenadas precisas
        BSPLeaf leftChild, rightChild;
        Room room;
        Point roomCenter; // Manter para cálculo de direções
        boolean horizontalSplit; // Para armazenar a orientação da divisão

        public BSPLeaf(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}