package org.example.virtualthreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VirtualThreadsDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("JDK 21 Virtual Threads Demo");
        System.out.println("============================");

        // Demo 1: Traditional Platform Threads
        System.out.println("\n1. Testing with Platform Threads:");
        long platformTime = testPlatformThreads();
        System.out.println("Platform threads completed in: " + platformTime + " ms");

        // Demo 2: Virtual Threads
        System.out.println("\n2. Testing with Virtual Threads:");
        long virtualTime = testVirtualThreads();
        System.out.println("Virtual threads completed in: " + virtualTime + " ms");

        // Performance Comparison
        System.out.println("\n3. Performance Comparison:");
        System.out.printf("Virtual threads are %.2fx faster%n", (double) platformTime / virtualTime);
    }

    /**
     * Test using traditional platform threads
     */
    private static long testPlatformThreads() throws InterruptedException {
        long start = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < 1000; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    // Simulate I/O operation (like database call or HTTP request)
                    try {
                        Thread.sleep(100); // Blocking operation
                        System.out.printf("Platform Task %d completed by thread: %s%n",
                                taskId, Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Test using virtual threads
     */
    private static long testVirtualThreads() throws InterruptedException {
        long start = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1000; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    // Same simulated I/O operation
                    try {
                        Thread.sleep(100); // Blocking operation
                        System.out.printf("Virtual Task %d completed by thread: %s%n",
                                taskId, Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        return System.currentTimeMillis() - start;
    }
}
