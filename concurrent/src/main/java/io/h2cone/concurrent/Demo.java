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

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Demo {

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        // ReentrantLock
        final Lock lock = new ReentrantLock();

        lock.lock();
        try {
            // critical section
        } finally {
            lock.unlock();
        }

        // ReadWriteLock
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        Lock readLock = readWriteLock.readLock();

        Lock writeLock = readWriteLock.writeLock();

        // Semaphore
        final Semaphore semaphore = new Semaphore(3);

        semaphore.acquire();
        // do something
        semaphore.release();

        // CountDownLatch
        final CountDownLatch latch = new CountDownLatch(2);

        latch.countDown();

        latch.await(3000, TimeUnit.MILLISECONDS);

        // CyclicBarrier
        final CyclicBarrier barrier = new CyclicBarrier(8);

        barrier.await();
    }
}
