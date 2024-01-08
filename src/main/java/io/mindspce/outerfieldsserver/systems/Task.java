package io.mindspce.outerfieldsserver.systems;

import io.mindspce.outerfieldsserver.ai.task.SubTask;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;


public class Task<T extends Enum<T>, U> {
    public final T taskType;
    public final long taskId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    public final U data;
    public final float taskWeight;
    public final List<SubTask<U>> subTasks;
    public final boolean concurrentSubTasks;
    public final TriConsumer<EventType, Consumer<?>, Boolean> enableListener;
    public final Consumer<EventType> disableListener;
    public final Consumer<U> onCompletion;
    private boolean isCompleted = false;

    public Task(T taskType, U data, float taskWeight, List<SubTask<U>> subtasks, boolean concurrentSubTasks,
            TriConsumer<EventType, Consumer<?>, Boolean> enableListener, Consumer<EventType> disableListener,
            Consumer<U> onCompletion) {
        this.taskType = taskType;
        this.data = data;
        this.taskWeight = taskWeight;
        this.subTasks = subtasks;
        this.concurrentSubTasks = concurrentSubTasks;
        this.enableListener = enableListener;
        this.disableListener = disableListener;
        this.onCompletion = onCompletion;
        if (concurrentSubTasks) { linkConcurrentListeners(); }
    }

    public T taskType() {
        return taskType;
    }

    public int remainingSubTasks() {
        return subTasks.size();
    }

    public List<SubTask<U>> getSubTaskList() {
        return subTasks;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isSuspendable() {
        if (subTasks.isEmpty()) { return true; }
        return subTasks.getFirst().suspendable();
    }

    public SubTask<U> getCurrentSubTask() {
        if (subTasks.isEmpty()) { return null; }
        return subTasks.getFirst();
    }

    private void linkConcurrentListeners() {
        subTasks.forEach(t -> {
            if (t.hasEventListener()) { t.linkEventListener(enableListener); }
        });
    }

    private void unLinkConcurrentListeners() {
        subTasks.forEach(t -> {
            if (t.hasEventListener()) { t.unLinkEventListener(disableListener); }
        });
    }

    public boolean onTick(Tick tick) {
        if (concurrentSubTasks) {
            subTasks.forEach(t -> t.onTick(tick));
        } else {
            subTasks.getFirst().onTick(tick);
        }
        boolean completed = completionCheck();
        if (completed) {
            isCompleted = true;
            if (onCompletion != null) { onCompletion.accept(data); }
        }
        return completed;
    }

    private boolean completionCheck() {
        if (concurrentSubTasks) {
            boolean completed = subTasks.stream().allMatch(SubTask::completed);
            if (completed) { unLinkConcurrentListeners(); }
            return completed;
        } else {
            if (subTasks.getFirst().completed()) {
                subTasks.getFirst().unLinkEventListener(disableListener);
                subTasks.removeFirst();
                if (subTasks.isEmpty()) { return true; }
                subTasks.getFirst().linkEventListener(enableListener);
                subTasks.getFirst().onStart();
            }
        }
        return false;
    }

    public static <T extends Enum<T>, U> Builder<T, U> builder(T taskType, U data) {
        return new Builder<>(taskType, data);
    }

    public static class Builder<T extends Enum<T>, U> {
        private T taskType;
        private U data;
        private float taskWeight = 0;
        private List<SubTask<U>> subTasks = new ArrayList<>();
        private boolean concurrentSubTasks = false;
        private TriConsumer<EventType, Consumer<?>, Boolean> enableListener;
        private Consumer<EventType> disableListener;
        private Consumer<U> onCompletion;

        public Builder(T taskType, U data) {
            this.taskType = taskType;
            this.data = data;
        }

        public Builder<T, U> setConcurrentSubTasks(boolean isConcurrent) {
            concurrentSubTasks = isConcurrent;
            return this;
        }

        public Builder<T, U> setListenerLinks(TriConsumer<EventType, Consumer<?>, Boolean> linkConsumer,
                Consumer<EventType> unlinkConsumer) {
            this.enableListener = linkConsumer;
            this.disableListener = unlinkConsumer;
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

        public Builder<T, U> addSubEventTask(
                U data,
                EventType eventType,
                Consumer<U> onStartConsumer,
                BiConsumer<U, Event<?>> eventConsumer,
                BiPredicate<U, Event<?>> eventPredicate,
                boolean suspendable) {
            subTasks.add(SubTask.ofOnEvent(data, eventType, onStartConsumer, eventConsumer, eventPredicate, suspendable));
            return this;
        }

        public Builder<T, U> addSubTickTask(
                U data,
                Consumer<U> onStartConsumer,
                BiConsumer<U, Tick> tickConsumer,
                BiPredicate<U, Tick> tickPredicate,
                boolean suspendable) {
            subTasks.add(SubTask.ofOnTick(data, onStartConsumer, tickConsumer, tickPredicate, suspendable));
            return this;
        }

        public Builder<T, U> addSuckEventTickTask(
                U data,
                EventType eventType,
                Consumer<U> onStartConsumer,
                BiConsumer<U, Event<?>> eventConsumer,
                BiPredicate<U, Event<?>> eventPredicate,
                BiConsumer<U, Tick> tickConsumer,
                BiPredicate<U, Tick> tickPredicate,
                boolean suspendable) {
            subTasks.add(SubTask.ofEventAndTick(data, eventType, onStartConsumer, eventConsumer,
                    eventPredicate, tickConsumer, tickPredicate, suspendable));
            return this;
        }

        public Task<T, U> builder() {
            if ((enableListener == null || disableListener == null)
                    && subTasks.stream().anyMatch(SubTask::hasEventListener)) {
                throw new IllegalStateException("Must add listener links for event based subtasks");
            }
            if (subTasks.isEmpty()) {
                throw new IllegalStateException("Must have subtasks");
            }
            return new Task<>(taskType, data, taskWeight, List.copyOf(subTasks), concurrentSubTasks,
                    enableListener, disableListener, onCompletion);
        }

    }
}
