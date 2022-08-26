package com.macho.muscle.core.actor;

import java.util.concurrent.Executor;

public class NoSender extends ActorRunner {
    public NoSender(ActorRef actorRef, int queueCapacity, Executor executor, ActorSystem actorSystem) {
        super(actorRef, queueCapacity, executor, actorSystem);
    }

    @Override
    protected void onRecvMessage(UserActorMessage message) {
        // todo dead letter process
    }

    @Override
    protected void onException(Exception e) {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
