package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.exception.ActorIsStoppedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class RemoteInvokeTask<T extends ActorLifecycle> implements ActorTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(RemoteInvokeTask.class);

    private final CompletableFuture<Object> future;
    private final ActorRef targetActorRef;

    private final String fullMethodName;
    private final Object[] args;

    public RemoteInvokeTask(ActorRef targetActorRef, String fullMethodName, Object[] args, CompletableFuture<Object> future) {
        this.targetActorRef = targetActorRef;
        this.future = future;
        this.fullMethodName = fullMethodName;
        this.args = args;
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
            future.completeExceptionally(new ActorIsStoppedException(targetActorRef.getActorInfo().getId()));
        }
    }

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        try {
            T target = actorContainer.getTarget();
            MethodInvokeHelper methodInvokeHelper = MethodInvokeHelperCache.getMethodInvokeHelper(target.getClass());
            Object result = methodInvokeHelper.doInvoke(target, fullMethodName, args);
            if (future != null) {
                if (result instanceof Future resultFuture) {
                    Object obj = resultFuture.get();
                    future.complete(obj);
                } else {
                    future.complete(result);
                }
            }
        } catch (Throwable e) {
            logger.debug(String.format(
                            "actor[%s] method[%s] invoke error!",
                            targetActorRef.getActorInfo().toString(),
                            fullMethodName),
                    e
            );

            if (future != null) {
                future.completeExceptionally(e);
            } else {
                logger.error(String.format(
                                "actor[%s] method[%s] invoke error!",
                                targetActorRef.getActorInfo().toString(),
                                fullMethodName),
                        e
                );
            }
        }
    }
}
