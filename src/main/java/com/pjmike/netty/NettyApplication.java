package com.pjmike.netty;

import com.pjmike.netty.sky.AgentNettyClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        AgentNettyClient client = new AgentNettyClient();
        client.start();
    }
}
