package com.pjmike.netty.controller;

import com.pjmike.netty.client.NettyClient;
import com.pjmike.netty.protocol.protobuf.MessageBase;
import com.pjmike.netty.sky.AgentNettyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author pjmike
 * @create 2018-10-24 16:47
 */
//@RestController
public class ConsumerController {
    @Resource
    private AgentNettyClient agentNettyClient;

    @GetMapping("/send")
    public String send() {
        MessageBase.Message message = new MessageBase.Message()
                .toBuilder().setCmd(MessageBase.Message.CommandType.NORMAL)
                .setContent("hello netty")
                .setRequestId(UUID.randomUUID().toString()).build();
        agentNettyClient.sendMsg(message);
        return "send ok";
    }

    @GetMapping("/send2")
    public String send2() {

        agentNettyClient.sendMsg2("{\"code\":0,\"data\":{\"key\":\"val\"},\"header\":{\"hk\":\"hv\"}}");
        return "send ok";
    }

    @GetMapping("/send3")
    public String send3() {

        agentNettyClient.sendMsg2("{\"code\":1,\"data\":{\"key3\":\"val\"},\"header\":{\"hk\":\"hv\"}}");
        agentNettyClient.sendMsg2("{\"code\":2,\"data\":{\"key3\":\"val\"},\"header\":{\"hk\":\"hv\"}}");
        return "send ok";
    }
}
