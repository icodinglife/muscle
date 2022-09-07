package com.macho.muscle.core.bench.wordcount;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.actor.MuscleSystem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openjdk.jmh.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WordCount {
    private static final Logger logger = LoggerFactory.getLogger(WordCount.class);

    private static final String FileName = "/Users/hunter/develop/workspace/mine/muscle/muscle-core/src/test/resources/test.txt";

    public static void main(String[] args) throws IOException {
        readTest();
//        workerTest();
    }

    private static void readTest() throws IOException {
        StopWatch stopwatch = StopWatch.createStarted();

        Collection<String> strings = FileUtils.readAllLines(new File(FileName));

        Map<String, Integer> resultMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(strings)) {
            for (String words : strings) {
                if (StringUtils.isNotBlank(words)) {
                    String[] wordArray = words.split("[^a-zA-z]+");
                    if (wordArray != null && wordArray.length > 0) {
                        for (String word : wordArray) {
                            if (StringUtils.isNotBlank(word)) {
                                String trimWord = word.trim();
                                Integer count = resultMap.get(trimWord);
                                if (count == null) {
                                    count = 0;
                                }
                                count += 1;
                                resultMap.put(trimWord, count);
                            }
                        }
                    }
                }
            }
        }
        stopwatch.stop();
        logger.info("time:{}", stopwatch.getNanoTime() / 1_000_000);

        MapUtils.debugPrint(System.out, "word-count", resultMap);
    }

    private static void workerTest() {
        final String WORKER = "worker";

        StopWatch stopwatch = StopWatch.createStarted();
        MuscleSystem muscleSystem = new MuscleSystem();

        List<ICountWorker> workerList = Lists.newArrayList();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            CountWorker worker = new CountWorker();
            muscleSystem.registerActor(WORKER + i, WORKER, worker, 1024);
            workerList.add(muscleSystem.newInvokeProxy(WORKER + i, WORKER, ICountWorker.class));
        }

        Master master = new Master(workerList);
        muscleSystem.registerActor("master", "master", master, 10);
        IMaster iMaster = muscleSystem.newInvokeProxy("master", "master", IMaster.class);
        Map<String, Integer> stringIntegerMap = iMaster.countWordsOfFile(FileName);
        stopwatch.stop();
        logger.info("time:{}", stopwatch.getNanoTime() / 1_000_000);

        MapUtils.debugPrint(System.out, "word-count", stringIntegerMap);
    }

    public interface IMaster extends ActorLifecycle {
        Map<String, Integer> countWordsOfFile(String fileName);
    }

    public interface ICountWorker extends ActorLifecycle {
        void prepare();

        void count(String words);

        Map<String, Integer> result();
    }

    public static final class Master implements IMaster {
        private final List<ICountWorker> workers;

        public Master(List<ICountWorker> workers) {
            this.workers = workers;
        }

        @Override
        public Map<String, Integer> countWordsOfFile(String fileName) {
            try {
                Collection<String> strings = FileUtils.readAllLines(new File(fileName));
                final int workerCount = workers.size();

                for (ICountWorker worker : workers) {
                    worker.prepare();
                }

                int index = 0;
                for (String str : strings) {
                    workers.get((index++ % workerCount)).count(str);
                }

                List<Map<String, Integer>> resultMaps = Lists.newArrayList();
                for (ICountWorker worker : workers) {
                    resultMaps.add(worker.result());
                }

                Map<String, Integer> mergedMap = Maps.newHashMap();
                for (Map<String, Integer> map : resultMaps) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        Integer count = mergedMap.get(entry.getKey());
                        if (count == null) {
                            count = 0;
                        }

                        count = count + entry.getValue();

                        mergedMap.put(entry.getKey(), count);
                    }
                }

                return mergedMap;
            } catch (Exception e) {
                logger.error("读取文件内容出错！", e);
            }

            return null;
        }

        @Override
        public void onException(Throwable e) {
            logger.error("master error", e);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {

        }
    }

    public static final class CountWorker implements ICountWorker {
        private final Map<String, Integer> resultMap = Maps.newHashMap();

        @Override
        public void onException(Throwable e) {
            logger.error("worker error", e);
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void prepare() {
            resultMap.clear();
        }

        @Override
        public void count(String words) {
            if (StringUtils.isNotBlank(words)) {
                String[] wordArray = words.split("[^a-zA-z]+");
                if (wordArray != null && wordArray.length > 0) {
                    for (String word : wordArray) {
                        if (StringUtils.isNotBlank(word)) {
                            String trimWord = word.trim();
                            Integer count = resultMap.get(trimWord);
                            if (count == null) {
                                count = 0;
                            }
                            count += 1;
                            resultMap.put(trimWord, count);
                        }
                    }
                }
            }
        }

        @Override
        public Map<String, Integer> result() {
            return resultMap;
        }
    }

//    public static void main(String[] args) {
//        String abc = "He, in good time, must his lieutenant be,And I--God bless the mark!--his Moorship's ancient.";
//        String[] split = abc.split("[^a-zA-z]+");
//        for (String str : split) {
//            System.out.println(str);
//        }
//    }
}
