package com.macho.muscle.core.cluster.registry;

import com.macho.muscle.core.nactor.ActorInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Registry {
    CompletableFuture<Long> registry(String key, ActorInfo actorInfo, long lease);

    CompletableFuture<Long> keepAlive(long leaseKey);

    CompletableFuture<List<ActorInfo>> getActorsWithName(String keyPrefix);

    void watchPrefix(String keyPrefix, Consumer<List<ActorInfo>> onAddCallback, Consumer<List<ActorInfo>> onRemoveCallback);
}
