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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo {

    static class Message {
    }

    static class Producer implements Runnable {
        private BlockingQueue<Message> queue;

        public Producer(BlockingQueue<Message> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (true) {
                try {
                    Message message = new Message();
                    queue.put(message);
                    System.out.printf("producer: message: %s, queueSize: %s\n", message.hashCode(), queue.size());
                    Thread.sleep(random.nextInt(5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private BlockingQueue<Message> queue;

        public Consumer(BlockingQueue<Message> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            Random random = new Random();
            for (; ; ) {
                try {
                    Message message = queue.take();
                    System.out.printf("consumer: message: %s, queueSize: %s\n", message.hashCode(), queue.size());
                    Thread.sleep(random.nextInt(5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int capacity = 3;
        BlockingQueue<Message> queue = new ArrayBlockingQueue<>(capacity);

        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}
