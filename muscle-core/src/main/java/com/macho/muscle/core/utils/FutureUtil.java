package com.macho.muscle.core.utils;

import com.macho.muscle.core.actor.ActorRunner;
import com.macho.muscle.core.actor.UserActorMessage;
import com.macho.muscle.core.actor.invoke.ReflectInvokeActorRunner;
import com.macho.muscle.core.actor.invoke.ReflectInvokeActorSystem;
import com.macho.muscle.core.actor.invoke.ReflectInvokeResponseMessage;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class FutureUtil {
    public static <T> void attachFuture(CompletableFuture<T> future, BiConsumer<T, ? super Throwable> whenComplete) {
        ReflectInvokeActorRunner currentActor = (ReflectInvokeActorRunner) ActorRunner.currentActor();
        if (currentActor == null) {
            throw new IllegalStateException("Can't get actor in current thread local context.");
        }
        ReflectInvokeActorSystem actorSystem = (ReflectInvokeActorSystem) currentActor.selfActorRef().getActorSystem();
        if (actorSystem == null) {
            throw new IllegalStateException("Can't get ReflectInvokeActorSystem in current thread local context.");
        }

        CompletableFuture<T> newFuture = new CompletableFuture<>();
        newFuture.whenComplete(whenComplete);

        long reqId = actorSystem.genRequestId();

        currentActor.addResultFuture(reqId, newFuture);

        future.whenComplete((res, err) -> {
            ReflectInvokeResponseMessage reflectInvokeResponseMessage = ReflectInvokeResponseMessage.builder()
                    .id(reqId)
                    .exception(err)
                    .result(res)
                    .build();
            UserActorMessage<Object> userActorMessage = UserActorMessage.builder()
                    .targetActorRef(currentActor.selfActorRef())
                    .data(reflectInvokeResponseMessage)
                    .build();

            actorSystem.dispatch(userActorMessage);
        });
    }
}
