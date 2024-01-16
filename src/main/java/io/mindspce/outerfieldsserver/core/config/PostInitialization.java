package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.factory.ThoughtFactory;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class PostInitialization implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // FIXME test NPC
        var npc = EntityManager.GET().newNonPlayerEntity(-1, "Test_NPC", List.of(EntityState.TEST),
                new ClothingItem[6], AreaId.TEST, IVector2.of(672, 960), IVector2.of(200, 200));
        npc.addComponent(ThoughtFactory.testThought(npc));
    }
}
