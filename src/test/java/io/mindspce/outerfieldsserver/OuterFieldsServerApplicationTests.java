package io.mindspce.outerfieldsserver;

import io.mindspce.outerfieldsserver.enums.Direction;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.jupiter.api.Test;


//@SpringBootTest
class OuterFieldsServerApplicationTests {

//    @Test
//    void gridTest() {
//        PlayerState muteRect = new PlayerState();
//        PlayerState2 rect = new PlayerState2();
//
//         new Thread(() -> {
//             int x = 0;
//             int y = 100;
//            while (true) {
//                try {
//                    Thread.sleep(33);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                muteRect.updatePlayerPos(x, y);
//                rect.updatePlayerPos(y, x);
//            }
//        }).start();
//        IMutVector2[][]  rect1 = null;
//        IMutVector2[][]  rect2 = null;
//        for (int k = 0; k < 2; k++) {
//            var t1 = System.nanoTime();
//            for (int x = 0; x < 100000; ++x) {
//              rect1 = muteRect.getLocalGrid();
//            }
//            System.out.println(System.nanoTime() - t1);
//            System.out.println(rect1);
//
//
//            var t2 = System.nanoTime();
//            for (int x = 0; x < 100000; ++x) {
//                rect2 = rect.getLocalGrid();
//            }
//            System.out.println(System.nanoTime() - t2);
//            System.out.println(rect2);
//        }
//    }

    @Test
    void gridTest() {
        IVector2[][] grid = new IVector2[3][3];
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                grid[x][y] = IVector2.of(x,y);
            }
        }
        var newX = Direction.WEST;
        var newY = Direction.NORTH;
        int x = newX == Direction.EAST ? 0 : 2;
        for (int y = 0; y < 3; ++y) {
            grid[x][y] = IVector2.of(-9,-9);
        }

        int y = newY == Direction.SOUTH ? 0 : 2;
        for (int j = 0; j < 3; ++j) {
            grid[j][y] = IVector2.of(-9,-9);
        }

        GridUtils.printGrid(grid);



    }


}
