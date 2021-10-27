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
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Copy from https://github.com/normanmaurer/netty-in-action
 *
 * @author h^2
 */
public class LogEventEncoder extends MessageToMessageEncoder<LogEvent> {
    private final InetSocketAddress remoteAddress;

    public LogEventEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, LogEvent event, List<Object> out) throws Exception {
        byte[] file = event.getLogfile().getBytes(StandardCharsets.UTF_8);
        byte[] msg = event.getMsg().getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = ctx.alloc().buffer(file.length + msg.length + 1);
        buffer.writeBytes(file);
        buffer.writeByte(LogEvent.SEPARATOR);
        buffer.writeBytes(msg);
        out.add(new DatagramPacket(buffer, remoteAddress));
    }
}
