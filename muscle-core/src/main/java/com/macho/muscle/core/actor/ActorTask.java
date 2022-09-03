package com.macho.muscle.core.actor;

import java.util.function.Consumer;

public interface ActorTask<T extends ActorLifecycle> extends Consumer<ActorContainer<T>> {

    default void cancel() {
    }

    default void stop() {
    }
}
