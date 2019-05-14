package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class Game implements Serializable {

    /* Feel free to change the width and height. */

    public static final int HEIGHT = 30;
    public static final int WIDTH = 60;
    public TETile[][] world;
    Position[][] worldP;
    Position player;
    Position AI;
    Position cur;
    Position door;
    List<Character> route;
    int seed;
    int level = 1;
    TERenderer ter;

    class Node {
        Position position;
        Node prev;
        int numSteps;
        char move;
        Node(Position p, Node pre, int n, char mv) {
            position = p;
            prev = pre;
            numSteps = n;
            move = mv;
        }
        int priority() {
            double distance = Math.abs(position.Px - door.Px) + Math.abs(position.Py - door.Py);
//            if(this.position.Tile == Tileset.COIN){
//                return 0;
//            }
            return (int) (Math.pow(distance, 0.5) + numSteps);
        }
        List<Node> neighbors() {
            List<Node> result = new ArrayList<>();
            if (this.position.left(worldP).Tile != Tileset.WALL && this.position.left(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.left(worldP), this, numSteps + 1, 'a'));
            if (this.position.right(worldP).Tile != Tileset.WALL && this.position.right(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.right(worldP), this, numSteps + 1, 'd'));
            if (this.position.up(worldP).Tile != Tileset.WALL && this.position.up(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.up(worldP), this, numSteps + 1, 'w'));
            if (this.position.down(worldP).Tile != Tileset.WALL && this.position.down(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.down(worldP), this, numSteps + 1, 's'));
            return result;
        }
        boolean isExit() {
            return position.Tile == Tileset.LOCKED_DOOR;
        }
    }

    public void findRoute(Boolean hard) {
        List<Position> visited = new ArrayList<>();
        route = new LinkedList<>();
        Node start = new Node(AI, null, 0, '0');
        Node pointer = start;
        PriorityQueue<Node> pq = new PriorityQueue<>((n1, n2) -> n1.priority() - n2.priority());
        while (!pointer.isExit()) {
            for (Node neighb : pointer.neighbors()) {
                if ((pointer.prev == null || !pointer.prev.position.equals(neighb.position)) && !visited.contains(neighb.position)) {
                    pq.add(neighb);
                    visited.add(neighb.position);
                }
            }
            pointer = pq.poll();
          //  System.out.println(pointer.numSteps + " " + pointer.move + " " + pointer.prev.numSteps);
        }
        int i = 1;
        while (pointer != null) {
           // System.out.println("step " + i++ + pointer.move);
            for(Node neighb:pointer.neighbors()) {
//                if()
//                    break;
                if (neighb.position.Tile == Tileset.COIN && pointer.position.Tile != Tileset.COIN) {
                    char backMove;
                    if (neighb.move == 'a') backMove = 'd';
                    else if (neighb.move == 'd') backMove = 'a';
                    else if (neighb.move == 's') backMove = 'w';
                    else backMove = 's';
                    route.add(backMove);
                    System.out.println(neighb.move + " route changed");
                    route.add(neighb.move);
                }
            }
//            for(Node neighb:pointer.neighbors()) {
//                char backMove1;
//                if (neighb.move == 'a') backMove1 = 'd';
//                else if (neighb.move == 'd') backMove1 = 'a';
//                else if (neighb.move == 's') backMove1 = 'w';
//                else backMove1 = 's';
//                if(neighb.position.Tile == Tileset.COIN || neighb.position.Tile == Tileset.FLOOR) {
//                    for (Node neighb2 : neighb.neighbors()) {
//                        if (neighb2.position.Tile == Tileset.COIN) {
//                            char backMove2;
//                            if (neighb2.move == 'a') backMove2 = 'd';
//                            else if (neighb2.move == 'd') backMove2 = 'a';
//                            else if (neighb2.move == 's') backMove2 = 'w';
//                            else backMove2 = 's';
//                            route.add(backMove2);
//                            route.add(backMove1);
//                            route.add(neighb.move);
//                            route.add(neighb2.move);
//                        }
//                    }
//               }
  //          }
            route.add(pointer.move);
            pointer = pointer.prev;
        }
        System.out.println(route.size());
        Collections.reverse(route);
    }

    public TETile[][] genWorld(int seed, Boolean hard) {
        Random random = new Random(seed);
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        world = new TETile[WIDTH][HEIGHT];
        worldP = Position.initializeP(world);
        Room[] rooms = Room.drawRandomRooms(worldP, RandomUtils.uniform(random, 7, 15), random);
        Room.directConnectAll(worldP, rooms);
        Room.makeTurn(worldP, random);
        Position.connectAll(worldP, random);
        door = Position.addDoor(worldP, random);
        System.out.println("door: " + door.Px + "," + door.Py);
        player = Position.addPlayer(worldP, random);
        AI = Position.addAI(worldP, random);
        cur = AI;
        if(hard){
            Position.addCoinsTraps(worldP,random,5,3);
        }
        findRoute(hard);
        return world;
    }

    public void moveCharacter() {
        String input = "level " + level + " movements: ";
        int n = level;
        int i = 0;
        while (i < 100) {
            if (StdDraw.hasNextKeyTyped()) {
                char nextMove = StdDraw.nextKeyTyped();
                //char nextMove = 'a';
                if (nextMove == 'Q') {
                    try {
                        saveGame(this, "game1.save");
                    } catch (Exception e) {
                        System.out.println("Couldn't save " + e.getMessage());
                    }
                    break;
                }
                input += nextMove;
                movePlayer(nextMove);

                AIPlayer();
                if (level != n) {
                    i = 0;
                    n++;
                    System.out.println(input);
                    input = "level " + n + " movements: ";
                }
                ter.renderFrame(world);
                i++;
            }
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            if (mouseX < 60 && mouseX >= 0 && mouseY < 30 && mouseY >= 0) {
                ter.renderFrame(world);
                StdDraw.setPenColor(Color.white);
                if (world[mouseX][mouseY].equals(Tileset.FLOOR)) {
                    StdDraw.textLeft(0.5, 29, "floor");
                }
                if (world[mouseX][mouseY].equals(Tileset.WALL)) {
                    StdDraw.textLeft(0.5, 29, "wall");
                }
                if (world[mouseX][mouseY].equals(Tileset.LOCKED_DOOR)) {
                    StdDraw.textLeft(0.5, 29, "locked door");
                }
                if (world[mouseX][mouseY].equals(Tileset.PLAYER)) {
                    StdDraw.textLeft(0.5, 29, "You");
                }
                if (world[mouseX][mouseY].equals(Tileset.FLOWER)) {
                    StdDraw.textLeft(0.5, 29, "AI player");
                }
                if (world[mouseX][mouseY].equals(Tileset.COIN)) {
                    StdDraw.textLeft(0.5, 29, "Coin");
                }
                if (world[mouseX][mouseY].equals(Tileset.TRAP)) {
                    StdDraw.textLeft(0.5, 29, "Trap");
                }
                StdDraw.show();
                StdDraw.pause(200);
                StdDraw.enableDoubleBuffering();
            }
        }
        if (i == 100) {
            this.drawLose();
            StdDraw.pause(2000);
        }
        System.out.println(input);
    }

    public void moveAI() {
        if (route.isEmpty()) {
            this.drawWin(AI.down(worldP), seed + ++level);
            return;
        }
        char nextMove = route.remove(0);
        if (nextMove == 'a') {
            AI.left(worldP).setTile(Tileset.FLOWER);
            AI.setTile(Tileset.FLOOR);
            AI = AI.left(worldP);
            cur = cur.left(worldP);
        }
        if (nextMove == 'd') {
            AI.right(worldP).setTile(Tileset.FLOWER);
            AI.setTile(Tileset.FLOOR);
            AI = AI.right(worldP);
            cur = cur.right(worldP);
        }
        if (nextMove == 'w') {
            AI.up(worldP).setTile(Tileset.FLOWER);
            AI.setTile(Tileset.FLOOR);
            AI = AI.up(worldP);
            cur = cur.up(worldP);
        }
        if (nextMove == 's') {
            AI.down(worldP).setTile(Tileset.FLOWER);
            AI.setTile(Tileset.FLOOR);
            AI = AI.down(worldP);
            cur = cur.down(worldP);
        }

    }


    public void movePlayer(char nextMove) {
        if (nextMove == 'a' && player.left(worldP).Tile.equals(Tileset.FLOOR)) {
            player.left(worldP).setTile(Tileset.PLAYER);
            player.setTile(Tileset.FLOOR);
            player = player.left(worldP);
        }
        if (nextMove == 'd' && player.right(worldP).Tile.equals(Tileset.FLOOR)) {
            player.right(worldP).setTile(Tileset.PLAYER);
            player.setTile(Tileset.FLOOR);
            player = player.right(worldP);
        }
        if (nextMove == 'w' && player.up(worldP).Tile.equals(Tileset.FLOOR)) {
            player.up(worldP).setTile(Tileset.PLAYER);
            player.setTile(Tileset.FLOOR);
            player = player.up(worldP);
        }
        if (nextMove == 's' && player.down(worldP).Tile.equals(Tileset.FLOOR)) {
            player.down(worldP).setTile(Tileset.PLAYER);
            player.setTile(Tileset.FLOOR);
            player = player.down(worldP);
        }
        if (nextMove == 'a' && player.left(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.left(worldP), seed + ++level);
            return;
        }
        if (nextMove == 'd' && player.right(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.right(worldP), seed + ++level);
            return;
        }
        if (nextMove == 's' && player.down(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.down(worldP), seed + ++level);
            return;
        }
        if (nextMove == 'w' && player.up(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.up(worldP), seed + ++level);
            return;
        }
    }

    public void drawWin(Position p, int level) {
        int realLevel = level - seed;
        p.setTile(Tileset.UNLOCKED_DOOR);
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.white);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(30, 15, "You Win! Next level: " + realLevel);
        StdDraw.show();
        StdDraw.pause(1000);
        genWorld(level, true);

    }

    public void drawLose() {
        StdDraw.setPenColor(Color.white);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(30, 15, "You Lose!");
        StdDraw.show();
    }


    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public static void drawMenu() {
        StdDraw.setCanvasSize(40 * 16, 40 * 16);
        StdDraw.clear(Color.black);
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.white);
        StdDraw.text(0.5, 0.7, "Game: AI-MAZE-ING");
        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(0.5, 0.45, "New Game(N)");
        StdDraw.text(0.5, 0.4, "Load Game(L)");
        StdDraw.text(0.5, 0.35, "Quit(Q)");
        StdDraw.show();
    }

    public void playWithKeyboard() {
        drawMenu();
        int seed = 0;
        String seedS = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Boolean hardMode = false;
                char nextKey = StdDraw.nextKeyTyped();
                if (nextKey == 'N' || nextKey == 'n') {
                    StdDraw.textLeft(0.25, 0.25, "New game_ please enter a seed:");
                    StdDraw.show();
                    StdDraw.pause(1000);
                }
                if (nextKey - '0' >= 0 && nextKey - '0' < 10) {
                    seedS += String.valueOf(nextKey);
                    seed = Integer.parseInt(seedS);
                    System.out.println(seedS);
                    StdDraw.textLeft(0.25, 0.25, "New game_ please enter a seed:" + seedS);
                    StdDraw.show();
                    StdDraw.pause(200);
                }
                if (nextKey == 's' || nextKey == 'S') {
                    this.seed = seed;
                    if(!hardMode){
                        genWorld(seed, true);
                        ter.renderFrame(world);
                        moveCharacter();
                        return;
                    }else {

                    }
                }
                if (nextKey == 'l' || nextKey == 'L') {
                    try {
                        Game loaded = loadGame("game1.save");
                        ter = new TERenderer();
                        ter.initialize(WIDTH, HEIGHT);
                        ter.renderFrame(loaded.world);
                        loaded.moveCharacter();
                        return;
                    } catch (Exception e) {
                        System.out.println("Couldn't load " + e.getMessage());
                    }
                }
                if (nextKey == 'Q' || nextKey == 'q') {
                    return;
                }
            }
        }
    }

    public TETile[][] playWithInputString(String input) {

        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        char first = input.charAt(0);
        if (first == 'n' || first == 'N') {
            String seedS = "";
            for (int i = 0; i < input.length(); i++) {
                char ithC = input.charAt(i);
                int ith = ithC - '0';
                if (ith < 9 && ith > 0) {
                    seedS = seedS + ith;
                    seed = Integer.parseInt(seedS);
                }
                if (ithC == 'S') {
                    genWorld(seed, true);
                }
                if (ithC == 'a' || ithC == 's' || ithC == 'w' || ithC == 'd') {
                    movePlayer(ithC);
                }
                if (ithC == 'Q') {
                    try {
                        saveGame(this, "game1.save");
                    } catch (Exception e) {
                        System.out.println("Couldn't save " + e.getMessage());
                    }
                    return world;
                }
            }
            ter.renderFrame(world);
            return world;
        } else if (first == 'l' || first == 'L') {
            try {
                Game loaded = loadGame("game1.save");
                for (int i = 1; i < input.length(); i++) {
                    char ithC = input.charAt(i);
                    if (ithC == 'a' || ithC == 's' || ithC == 'w' || ithC == 'd') {
                        loaded.movePlayer(ithC);
                    }
                    if (ithC == 'Q') {
                        saveGame(loaded, "game1.save");
                        return loaded.world;
                    }
                }
                ter.renderFrame(loaded.world);
                return loaded.world;
            } catch (Exception e) {
                System.out.println("Couldn't load " + e.getMessage());
            }
        }
        System.out.println("Input must start with N/n(new game) or L/l(load game)");
        return null;
    }

    public void saveGame(Serializable game, String filename) throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(Paths.get(filename)));
        os.writeObject(game);
    }

    public Game loadGame(String filename) throws Exception {
        ObjectInputStream is = new ObjectInputStream(Files.newInputStream(Paths.get(filename)));
        Game savedGame = (Game) is.readObject();
        return savedGame;
    }

    public void AIPlayer() {
        moveAI();
        ter.renderFrame(world);
    }

//    public void randomPlayer() {
//        moveRandom();
//        ter.renderFrame(world);
//    }
}
