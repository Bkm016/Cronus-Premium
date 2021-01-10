package ink.ptms.cronus;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author 坏黑
 * @Since 2018-12-24 16:32
 */
public class CronusMirror {

    private final static Map<String, Data> dataMap = Maps.newConcurrentMap();

    public static Map<String, Data> getMirrors() {
        return dataMap;
    }

    public static Data getMirror(String id) {
        return dataMap.computeIfAbsent(id, new Function<String, Data>() {

            @Override
            public Data apply(String i) {
                return new Data();
            }
        });
    }

    public static Data getMirror() {
        return new Data();
    }

    public static boolean isIgnored(Class<?> clazz) {
        return clazz.isAnnotationPresent(Ignore.class);
    }

    public static class Data {

        private double timeTotal;
        private double timeLatest;
        private long times;
        private long runtime;

        public Data start() {
            runtime = System.nanoTime();
            return this;
        }

        public Data stop() {
            timeLatest = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - runtime);
            timeTotal += timeLatest;
            times += 1;
            return this;
        }

        public Data reset() {
            timeTotal = 0;
            timeLatest = 0;
            times = 0;
            return this;
        }

        public Data check(Runnable runnable) {
            start();
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            stop();
            return this;
        }

        public double getTimeTotal() {
            return timeTotal;
        }

        public double getTimeLatest() {
            return timeLatest;
        }

        public long getTimes() {
            return times;
        }
    }
}
