package org.example.singleton;


public class SingletonTest {
    public static void main(String[] args) {
        Runnable task = () -> {
            Singleton instance = Singleton.getInstance();
            System.out.println(Thread.currentThread().getName() + " got instance: " + instance);
        };

        for(int i=0;i<5;i++) {
            Thread thread = new Thread(task , "Thread-" + i);
            thread.start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Singleton instance1 = Singleton.getInstance();
        Singleton instance2 = Singleton.getInstance();
        System.out.println("Instance equality check: " + (instance1 == instance2));
    }
}