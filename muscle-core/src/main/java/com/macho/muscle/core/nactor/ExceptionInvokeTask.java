package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionInvokeTask<T extends ActorLifecycle> implements ActorTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionInvokeTask.class);

    private final Throwable throwable;

    public ExceptionInvokeTask(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        ActorLifecycle target = actorContainer.getTarget();

        target.onException(throwable);
    }
}
