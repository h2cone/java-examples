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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

@Deprecated
public class NioServer {

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[1]);
        SocketAddress socketAddress = new InetSocketAddress(port);

        Selector acceptor = Selector.open();
        Selector selector = Selector.open();

        // accept
        new Thread(() -> {
            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(acceptor, SelectionKey.OP_ACCEPT);
                serverSocketChannel.socket().bind(socketAddress);

                while (true) {
                    if (acceptor.select(10) > 0) {
                        Set<SelectionKey> selectionKeys = acceptor.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeys.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();
                            if (selectionKey.isAcceptable()) {
                                try {
                                    SocketChannel socketChannel = serverSocketChannel.accept();
                                    if (socketChannel != null) {
                                        socketChannel.configureBlocking(false);
                                        socketChannel.register(selector, SelectionKey.OP_READ);
                                    }
                                } finally {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // read/write
        new Thread(() -> {
            try {
                while (true) {
                    if (selector.select(10) > 0) {
                        Set<SelectionKey> selectionKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeys.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();
                            if (selectionKey.isReadable()) {
                                try {
                                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    socketChannel.read(buffer);
                                    if (buffer.position() > 0) {
                                        buffer.flip();
                                        String msg = Charset.defaultCharset().newDecoder().decode(buffer).toString();
                                        System.out.printf("%s receive '%s' from %s\n", Thread.currentThread().getName(), msg, socketChannel.getRemoteAddress());

                                        msg = String.format("i am %s", Thread.currentThread().getName());
                                        buffer = ByteBuffer.wrap(msg.getBytes());
                                        while (buffer.hasRemaining()) {
                                            socketChannel.write(buffer);
                                        }
                                    }
                                } finally {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.printf("server running on %s\n", port);
    }
}
