package com.macho.muscle.core.bench.counter;

import com.macho.muscle.core.actor.MuscleSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static final int TOTAL = 10;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MuscleSystem muscleSystem = new MuscleSystem();

//        int i =0;
        for (int i = 0; i < 100000; i += TOTAL) {
            ICounter iCounterAct = new Counter(muscleSystem, i, i + TOTAL);
            muscleSystem.registerActor("act" + i, ICounter.class.getName(), iCounterAct, 10);

            ICounter iCounter1 = muscleSystem.newInvokeProxy("act" + i, ICounter.class.getName(), ICounter.class);

            CompletableFuture<String> count = iCounter1.count(i);

//            logger.info("count {}", count.get());

            iCounter1.shutdown();
        }

        TimeUnit.SECONDS.sleep(50);

        muscleSystem.shutdown();
    }
}
