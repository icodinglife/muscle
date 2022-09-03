package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;

import java.util.function.Consumer;

public interface ActorTask<T extends ActorLifecycle> extends Consumer<ActorContainer<T>> {

    default void cancel() {
    }

    default void stop() {
    }
}
