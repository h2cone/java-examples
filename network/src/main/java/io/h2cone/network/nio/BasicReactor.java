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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Reactor Pattern # Single threaded version
 * <p>
 * http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf
 */
public class BasicReactor {

    static class Reactor implements Runnable {
        final Selector selector;
        final ServerSocketChannel serverSocketChannel;
        final Processable processable;

        public Reactor(int port, Processable processable) throws IOException {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new Acceptor());        // (1)

            this.processable = processable;
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
                            dispatch(selectionKey);     // (2)
                        } finally {
                            iterator.remove();
                        }
                    }
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
                        new Handler(selector, socketChannel, processable);      // (4)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        static class Handler implements Runnable {
            final SelectionKey selectionKey;
            final SocketChannel socketChannel;
            final Processable processable;

            ByteBuffer inputBuf = ByteBuffer.allocate(1024);
            ByteBuffer outputBuf = ByteBuffer.allocate(1024);
            static int READING = 0, WRITING = 1;
            int state = READING;

            public Handler(Selector selector, SocketChannel socketChannel, Processable processable) throws IOException {
                this.socketChannel = socketChannel;
                this.socketChannel.configureBlocking(false);
                // (5)
                selectionKey = this.socketChannel.register(selector, 0);
                selectionKey.attach(this);
                selectionKey.interestOps(SelectionKey.OP_READ);
                selector.wakeup();

                this.processable = processable;
            }

            @Override
            public void run() {
                if (state == READING) {
                    read();
                } else if (state == WRITING) {
                    write();
                }
            }

            private void read() {
                processable.read(socketChannel, inputBuf);
                if (processable.inputCompleted(inputBuf)) {
                    state = WRITING;
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            }

            private void write() {
                processable.write(socketChannel, outputBuf);
                if (processable.outputCompleted(outputBuf)) {
                    selectionKey.cancel();      // (6)
                }
            }
        }

        interface Processable {
            boolean inputCompleted(ByteBuffer inputBuf);

            void read(SocketChannel socketChannel, ByteBuffer inputBuf);

            void write(SocketChannel socketChannel, ByteBuffer outputBuf);

            boolean outputCompleted(ByteBuffer outputBuf);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);

        new Thread(new Reactor(port, new Reactor.Processable() {

            @Override
            public boolean inputCompleted(ByteBuffer inputBuf) {
                return inputBuf.position() > 1;
            }

            @Override
            public void read(SocketChannel socketChannel, ByteBuffer inputBuf) {
                try {
                    socketChannel.read(inputBuf);
                    inputBuf.flip();
                    String msg = Charset.defaultCharset().newDecoder().decode(inputBuf).toString();
                    System.out.printf("%s receive '%s' from %s\n", Thread.currentThread().getName(), msg, socketChannel.getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void write(SocketChannel socketChannel, ByteBuffer outputBuf) {
                String msg = String.format("i am %s", Thread.currentThread().getName());
                try {
                    outputBuf.put(ByteBuffer.wrap(msg.getBytes()));
                    outputBuf.flip();
                    socketChannel.write(outputBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean outputCompleted(ByteBuffer outputBuf) {
                return !outputBuf.hasRemaining();
            }
        })).start();

        System.out.printf("server running on %s\n", port);
    }
}
