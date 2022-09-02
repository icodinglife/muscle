package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;
import com.macho.muscle.core.cluster.ClusterSystem;
import com.macho.muscle.core.cluster.node.ClusterNode;
import com.macho.muscle.core.cluster.transport.TransportActorMessage;
import com.macho.muscle.core.exception.ActorNotFoundException;
import com.macho.muscle.core.exception.TargetActorRejectException;
import com.macho.muscle.core.utils.KryoUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class ActorSystem {

    private final String systemName;
    private final Executor executor;

    private final Map<String, ActorRunner> actorIdToActorRunnerMap = Maps.newConcurrentMap();

    private final Timer timer = new HashedWheelTimer();

    protected ClusterSystem clusterSystem;

    public ActorSystem(String systemName, int parallelism) {
        this.systemName = systemName;
        this.executor = new ForkJoinPool(parallelism);

        init();
    }

    public void startCluster(String etcdAddr, int nodePort) {
        clusterSystem = new ClusterSystem(etcdAddr, nodePort, this);

        clusterSystem.start();
    }

    public Executor getExecutor() {
        return executor;
    }

    private void init() {

    }

    public Timer getTimer() {
        return timer;
    }

    public ActorRunner registerActor(String id, String serviceName, boolean publish, Function<ActorRef, ActorRunner> actorBuilder) {
        return actorIdToActorRunnerMap.computeIfAbsent(id, k -> {
            ActorRef actorRef = buildActorRef(id, serviceName);

            ActorRunner actorRunner = actorBuilder.apply(actorRef);

            actorRunner.offer(SystemActorMessage.START_MESSAGE);

            if (publish) {
                actorRunner.offer(SystemActorMessage.PUBLISH_MESSAGE);
            }

            return actorRunner;
        });
    }

    public ActorRunner registerActor(String id, String serviceName, Function<ActorRef, ActorRunner> actorBuilder) {
        return registerActor(id, serviceName, false, actorBuilder);
    }

    void deregisterActor(String id) {
        actorIdToActorRunnerMap.remove(id);
    }

    protected ActorRef buildActorRef(String id, String serviceName) {
        return buildActorRef(id, serviceName, false);
    }

    protected ActorRef buildActorRef(String id, String serviceName, boolean remote) {
        ActorInfo actorInfo = ActorInfo.builder()
                .id(id)
                .service(serviceName)
                .nodeInfo(
                        remote ? clusterSystem.getClusterNodeWithServiceAndActorId(serviceName, id).getNodeInfo() :
                                (clusterSystem == null ? null : clusterSystem.getNodeInfo())
                )
                .build();

        return new ActorRef(actorInfo, this, remote);
    }

    protected void checkClusterSystem() {
        if (this.clusterSystem == null) {
            throw new IllegalStateException("ClusterSystem not initialized.");
        }
    }

    public ClusterSystem getClusterSystem() {
        return clusterSystem;
    }

    public <T> void dispatchRemoteMessage(UserActorMessage<T> actorMessage) {
        checkClusterSystem();

        ActorInfo targetActorInfo = actorMessage.getTargetActorRef().getActorInfo();
        String targetService = targetActorInfo.getService();
        String targetActorId = targetActorInfo.getId();

        ClusterNode targetClusterNode = clusterSystem.getClusterNodeWithServiceAndActorId(targetService, targetActorId);
        CompletableFuture<Void> transferFuture = targetClusterNode.transferRemoteMessage(buildTransportActorMessage(actorMessage));
        transferFuture.whenComplete((v, err) -> {
            if (err != null) {
                // todo process dead letter
            }
        });
    }

    protected TransportActorMessage buildTransportActorMessage(UserActorMessage actorMessage) {
        return TransportActorMessage.builder()
                .type(actorMessage.getMessageType().getCode())
                .sourceActorInfo(actorMessage.getSourceActorRef().getActorInfo())
                .targetActorInfo(actorMessage.getTargetActorRef().getActorInfo())
                .data(KryoUtil.serialize(actorMessage.getData()))
                .build();
    }

    public <T> void dispatch(UserActorMessage<T> actorMessage) {
        dispatch(actorMessage.getTargetActorRef(), actorMessage);
    }

    public <T> void dispatchFromRemote(UserActorMessage<T> actorMessage) {
        try {
            dispatch(actorMessage.getTargetActorRef(), actorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            // todo process exception，response exception to source
            // todo process dead letter here，process exception return in invoke system.
        }
    }

    void dispatchSystem(ActorRef targetActorRef, SystemActorMessage actorMessage) {
        ActorRunner targetActorRunner = actorIdToActorRunnerMap.get(targetActorRef.getActorInfo().getId());

        if (targetActorRunner == null) {
            throw new ActorNotFoundException(targetActorRef.getActorInfo().toString());
        }

        boolean offerSuccess = targetActorRunner.offer(actorMessage);
        if (!offerSuccess) {
            throw new TargetActorRejectException(targetActorRef.getActorInfo().toString());
        }
    }

    private void dispatch(ActorRef targetActorRef, ActorMessage actorMessage) {
        String targetActorId = targetActorRef.getActorInfo().getId();

        ActorRunner targetActorRunner = actorIdToActorRunnerMap.get(targetActorId);

        if (targetActorRunner == null) {
            throw new ActorNotFoundException(targetActorId);
        }

        boolean offerSuccess = targetActorRunner.offer(actorMessage);
        if (!offerSuccess) {
            throw new TargetActorRejectException(targetActorId);
        }
    }

    // publish 放到 actorrunner 里面，register actor 的时候传参数是否需要 publish，如果 publish，则利用系统消息投递到 actor 内部执行 publish，在初始化之后 publish
//    public void publishActor(ActorInfo actorInfo) {
//        checkClusterSystem();
//
//        clusterSystem.registerActor(actorInfo);
//    }
}
