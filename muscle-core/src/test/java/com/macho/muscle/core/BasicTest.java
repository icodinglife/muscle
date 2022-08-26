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
//public class BasicTest {
//    public static void main(String[] args) throws IOException {
//        ActorSystem actorSystem = new ActorSystem("basic", Runtime.getRuntime().availableProcessors());
//
//        BasicTestActor basicTestActor = new BasicTestActor();
//
//        ActorRef basicRef = actorSystem.newActor("basic", basicTestActor, 1024);
//
//        for (int i = 0; i < 5; i++) {
//            basicRef.send(null, new GreetMessage("hello " + i));
//        }
//        for (int i = 0; i < 5; i++) {
//            basicRef.send(null, new GoodbyeMessage("hello " + i));
//        }
//
//        System.in.read();
//    }
//
//    private static class BasicTestActor extends AbsActor {
//        @Override
//        protected void onRecvMessage(UserActorMessage<?> msg) {
//            Object message = msg.getData();
//            log.info("onRecvMessage: {}", message);
//            if (message instanceof GreetMessage) {
//                log.info("Say Hello to {}", ((GreetMessage) message).getName());
//            }
//            if (message instanceof GoodbyeMessage) {
//                log.info("Say Goodbye to {}", ((GoodbyeMessage) message).getName());
//            }
//        }
//
//        @Override
//        protected void onException(Exception e) {
//            log.error("BasicTestActor Exception", e);
//        }
//
//        @Override
//        protected void onStart() {
//            log.info("BasicTestActor onStart");
//        }
//
//        @Override
//        protected void onSuspend() {
//            log.info("BasicTestActor onSuspend");
//        }
//
//        @Override
//        protected void onStop() {
//            log.info("BasicTestActor onStop");
//        }
//    }
//
//    @Data
//    private static class GreetMessage {
//        private final String name;
//    }
//
//    @Data
//    private static class GoodbyeMessage {
//        private final String name;
//    }
//}
