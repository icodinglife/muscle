package com.macho.muscle.core;

import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.actor.ActorRunner;
import com.macho.muscle.core.actor.ActorSystem;
import com.macho.muscle.core.actor.invoke.ReflectInvokeActorSystem;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReflectInvokeTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        ReflectInvokeActorSystem actorSystem = new ReflectInvokeActorSystem("ReflectInvoke", Runtime.getRuntime().availableProcessors());

        String actorId = "test";
        String service = "TestService";

        actorSystem.registerReflectInvokeActor(actorId, service, new TestImpl(), 1024);

        TestInterface test = actorSystem.newReflectInvokeProxy(actorId, service, TestInterface.class);

//        test.test().whenComplete((msg, err) -> {
//            log.info("NoSender " + msg);
//        });

        A a = new A(test);

        actorSystem.registerReflectInvokeActor("a", "A", a, 1024);

        TimeUnit.SECONDS.sleep(5);

        test.finish().whenComplete((msg, err) -> {
            log.info("finish: " + msg);
        });

        TimeUnit.MICROSECONDS.sleep(10);

        try {
            test.test().whenComplete((msg, err) -> {
                log.info("NoSender2 " + msg);
            });
        } catch (Exception e) {
            log.error("2 exception", e);
        }

        System.in.read();
    }

    public static class A implements ActorLifecycle {
        private TestInterface test;

        public A(TestInterface test) {
            this.test = test;
        }

        @Override
        public void onException(Exception e) {
            log.error("a exception", e);
        }

        @Override
        public void onStart() {
            CompletableFuture<String> test1 = test.test();
            test1.whenComplete((msg, err) -> {
                if (err != null) {
                    log.error("a exception", err);
                }
                log.info("a " + msg);
            });
        }

        @Override
        public void onStop() {
            log.info("a stop");
        }
    }

    public interface TestInterface extends ActorLifecycle {
        CompletableFuture<String> test();

        CompletableFuture<String> finish();
    }

    public static class TestImpl implements TestInterface {

        @Override
        public CompletableFuture<String> test() {
            log.info("hello test");
//            return CompletableFuture.completedFuture("test");
            return doInOtherThread();
        }

        @Override
        public CompletableFuture<String> finish() {
            ActorRunner.currentActor().stop();

            return CompletableFuture.completedFuture("Success");
        }

        public CompletableFuture<String> doInOtherThread() {
            CompletableFuture<String> future = new CompletableFuture<>();

            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                future.complete("test");
            }).start();

            return future;
        }

        @Override
        public void onException(Exception e) {
            log.error("exception!", e);
        }

        @Override
        public void onStart() {
            log.info("onstart...");
        }

        @Override
        public void onStop() {
            log.info("onstop...");
        }
    }
}
