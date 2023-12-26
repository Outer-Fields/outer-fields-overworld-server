package io.mindspce.outerfieldsserver.systems.event;

public interface SystemListener {

    void onEvent(Event<?> event);

    void onCallback(Callback<?> callback);
}
