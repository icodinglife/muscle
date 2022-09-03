//package com.macho.muscle.core.actor;
//
//import lombok.Data;
//
//@Data
//public class ActorRef {
//    private final ActorInfo actorInfo;
//    private final ActorSystem actorSystem;
//
//    private final boolean remote;
//
//    public ActorRef(ActorInfo actorInfo, ActorSystem actorSystem) {
//        this.actorInfo = actorInfo;
//        this.actorSystem = actorSystem;
//        this.remote = false;
//    }
//
//    public ActorRef(ActorInfo actorInfo, ActorSystem actorSystem, boolean remote) {
//        this.actorInfo = actorInfo;
//        this.actorSystem = actorSystem;
//        this.remote = remote;
//    }
//
//    public <M> void send(ActorRef sourceActorRef, M message) {
//        UserActorMessage<M> actorMessage = UserActorMessage.<M>builder()
//                .sourceActorRef(sourceActorRef)
//                .targetActorRef(this)
//                .data(message)
//                .build();
//
//        if (remote) {
//            actorSystem.dispatchRemoteMessage(actorMessage);
//        } else {
//            actorSystem.dispatch(actorMessage);
//        }
//    }
//}
