/*
 * Copyright 2020 huangh https://github.com/h2cone
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reactor Pattern # Single threaded version
 * <p>
 * http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf
 */
public class SingleThreadedReactor {

    static class Reactor implements Runnable {
        final Selector selector;
        final ServerSocketChannel serverSocketChannel;
        final ChannelHandler channelHandler;

        public Reactor(int port, ChannelHandler channelHandler) throws IOException {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new Acceptor());        // (1)

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
                        SelectionKey selectionKey = iterator.next();
                        dispatch(selectionKey);     // (2)
                        iterator.remove();
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
                runnable.run();     // (3)
            }
        }

        class Acceptor implements Runnable {

            @Override
            public void run() {
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        new Handler(selector, socketChannel, channelHandler);      // (4)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        static class Handler implements Runnable {
            final SelectionKey selectionKey;
            final SocketChannel socketChannel;
            final ChannelHandler channelHandler;

            ByteBuffer inputBuf = ByteBuffer.allocate(1024);
            ByteBuffer outputBuf = ByteBuffer.allocate(1024);
            static int READING = 0, WRITING = 1;
            int state = READING;

            public Handler(Selector selector, SocketChannel socketChannel, ChannelHandler channelHandler) throws IOException {
                this.socketChannel = socketChannel;
                this.socketChannel.configureBlocking(false);
                // (5)
                selectionKey = this.socketChannel.register(selector, 0);
                selectionKey.attach(this);
                selectionKey.interestOps(SelectionKey.OP_READ);
                selector.wakeup();

                this.channelHandler = channelHandler;
            }

            @Override
            public void run() {
                try {
                    if (state == READING) {
                        read();
                    } else if (state == WRITING) {
                        write();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void read() throws IOException {
                channelHandler.read(socketChannel, inputBuf);
                if (channelHandler.inputCompleted(inputBuf)) {
                    channelHandler.process(inputBuf, outputBuf);
                    state = WRITING;
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            }

            private void write() throws IOException {
                channelHandler.write(socketChannel, outputBuf);
                if (channelHandler.outputCompleted(outputBuf)) {
                    selectionKey.cancel();      // (6)
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Reactor(port, new DefaultChannelHandler()));

        System.out.printf("server running on %s\n", port);
    }
}
