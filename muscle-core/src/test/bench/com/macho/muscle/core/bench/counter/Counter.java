package com.macho.muscle.core.bench.counter;

import com.macho.muscle.core.actor.MuscleSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Counter implements ICounter {
    private static final Logger log = LoggerFactory.getLogger(Counter.class);

    private final MuscleSystem muscleSystem;
    private final int TOTAL;
    private final int value;

    public Counter(MuscleSystem muscleSystem, int value, int total) {
        this.muscleSystem = muscleSystem;
        this.value = value;
        this.TOTAL = total;
    }

    @Override
    public void onException(Throwable e) {
        log.error("counter exception", e);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void shutdown() {
        if (value >= TOTAL - 1) {
            muscleSystem.stopActor("act" + value);
            return;
        }

        int c = value + 1;
        String nextActId = "act" + c;
        String svc = ICounter.class.getName();

        ICounter iCounter = muscleSystem.newInvokeProxy(nextActId, svc, ICounter.class);

        iCounter.shutdown();

        muscleSystem.stopActor("act" + value);
    }

    @Override
    public CompletableFuture<String> count(int i) {
        if (i >= TOTAL - 1) {
//            log.info("count:{}", i);

            return CompletableFuture.completedFuture("End");
        }

        int c = i + 1;
        String nextActId = "act" + c;
        String svc = ICounter.class.getName();

        ICounter nextAct = new Counter(muscleSystem, c, TOTAL);

        muscleSystem.registerActor(nextActId, svc, nextAct, 10);

        ICounter iCounter = muscleSystem.newInvokeProxy(nextActId, svc, ICounter.class);

        try {
            String res = iCounter.count(c).get();

//            if (i % 100_000 == 0) {
//                log.info("count:{}", i);
//            }

            return CompletableFuture.completedFuture(res);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
