package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.exception.ActorIsStoppedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class InvokeTask<T extends ActorLifecycle, R> implements ActorTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(InvokeTask.class);

    private final ActorRef targetActorRef;
    private final CompletableFuture<R> future;

    private final String fullMethodName;
    private final Object[] args;
    private final MethodInvokeHelper<T> methodInvokeHelper;

    public InvokeTask(ActorRef targetActorRef, Class<T> targetClass, String fullMethodName, Object[] args, CompletableFuture<R> future) {
        this.targetActorRef = targetActorRef;
        this.fullMethodName = fullMethodName;
        this.args = args;
        this.future = future;

        this.methodInvokeHelper = (MethodInvokeHelper<T>) MethodInvokeHelperCache.getMethodInvokeHelper(targetClass);
    }

    @Override
    public void cancel() {
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void stop() {
        if (future != null) {
            future.completeExceptionally(
                    new ActorIsStoppedException(targetActorRef.getActorInfo().getId())
            );
        }
    }

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        try {
            T target = actorContainer.getTarget();

            R result = methodInvokeHelper.doInvoke(target, fullMethodName, args);

            if (future != null) {
                future.complete(result);
            }
        } catch (Throwable e) {
            logger.error(String.format(
                            "actor[%s] method[%s] invoke error!",
                            targetActorRef.getActorInfo().toString(),
                            fullMethodName),
                    e
            );

            if (future != null) {
                future.completeExceptionally(e);
            } else {
                logger.error(
                        String.format(
                                "actor[%s] method[%s] invoke error!",
                                targetActorRef.getActorInfo().toString(),
                                fullMethodName),
                        e
                );
            }
        }
    }
}
