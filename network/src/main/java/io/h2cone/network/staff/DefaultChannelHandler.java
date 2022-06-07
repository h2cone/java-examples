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

package io.h2cone.network.staff;

import io.h2cone.network.bio.BioServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

@Deprecated
public class DefaultChannelHandler implements ChannelHandler {
    public static final String SEND = "i am %s";
    public static final String RECEIVE = "%s receive '%s'";

    @Override
    public void read(SocketChannel socketChannel, ByteBuffer inputBuf) throws IOException {
        socketChannel.read(inputBuf);
    }

    @Override
    public boolean inputCompleted(ByteBuffer inputBuf) {
        return inputBuf.position() > 2;
    }

    @Override
    public void process(ByteBuffer inputBuf, ByteBuffer outputBuf) {
        try {
            inputBuf.flip();
            String msg = Charset.defaultCharset().newDecoder().decode(inputBuf).toString();
            System.out.printf(RECEIVE + "\n", Thread.currentThread().getName(), msg);

            // consuming
            Thread.sleep(BioServer.DELAY_TIME);

            msg = String.format(SEND, Thread.currentThread().getName());
            outputBuf.put(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(SocketChannel socketChannel, ByteBuffer outputBuf) throws IOException {
        outputBuf.flip();
        socketChannel.write(outputBuf);
    }

    @Override
    public boolean outputCompleted(ByteBuffer outputBuf) {
        return !outputBuf.hasRemaining();
    }
}
