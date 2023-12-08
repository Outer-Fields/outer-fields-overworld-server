package io.mindspce.outerfieldsserver;


import io.mindspice.mindlib.data.geometry.IMutRec2;
import io.mindspice.mindlib.data.geometry.IRec2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class OuterFieldsServerApplicationTests {

	@Test
	void gridTest() {
		for (int k = 0; k < 2; k++) {
			IMutRec2 muteRect = new IMutRec2(0, 0, 1024, 1024);
			var t1 = System.nanoTime();
			for (int x = 0; x < 10000; ++x) {
				for (int y = 0; y < 1000; ++y) {
					muteRect = muteRect.reCenter(x, y);
				}
			}
			System.out.println(System.nanoTime() - t1);
			System.out.println(muteRect);
			IRec2 rect = new IRec2(0, 0, 1024, 1024);
			var t2 = System.nanoTime();
			for (int x = 0; x < 10000; ++x) {
				for (int y = 0; y < 1000; ++y) {
					rect = IRec2.of(x, y, 1024, 1024);
				}
			}
			System.out.println(System.nanoTime() - t2);
			System.out.println(rect);
		}
	}


}
