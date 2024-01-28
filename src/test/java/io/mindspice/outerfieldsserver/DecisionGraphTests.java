package io.mindspice.outerfieldsserver;

import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.LocationEntity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.jupiter.api.Test;


public class DecisionGraphTests {

    public static class TestFocus {
        public Entity entity = new LocationEntity(1, AreaId.GLOBAL, IVector2.of(0, 0), 10);
    }

    @Test
    void test() {
//        ComponentSystem
//        DecisionGraphBuilder<TestFocus> graph = new DecisionGraphBuilder<>(new RootNode<>())
//                .addChild(null)
//                .addSibling()
//                .addChild()

    }
}
