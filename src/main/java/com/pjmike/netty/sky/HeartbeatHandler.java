package com.pjmike.netty.sky;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", ResponseCode.RESP_TYPE_HEARTBEAT.getCode());
        data.put("instanceId", "1");
        data.put("appId", "1");
        String d = JSON.toJSONString(data);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                log.info("已经5s没有发送消息给服务端");
                //向服务端送心跳包
                //发送心跳消息，并在发送失败时关闭该连接

                ctx.writeAndFlush(d).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //如果运行过程中服务端挂了,执行重连机制
        EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            AgentNettyClient client = new AgentNettyClient();
            client.start();
        }, 10L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获的异常：{}",cause.getMessage());
        ctx.channel().close();
    }
}
