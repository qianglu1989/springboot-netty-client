/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.pjmike.netty.sky;

import com.google.common.collect.ImmutableMap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/10/22 17:30
 */
@ChannelHandler.Sharable
@Slf4j
public class AgentRequestHandler extends ChannelInboundHandlerAdapter {


    private final Map<Integer, Processor> processorMap;

    public AgentRequestHandler(List<Processor> processors) {
        ImmutableMap.Builder<Integer, Processor> builder = new ImmutableMap.Builder<Integer, Processor>();
        for (Processor<?> processor : processors) {
            for (Integer type : processor.types()) {
                builder.put(type, processor);
            }
        }
        processorMap = builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("request process error", cause);
        ctx.channel().close();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        String data = (String) msg;
//        if (code != ResponseCode.RESP_TYPE_HEARTBEAT.getCode()) {
//            log.info("agent receive request: id={}, sourceIp={}, code={}", id, ctx.channel().remoteAddress(), code);
//        }
//        final ResponseHandler handler = NettyExecuteHandler.of(header, ctx);
//        ctx.channel().attr(AgentConstants.attributeKey).set(id);

        Processor processor = processorMap.get(1);
        if (processor == null) {
//            handler.handleError(new IllegalArgumentException("unknown code [" + code + "]"));
            return;
        }


        processor.process("header", "test");
    }
}
