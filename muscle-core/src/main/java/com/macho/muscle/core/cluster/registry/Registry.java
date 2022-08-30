package com.macho.muscle.core.cluster.registry;

import com.macho.muscle.core.cluster.ServiceInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Registry {
    CompletableFuture<Boolean> registry(String key, ServiceInfo serviceInfo, long lease);

    CompletableFuture<List<ServiceInfo>> getServicesWithName(String keyPrefix);

    void watchPrefix(String keyPrefix, Consumer<List<ServiceInfo>> onAddCallback, Consumer<List<ServiceInfo>> onRemoveCallback);
}
