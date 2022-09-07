package com.macho.muscle.core.bench.counter;

import com.macho.muscle.core.actor.MuscleSystem;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JmhCounter {
    private static final Logger logger = LoggerFactory.getLogger(JmhCounter.class);

    public final int TOTAL = 1_000_000;

    @Threads(1)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 15, time = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void run() throws ExecutionException, InterruptedException {
//        logger.info("start...");
        MuscleSystem muscleSystem = new MuscleSystem();

        ICounter iCounterAct = new Counter(muscleSystem, 0, TOTAL);

        muscleSystem.registerActor("act", ICounter.class.getName(), iCounterAct, 10);

        ICounter iCounter1 = muscleSystem.newInvokeProxy("act", ICounter.class.getName(), ICounter.class);

        CompletableFuture<String> count = iCounter1.count(0);

        count.get();

        muscleSystem.shutdown();
//        logger.info("count {}", count.get());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhCounter.class.getSimpleName())
                .result("result.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
