package org.example.threadlocal;

import io.netty.util.concurrent.FastThreadLocal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadLocalBenchmark {
    // JDK ThreadLocal implementation
    private static final ThreadLocal<String> jdkThreadLocal = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "JDK-ThreadLocal-Value";
        }
    };

    // Netty FastThreadLocal implementation
    private static final FastThreadLocal<String> nettyFastThreadLocal = new FastThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "Netty-FastThreadLocal-Value";
        }
    };

    private static final int THREAD_COUNT = 100;
    private static final int OPERATIONS_PER_THREAD = 10000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("ThreadLocal vs FastThreadLocal Performance Comparison");
        System.out.println("====================================================");

        // Warm up JVM
        warmUp();

        // Benchmark ThreadLocal
        System.out.println("\n1. Testing JDK ThreadLocal:");
        long jdkTime = benchmarkJDKThreadLocal();
        System.out.println("JDK ThreadLocal completed in: " + jdkTime + " ms");

        // Benchmark FastThreadLocal
        System.out.println("\n2. Testing Netty FastThreadLocal:");
        long nettyTime = benchmarkNettyFastThreadLocal();
        System.out.println("Netty FastThreadLocal completed in: " + nettyTime + " ms");

        // Performance Comparison
        System.out.println("\n3. Performance Comparison:");
        double speedup = (double) jdkTime / nettyTime;
        System.out.printf("Netty FastThreadLocal is %.2fx %s than JDK ThreadLocal%n",
                speedup, speedup > 1 ? "faster" : "slower");
    }

    private static void warmUp() throws InterruptedException {
        System.out.println("Warming up JVM...");

        // Warm up with both implementations
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        jdkThreadLocal.get();
                        jdkThreadLocal.set("warmup-value-" + j);
                        nettyFastThreadLocal.get();
                        nettyFastThreadLocal.set("warmup-value-" + j);
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
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        // Get value
                        String value = jdkThreadLocal.get();

                        // Set new value
                        jdkThreadLocal.set("value-" + j);

                        // Remove to prevent memory leaks (simulate real usage)
                        if (j % 100 == 0) {
                            jdkThreadLocal.remove();
                        }

                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

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
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        // Get value
                        String value = nettyFastThreadLocal.get();

                        // Set new value
                        nettyFastThreadLocal.set("value-" + j);

                        // Remove to prevent memory leaks (simulate real usage)
                        if (j % 100 == 0) {
                            nettyFastThreadLocal.remove();
                        }

                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Total operations with Netty FastThreadLocal: " + totalOperations.get());
        return System.currentTimeMillis() - start;
    }
}
