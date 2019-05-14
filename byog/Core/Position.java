package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Position implements Serializable {
    public int Px;
    public int Py;
    public TETile Tile;
    TETile[][] World;
    public int id;

    public Position left(Position[][]worldP){
        if(Px>0) {
            return worldP[Px-1][Py];
        }else{
            return this;
        }
    }
    public Position right(Position[][]worldP){
        if(Px<59) {
           return worldP[Px+1][Py];
        }else {
            return this;
        }
    }
    public Position up(Position[][] worldP){
        if(Py<29){
            return worldP[Px][Py+1];
        }
        return this;
    }
    public Position down(Position[][]worldP){
        if(Py>0){
            return worldP[Px][Py-1];
        }
        return this;
    }
    public void hasNeighbor(Position[][]worldP){
        if(left(worldP).Tile==Tileset.FLOOR&&left(worldP).id!=id){
            left(worldP).id=id;
            left(worldP).hasNeighbor(worldP);
        }
        if(up(worldP).Tile==Tileset.FLOOR&&up(worldP).id!=id){
            up(worldP).id=id;
            up(worldP).hasNeighbor(worldP);
        }
        if(down(worldP).Tile==Tileset.FLOOR&&down(worldP).id!=id){
            down(worldP).id=id;
            down(worldP).hasNeighbor(worldP);
        }
        if(right(worldP).Tile==Tileset.FLOOR&&right(worldP).id!=id){
            right(worldP).id=id;
            right(worldP).hasNeighbor(worldP);
        }
    }

    public Position(int px,int py,TETile[][]world){
        Px=px;
        Py=py;
        World=world;
        id=0;
    }
    public void setTile(TETile tile){
        World[Px][Py]=tile;
        Tile=tile;
    }
    public static Position[][] initializeP(TETile[][]world){
        Position[][] position = new Position[world.length][world[0].length];

        for(int x=0;x<world.length;x++){
            for(int y=0;y<world[0].length;y++){
                position[x][y]=new Position(x,y,world);
                position[x][y].setTile(Tileset.NOTHING);
            }
        }
        return position;
    }
    public void drawHorizontal(Position[][] position,int length, TETile tile){
        for(int i=Px;i<Px+length;i++){
            position[i][Py].setTile(tile);
        }
    }
    public void drawVertical(Position[][] position,int length, TETile tile){
        for(int i=Py;i<Py+length;i++){
            position[Px][i].setTile(tile);
        }
    }

    public int distanceX(Position[][] world){
        for(int x=Px;x<60;x++){
            if(world[x][Py].Tile!=Tileset.NOTHING){
                return x-Px;
            }
        }
        return 60-Px;
    }
    public int distanceY(Position[][] world){
        for(int y=Py;y<30;y++){
            if(world[Px][y].Tile!=Tileset.NOTHING){
                return y-Py;
            }
        }
        return 30-Py;
    }
    public int distanceXY(Position[][] world){
        int minY=30-Py;
        for(int x=Px;x<Px+distanceX(world);x++){
            minY=Math.min(minY,world[x][Py].distanceY(world));
            }
        return minY;
    }
    public void drawVerticalHallway(Position[][] position,int length){
        drawVertical(position,length,Tileset.FLOOR);
        left(position).drawVertical(position,length,Tileset.WALL);
        right(position).drawVertical(position,length,Tileset.WALL);
    }
    public void drawHorizontalHallway(Position[][]position,int length){
        drawHorizontal(position,length,Tileset.FLOOR);
        up(position).drawHorizontal(position,length,Tileset.WALL);
        down(position).drawHorizontal(position,length,Tileset.WALL);
    }

    public int directConnectV(Position[][]world,Room rm) {
        if (rm.position.Px < Px && Px < rm.position.Px + rm.width - 1&& down(world).Tile==Tileset.FLOOR) {
            if (Py < rm.position.Py) {
                for (int i = Py+1; i < rm.position.Py; i++) {
                    if (world[Px][i].Tile != Tileset.NOTHING)
                        return 0;
                }
                drawVerticalHallway(world, rm.position.Py - Py + 1);
                return rm.position.Py-Py+1;
            }/*elseif(Py>rm.position.Py+rm.height-1){
                for(int i=rm.position.Py+rm.height;i<=Py;i++){
                    if(world[Px][i].Tile==Tileset.FLOOR)
                        return 0;
                }
                drawVerticalHallway(world,Py-rm.position.Py+1);
                return 1;
            }*/
        }
        return 0;
    }

    public int directConnectH(Position[][]world,Room rm) {
        if (rm.position.Py < Py && Py < rm.position.Py + rm.height - 1&&left(world).Tile==Tileset.FLOOR) {
            if (Px < rm.position.Px) {
                for (int i = Px+1; i < rm.position.Px; i++) {
                    if (world[i][Py].Tile != Tileset.NOTHING)
                        return 0;
                }
                drawHorizontalHallway(world, rm.position.Px - Px + 1);
                return rm.position.Px - Px + 1;
            }/*elseif(Px>rm.position.Px+rm.width-1){
                for(int i=rm.position.Px+rm.width;i<=Px;i++){
                    if(world[i][Py].Tile==Tileset.FLOOR)
                        return 0;
                }
                drawHorizontalHallway(world,Px-rm.position.Px+1);
                return 1;
            }*/
        }
        return 0;
    }
    public boolean connectToNearH(Position[][]world) {
        for (int x = Px + 1; x < 60; x++) {
            if (world[x][Py].Tile == Tileset.FLOOR) {
                drawHorizontalHallway(world, x - Px);
                return true;
            }
        }
        return false;
    }
    public boolean connectToNearV(Position[][]world) {
        for (int y = Py + 1; y < 30; y++) {
            if (world[Px][y].Tile == Tileset.FLOOR) {
                up(world).drawVerticalHallway(world, y - Py - 1);
                return true;
            }
        }
        return false;
    }
    public static Position findConnectPH(Position[][]world) {
        Room.checkConnect(world);
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 30; y++) {
                Position p = world[x][y];
                if (p.Tile == Tileset.WALL && p.left(world).Tile == Tileset.FLOOR) {
                    for (int x1 = p.Px; x1 < 60; x1++) {
                        if (world[x1][p.Py].Tile == Tileset.FLOOR ) {
                            if(world[x1][p.Py].id != p.left(world).id)
                            return p;
                            else break;
                        }
                    }
                }

            }
        }
        return null;
    }
    public static Position findConnectPV(Position[][]world) {
        Room.checkConnect(world);
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 30; y++) {
                Position p = world[x][y];
                if (p.Tile == Tileset.WALL && p.down(world).Tile == Tileset.FLOOR) {
                    for (int y1 = p.Py; y1 < 30; y1++) {
                        if (world[p.Px][y1].Tile == Tileset.FLOOR ) {
                            if(world[p.Px][y1].id != p.down(world).id)
                            return p;
                            else break;
                        }
                    }
                }

            }
        }
        return null;
    }

    public static void connectAll(Position[][]world, Random random){
        Position PH= findConnectPH(world);
        if(PH!=null){
            PH.connectToNearH(world);
            System.out.println("horizontal connection succeed "+PH.Px+","+PH.Py);
        }
        Position PV= findConnectPV(world);
        if(PV!=null){
            PV.down(world).connectToNearV(world);
            System.out.println("vertical connection succeed"+PV.Px+","+PV.Py);
        }
        if(Room.checkConnect(world)==1){
            return;
        }
        System.out.println("not connected, draw additional room...");
        Room.drawRandomRooms(world,1, random);
        connectAll(world,random);
    }

    public static Position addDoor(Position[][]world,Random random){
        Position p = world[0][0];
        for(int x=random.nextInt(40);x<60;x++){
            for(int y=random.nextInt(20);y<30;y++){
                p=world[x][y];
                if(p.down(world).Tile==Tileset.NOTHING&&p.up(world).Tile==Tileset.FLOOR){
                    p.setTile(Tileset.LOCKED_DOOR);
                    return p;
                }else if(p.left(world).Tile==Tileset.NOTHING&&p.right(world).Tile==Tileset.FLOOR){
                    p.setTile(Tileset.LOCKED_DOOR);
                    return p;
                }else if(p.right(world).Tile==Tileset.NOTHING&&p.left(world).Tile==Tileset.FLOOR){
                    p.setTile(Tileset.LOCKED_DOOR);
                    return p;
                }else if(p.up(world).Tile==Tileset.NOTHING&&p.down(world).Tile==Tileset.FLOOR){
                    p.setTile(Tileset.LOCKED_DOOR);
                    return p;
                }
            }
        }
        return p;
    }

    public static Position addPlayer(Position[][]world,Random random) {
        for (int x = random.nextInt(40); x < 60; x++) {
            for (int y = random.nextInt(20); y < 30; y++) {
                Position p = world[x][y];
                if (p.Tile == Tileset.FLOOR) {
                    p.setTile(Tileset.PLAYER);
                    return p;
                }
            }
        }
        return null;
    }
    public static Position addAI(Position[][]world,Random random) {
        for (int x = random.nextInt(40); x < 60; x++) {
            for (int y = random.nextInt(20); y < 30; y++) {
                Position p = world[x][y];
                if (p.Tile == Tileset.FLOOR) {
                    p.setTile(Tileset.FLOWER);
                    return p;
                }
            }
        }
        return null;
    }
    public static void addCoinsTraps(Position[][]world,Random random, int coin, int trap) {
        while(coin > 0){
            int x = random.nextInt(40);
            int y = random.nextInt(20);
            Position p = world[x][y];
            if (p.Tile == Tileset.FLOOR) {
                p.setTile(Tileset.COIN);
                coin--;
            }
        }
        while(trap > 0){
            int x = random.nextInt(40);
            int y = random.nextInt(20);
            Position p = world[x][y];
            if (p.Tile == Tileset.FLOOR) {
                System.out.println("trap at ("+ x+" ," + y +")");
                p.setTile(Tileset.TRAP);
                trap--;
            }
            world[7][7].setTile(Tileset.COIN);
        }
    }
}
