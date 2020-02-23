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

package io.h2cone.tcn;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CounterTest {
    private static final long timeout = 10000;

    private final long threads = 2;
    private final long times = 1000;
    private final long excepted = threads * times;

    @Test(timeout = CounterTest.timeout)
    public void testIncrement() {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.increment();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertNotEquals(excepted, counter.value());
    }

    private void startThreads(Counter counter, Runnable runnable) {
        for (int i = 0; i < threads; i++) {
            new Thread(runnable).start();
        }
        while (counter.value() != excepted) {
        }
        System.out.printf("threadName: %s, exceptedCounterValue: %s, actualCounterValue: %s\n", Thread.currentThread().getName(), excepted, counter.value());
    }


    @Test(timeout = CounterTest.timeout)
    public void testIncrementUseSyncMethod() {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.incrementUseSyncMethod();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }

    @Test(timeout = CounterTest.timeout)
    public void testIncrementUseSyncStmt() {
        Counter counter = new Counter();

        startThreads(counter, () -> {
            for (int j = 0; j < times; j++) {
                counter.incrementUseSyncStmt();
            }
            System.out.printf("threadName: %s, counterValue: %s\n", Thread.currentThread().getName(), counter.value());
        });
        Assert.assertEquals(excepted, counter.value());
    }

    @Test(timeout = CounterTest.timeout)
    public void testIncrementUseSyncBlock() {
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

    @Test(timeout = CounterTest.timeout)
    public void testIncrementUseReentrantLock() {
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
