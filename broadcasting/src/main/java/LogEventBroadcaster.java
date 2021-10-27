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
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

/**
 * Copy from https://github.com/normanmaurer/netty-in-action
 *
 * @author h^2
 */
public class LogEventBroadcaster {
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private final File file;

    public LogEventBroadcaster(InetSocketAddress address, File file) {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(address));
        this.file = file;
    }

    public void run() throws InterruptedException, IOException {
        Channel channel = bootstrap.bind(0).sync().channel();
        long pointer = 0;
        while (!Thread.interrupted()) {
            long len = file.length();
            if (pointer > len) {
                // file was rest
                pointer = len;
            } else if (pointer < len) {
                // content was added
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(pointer);
                String line;
                while ((line = raf.readLine()) != null) {
                    channel.writeAndFlush(new LogEvent(file.getAbsolutePath(), line));
                }
                pointer = raf.getFilePointer();
                raf.close();
            }
            Thread.sleep(1000);
        }
    }

    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }
        InetSocketAddress address = new InetSocketAddress("255.255.255.255", Integer.parseInt(args[0]));
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(address, new File(args[1]));
        try {
            broadcaster.run();
        } finally {
            broadcaster.stop();
        }
    }
}
