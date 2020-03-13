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

package io.h2cone.network.nio;

import io.h2cone.network.staff.ChannelHandler;
import io.h2cone.network.staff.DefaultChannelHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Reactor Pattern # Multithreaded Designs（incomplete version）
 * <p>
 * http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf
 */
public class MultithreadedReactor {

    static class Reactor implements Runnable {
        final Selector selector;
        final ServerSocketChannel serverSocketChannel;
        final Executor executor;
        final ChannelHandler channelHandler;

        public Reactor(int port, Executor executor, ChannelHandler channelHandler) throws IOException {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new Acceptor());

            this.executor = executor;
            this.channelHandler = channelHandler;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        try {
                            SelectionKey selectionKey = iterator.next();
                            dispatch(selectionKey);
                        } finally {
                            iterator.remove();
                        }
                    }
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void dispatch(SelectionKey selectionKey) {
            Runnable runnable = (Runnable) selectionKey.attachment();
            if (runnable != null) {
                runnable.run();
            }
        }

        class Acceptor implements Runnable {

            @Override
            public void run() {
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        new Handler(selector, socketChannel, executor, channelHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        static class Handler implements Runnable {
            final Selector selector;
            final SelectionKey selectionKey;
            final SocketChannel socketChannel;
            final ChannelHandler channelHandler;

            ByteBuffer inputBuf = ByteBuffer.allocate(1024);
            ByteBuffer outputBuf = ByteBuffer.allocate(1024);
            static int READING = 0, PROCESSING = 1, WRITING = 2;
            int state = READING;

            final Executor executor;

            public Handler(Selector selector, SocketChannel socketChannel, Executor executor, ChannelHandler channelHandler) throws IOException {
                this.selector = selector;
                this.socketChannel = socketChannel;
                this.socketChannel.configureBlocking(false);

                selectionKey = this.socketChannel.register(selector, 0);
                selectionKey.attach(this);
                selectionKey.interestOps(SelectionKey.OP_READ);
                selector.wakeup();

                this.executor = executor;
                this.channelHandler = channelHandler;
            }

            @Override
            public void run() {
                try {
                    if (state == READING) {
                        read();
                    } else if (state == PROCESSING) {
                        processAndHandOff();
                    } else if (state == WRITING) {
                        write();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private synchronized void read() throws IOException {
                channelHandler.read(socketChannel, inputBuf);
                if (channelHandler.inputCompleted(inputBuf)) {
                    state = PROCESSING;
                    executor.execute(this::processAndHandOff);
                }
            }

            private synchronized void processAndHandOff() {
                channelHandler.process(inputBuf, outputBuf);
                state = WRITING;
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }

            private void write() throws IOException {
                channelHandler.write(socketChannel, outputBuf);
                if (channelHandler.outputCompleted(outputBuf)) {
                    selectionKey.cancel();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Reactor(port, Executors.newCachedThreadPool(), new DefaultChannelHandler()));

        System.out.printf("server running on %s\n", port);
    }
}
