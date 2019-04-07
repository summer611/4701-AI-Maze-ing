package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;


import java.util.Random;

public class Room {
    public Position position;
    public int width;
    public int height;

    public Room(Position p,int w,int h){
        position=p;
        width=w;
        height=h;
    }

    public static Room drawRoom(Position[][] world,Position p,int w,int h){
        p.drawHorizontal(world,w,Tileset.WALL);
        p.drawVertical(world,h,Tileset.WALL);
        for(int x=p.Px+1;x<p.Px+w;x++){
            world[x][p.Py+1].drawVertical(world,h-2,Tileset.FLOOR);
        }
        world[p.Px+w-1][p.Py].drawVertical(world,h-1,Tileset.WALL);
        world[p.Px][p.Py+h-1].drawHorizontal(world,w,Tileset.WALL);
        return new Room(p,w,h);
    }

    public static Room drawARandomRoom(Random random,Position[][] world,Position p,int minLength,int maxWidth,int maxHeight){
        int w=RandomUtils.uniform(random,minLength,maxWidth);
        int h=RandomUtils.uniform(random,minLength,maxHeight);
        Room randRoom=drawRoom(world,p,w,h);
        return randRoom;
    }

    public static Room[] drawRandomRooms(Position[][] world, int num, Random random){
        int i=0;
        Room[] randRooms=new Room[num];
        while(i<num){
            int px=RandomUtils.uniform(random,60);
            int py=RandomUtils.uniform(random,30);
            Position roomP=world[px][py];
            int maxX=Math.min(20,roomP.distanceX(world));
            int maxY=roomP.distanceXY(world);
            if(roomP.Tile==Tileset.NOTHING&&Math.min(maxX,maxY)>4){
                randRooms[i]=drawARandomRoom(random,world,roomP,4,maxX,maxY);
                i++;
            }
        }
        return randRooms;
    }

    public void directConnect(Position[][]world,Room b) {
        for (int x = position.Px + 1; x < position.Px + width - 1; x++) {
            if(world[x][position.Py + height - 1].directConnectV(world, b)>0){
                return;
            }

        }
        for (int y = position.Py + 1; y < position.Py + height - 1; y++) {
            if(world[position.Px + width - 1][y].directConnectH(world, b)>0){
                return;
            }
        }

    }// The weird thing is once the returned room is assigned, the map would be a mess(isolated rooms and double walls)

    public static void directConnectAll(Position[][]world,Room[] rooms) {
        for (int i = 0; i < rooms.length; i++) {
            for (Room rm : rooms) {
                rooms[i].directConnect(world, rm);
            }
        }
    }
    public static void makeTurn(Position[][]world,Random random){
        boolean success=false;
        for(int x=0;x<60;x++) {
            for (int y = 0; y < 30; y++) {
                Position p = world[x][y];
                int distanceX = p.distanceX(world);
                int distanceUpX = p.up(world).distanceX(world);
                int distanceY = p.distanceY(world);
                int distanceRightY = p.right(world).distanceY(world);
                boolean connectX = (distanceX > 2 && distanceX < 60 - p.Px) && (distanceUpX > 2 && distanceUpX < 60 - p.Px);
                boolean connextY = (distanceY > 2 && distanceY < 30 - p.Py) && (distanceRightY > 2 && distanceRightY < 30 - p.Py);
                if (p.Tile == Tileset.NOTHING && connectX && connextY) {
                    drawRoom(world, p, 3, 3);
                    success = world[x + 1][y + 1].connectToNearH(world) && world[x + 1][y + 1].connectToNearV(world);
                    break;
                }
            }
            if (success) {
                break;
            }
        }
        if(!success){
            drawRandomRooms(world,1,random);
            makeTurn(world,random);
        }
    }

    public static void clearID(Position[][]world) {
        for(int x=0;x<60;x++){
            for(int y=0;y<30;y++){
                Position p=world[x][y];
                p.id = 0;
            }
        }
    }
    public static int checkConnect(Position[][]world) {
        clearID(world);
        int check = 0;
       // for (Position[] line : world) {
        //    for (Position p : line) {
        int count=0;
        for(int x=0;x<60;x++){
            for(int y=0;y<30;y++){
                count++;
                Position p=world[x][y];
               // System.out.println(count+":"+p.id+p.Tile);
                if (p.Tile == Tileset.FLOOR && p.id == 0) {
                    p.id = ++check;
                    p.hasNeighbor(world);
                }
            }
        }return check;
        }
       /*for (Position[] line : world) {
            for (Position p : line) {
                if (p.Tile == Tileset.FLOOR && p.id != check) {
                    System.out.println("trying to connect...");
                    if(p.connectToNear(world)>0){
                        checkConnect(world);
                    }
                }
            }
        }
        return true;*/




}
