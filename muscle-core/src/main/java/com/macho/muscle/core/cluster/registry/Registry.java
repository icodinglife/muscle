package com.macho.muscle.core.cluster.registry;

import com.macho.muscle.core.actor.ActorInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Registry {
    CompletableFuture<Boolean> registry(String key, ActorInfo actorInfo, long lease);

    CompletableFuture<List<ActorInfo>> getActorsWithName(String keyPrefix);

    void watchPrefix(String keyPrefix, Consumer<List<ActorInfo>> onAddCallback, Consumer<List<ActorInfo>> onRemoveCallback);
}
