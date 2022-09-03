//package com.macho.muscle.core.actor.invoke;
//
//import com.macho.muscle.core.actor.*;
//import com.macho.muscle.core.cluster.node.ClusterNode;
//
//import java.lang.reflect.Proxy;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicLong;
//
//public class ReflectInvokeActorSystem extends ActorSystem {
//
//    private ReflectInvokeNoSender reflectInvokeNoSender;
//
//    private final AtomicLong requestIdCounter = new AtomicLong(0L);
//
//    public ReflectInvokeActorSystem(String systemName, int parallelism) {
//        super(systemName, parallelism);
//    }
//
//    // todo split from constructor
//    public void init() {
//        this.reflectInvokeNoSender = (ReflectInvokeNoSender) registerActor(
//                "System-ReflectInvokeNoSender",
//                "ReflectInvokeNoSender",
//                (actorRef) -> new ReflectInvokeNoSender(actorRef, 1024, getExecutor(), this)
//        );
//    }
//
//    public long genRequestId() {
//        return requestIdCounter.incrementAndGet();
//    }
//
//
//    public <T extends ActorLifecycle> void registerReflectInvokeActor(String id, String serviceName, T target, int queueCapacity) {
//        registerActor(
//                id,
//                serviceName,
//                actorRef -> new ReflectInvokeActorRunner<T>(actorRef, target, queueCapacity, getExecutor(), this)
//        );
//    }
//
//    public <T> T newReflectInvokeProxy(String id, String serviceName, Class<T> targetClass) {
//        ActorRef actorRef = buildActorRef(id, serviceName);
//
//        return (T) Proxy.newProxyInstance(
//                targetClass.getClassLoader(),
//                new Class[]{targetClass},
//                new DynamicInvokeProxy(actorRef, this)
//        );
//    }
//
//    public <T> T newRemoteReflectInvokeProxy(String id, String serviceName, Class<T> targetClass) {
//        ActorRef actorRef = buildActorRef(id, serviceName, true);
//
//        return (T) Proxy.newProxyInstance(
//                targetClass.getClassLoader(),
//                new Class[]{targetClass},
//                new DynamicInvokeProxy(actorRef, this)
//        );
//    }
//
//    public ReflectInvokeNoSender getReflectInvokeNoSender() {
//        return reflectInvokeNoSender;
//    }
//
//    @Override
//    public <T> void dispatchRemoteMessage(UserActorMessage<T> actorMessage) {
//        checkClusterSystem();
//
//        ActorInfo targetActorInfo = actorMessage.getTargetActorRef().getActorInfo();
//        String targetService = targetActorInfo.getService();
//        String targetActorId = targetActorInfo.getId();
//
//        ClusterNode targetClusterNode = clusterSystem.getClusterNodeWithServiceAndActorId(targetService, targetActorId);
//        CompletableFuture<Void> transferFuture = targetClusterNode.transferRemoteMessage(buildTransportActorMessage(actorMessage));
//        transferFuture.whenComplete((v, err) -> {
//            if (err != null) {
//                ReflectInvokeResponseMessage response = ReflectInvokeResponseMessage.builder()
//                        .id(((IReflectInvokeMessage) actorMessage.getData()).getId())
//                        .build();
//                UserActorMessage responseActorMessage = UserActorMessage.builder()
//                        .sourceActorRef(actorMessage.getTargetActorRef())
//                        .targetActorRef(actorMessage.getSourceActorRef())
//                        .data(response)
//                        .build();
//
//                dispatch(responseActorMessage);
//            }
//        });
//    }
//}
