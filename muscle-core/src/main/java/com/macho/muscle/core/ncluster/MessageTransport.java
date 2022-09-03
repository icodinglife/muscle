package com.macho.muscle.core.ncluster;

import java.util.concurrent.CompletableFuture;

public interface MessageTransport {
    CompletableFuture<Object> transfer(TransportActorMessage message);

    void disconnect();
}