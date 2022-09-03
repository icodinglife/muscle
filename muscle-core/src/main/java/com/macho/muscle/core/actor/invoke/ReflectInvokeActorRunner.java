//package com.macho.muscle.core.actor.invoke;
//
//import com.google.common.collect.Maps;
//import com.macho.muscle.core.actor.*;
//import com.macho.muscle.core.exception.ActorIsStoppedException;
//
//import java.util.Map;
//import java.util.Queue;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//public class ReflectInvokeActorRunner<T extends ActorLifecycle> extends ActorRunner {
//    private final T target;
//    private final MethodInvokeHelper<T> methodInvokeHelper;
//    private final Map<Long, CompletableFuture<Object>> requestIdToFutureMap = Maps.newHashMap();
//
//    public ReflectInvokeActorRunner(ActorRef actorRef, T instance, int queueCapacity, Executor executor, ActorSystem actorSystem) {
//        super(actorRef, queueCapacity, executor, actorSystem);
//        this.target = instance;
//
//        if (instance != null) {
//            this.methodInvokeHelper = MethodInvokeHelperCache.getMethodInvokeHelper(instance.getClass());
//        } else {
//            this.methodInvokeHelper = null;
//        }
//    }
//
//    protected void processRequestMessage(ActorRef sourceActorRef, ReflectInvokeRequestMessage reflectInvokeRequestMessage) {
//        Long requestID = reflectInvokeRequestMessage.getId();
//
//        CompletableFuture<?> resultFuture = methodInvokeHelper.doInvoke(
//                target,
//                reflectInvokeRequestMessage.getMethodName(),
//                reflectInvokeRequestMessage.getArgs()
//        );
//
//        if (resultFuture != null) {
//            resultFuture.whenComplete((result, error) -> {
//                // thread safe with actor
//                ReflectInvokeResponseMessage reflectInvokeResponseMessage;
//
//                if (error != null) {
//                    reflectInvokeResponseMessage = ReflectInvokeResponseMessage.builder()
//                            .id(requestID)
//                            .exception(error)
//                            .build();
//                } else {
//                    reflectInvokeResponseMessage = ReflectInvokeResponseMessage.builder()
//                            .id(requestID)
//                            .result(result)
//                            .build();
//                }
//
//                sourceActorRef.send(selfActorRef(), reflectInvokeResponseMessage);
//            });
//        }
//    }
//
//    protected void processResponseMessage(ReflectInvokeResponseMessage reflectInvokeResponseMessage) {
//        Long requestId = reflectInvokeResponseMessage.getId();
//
//        CompletableFuture<Object> resultFuture = requestIdToFutureMap.get(requestId);
//
//        if (resultFuture == null) {
//            throw new IllegalStateException("Can not find result future of request: " + requestId);
//        }
//
//        if (reflectInvokeResponseMessage.getException() != null) {
//            resultFuture.completeExceptionally(reflectInvokeResponseMessage.getException());
//        } else {
//            resultFuture.complete(reflectInvokeResponseMessage.getResult());
//        }
//    }
//
//    @Override
//    protected void onRecvMessage(UserActorMessage<?> message) {
//        IReflectInvokeMessage reflectInvokeMessage = (IReflectInvokeMessage) message.getData();
//
//        IReflectInvokeMessage.InvokeMessageType invokeMessageType = reflectInvokeMessage.getType();
//
//        switch (invokeMessageType) {
//            case REQUEST: {
//                ReflectInvokeRequestMessage reflectInvokeRequestMessage = (ReflectInvokeRequestMessage) reflectInvokeMessage;
//
//                processRequestMessage(message.getSourceActorRef(), reflectInvokeRequestMessage);
//            }
//            break;
//            case RESPONSE: {
//                ReflectInvokeResponseMessage reflectInvokeResponseMessage = (ReflectInvokeResponseMessage) reflectInvokeMessage;
//
//                processResponseMessage(reflectInvokeResponseMessage);
//            }
//            break;
//            default:
//                throw new IllegalStateException("Unknown ReflectInvokeMessage type");
//        }
//    }
//
//    @Override
//    public void rejectUnpressedMessages(Queue<ActorMessage<?>> queue) {
//        ActorMessage<?> actorMessage = queue.poll();
//        while (actorMessage != null) {
//            if (actorMessage.getMessageType() == ActorMessageType.USER_MESSAGE) {
//                UserActorMessage<?> userActorMessage = (UserActorMessage<?>) actorMessage;
//                IReflectInvokeMessage reflectInvokeMessage = (IReflectInvokeMessage) userActorMessage.getData();
//
//                IReflectInvokeMessage.InvokeMessageType invokeMessageType = reflectInvokeMessage.getType();
//
//                switch (invokeMessageType) {
//                    case REQUEST: {
//                        ReflectInvokeResponseMessage reflectInvokeResponseMessage = ReflectInvokeResponseMessage.builder()
//                                .id(reflectInvokeMessage.getId())
//                                .exception(new ActorIsStoppedException(selfActorRef().getActorInfo().getId()))
//                                .build();
//
//                        userActorMessage.getSourceActorRef().send(selfActorRef(), reflectInvokeResponseMessage);
//                    }
//                    break;
//                    case RESPONSE: {
//                        ReflectInvokeResponseMessage reflectInvokeResponseMessage = (ReflectInvokeResponseMessage) reflectInvokeMessage;
//
//                        processResponseMessage(reflectInvokeResponseMessage);
//                    }
//                    break;
//                    default:
//                        throw new IllegalStateException("Unknown ReflectInvokeMessage type");
//                }
//            }
//
//            actorMessage = queue.poll();
//        }
//
//        for (Map.Entry<Long, CompletableFuture<Object>> entry : requestIdToFutureMap.entrySet()) {
//            CompletableFuture<Object> future = entry.getValue();
//
//            future.completeExceptionally(new ActorIsStoppedException(selfActorRef().getActorInfo().getId()));
//        }
//    }
//
//    public void addResultFuture(Long requestId, CompletableFuture<Object> resultFuture) {
//        requestIdToFutureMap.put(requestId, resultFuture);
//    }
//
//    @Override
//    protected void onException(Exception e) {
//        target.onException(e);
//    }
//
//    @Override
//    protected void onStart() {
//        target.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        target.onStop();
//    }
//}
