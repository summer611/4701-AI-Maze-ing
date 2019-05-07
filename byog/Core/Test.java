package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.Random;
import edu.princeton.cs.introcs.StdDraw;

public class Test {
    @org.junit.Test

    //public void testDraw(){
    public static void main(String[] args){
       /* TERenderer ter = new TERenderer();
        ter.initialize(60,30);

        TETile[][] world = new TETile[60][30];
        Position[][] worldP=Position.initializeP(world);

        //Room room1=Room.drawRoom(worldP,worldP[10][10],8,10);
        //System.out.println(worldP[4][0].distanceXY(worldP));
        Room[] rooms=Room.drawRandomRooms(worldP,RandomUtils.uniform(new Random(),2,10),new Random());
        //worldP[12][0].directConnect(worldP,room1);
        //worldP[2][12].directConnect(worldP,room1);
        /*for(Room rm :rooms){
            room1.directConnect(worldP,rm);
        }

        Room.directConnectAll(worldP,rooms);
        System.out.println(rooms.length+" :"+Room.checkConnect(worldP));
        Room.makeTurn(worldP,new Random());
        ter.renderFrame(world);
        System.out.println(Room.checkConnect(worldP));
        Position.connectAll(worldP,new Random());
        ter.renderFrame(world);
        System.out.println(Room.checkConnect(worldP)+" run finished");*/



        int n=100;
        double[] x = new double[n+1];
        double[] y = new double[n+1];
        double[] z = new double[n+1];
        for (int i = 0; i <= n; i++) {
            x[i] = i;
            y[i] = factorialRecursive(i);
            z[i]=Math.pow(i,i);
        }

        // rescale the coordinate system
        StdDraw.setXscale(0, n+1);
        StdDraw.setYscale(0,n*n*n*n);

        // plot the approximation to the function
        for (int i = 0; i < n; i++) {
            StdDraw.line(x[i], y[i], x[i+1], y[i+1]);
            StdDraw.line(x[i], z[i], x[i+1], z[i+1]);
        }
        StdDraw.show();

    }
    public static long factorialRecursive(int n) {
        // 0！=1，（0 的阶乘是存在的）
        if (n == 0) {
            return 1;
        }
        if (n < 2)
            return n * 1;
        return n * factorialRecursive(n - 1);
    }
}
