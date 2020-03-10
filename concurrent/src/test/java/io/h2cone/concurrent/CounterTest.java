/*
 * Copyright 2020 hehuang https://github.com/h2cone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.h2cone.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CounterTest {
    private static final long wait = 3000;

    private final long threads = 2;
    private final long times = 1000000;
    private final long excepted = threads * times;

    @Test
    public void testIncrement() throws InterruptedException {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.increment();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertNotEquals(excepted, counter.value());
    }

    private void startThreads(Counter counter, Runnable runnable) throws InterruptedException {
        for (int i = 0; i < threads; i++) {
            new Thread(runnable).start();
        }
        Thread.sleep(CounterTest.wait);
        System.out.printf("threadName: %s, exceptedCounterValue: %s, actualCounterValue: %s\n", Thread.currentThread().getName(), excepted, counter.value());
    }

    @Test
    public void testIncrementSyncMethod() throws InterruptedException {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.incrementSyncMethod();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }

    @Test
    public void testIncrementSyncStmt() throws InterruptedException {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.incrementSyncStmt();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }

    @Test
    public void testIncrementSyncBlock() throws InterruptedException {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                synchronized (counter) {
                    counter.increment();
                }
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }

    @Test
    public void testIncrementReentrantLock() throws InterruptedException {
        Counter counter = new Counter();
        Lock lock = new ReentrantLock();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                lock.lock();
                try {
                    counter.increment();
                } finally {
                    lock.unlock();
                }
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }
}
