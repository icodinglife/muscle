package com.macho.muscle.core.actor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class InvokeTest {
    @Test
    public void testInvoke() {
        String actorId = "abc";
        String service = "test";

        MuscleSystem system = new MuscleSystem();
        system.registerActor(actorId, service, new TestInvoker(), 1024);

        ITestInvoker ivk = system.newInvokeProxy(actorId, service, ITestInvoker.class);
        String v = ivk.hello("hello");
        log.info("hello, {}", v);
    }

    public interface ITestInvoker extends ActorLifecycle {
        String hello(String v);
    }

    public static class TestInvoker implements ITestInvoker {

        @Override
        public void onException(Throwable e) {
            log.error("TestInvoker Error!", e);
        }

        @Override
        public void onStart() {
            log.info("TestInvoker start");
        }

        @Override
        public void onStop() {
            log.info("TestInvoker stop");
        }

        @Override
        public String hello(String v) {
            log.info("recv: {}", v);
            return "World!";
        }
    }
}
