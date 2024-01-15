package io.mindspce.outerfieldsserver.ai.task;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.TriConsumer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;


public class Task<T extends Enum<T>, U> {
    public final T taskType;
    public final long taskId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    public final U data;
    public final float taskWeight;
    public final List<SubTask<U>> subTasks;
    public final boolean concurrentSubTasks;

    public TriConsumer<EventType, Consumer<?>, Boolean> enableListener;
    public Consumer<EventType> disableListener;

    public final Consumer<U> onCompletion;
    private boolean isCompleted = false;
    private final List<SubTask<U>> restoredSubTasks;

    public Task(T taskType, U data, float taskWeight, List<SubTask<U>> subtasks, boolean concurrentSubTasks,
            Consumer<U> onCompletion) {
        this.taskType = taskType;
        this.data = data;
        this.taskWeight = taskWeight;
        this.subTasks = subtasks;
        this.concurrentSubTasks = concurrentSubTasks;
        this.onCompletion = onCompletion;
        restoredSubTasks = List.copyOf(subtasks);
    }

    public void linkListenerControls(TriConsumer<EventType, Consumer<?>, Boolean> enableListener,
            Consumer<EventType> disableListener) {
        this.enableListener = enableListener;
        this.disableListener = disableListener;
    }

    public T taskType() {
        return taskType;
    }

    public long taskId() {
        return taskId;
    }

    public int remainingSubTasks() {
        return subTasks.size();
    }

    public List<SubTask<U>> getSubTaskList() {
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

    public void reset() {
        subTasks.addAll(restoredSubTasks);
        isCompleted = false;
    }

    public boolean isSuspendable() {
        if (subTasks.isEmpty()) { return true; }
        return subTasks.getFirst().suspendable();
    }

    public SubTask<U> getCurrentSubTask() {
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

    public static <T extends Enum<T>, U> Builder<T, U> builder(T taskType, U data,
            Pair<TriConsumer<EventType, Consumer<?>, Boolean>, Consumer<EventType>> eventHooks) {
        return new Builder<>(taskType, data, eventHooks);
    }

    public static class Builder<T extends Enum<T>, U> {
        private T taskType;
        private U data;
        private float taskWeight = 0;
        private List<SubTask<U>> subTasks = new LinkedList<>();
        private boolean concurrentSubTasks = false;
        private Consumer<U> onCompletion;
        public TriConsumer<EventType, Consumer<?>, Boolean> enableListener;
        public Consumer<EventType> disableListener;

        public Builder(T taskType, U data, Pair<TriConsumer<EventType, Consumer<?>,
                Boolean>, Consumer<EventType>> eventHooks) {
            this.taskType = taskType;
            this.data = data;
            this.enableListener = eventHooks.first();
            this.disableListener = eventHooks.second();
        }

        public Builder<T, U> setConcurrentSubTasks(boolean isConcurrent) {
            concurrentSubTasks = isConcurrent;
            return this;
        }

        public Builder<T, U> setOnCompletion(Consumer<U> completionConsumer) {
            this.onCompletion = completionConsumer;
            return this;
        }

        public Builder<T, U> setTaskWeight(float weight) {
            this.taskWeight = weight;
            return this;
        }

        public Builder<T, U> setConcurrent(boolean concurrent) {
            this.concurrentSubTasks = concurrent;
            return this;
        }

        public Builder<T, U> addSubTask(SubTask<U> subTask) {
            subTasks.add(subTask);
            return this;
        }

        public Builder<T, U> addSubEventTask(
                EventType eventType,
                BiPredicate<U, Event<?>> completionEventPredicate,
                Consumer<U> onStartConsumer,
                boolean suspendable

        ) {
            subTasks.add(SubTask.ofOnEvent(eventType, data, onStartConsumer, completionEventPredicate, suspendable,
                    enableListener, disableListener));
            return this;
        }

        public Builder<T, U> addSubTickTask(
                BiPredicate<U, Tick> completionTickPredicate,
                Consumer<U> onStartConsumer,
                boolean suspendable) {
            subTasks.add(SubTask.ofOnTick(data, onStartConsumer, completionTickPredicate,
                    suspendable, enableListener, disableListener));
            return this;
        }

        public Builder<T, U> addSubEventTickTask(
                EventType eventType,
                BiPredicate<U, Event<?>> completionEventPredicate,
                BiPredicate<U, Tick> completionTickPredicate,
                Consumer<U> onStartConsumer,
                boolean suspendable) {
            subTasks.add(SubTask.ofEventAndTick(eventType, data, onStartConsumer, completionEventPredicate,
                    completionTickPredicate, suspendable, enableListener, disableListener));
            return this;
        }

        public Task<T, U> build() {
            if (subTasks.isEmpty()) {
                throw new IllegalStateException("Must have subtasks");
            }
            return new Task<>(taskType, data, taskWeight, subTasks, concurrentSubTasks, onCompletion);
        }

    }
}
