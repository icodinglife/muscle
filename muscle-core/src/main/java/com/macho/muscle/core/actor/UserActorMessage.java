//package com.macho.muscle.core.actor;
//
//import lombok.Builder;
//import lombok.Data;
//
//@Data
//@Builder
//public class UserActorMessage<T> implements ActorMessage<T> {
//    private final ActorRef sourceActorRef;
//    private final ActorRef targetActorRef;
//
//    private final T data;
//
//    @Override
//    public ActorMessageType getMessageType() {
//        return ActorMessageType.USER_MESSAGE;
//    }
//
//    @Override
//    public T getData() {
//        return data;
//    }
//}
