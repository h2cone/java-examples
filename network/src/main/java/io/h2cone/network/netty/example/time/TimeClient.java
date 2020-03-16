package io.h2cone.network.netty.example.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * https://netty.io/wiki/user-guide-for-4.x.html
 */
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        String host = "127.0.0.1";
        /*String host = args[0];
        int port = Integer.parseInt(args[1]);*/

        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
