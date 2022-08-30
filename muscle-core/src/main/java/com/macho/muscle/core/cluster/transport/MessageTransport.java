package com.macho.muscle.core.cluster.transport;

import com.macho.muscle.core.actor.ActorMessage;

import java.util.concurrent.CompletableFuture;

public interface MessageTransport {
    CompletableFuture<Void> transfer(TransportActorMessage message);

    void disconnect();
}