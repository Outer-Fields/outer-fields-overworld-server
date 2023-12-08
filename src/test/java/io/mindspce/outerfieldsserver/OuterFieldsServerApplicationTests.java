package io.mindspce.outerfieldsserver;

import io.mindspce.outerfieldsserver.objects.player.PlayerState;
import io.mindspce.outerfieldsserver.objects.player.PlayerState2;
import io.mindspice.mindlib.data.geometry.IMutVector2;
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


}
