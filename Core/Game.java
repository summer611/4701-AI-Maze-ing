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


public class Game implements Serializable{

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
    int level=1;
    TERenderer ter;

    class Node{
        Position position;
        Node prev;
        int numSteps;
        char move;

        Node(Position p, Node pre, int n, char mv){
            position = p;
            prev = pre;
            numSteps = n;
            move = mv;
        }

        int priority(){
            double distance = Math.abs(position.Px - door.Px) + Math.abs(position.Py - door.Py);
            return (int)(Math.pow(distance, 0.5) + numSteps);
        }

        List<Node> neighbors(){
            List<Node> result = new ArrayList<>();
            if(this.position.left(worldP).Tile != Tileset.WALL)
                result.add(new Node(position.left(worldP), this, numSteps + 1, 'a'));
            if(this.position.right(worldP).Tile != Tileset.WALL)
                result.add(new Node(position.right(worldP), this, numSteps + 1, 'd'));
            if(this.position.up(worldP).Tile != Tileset.WALL)
                result.add(new Node(position.up(worldP), this, numSteps + 1, 'w'));
            if(this.position.down(worldP).Tile != Tileset.WALL)
                result.add(new Node(position.down(worldP), this, numSteps + 1, 's'));
            return result;
        }

        boolean isExit(){
            return position.Tile == Tileset.LOCKED_DOOR;
        }
    }

    public void findRoute(){
        List<Position> visited = new ArrayList<>();
        route = new LinkedList<>();
        Node start = new Node(AI, null, 0, '0');
        Node pointer = start;
        PriorityQueue<Node> pq = new PriorityQueue<>((n1, n2) -> n1.priority()-n2.priority());
        while (!pointer.isExit()){
            for(Node neighb : pointer.neighbors()){
                if((pointer.prev == null || !pointer.prev.position.equals(neighb.position)) && !visited.contains(neighb.position)){
                    pq.add(neighb);
                    visited.add(neighb.position);
                }
            }
            pointer = pq.poll();
            System.out.println(pointer.numSteps + " " + pointer.move + " " + pointer.prev.numSteps);
        }
        int i = 1;
        while(pointer != null){
            System.out.println("step " + i++ + pointer.move);
            route.add(pointer.move);
            pointer = pointer.prev;
        }
        System.out.println(route.size());
        Collections.reverse(route);
    }

    public TETile[][] genWorld(int seed) {
        Random random = new Random(seed);
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        world = new TETile[WIDTH][HEIGHT];
        worldP = Position.initializeP(world);
        Room[] rooms = Room.drawRandomRooms(worldP, RandomUtils.uniform(random, 7, 15), random);
        Room.directConnectAll(worldP, rooms);
       // System.out.println(rooms.length + " :" + Room.checkConnect(worldP));
        Room.makeTurn(worldP, random);
        Position.connectAll(worldP, random);
        door = Position.addDoor(worldP, random);
        System.out.println("door: " + door.Px + "," + door.Py);
        player=Position.addPlayer(worldP,random);
        AI = Position.addAI(worldP,random);
        cur = AI;
        findRoute();
        //ter.renderFrame(world);
        //moveCharacter();
        return world;

    }

    public void moveCharacter() {

        String input = "level " + level+" movements: ";
        int n = level;
        int i = 0;
        while (i < 100) {
            if (StdDraw.hasNextKeyTyped()) {
                char nextMove = StdDraw.nextKeyTyped();
                if(nextMove=='Q'){
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
                if(level!=n){
                    i=0;
                    n++;
                    System.out.println(input);
                    input="level "+n+" movements: ";
                }
                ter.renderFrame(world);
                i++;
            }
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            if (mouseX < 60 && mouseX >= 0 && mouseY < 30 && mouseY >= 0) {
                ter.renderFrame(world);
                if (world[mouseX][mouseY].equals(Tileset.FLOOR)) {
                    StdDraw.setPenColor(Color.white);
                    StdDraw.textLeft(0.5, 29, "floor");
                }
                if (world[mouseX][mouseY].equals(Tileset.WALL)) {
                    StdDraw.setPenColor(Color.white);
                    StdDraw.textLeft(0.5, 29, "wall");
                }
                if (world[mouseX][mouseY].equals(Tileset.LOCKED_DOOR)) {
                    StdDraw.setPenColor(Color.white);
                    StdDraw.textLeft(0.5, 29, "locked door");
                }
                if (world[mouseX][mouseY].equals(Tileset.PLAYER)) {
                    StdDraw.setPenColor(Color.white);
                    StdDraw.textLeft(0.5, 29, "player");
                }
                StdDraw.show();
                StdDraw.pause(200);
                StdDraw.enableDoubleBuffering();
            }
        }
        if(i==100) {
            this.drawLose();
            StdDraw.pause(2000);
        }
        System.out.println(input);

    }

    public void moveAI(){
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
    if(route.isEmpty()){
        this.drawWin(AI.down(worldP), seed + ++level, AI);
        return;
    }

    }



    public void movePlayer(char nextMove){
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
            this.drawWin(player.left(worldP),  seed + ++level, player);
            return;
        }
        if (nextMove == 'd' && player.right(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.right(worldP),  seed + ++level,player);
            return;
        }
        if (nextMove == 's' && player.down(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.down(worldP), seed + ++level,player);
            return;
        }
        if (nextMove == 'w' && player.up(worldP).Tile.equals(Tileset.LOCKED_DOOR)) {
            this.drawWin(player.up(worldP),seed + ++level,player);
            return;
        }
    }



    public void drawWin(Position p,int level, Position pr){
        int realLevel=level-seed;
        p.setTile(Tileset.UNLOCKED_DOOR);
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.white);
        Font font=new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        if (pr.equals(player) ) {
            StdDraw.text(30, 15, "Great Job: You Win! Next level: " + realLevel);
        }
        else if (pr.equals(AI)){
            StdDraw.text(30, 15, "Oops: The AI Wins! Next level: " + realLevel);
        }
        StdDraw.show();
        StdDraw.pause(1000);
        genWorld(level);
    }


    public void drawLose(){
        StdDraw.setPenColor(Color.white);
        Font font=new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(30,15,"You Lose: Ran out of steps!");
        StdDraw.show();
    }


    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public static void drawMenu(){
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
                char nextKey = StdDraw.nextKeyTyped();
                if (nextKey == 'N' || nextKey == 'n') {
                    StdDraw.textLeft(0.25, 0.25, "New game_ please enter a seed:");
                    StdDraw.setPenColor(Color.yellow);
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
                    genWorld(seed);
                    ter.renderFrame(world);
                    moveCharacter();
                    return;
                }
                if(nextKey=='l'||nextKey=='L'){
                    try {
                       Game loaded=loadGame("game1.save");
                        ter = new TERenderer();
                        ter.initialize(WIDTH,HEIGHT);
                        ter.renderFrame(loaded.world);
                        loaded.moveCharacter();
                        return;
                    }
                    catch (Exception e){
                        System.out.println("Couldn't load "+e.getMessage());
                    }
                }

//                    StdDraw.text(0.5, 0.55, "(1) You'll play against an AI player.");
//                    StdDraw.text(0.5, 0.5, "(2) Use 'AWSD' to control your direction in the maze. ");
//                    StdDraw.text(0.5, 0.35, "(5) Once arriving at the exit, you must wait until another player" +
//                            "get to the exit.");


                if (nextKey=='m'||nextKey=='M'){
                    StdDraw.setCanvasSize(40 * 16, 40 * 16);
                    StdDraw.clear(Color.black);
                    StdDraw.setPenColor(Color.white);
                    Font font = new Font("Monaco", Font.BOLD, 30);
                    StdDraw.setFont(font);
                    StdDraw.text(0.5, 0.75, "Basic Mode V.S Advanced Mode");
                    Font smallFont2 = new Font("Monaco", Font.BOLD, 14);
                    StdDraw.setFont(smallFont2);
                    StdDraw.text(0.5, 0.50, "Basic Mode: The player who gets to the exit first wins.");
                    StdDraw.text(0.5, 0.45, "Advanced Mode: The player who gets the higher points wins.");
                    StdDraw.text(0.5, 0.40, "You earn 5 points when getting a coin.");
                    StdDraw.text(0.5, 0.35,  "You get 3 points deducted when falling into the trap.");
                    StdDraw.text(0.5, 0.30, "Once arriving at the exit, the player must wait until another player" +
                            "get to the exit.");
                    StdDraw.setPenColor(Color.yellow);
                    Font insFont = new Font("Monaco", Font.BOLD, 14);
                    StdDraw.text(0.5, 0.25, "Type 'N' and the map you want to load and "+
                            "enter 'S' to start.");

                }
                if(nextKey=='Q'||nextKey=='q'){
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
        ter.initialize(WIDTH,HEIGHT);
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
                    genWorld(seed);
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
                Game loaded=loadGame("game1.save");
                for (int i = 1; i < input.length(); i++) {
                    char ithC=input.charAt(i);
                    if(ithC=='a'||ithC=='s'||ithC=='w'||ithC=='d'){
                        loaded.movePlayer(ithC);
                    }
                    if (ithC == 'Q') {
                        saveGame(loaded, "game1.save");
                        return loaded.world;
                    }
                }
                ter.renderFrame(loaded.world);
                return loaded.world;
            }
            catch (Exception e){
                System.out.println("Couldn't load "+e.getMessage());
            }
        }
        System.out.println("Input must start with N/n(new game) or L/l(load game)");
        return null;
    }

    public void saveGame(Serializable game,String filename) throws Exception{
        ObjectOutputStream os=new ObjectOutputStream(Files.newOutputStream(Paths.get(filename)));
        os.writeObject(game);
    }

    public Game loadGame(String filename) throws Exception{
        ObjectInputStream is=new ObjectInputStream(Files.newInputStream(Paths.get(filename)));
        Game savedGame=(Game)is.readObject();
        return savedGame;
    }

    public void AIPlayer(){
        moveAI();
        ter.renderFrame(world);
    }

}
