/*
 * Copyright 2021 huangh https://github.com/h2cone
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Copy from https://github.com/normanmaurer/netty-in-action
 *
 * @author h^2
 */
public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        ByteBuf content = packet.content();
        int readableBytes = content.readableBytes();
        int idx = content.indexOf(0, readableBytes, LogEvent.SEPARATOR);
        String filename = content.slice(0, idx).toString(StandardCharsets.UTF_8);
        String logMsg = content.slice(idx + 1, readableBytes).toString(StandardCharsets.UTF_8).trim();

        LogEvent event = new LogEvent(packet.sender(), System.currentTimeMillis(), filename, logMsg);
        out.add(event);
    }
}
