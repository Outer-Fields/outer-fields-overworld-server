package io.mindspice.outerfieldsserver.ai.decisiongraph;

import io.mindspice.outerfieldsserver.ai.task.Task;


public record NewActionTask<T>(Task<T> task, boolean interruptExisting) {
    public static <T> NewActionTask<T> of(Task<T> task, boolean interruptExisting) {
        return new NewActionTask<T>(task, interruptExisting);
    }
}
