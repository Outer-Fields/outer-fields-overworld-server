package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;


public abstract class Decision<T> {
    private final String name;

    public Decision(String name) {
        this.name = name;
    }

    public abstract boolean getDecision(T focusState);

    public String getName() {
        return name;
    }

}
