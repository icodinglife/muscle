//package com.macho.muscle.core;
//
//import com.macho.muscle.core.actor.AbsActor;
//import com.macho.muscle.core.actor.ActorRef;
//import com.macho.muscle.core.actor.ActorSystem;
//import com.macho.muscle.core.actor.UserActorMessage;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//
//@Slf4j
//public class PingPongTest {
//
//    public static void main(String[] args) throws IOException {
//        ActorSystem actorSystem = new ActorSystem("ping-pong", Runtime.getRuntime().availableProcessors());
//        ActorRef pong = actorSystem.newActor("pong", new PongActor(), 1024);
//        ActorRef ping = actorSystem.newActor("ping", new PingActor(pong), 1024);
//
//        System.in.read();
//    }
//
//    private static class PongActor extends AbsActor {
//
//        @Override
//        protected void onRecvMessage(UserActorMessage<?> message) {
//            Object data = message.getData();
//            if (data instanceof Ping) {
//                log.info("{}", ((Ping) data).getValue());
//                message.getSourceActorRef().send(getSelfRef(), new Pong("pong"));
//            }
//        }
//
//        @Override
//        protected void onException(Exception e) {
//            log.error("pong exception", e);
//        }
//
//        @Override
//        protected void onStart() {
//
//        }
//
//        @Override
//        protected void onSuspend() {
//
//        }
//
//        @Override
//        protected void onStop() {
//
//        }
//    }
//
//    private static class PingActor extends AbsActor {
//        private ActorRef pongRef;
//
//        public PingActor(ActorRef pongRef) {
//            this.pongRef = pongRef;
//        }
//
//        @Override
//        protected void onRecvMessage(UserActorMessage<?> message) {
//            Object data = message.getData();
//            if (data instanceof Pong) {
//                log.info("{}", ((Pong) data).getValue());
//                message.getSourceActorRef().send(getSelfRef(), new Ping("ping"));
//            }
//        }
//
//        @Override
//        protected void onException(Exception e) {
//            log.error("ping exception", e);
//        }
//
//        @Override
//        protected void onStart() {
//            log.info("start...");
//            pongRef.send(getSelfRef(), new Ping("ping"));
//        }
//
//        @Override
//        protected void onSuspend() {
//
//        }
//
//        @Override
//        protected void onStop() {
//
//        }
//    }
//
//    @Data
//    private static class Ping {
//        private final String value;
//    }
//
//    @Data
//    private static class Pong {
//        private final String value;
//    }
//}
