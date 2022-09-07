package com.macho.muscle.core.cluster;

import java.util.concurrent.CompletableFuture;

public interface MessageTransport {
    CompletableFuture<Object> transfer(TransportActorMessage message);

    void disconnect();
}