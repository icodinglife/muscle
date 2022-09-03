package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;

public class StopTask<T extends ActorLifecycle> implements ActorTask<T> {
    @Override
    public void accept(ActorContainer<T> actorContainer) {
        actorContainer.stop();
    }
}
