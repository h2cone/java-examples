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

import java.util.Random;

/**
 * Copy from https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html
 */
public class ProducerConsumerExample {

    static class Drop {
        // Message sent from producer
        // to consumer.
        private String message;
        // True if consumer should wait
        // for producer to send message,
        // false if producer should wait for
        // consumer to retrieve message.
        private boolean empty = true;

        public synchronized String take() {
            // Wait until message is
            // available.
            while (empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            // Toggle status.
            empty = true;
            // Notify producer that
            // status has changed.
            notifyAll();
            return message;
        }

        public synchronized void put(String message) {
            // Wait until message has
            // been retrieved.
            while (!empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            // Toggle status.
            empty = false;
            // Store message.
            this.message = message;
            // Notify consumer that status
            // has changed.
            notifyAll();
        }
    }

    static class Producer implements Runnable {
        private Drop drop;

        public Producer(Drop drop) {
            this.drop = drop;
        }

        public void run() {
            String importantInfo[] = {
                    "Mares eat oats",
                    "Does eat oats",
                    "Little lambs eat ivy",
                    "A kid will eat ivy too"
            };
            Random random = new Random();

            for (int i = 0;
                 i < importantInfo.length;
                 i++) {
                drop.put(importantInfo[i]);
                try {
                    Thread.sleep(random.nextInt(5000));
                } catch (InterruptedException e) {
                }
            }
            drop.put("DONE");
        }
    }

    static class Consumer implements Runnable {
        private Drop drop;

        public Consumer(Drop drop) {
            this.drop = drop;
        }

        public void run() {
            Random random = new Random();
            for (String message = drop.take();
                 !message.equals("DONE");
                 message = drop.take()) {
                System.out.format("MESSAGE RECEIVED: %s%n", message);
                try {
                    Thread.sleep(random.nextInt(5000));
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        Drop drop = new Drop();
        new Thread(new Producer(drop)).start();
        new Thread(new Consumer(drop)).start();
    }
}
