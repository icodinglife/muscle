//package com.macho.muscle.core.actor.invoke;
//
//import com.macho.muscle.core.actor.ActorRef;
//import com.macho.muscle.core.actor.ActorRunner;
//import com.macho.muscle.core.actor.ActorSystem;
//import com.macho.muscle.core.utils.MuscleReflectUtil;
//
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.util.concurrent.CompletableFuture;
//
//public class DynamicInvokeProxy implements InvocationHandler {
//
//    private final ActorRef actorRef;
//    private final ReflectInvokeActorSystem actorSystem;
//
//    public DynamicInvokeProxy(ActorRef actorRef, ReflectInvokeActorSystem actorSystem) {
//        this.actorRef = actorRef;
//        this.actorSystem = actorSystem;
//    }
//
//    private ReflectInvokeRequestMessage buildInvokeMessage(Method method, Object[] args) {
//        String fullMethodName = MuscleReflectUtil.getFullMethodName(method);
//
//        Long requestId = actorSystem.genRequestId();
//
//        return ReflectInvokeRequestMessage.builder()
//                .id(requestId)
//                .methodName(fullMethodName)
//                .args(args)
//                .build();
//    }
//
//    @Override
//    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
//        ReflectInvokeActorRunner currentActor = (ReflectInvokeActorRunner) ActorRunner.currentActor();
//
//        if (currentActor == null) {
//            currentActor = actorSystem.getReflectInvokeNoSender();
//        }
//
//        ReflectInvokeRequestMessage reflectInvokeMessage = buildInvokeMessage(method, args);
//
//        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
//
//        try {
//            actorRef.send(currentActor.selfActorRef(), reflectInvokeMessage);
//        } catch (Throwable e) {
//
//            throw e;
//        }
//
//        if (method.getReturnType() != void.class) {
//            currentActor.addResultFuture(reflectInvokeMessage.getId(), resultFuture);
//        }
//
//        return resultFuture;
//    }
//}
