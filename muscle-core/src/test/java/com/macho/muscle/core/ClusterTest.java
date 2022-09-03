//package com.macho.muscle.core;
//
//import com.macho.muscle.core.actor.ActorInfo;
//import com.macho.muscle.core.actor.ActorLifecycle;
//import com.macho.muscle.core.actor.ActorRef;
//import com.macho.muscle.core.actor.ActorRunner;
//import com.macho.muscle.core.actor.invoke.ReflectInvokeActorRunner;
//import com.macho.muscle.core.actor.invoke.ReflectInvokeActorSystem;
//import com.macho.muscle.core.cluster.ClusterSystem;
//import lombok.extern.slf4j.Slf4j;
//import net.bytebuddy.implementation.bytecode.Throw;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//public class ClusterTest {
//    @Test
//    public void testClusterStart() throws IOException {
//        ClusterSystem clusterSystem = new ClusterSystem("ip:///127.0.0.1:2379", 16666, null);
//
//        clusterSystem.start();
//
//        clusterSystem.registerActor(ActorInfo.builder()
//                .id("testactor1")
//                .service("testservice")
//                .nodeInfo(clusterSystem.getNodeInfo())
//                .build());
//
//        System.in.read();
//    }
//
//    @Test
//    public void testActorSystemCluster1() throws IOException {
//        ReflectInvokeActorSystem actorSystem = new ReflectInvokeActorSystem("test1", Runtime.getRuntime().availableProcessors());
//        actorSystem.startCluster("ip:///127.0.0.1:2379", 16666);
//        actorSystem.init();
//
//        TestActor testActor = new TestActor("actor-abcd");
//        actorSystem.registerReflectInvokeActor("abcd", "TestActor", testActor, 1024);
//
//        ITestActor ta = actorSystem.newReflectInvokeProxy("abcd", "TestActor", ITestActor.class);
//
//        ta.testFunc("1");
//
//        System.in.read();
//    }
//
//    @Test
//    public void testActorSystemCluster2() throws IOException, InterruptedException {
//        ReflectInvokeActorSystem actorSystem = new ReflectInvokeActorSystem("test2", Runtime.getRuntime().availableProcessors());
//        actorSystem.startCluster("ip:///127.0.0.1:2379", 16667);
//        actorSystem.init();
//
//        TimeUnit.SECONDS.sleep(3);
//
//        TestActor testActor = new TestActor("actor-efgh");
//        actorSystem.registerReflectInvokeActor("efgh", "TestActor", testActor, 1024);
//
//        List<String> ids = actorSystem.getClusterSystem().getActorIdsWithService("TestActor");
//
//        for (String id : ids) {
//            log.info("actor: {}", id);
//        }
//
//        System.in.read();
//    }
//
//    public static class TestActor implements ITestActor, ActorLifecycle {
//        private String value;
//
//        public TestActor(String value) {
//            this.value = value;
//        }
//
//        @Override
//        public void onException(Throwable e) {
//            log.error("exception!", e);
//        }
//
//        @Override
//        public void onStart() {
//            log.info("TestActor started...");
//            ActorRef actorRef = ActorRunner.currentActor().selfActorRef();
////            actorRef.getActorSystem().publishActor(actorRef.getActorInfo());
//
//            if ("actor-efgh".equals(value)) {
//                ITestActor ta = ((ReflectInvokeActorSystem) ActorRunner.currentActor().selfActorRef().getActorSystem()).newRemoteReflectInvokeProxy("abcd", "TestActor", ITestActor.class);
//
//                ta.testFunc("2").whenComplete((res, err) -> {
//                    if (err != null) {
//                        log.error("err", err);
//                        return;
//                    }
//
//                    log.info("test2 get res:{}", res);
//                });
//            }
//        }
//
//        @Override
//        public void onStop() {
//
//        }
//
//        @Override
//        public CompletableFuture<String> testFunc(String args) {
//            log.info("{} testFunc invoked:{}!", value, args);
//            return CompletableFuture.completedFuture(value);
//        }
//    }
//
//    interface ITestActor {
//        CompletableFuture<String> testFunc(String args);
//    }
//}
