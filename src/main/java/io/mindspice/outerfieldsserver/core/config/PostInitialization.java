package io.mindspice.outerfieldsserver.core.config;

import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.factory.ThoughtFactory;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.enums.EntityState;
import io.mindspice.outerfieldsserver.systems.event.Event;
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
                new ClothingItem[6], AreaId.TEST, IVector2.of(672, 960), IVector2.of(256, 256), false);
       npc.addComponent(ThoughtFactory.aggressiveEnemyThought(npc));
        Event.emitAndRegisterEntity(SystemType.NPC, AreaId.TEST, IVector2.of(672, 960), npc);
    }
}
