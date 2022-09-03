package com.macho.muscle.core.actor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.macho.muscle.core.actor.MuscleSystem.PUBLISH;

@Slf4j
public class ClusterInvokeTest {
    private static final String ETCD_ADDR = "ip:///127.0.0.1:2379";

    @Test
    public void startServer1() throws IOException {
        MuscleSystem muscleSystem = new MuscleSystem();
        muscleSystem.startCluster(ETCD_ADDR, 16666);

        InvokeTest.TestInvoker testInvoker = new InvokeTest.TestInvoker();
        muscleSystem.registerActor("act1", "svc1", testInvoker, 1024, PUBLISH);

        System.in.read();
    }

    @Test
    public void startServer2() throws InterruptedException {
        MuscleSystem muscleSystem = new MuscleSystem();
        muscleSystem.startCluster(ETCD_ADDR, 16667);

        TimeUnit.SECONDS.sleep(3);

        InvokeTest.ITestInvoker iTestInvoker = muscleSystem.newInvokeProxy("act1", "svc1", InvokeTest.ITestInvoker.class, true);
        String hello = iTestInvoker.hello("hello");
        log.info("hello, {}", hello);
        String hello2 = iTestInvoker.hello("hello");
        log.info("hello, {}", hello2);
    }

    @Test
    public void startTest3() throws InterruptedException {
        MuscleSystem muscleSystem = new MuscleSystem();
        muscleSystem.startCluster(ETCD_ADDR, 16668);

        TimeUnit.SECONDS.sleep(3);

        List<String> svc1 = muscleSystem.getClusterSystem().getActorIdsWithService("svc1");
        if (CollectionUtils.isEmpty(svc1)) {
            throw new RuntimeException("actor list is empty");
        }

        InvokeTest.ITestInvoker iTestInvoker = muscleSystem.newInvokeProxy(svc1.get(0), "svc1", InvokeTest.ITestInvoker.class, true);
        String hello = iTestInvoker.hello("hello");
        log.info("hello, {}", hello);
        String hello2 = iTestInvoker.hello("hello");
        log.info("hello, {}", hello2);
    }

}
