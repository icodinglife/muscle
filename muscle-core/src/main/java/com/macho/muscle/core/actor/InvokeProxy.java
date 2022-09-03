package com.macho.muscle.core.actor;

import com.macho.muscle.core.exception.TargetActorRejectException;
import com.macho.muscle.core.utils.MuscleReflectUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class InvokeProxy<T extends ActorLifecycle> implements InvocationHandler {

    private final ActorRef actorRef;
    private final MuscleSystem system;

    private final Class<T> targetClass;

    public InvokeProxy(ActorRef actorRef, Class<T> targetClass, MuscleSystem system) {
        this.actorRef = actorRef;
        this.system = system;
        this.targetClass = targetClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean hasReturnValue = method.getReturnType() != void.class;

        CompletableFuture<Object> resultFuture = hasReturnValue ? new CompletableFuture<>() : null;

        String fullMethodName = MuscleReflectUtil.getFullMethodName(method);

        InvokeTask<T, Object> invokeTask = new InvokeTask<>(actorRef, targetClass, fullMethodName, args, resultFuture);

        String targetActorId = actorRef.getActorInfo().getId();
        if (!system.dispatch(targetActorId, invokeTask)) {
            throw new TargetActorRejectException(targetActorId);
        }

        if (hasReturnValue) {
            return resultFuture.get();
        } else {
            return null;
        }
    }
}
