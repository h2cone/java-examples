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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Copy from https://github.com/normanmaurer/netty-in-action
 *
 * @author h^2
 */
public class LogEventMonitor {
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;

    public LogEventMonitor(InetSocketAddress address) {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LogEventDecoder());
                        pipeline.addLast(new LogEventHandler());
                    }
                })
                .localAddress(address);
    }

    public Channel bind() throws InterruptedException {
        return bootstrap.bind().sync().channel();
    }

    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        InetSocketAddress address = new InetSocketAddress(Integer.parseInt(args[0]));
        LogEventMonitor monitor = new LogEventMonitor(address);
        try {
            Channel channel = monitor.bind();
            channel.closeFuture().sync();
        } finally {
            monitor.stop();
        }
    }
}
