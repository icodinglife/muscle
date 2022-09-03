package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;

public class StartTask<T extends ActorLifecycle> implements ActorTask<T> {
    @Override
    public void accept(ActorContainer<T> actorContainer) {
        actorContainer.start();

        ActorLifecycle target = actorContainer.getTarget();

        try {
            target.onStart();
        } catch (Throwable e) {
            try {
                target.onException(e);
            } catch (Throwable ee) {
                throw new IllegalStateException(ee);
            }
        }
    }
}
