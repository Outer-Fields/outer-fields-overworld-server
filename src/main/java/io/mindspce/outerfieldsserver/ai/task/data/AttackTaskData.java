package io.mindspce.outerfieldsserver.ai.task.data;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;
import io.mindspice.mindlib.data.wrappers.MutableValue;


public record AttackTaskData(
        Entity targetEntity,
        Entity attackingEntity,
        LazyFinalValue<Boolean> finished,
        MutableValue<Boolean> onFinish
) { }
