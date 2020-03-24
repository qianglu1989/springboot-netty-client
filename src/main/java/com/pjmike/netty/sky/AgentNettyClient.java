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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pjmike.netty.protocol.protobuf.MessageBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenyu.nie created on 2018 2018/10/25 16:37
 */
@Slf4j
public class AgentNettyClient {


    private final Bootstrap bootstrap = new Bootstrap();

    private final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() - 1, new ThreadFactoryBuilder().setNameFormat("oap-netty-server-worker").build());

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final SettableFuture<Void> started = SettableFuture.create();

    private volatile Channel channel;

    private volatile List<String> tcpServers;

    private Random random = new Random();

    private SocketChannel socketChannel;

    public AgentNettyClient() {
//        tcpServers = Arrays.asList(Config.Agent.TCP_SERVERS.split(","));
        tcpServers = Arrays.asList("127.0.0.1:9999".split(","));

    }

    public void sendMsg(MessageBase.Message message) {
        socketChannel.writeAndFlush(message);
    }
    public void sendMsg2(String message) {
        socketChannel.writeAndFlush(message);
    }

    public void start() {
        final IdleStateHandler idleStateHandler = new IdleStateHandler(1, 1, 1, TimeUnit.SECONDS);

        List<Processor> processors = ImmutableList.copyOf(ServiceLoader.load(Processor.class));
        final AgentRequestHandler requestHandler = new AgentRequestHandler(ImmutableList.of(new HeartbeatProcessor()));

        final ConnectionManagerHandler connectionManagerHandler = new ConnectionManagerHandler();

        int index = Math.abs(random.nextInt()) % tcpServers.size();

        String server = tcpServers.get(index);
        String[] ipAndPort = server.split(":");
        bootstrap.group(WORKER_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast("encoder", new StringEncoder())
                                .addLast("decoder", new StringDecoder())
                                .addLast("idle", idleStateHandler)
                                .addLast("heartbeatHandler", new HeartbeatHandler())
                                .addLast(requestHandler)
                                .addLast(connectionManagerHandler);
                    }
                });
        bootstrap.connect(ipAndPort[0], Integer.parseInt(ipAndPort[1])).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("bistoury netty client start success, {}");
                    channel = future.channel();
                    socketChannel = (SocketChannel) channel;

//                    closeFuture(taskStore);
                    running.compareAndSet(false, true);
                    started.set(null);
                } else {
                    started.set(null);
                    log.warn("bistoury netty client start fail, {}");
                }
            }
        });


        try {
            started.get();
        } catch (InterruptedException e) {
            log.error("start bistoury netty client error", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("start bistoury netty client error", e);
        }

    }

    public boolean isRunning() {
        return running.get();
    }

//    private void closeFuture(final DefaultTaskStore taskStore) {
//        channel.closeFuture().addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                taskStore.close();
//            }
//        });
//    }

    @ChannelHandler.Sharable
    private class ConnectionManagerHandler extends ChannelDuplexHandler {

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
            log.warn("agent netty client channel disconnect, {}", ctx.channel());
            destroyAndSync();
            super.disconnect(ctx, future);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("agent netty client channel active, {}", ctx.channel());
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("agent netty client channel inactive, {}", ctx.channel());
            destroyAndSync();
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                log.warn("agent netty client idle, {}", ctx.channel());
                destroyAndSync();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            destroyAndSync();
        }
    }

    public synchronized void destroyAndSync() {
        if (running.compareAndSet(true, false)) {
            log.warn("agent netty client destroy, {}", channel);
            try {
                channel.close().syncUninterruptibly();
            } catch (Exception e) {
                log.error("close channel error", e);
            }
        }
    }
}
