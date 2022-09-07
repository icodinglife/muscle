package com.macho.muscle.core.bench.counter;

import com.macho.muscle.core.actor.ActorLifecycle;

import java.util.concurrent.CompletableFuture;

public interface ICounter extends ActorLifecycle {
    CompletableFuture<String> count(int i);

    void shutdown();
}
