package com.macho.muscle.core.actor;

import com.macho.muscle.core.cluster.ClusterNode;
import com.macho.muscle.core.cluster.TransportActorMessage;
import com.macho.muscle.core.exception.TargetActorRejectException;
import com.macho.muscle.core.utils.KryoUtil;
import com.macho.muscle.core.utils.MuscleReflectUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class InvokeProxy<T extends ActorLifecycle> implements InvocationHandler {

    private final ActorRef actorRef;
    private final MuscleSystem system;

    private final Class<T> targetClass;

    private final boolean remote;

    public InvokeProxy(ActorRef actorRef, Class<T> targetClass, MuscleSystem system) {
        this(actorRef, targetClass, system, false);
    }

    public InvokeProxy(ActorRef actorRef, Class<T> targetClass, MuscleSystem system, boolean remote) {
        this.actorRef = actorRef;
        this.system = system;
        this.targetClass = targetClass;
        this.remote = remote;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean hasReturnValue = method.getReturnType() != void.class;

        CompletableFuture<Object> resultFuture = hasReturnValue ? new CompletableFuture<>() : null;

        String fullMethodName = MuscleReflectUtil.getFullMethodName(method);

        ActorInfo actorInfo = actorRef.getActorInfo();
        String targetActorId = actorInfo.getId();

        if (remote) {
            ClusterNode remoteActorClusterNode = system.getClusterSystem()
                    .getClusterNodeWithServiceAndActorId(actorInfo.getService(), targetActorId);

            TransportActorMessage transportActorMessage = new TransportActorMessage(
                    null, actorInfo, fullMethodName, KryoUtil.serialize(args)
            );

            CompletableFuture<Object> remoteFuture = remoteActorClusterNode.transferRemoteMessage(transportActorMessage);

            if (hasReturnValue) {
                resultFuture = remoteFuture;
            }
        } else {
            InvokeTask<T, Object> invokeTask = new InvokeTask<>(actorRef, targetClass, fullMethodName, args, resultFuture);

            if (!system.dispatch(targetActorId, invokeTask)) {
                throw new TargetActorRejectException(targetActorId);
            }
        }

        if (hasReturnValue) {
            return resultFuture.get();
        } else {
            return null;
        }
    }
}
