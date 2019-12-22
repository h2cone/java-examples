package io.h2cone.attach.app;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;

public class DogMain {

    public static void main(String[] args) throws InterruptedException {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.printf("managed bean name: %s\n", name);
        while (true) {
            Thread.sleep(10000);
            CompletableFuture.runAsync(() -> System.out.println("Woof Woof"));
        }
    }
}
