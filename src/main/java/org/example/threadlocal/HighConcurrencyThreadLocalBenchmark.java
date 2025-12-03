package org.example.threadlocal;

import io.netty.util.concurrent.FastThreadLocal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class HighConcurrencyThreadLocalBenchmark {

    // Multiple ThreadLocal instances to simulate real-world usage
    private static final ThreadLocal<String> jdkThreadLocal1 = ThreadLocal.withInitial(() -> "value1");
    private static final ThreadLocal<Integer> jdkThreadLocal2 = ThreadLocal.withInitial(() -> 1);
    private static final ThreadLocal<Map<String, Object>> jdkThreadLocal3 = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<List<String>> jdkThreadLocal4 = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Long> jdkThreadLocal5 = ThreadLocal.withInitial(() -> 100L);

    // Corresponding FastThreadLocal instances
    private static final FastThreadLocal<String> nettyFastThreadLocal1 = new FastThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "value1";
        }
    };

    private static final FastThreadLocal<Integer> nettyFastThreadLocal2 = new FastThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 1;
        }
    };

    private static final FastThreadLocal<Map<String, Object>> nettyFastThreadLocal3 = new FastThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private static final FastThreadLocal<List<String>> nettyFastThreadLocal4 = new FastThreadLocal<List<String>>() {
        @Override
        protected List<String> initialValue() {
            return new ArrayList<>();
        }
    };

    private static final FastThreadLocal<Long> nettyFastThreadLocal5 = new FastThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 100L;
        }
    };

    private static final int THREAD_COUNT = 200;
    private static final int OPERATIONS_PER_THREAD = 5000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("High Concurrency ThreadLocal vs FastThreadLocal Benchmark");
        System.out.println("======================================================");

        // Warm up JVM
        warmUp();

        // Benchmark ThreadLocal
        System.out.println("\n1. Testing JDK ThreadLocal with 5 variables:");
        long jdkTime = benchmarkJDKThreadLocal();
        System.out.println("JDK ThreadLocal completed in: " + jdkTime + " ms");

        // Benchmark FastThreadLocal
        System.out.println("\n2. Testing Netty FastThreadLocal with 5 variables:");
        long nettyTime = benchmarkNettyFastThreadLocal();
        System.out.println("Netty FastThreadLocal completed in: " + nettyTime + " ms");

        // Performance Comparison
        System.out.println("\n3. Performance Comparison:");
        double speedup = (double) jdkTime / nettyTime;
        if (speedup > 1) {
            System.out.printf("Netty FastThreadLocal is %.2fx faster than JDK ThreadLocal%n", speedup);
        } else {
            System.out.printf("Netty FastThreadLocal is %.2fx slower than JDK ThreadLocal%n", 1/speedup);
        }
    }

    private static void warmUp() throws InterruptedException {
        System.out.println("Warming up JVM...");

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        // Access all ThreadLocal variables
                        jdkThreadLocal1.get();
                        jdkThreadLocal1.set("warmup-value-" + j);
                        jdkThreadLocal2.get();
                        jdkThreadLocal2.set(j);
                        jdkThreadLocal3.get();
                        jdkThreadLocal4.get();
                        jdkThreadLocal5.get();
                        jdkThreadLocal5.set((long)j);

                        // Access all FastThreadLocal variables
                        nettyFastThreadLocal1.get();
                        nettyFastThreadLocal1.set("warmup-value-" + j);
                        nettyFastThreadLocal2.get();
                        nettyFastThreadLocal2.set(j);
                        nettyFastThreadLocal3.get();
                        nettyFastThreadLocal4.get();
                        nettyFastThreadLocal5.get();
                        nettyFastThreadLocal5.set((long)j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Warm up completed.\n");
    }

    private static long benchmarkJDKThreadLocal() throws InterruptedException {
        long start = System.currentTimeMillis();
        AtomicLong totalOperations = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    Random random = new Random();
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        // Perform various operations on multiple ThreadLocal variables
                        jdkThreadLocal1.set("value-" + j);
                        String val1 = jdkThreadLocal1.get();

                        jdkThreadLocal2.set(j);
                        Integer val2 = jdkThreadLocal2.get();

                        Map<String, Object> map = jdkThreadLocal3.get();
                        map.put("key" + j, random.nextInt());

                        List<String> list = jdkThreadLocal4.get();
                        list.add("item-" + j);

                        jdkThreadLocal5.set(System.nanoTime());
                        Long val5 = jdkThreadLocal5.get();

                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        System.out.println("Total operations with JDK ThreadLocal: " + totalOperations.get());
        return System.currentTimeMillis() - start;
    }

    private static long benchmarkNettyFastThreadLocal() throws InterruptedException {
        long start = System.currentTimeMillis();
        AtomicLong totalOperations = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    Random random = new Random();
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        // Perform various operations on multiple FastThreadLocal variables
                        nettyFastThreadLocal1.set("value-" + j);
                        String val1 = nettyFastThreadLocal1.get();

                        nettyFastThreadLocal2.set(j);
                        Integer val2 = nettyFastThreadLocal2.get();

                        Map<String, Object> map = nettyFastThreadLocal3.get();
                        map.put("key" + j, random.nextInt());

                        List<String> list = nettyFastThreadLocal4.get();
                        list.add("item-" + j);

                        nettyFastThreadLocal5.set(System.nanoTime());
                        Long val5 = nettyFastThreadLocal5.get();

                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        System.out.println("Total operations with Netty FastThreadLocal: " + totalOperations.get());
        return System.currentTimeMillis() - start;
    }
}
