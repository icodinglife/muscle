package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;
import com.macho.muscle.core.exception.ActorNotFoundException;
import com.macho.muscle.core.cluster.ClusterSystem;
import com.macho.muscle.core.cluster.NodeInfo;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.*;

public class MuscleSystem {
    private static final Logger logger = LoggerFactory.getLogger(MuscleSystem.class);

    private final Map<String, ActorContainer<? extends ActorLifecycle>> actorIdToContainerMap = Maps.newConcurrentMap();

    private final Timer timer = new HashedWheelTimer();

    private final Executor executor;
    private ClusterSystem clusterSystem;

    public MuscleSystem() {
        executor = newVirtualThreadPerTaskExecutor();
    }

    public void startCluster(String etcdAddr, int port) {
        try {
            clusterSystem = new ClusterSystem(etcdAddr, port);

            clusterSystem.start(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public ClusterSystem getClusterSystem() {
        return clusterSystem;
    }

    public <T extends ActorLifecycle> T newInvokeProxy(String actorId, String service, Class<T> targetClass) {
        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new InvokeProxy<T>(
                        buildActorRef(actorId, service, clusterSystem == null ? null : clusterSystem.getNodeInfo()),
                        targetClass,
                        this
                )
        );
    }

    public <T extends ActorLifecycle> ActorContainer<? extends ActorLifecycle> registerActor(String actorId, String service, T target, int queueCapacity) {
        return actorIdToContainerMap.computeIfAbsent(actorId, (id) -> initActorContainer(id, service, target, queueCapacity));
    }

    private <T extends ActorLifecycle> ActorContainer<? extends ActorLifecycle> initActorContainer(String actorId, String service, T target, int queueCapacity) {
        ActorRef actorRef = buildActorRef(actorId, service, clusterSystem == null ? null : clusterSystem.getNodeInfo());

        ActorContainer<T> actorContainer = new ActorContainer<>(this, actorRef, target, queueCapacity);

        actorContainer.schedule(new StartTask());

        executor.execute(actorContainer);

        return actorContainer;
    }

    public ActorRef buildActorRef(String actorId, String service, NodeInfo nodeInfo) {
        ActorInfo actorInfo = new ActorInfo(actorId, service, nodeInfo);
        ActorRef actorRef = new ActorRef(actorInfo);

        return actorRef;
    }

    public ActorRef buildActorRef(ActorInfo actorInfo) {
        ActorRef actorRef = new ActorRef(actorInfo);

        return actorRef;
    }

    public boolean dispatch(String actorId, ActorTask<?> actorTask) {
        ActorContainer<?> actorContainer = actorIdToContainerMap.get(actorId);
        if (actorContainer == null) {
            throw new ActorNotFoundException(actorId);
        }

        return actorContainer.schedule(actorTask);
    }

    public void schedule(TimerTask task, long delay, TimeUnit timeUnit) {
        timer.newTimeout(task, delay, timeUnit);
    }
}
