package com.macho.muscle.core.actor;

public class StopTask<T extends ActorLifecycle> implements ActorTask<T> {
    @Override
    public void accept(ActorContainer<T> actorContainer) {
        actorContainer.stop();
    }
}
