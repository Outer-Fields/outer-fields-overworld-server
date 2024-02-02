package io.mindspice.outerfieldsserver.enums;

public enum NetAction {
    TEST(-1);

    public final int value;

    NetAction(int value) { this.value = value; }

    public static NetAction fromValue(int value){
        for(int i =0; i< NetAction.values().length; ++i){
            return NetAction.values()[i];
        }
        return null;
    }
}

