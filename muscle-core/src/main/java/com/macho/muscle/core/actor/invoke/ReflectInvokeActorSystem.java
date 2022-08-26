package com.macho.muscle.core.actor.invoke;

import com.macho.muscle.core.actor.*;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

public class ReflectInvokeActorSystem extends ActorSystem {

    private ReflectInvokeNoSender reflectInvokeNoSender;

    private final AtomicLong requestIdCounter = new AtomicLong(0L);

    public ReflectInvokeActorSystem(String systemName, int parallelism) {
        super(systemName, parallelism);

        init();
    }

    private void init() {
        this.reflectInvokeNoSender = (ReflectInvokeNoSender) registerActor(
                "System-ReflectInvokeNoSender",
                "ReflectInvokeNoSender",
                (actorRef) -> new ReflectInvokeNoSender(actorRef, 1024, getExecutor(), this)
        );
    }

    public long genRequestId() {
        return requestIdCounter.incrementAndGet();
    }


    public <T extends ActorLifecycle> void registerReflectInvokeActor(String id, String serviceName, T target, int queueCapacity) {
        registerActor(
                id,
                serviceName,
                actorRef -> new ReflectInvokeActorRunner<T>(actorRef, target, queueCapacity, getExecutor(), this)
        );
    }

    public <T> T newReflectInvokeProxy(String id, String serviceName, Class<T> targetClass) {
        ActorRef actorRef = buildActorRef(id, serviceName);

        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new DynamicInvokeProxy(actorRef, this)
        );
    }

    public ReflectInvokeNoSender getReflectInvokeNoSender() {
        return reflectInvokeNoSender;
    }

}
