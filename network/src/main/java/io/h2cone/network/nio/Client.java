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

import io.h2cone.network.bio.BioClient;
import io.h2cone.network.staff.DefaultChannelHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {

    public static void main(String[] args) {
        String host = args.length == 0 ? "127.0.0.1" : args[0];
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[1]);
        SocketAddress socketAddress = new InetSocketAddress(host, port);

        Runnable runnable = () -> {
            try {
                SocketChannel socketChannel = SocketChannel.open(socketAddress);
                socketChannel.configureBlocking(true);
                // write
                String msg = String.format(DefaultChannelHandler.SEND, Thread.currentThread().getName());
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                socketChannel.write(buffer);
                // read
                buffer = ByteBuffer.allocate(1024);
                socketChannel.read(buffer);
                if (buffer.position() > 0) {
                    buffer.flip();
                    msg = Charset.defaultCharset().newDecoder().decode(buffer).toString();
                    System.out.printf(DefaultChannelHandler.RECEIVE + "\n", Thread.currentThread().getName(), msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        for (int i = 0; i < BioClient.NUMBER_OF_CLIENTS; i++) {
            new Thread(runnable).start();
        }
    }
}
