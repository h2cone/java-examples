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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy from https://github.com/normanmaurer/netty-in-action
 *
 * @author h^2
 */
public class LogEventHandler extends SimpleChannelInboundHandler<LogEvent> {
    private static final Logger log = LoggerFactory.getLogger(LogEvent.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent event) throws Exception {
        String builder = event.getReceivedTimestamp() +
                " [" +
                event.getSource().toString() +
                "] [" +
                event.getLogfile() +
                "] : " +
                event.getMsg();
        log.info(builder);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("oops", cause);
        ctx.close();
    }
}
