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

package io.h2cone.network.staff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ChannelHandler {

    void read(SocketChannel socketChannel, ByteBuffer inputBuf) throws IOException;

    boolean inputCompleted(ByteBuffer inputBuf);

    void process(ByteBuffer inputBuf, ByteBuffer outputBuf);

    void write(SocketChannel socketChannel, ByteBuffer outputBuf) throws IOException;

    boolean outputCompleted(ByteBuffer outputBuf);

}
