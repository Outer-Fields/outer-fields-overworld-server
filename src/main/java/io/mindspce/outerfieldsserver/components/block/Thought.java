package io.mindspce.outerfieldsserver.components.block;

import io.mindspce.outerfieldsserver.components.subcomponents.Behavior;
import io.mindspce.outerfieldsserver.components.subcomponents.FreeWill;


public interface Thought extends FreeWill, Behavior {

    void onUpdate();
}
