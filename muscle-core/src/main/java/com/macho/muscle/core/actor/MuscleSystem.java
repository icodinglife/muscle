package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;
import com.macho.muscle.core.exception.ActorNotFoundException;
import com.macho.muscle.core.cluster.ClusterSystem;
import com.macho.muscle.core.cluster.NodeInfo;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.*;

public class MuscleSystem {
    private static final Logger logger = LoggerFactory.getLogger(MuscleSystem.class);

    public static final boolean PUBLISH = Boolean.TRUE;

    private final Map<String, ActorContainer<? extends ActorLifecycle>> actorIdToContainerMap = Maps.newConcurrentMap();

    private final HashedWheelTimer timer = new HashedWheelTimer();

    private final Executor executor;
    private ClusterSystem clusterSystem;

    public MuscleSystem() {
        executor = newVirtualThreadPerTaskExecutor();

        timer.start();
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
        return newInvokeProxy(actorId, service, targetClass, false);
    }

    public <T extends ActorLifecycle> T newInvokeProxy(String actorId, String service, Class<T> targetClass, boolean remote) {
        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new InvokeProxy<T>(
                        buildActorRef(actorId, service, clusterSystem == null ? null : clusterSystem.getNodeInfo()),
                        targetClass,
                        this,
                        remote
                )
        );
    }


    public <T extends ActorLifecycle> ActorContainer<? extends ActorLifecycle> registerActor(String actorId, String service, T target, int queueCapacity) {
        return registerActor(actorId, service, target, queueCapacity, !PUBLISH);
    }

    public <T extends ActorLifecycle> ActorContainer<? extends ActorLifecycle> registerActor(String actorId, String service, T target, int queueCapacity, boolean publish) {
        return actorIdToContainerMap.computeIfAbsent(actorId, (id) -> initActorContainer(id, service, target, queueCapacity, publish));
    }

    private <T extends ActorLifecycle> ActorContainer<? extends ActorLifecycle> initActorContainer(String actorId, String service, T target, int queueCapacity, boolean publish) {
        ActorRef actorRef = buildActorRef(actorId, service, clusterSystem == null ? null : clusterSystem.getNodeInfo());

        ActorContainer<T> actorContainer = new ActorContainer<>(this, actorRef, target, queueCapacity);

        actorContainer.schedule(new StartTask());
        if (publish) {
            actorContainer.schedule(new PublishTask());
        }

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

    public void shutdown() {
        for (Map.Entry<String, ActorContainer<? extends ActorLifecycle>> entry : actorIdToContainerMap.entrySet()) {
            dispatch(entry.getKey(), new StopTask<>());
        }

        if (actorIdToContainerMap.size() > 0) {
            logger.warn("also has actor in the system.");
        }

        this.timer.stop();
        ExecutorService executorService = (ExecutorService) executor;
        executorService.shutdown();
    }

    public void stopActor(String actorId) {
        dispatch(actorId, new StopTask<>());
    }

    void removeActor(String actorId) {
        actorIdToContainerMap.remove(actorId);
    }

    public <T extends ActorLifecycle> void watchActorStop(ActorRef selfRef, ActorInfo remoteActorInfo, Consumer<T> whenStop) {
        clusterSystem.watchRemoteActorStop(
                remoteActorInfo,
                () -> {
                    if (selfRef != null) {
                        dispatch(selfRef.getActorInfo().getId(), new FunctionCallTask<>(whenStop));
                    } else {
                        whenStop.accept(null);
                    }
                }
        );
    }
}
