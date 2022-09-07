package com.macho.muscle.core.actor;

import java.util.function.Consumer;

public class FunctionCallTask<T extends ActorLifecycle> implements ActorTask<T> {
    private final Consumer<T> function;

    public FunctionCallTask(Consumer<T> function) {
        this.function = function;
    }

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        T target = actorContainer.getTarget();

        function.accept(target);
    }
}
