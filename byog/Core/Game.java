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
    int reallevel= 1;
    TERenderer ter;
    int[] scores = {100, 100};
    Set<Position> colloected = new HashSet<>();
    Boolean[] finished = {false, false};

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
            if (this.position.Tile == Tileset.COIN && !colloected.contains(position)) {
                colloected.add(position);
                this.numSteps -= 10;
            } else if (this.position.Tile == Tileset.TRAP) {
                this.numSteps += 20;
            }
            return (int) (Math.pow(distance, 0.5) + numSteps);
        }

        List<Node> neighbors() {
            List<Node> result = new ArrayList<>();
            if (this.position.left(worldP).Tile != Tileset.WALL && this.position.left(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.left(worldP), this, numSteps + 1, 'a'));
            if (this.position.right(worldP).Tile != Tileset.WALL && this.position.left(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.right(worldP), this, numSteps + 1, 'd'));
            if (this.position.up(worldP).Tile != Tileset.WALL && this.position.left(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.up(worldP), this, numSteps + 1, 'w'));
            if (this.position.down(worldP).Tile != Tileset.WALL && this.position.left(worldP).Tile != Tileset.TRAP)
                result.add(new Node(position.down(worldP), this, numSteps + 1, 's'));
            return result;
        }

        boolean isExit() {
            return position.Tile == Tileset.LOCKED_DOOR;
        }
    }

    public void findRoute(Boolean hard) {
        Map<Position, Integer> visited = new HashMap<>();
        route = new LinkedList<>();
        colloected = new HashSet<>();
        Node start = new Node(AI, null, 0, '0');
        Node pointer = start;
        PriorityQueue<Node> pq = new PriorityQueue<>((n1, n2) -> n1.priority() - n2.priority());
        while (!pointer.isExit()) {
            for (Node neighb : pointer.neighbors()) {
                if (pointer.prev == null || visited.getOrDefault(neighb.position, Integer.MAX_VALUE) > neighb.numSteps) {
                    pq.add(neighb);
                    visited.put(neighb.position, neighb.numSteps);
                    //   System.out.println(neighb.position+ " ," + neighb.numSteps);
                }
            }
            pointer = pq.poll();
            //   System.out.println(pointer.numSteps + " " + pointer.move + " " + pointer.prev.numSteps);
        }
        while (pointer != null) {
            route.add(pointer.move);
            pointer = pointer.prev;
        }
        System.out.println(route.size());
        Collections.reverse(route);
    }

    public TETile[][] genWorld(int seed, Boolean hard) {
        Random random = new Random(seed);
        ter = new TERenderer();
        finished[0] = false;
        finished[1] = false;
        scores[0] = 100;
        scores[1] =100;
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
        if (hard) {
            Position.addCoinsTraps(worldP, random, 5, 3);
        }
        findRoute(hard);
        return world;

    }

    public void moveCharacter(Boolean hard) {

        int n = level;
        while (true) {
            if(finished[0]){
                movePlayer('x',hard);
                AIPlayer(hard);
                if (level != n) {
                    n++;
                }
                ter.renderFrame(world);
            }
            else if (StdDraw.hasNextKeyTyped()) {
                char nextMove = StdDraw.nextKeyTyped();
                if (nextMove == 'Q') {
                    try {
                        saveGame(this, "game1.save");
                    } catch (Exception e) {
                        System.out.println("Couldn't save " + e.getMessage());
                    }
                    break;
                }
                movePlayer(nextMove, hard);
                if (!finished[1]) AIPlayer(hard);
                if (level != n) {
                    n++;
                }
                ter.renderFrame(world);
            }
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            if (mouseX < 60 && mouseX >= 0 && mouseY < 30 && mouseY >= 0) {
                ter.renderFrame(world);
                StdDraw.setPenColor(Color.white);
                StdDraw.textRight(60, 29, "Your Score: " + scores[0]);
                StdDraw.textRight(60, 27, "AI Score: " + scores[1]);
                if (world[mouseX][mouseY].equals(Tileset.FLOOR)) {
                    StdDraw.textLeft(0.5, 29, "floor");
                }
                if (world[mouseX][mouseY].equals(Tileset.WALL)) {
                    StdDraw.textLeft(0.5, 29, "wall");
                }
                if (world[mouseX][mouseY].equals(Tileset.LOCKED_DOOR)) {
                    StdDraw.textLeft(0.5, 29, "locked door");
                }
                if (world[mouseX][mouseY].equals(Tileset.UNLOCKED_DOOR)) {
                    StdDraw.textLeft(0.5, 29, "unlocked door");
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
    }

    public void moveAI(Boolean mode) {
        char nextMove = route.remove(0);
        TETile nextTile = Tileset.FLOOR;
        if (nextMove == 'a') {
            if (AI.left(worldP).Tile == Tileset.COIN)
                scores[1] += 10;
            else if (AI.left(worldP).Tile == Tileset.TRAP) {
                scores[1] -= 20;
                nextTile = Tileset.TRAP;
            }
            AI.left(worldP).setTile(Tileset.FLOWER);
            AI.setTile(nextTile);
            AI = AI.left(worldP);
            cur = cur.left(worldP);
        }
        if (nextMove == 'd') {
            if (AI.right(worldP).Tile == Tileset.COIN)
                scores[1] += 10;
            else if (AI.right(worldP).Tile == Tileset.TRAP) {
                scores[1] -= 20;
                nextTile = Tileset.TRAP;
            }
            AI.right(worldP).setTile(Tileset.FLOWER);
            AI.setTile(nextTile);
            AI = AI.right(worldP);
            cur = cur.right(worldP);
        }
        if (nextMove == 'w') {
            if (AI.up(worldP).Tile == Tileset.COIN)
                scores[1] += 10;
            else if (AI.up(worldP).Tile == Tileset.TRAP) {
                scores[1] -= 20;
                nextTile = Tileset.TRAP;
            }
            AI.up(worldP).setTile(Tileset.FLOWER);
            AI.setTile(nextTile);
            AI = AI.up(worldP);
            cur = cur.up(worldP);
        }
        if (nextMove == 's') {
            if (AI.down(worldP).Tile == Tileset.COIN)
                scores[1] += 10;
            else if (AI.down(worldP).Tile == Tileset.TRAP) {
                scores[1] -= 20;
                nextTile = Tileset.TRAP;
            }
            AI.down(worldP).setTile(Tileset.FLOWER);
            AI.setTile(nextTile);
            AI = AI.down(worldP);
            cur = cur.down(worldP);
        }
        scores[1]--;
        if (route.isEmpty()) {
            finished[1] = true;
            //          if(finished[0])
            this.drawWin(AI, seed + ++level, AI, mode);
            return;
        }
    }


    public void movePlayer(char nextMove, Boolean mode) {
        TETile nextTile = Tileset.FLOOR;
        if (nextMove == 'a' && (player.left(worldP).Tile.equals(Tileset.FLOOR) || player.left(worldP).Tile.equals(Tileset.COIN))) {
            if (player.left(worldP).Tile == Tileset.COIN)
                scores[0] += 10;
            else if (player.left(worldP).Tile == Tileset.TRAP){
                nextTile = Tileset.TRAP;
                scores[0] -= 20;
            }
            player.left(worldP).setTile(Tileset.PLAYER);
            player.setTile(nextTile);
            player = player.left(worldP);
            scores[0] -= 1;
        }
        if (nextMove == 'd' && (player.right(worldP).Tile.equals(Tileset.FLOOR) || player.right(worldP).Tile.equals(Tileset.COIN))) {
            if (player.right(worldP).Tile == Tileset.COIN)
                scores[0] += 10;
            else if (player.right(worldP).Tile == Tileset.TRAP){
                nextTile = Tileset.TRAP;
                scores[0] -= 20;
            }
            player.right(worldP).setTile(Tileset.PLAYER);
            player.setTile(nextTile);
            player = player.right(worldP);
            scores[0] -= 1;
        }
        if (nextMove == 'w' && (player.up(worldP).Tile.equals(Tileset.FLOOR) || player.up(worldP).Tile.equals(Tileset.COIN))) {
            if (player.up(worldP).Tile == Tileset.COIN)
                scores[0] += 10;
            else if (player.up(worldP).Tile == Tileset.TRAP){
                nextTile = Tileset.TRAP;
                scores[0] -= 20;
            }
            player.up(worldP).setTile(Tileset.PLAYER);
            player.setTile(nextTile);
            scores[0] -= 1;
            player = player.up(worldP);
        }
        if (nextMove == 's' && (player.down(worldP).Tile.equals(Tileset.FLOOR) || player.down(worldP).Tile.equals(Tileset.COIN))) {
            if (player.down(worldP).Tile == Tileset.COIN)
                scores[0] += 10;
            else if (player.down(worldP).Tile == Tileset.TRAP){
                nextTile = Tileset.TRAP;
                scores[0] -= 20;
            }
            player.down(worldP).setTile(Tileset.PLAYER);
            player.setTile(nextTile);
            player = player.down(worldP);
            scores[0] -= 1;
        }
        if (nextMove == 'a' && (player.left(worldP).Tile.equals(Tileset.LOCKED_DOOR) || player.left(worldP).Tile.equals(Tileset.UNLOCKED_DOOR))) {
            System.out.println("YOu got to the door");

            finished[0] = true;
            player.setTile(Tileset.FLOOR);
            this.drawWin(player.left(worldP), seed + ++level, player, mode);
            return;
        }
        if (nextMove == 'd' && (player.right(worldP).Tile.equals(Tileset.LOCKED_DOOR) || player.right(worldP).Tile.equals(Tileset.UNLOCKED_DOOR))) {
            System.out.println("YOu got to the door");

            finished[0] = true;
            player.setTile(Tileset.FLOOR);
            this.drawWin(player.right(worldP), seed + ++level, player, mode);
            return;
        }
        if (nextMove == 's' && (player.down(worldP).Tile.equals(Tileset.LOCKED_DOOR) || player.down(worldP).Tile.equals(Tileset.UNLOCKED_DOOR))) {
            System.out.println("YOu got to the door");
            finished[0] = true;
            player.setTile(Tileset.FLOOR);
            this.drawWin(player.down(worldP), seed + ++level, player, mode);
            return;
        }
        if (nextMove == 'w' && (player.up(worldP).Tile.equals(Tileset.LOCKED_DOOR) || player.up(worldP).Tile.equals(Tileset.UNLOCKED_DOOR))) {
            System.out.println("YOu got to the door");
            finished[0] = true;
            player.setTile(Tileset.FLOOR);
            this.drawWin(player.up(worldP), seed + ++level, player, mode);
            return;
        }
    }


    public void drawWin(Position p, int level, Position pr, Boolean hard) {
        p.setTile(Tileset.UNLOCKED_DOOR);
        ter.renderFrame(world);
        if(finished[0] && finished[1]){
            StdDraw.setPenColor(Color.white);
            Font font = new Font("Monaco", Font.BOLD, 30);
            StdDraw.setFont(font);
            String winner = scores[0] > scores[1]? "You":"AI";
            StdDraw.text(30, 15, "Your score: " + scores[0] +" AI score: "+ scores[1]);
            StdDraw.text(30, 13, winner+ " wins by  " + Math.abs(scores[0] - scores[1]) +" points! Next Level: "+ ++reallevel);

            StdDraw.show();
            StdDraw.pause(2000);
            genWorld(level, hard);
        }
    }

    public void drawLose() {
        StdDraw.setPenColor(Color.white);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(30, 15, "You Lose: Ran out of 100 steps!");
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
//        StdDraw.text(0.5, 0.35, "Game Rule(R)");
        StdDraw.text(0.5, 0.35, "Game Mode(M)");
        StdDraw.text(0.5, 0.30, "Quit(Q)");
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
                    Font font = new Font("Monaco", Font.BOLD, 15);
                    StdDraw.setFont(font);
                    StdDraw.textLeft(0.25, 0.25, "Please choose a mode:");
                    StdDraw.textLeft(0.25, 0.20, "'B' for Basic, 'A' for advanced");
                    StdDraw.setPenColor(Color.yellow);
                    StdDraw.show();
                    StdDraw.pause(1000);
                }
                if (nextKey == 'B' || nextKey == 'b') {
//                    StdDraw.textLeft(0.25, 0.15, "New game_ please enter a seed:");
//                    StdDraw.show();
//                    if (nextKey - '0' >= 0 && nextKey - '0' < 10) {
//                        seedS += String.valueOf(nextKey);
//                        seed = Integer.parseInt(seedS);
//                        System.out.println(seedS);
//                        StdDraw.textLeft(0.25, 0.25, "New game_ please enter a seed:" + seedS);
//                        StdDraw.show();
//                        StdDraw.pause(200);
//                    }
//                    if (nextKey == 's' || nextKey == 'S') {
//                        this.seed = seed;
                    genWorld(seed, false);
                    ter.renderFrame(world);
                    moveCharacter(false);
                    return;
//                    }
                }
                if (nextKey == 'A' || nextKey == 'a') {
                    genWorld(seed, true);
                    ter.renderFrame(world);
                    moveCharacter(true);
                    return;
                }

//                if (nextKey - '0' >= 0 && nextKey - '0' < 10) {
//                    seedS += String.valueOf(nextKey);
//                    seed = Integer.parseInt(seedS);
//                    System.out.println(seedS);
//                    StdDraw.textLeft(0.25, 0.25, "New game_ please enter a seed:" + seedS);
//                    StdDraw.show();
//                    StdDraw.pause(200);
//                }
//                if (nextKey == 's' || nextKey == 'S') {
//                    this.seed = seed;
//                    genWorld(seed,true);
//                }
                if (nextKey == 'l' || nextKey == 'L') {
                    try {
                        Game loaded = loadGame("game1.save");
                        ter = new TERenderer();
                        ter.initialize(WIDTH, HEIGHT);
                        ter.renderFrame(loaded.world);
                        // need to modify this.
                        loaded.moveCharacter(true);
                        return;
                    } catch (Exception e) {
                        System.out.println("Couldn't load " + e.getMessage());
                    }
                }

//                    StdDraw.text(0.5, 0.55, "(1) You'll play against an AI player.");
//                    StdDraw.text(0.5, 0.5, "(2) Use 'AWSD' to control your direction in the maze. ");
//                    StdDraw.text(0.5, 0.35, "(5) Once arriving at the exit, you must wait until another player" +
//                            "get to the exit.");


                if (nextKey == 'm' || nextKey == 'M') {
                    StdDraw.setCanvasSize(40 * 16, 40 * 16);
                    StdDraw.clear(Color.black);
                    StdDraw.setPenColor(Color.white);
                    Font font = new Font("Monaco", Font.BOLD, 35);
                    StdDraw.setFont(font);
                    StdDraw.text(0.5, 0.75, "Basic Mode V.S Advanced Mode");
                    Font smallFont2 = new Font("Monaco", Font.BOLD, 16);
                    StdDraw.setFont(smallFont2);
                    StdDraw.text(0.5, 0.50, "Basic Mode: The player who gets to the exit first wins.");
                    StdDraw.text(0.5, 0.45, "Advanced Mode: The player who gets the higher points wins.");
                    StdDraw.text(0.5, 0.40, "You earn 10 points when getting a coin.");
                    StdDraw.text(0.5, 0.35, "You lose 20 points when falling into the trap.");
                    StdDraw.text(0.5, 0.30, "When both players get to the exit, show the winner and enter next level");
                    StdDraw.setPenColor(Color.yellow);
                    Font insFont = new Font("Monaco", Font.BOLD, 14);
                    StdDraw.text(0.5, 0.25, "Type 'B' to enter Basic Mode and 'A' to advanced Mode " +
                            "enter 'S' to start.");

                }
                if (nextKey == 'Q' || nextKey == 'q') {
                    return;
                }
            }
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // TODO: Fill out this method to run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
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
                if (ithC == 'B') {
                    genWorld(seed, false);
                }
                if (ithC == 'A') {
                    genWorld(seed, true);
                }

                if (ithC == 'a' || ithC == 's' || ithC == 'w' || ithC == 'd') {
                    movePlayer(ithC, false);
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
                        loaded.movePlayer(ithC, true);
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

    public void AIPlayer(Boolean hard) {
        moveAI(hard);
        ter.renderFrame(world);
    }

}
