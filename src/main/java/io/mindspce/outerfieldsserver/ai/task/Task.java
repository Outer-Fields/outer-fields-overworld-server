package io.mindspce.outerfieldsserver.ai.task;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.TriConsumer;

import java.util.LinkedList;
import java.util.List;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.UUID;


public class Task<T> {
    private final String taskName;
    private final long taskId = java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    private final T data;
    private final float taskWeight;
    private final List<SubTask<T>> subTasks;
    private final boolean concurrentSubTasks;


    public final Consumer<T> onCompletion;
    private boolean isCompleted = false;

    public Task(String TaskName, T data, float taskWeight, List<SubTask<T>> subtasks, boolean concurrentSubTasks,
            Consumer<T> onCompletion) {
        this.taskName = TaskName;
        this.data = data;
        this.taskWeight = taskWeight;
        this.subTasks = subtasks;
        this.concurrentSubTasks = concurrentSubTasks;
        this.onCompletion = onCompletion;
    }

    public void linkListenerHooks(TriConsumer<EventType, Consumer<?>, Boolean> enableListener,
            Consumer<EventType> disableListener) {
        subTasks.forEach(t -> t.linkListenerHooks(enableListener, disableListener));
    }

    public float taskWeight() {
        return taskWeight;
    }

    public String taskName() {
        return taskName;
    }

    public long taskId() {
        return taskId;
    }

    public int remainingSubTasks() {
        return subTasks.size();
    }

    public List<SubTask<T>> getSubTaskList() {
        return subTasks;
    }

    public void suspend() {
        if (subTasks.isEmpty()) { return; }
        if (concurrentSubTasks) {
            subTasks.forEach(SubTask::suspend);
        } else {
            subTasks.getFirst().suspend();
        }
    }

    public void unsuspend() {
        if (subTasks.isEmpty()) { return; }
        if (concurrentSubTasks) {
            subTasks.forEach(SubTask::unsuspend);
        } else {
            subTasks.getFirst().unsuspend();
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void start() {
        if (subTasks.isEmpty()) { return; }
        unsuspend();
        if (concurrentSubTasks) {

            subTasks.forEach(SubTask::onStart);
        } else {
            subTasks.getFirst().onStart();
        }

    }

    public boolean isSuspendable() {
        if (subTasks.isEmpty()) { return true; }
        return subTasks.getFirst().suspendable();
    }

    public SubTask<T> getCurrentSubTask() {
        if (subTasks.isEmpty()) { return null; }
        return subTasks.getFirst();
    }

    public boolean onTick(Tick tick) {
        if (subTasks.isEmpty()) { return true; }
        if (completionCheck()) {
            isCompleted = true;
            if (onCompletion != null) { onCompletion.accept(data); }
            return true;
        }

        if (concurrentSubTasks) {
            subTasks.forEach(t -> t.onTick(tick));
        } else {
            subTasks.getFirst().onTick(tick);
        }

        return false;
    }

    private boolean completionCheck() {
        if (concurrentSubTasks) {
            return subTasks.stream().allMatch(SubTask::completed);
        } else {
            if (subTasks.getFirst().completed()) {
                subTasks.removeFirst();
                if (subTasks.isEmpty()) { return true; }
                subTasks.getFirst().onStart();
            }
        }
        return false;
    }

    public static <T> Builder<T> builder(String taskName, T data) {
        return new Builder<>(taskName, data);
    }

    public static class Builder<T> {
        private String taskName;
        private T data;
        private float taskWeight = 0;
        private List<SubTask<T>> subTasks = new LinkedList<>();
        private boolean concurrentSubTasks = false;
        private Consumer<T> onCompletion;

        public Builder(String taskName, T data) {
            this.taskName = taskName;
            this.data = data;
        }

        public Builder<T> setConcurrentSubTasks(boolean isConcurrent) {
            concurrentSubTasks = isConcurrent;
            return this;
        }

        public Builder<T> setOnCompletion(Consumer<T> completionConsumer) {
            this.onCompletion = completionConsumer;
            return this;
        }

        public Builder<T> setTaskWeight(float weight) {
            this.taskWeight = weight;
            return this;
        }

        public Builder<T> setConcurrent(boolean concurrent) {
            this.concurrentSubTasks = concurrent;
            return this;
        }

        public Builder<T> addSubTask(SubTask<T> subTask) {
            subTasks.add(subTask);
            return this;
        }

        public Builder<T> addSubEventTask(
                EventType completionEvent,
                BiPredicate<T, Event<?>> completionEventPredicate,
                Consumer<T> onStartConsumer,
                boolean suspendable
        ) {
            subTasks.add(SubTask.ofOnEvent(completionEvent, data, onStartConsumer, completionEventPredicate, suspendable));
            return this;
        }

        public Builder<T> addSubTickTask(
                BiPredicate<T, Tick> completionTickPredicate,
                Consumer<T> onStartConsumer,
                boolean suspendable) {
            subTasks.add(SubTask.ofOnTick(data, onStartConsumer, completionTickPredicate,
                    suspendable));
            return this;
        }

        public Builder<T> addSubEventTickTask(
                EventType completionEvent,
                BiPredicate<T, Event<?>> completionEventPredicate,
                BiPredicate<T, Tick> completionTickPredicate,
                Consumer<T> onStartConsumer,
                boolean suspendable) {
            subTasks.add(SubTask.ofEventAndTick(completionEvent, data, onStartConsumer, completionEventPredicate,
                    completionTickPredicate, suspendable));
            return this;
        }

        public Task<T> build() {
            if (subTasks.isEmpty()) {
                throw new IllegalStateException("Must have subtasks");
            }
            return new Task<>(taskName, data, taskWeight, subTasks, concurrentSubTasks, onCompletion);
        }

    }
}
