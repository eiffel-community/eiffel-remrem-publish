package com.ericsson.eiffel.remrem.producer.helper;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

@Component("rmqHelper") @Slf4j public class RMQHelper {

    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    private Connection rabbitConnection;
    private List<Channel> rabbitChannels;

    @PostConstruct public void init() {
        log.info("RMQHelper init ...");
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            rabbitConnection = factory.newConnection();
            rabbitChannels = new ArrayList<>();

            for (int i = 0; i < CHANNEL_COUNT; i++) {
                rabbitChannels.add(rabbitConnection.createChannel());
            }
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void send(String routingKey, String msg) throws IOException {
        giveMeRandomChannel()
            .basicPublish(exchangeName, routingKey, MessageProperties.BASIC, msg.getBytes());
    }


    private Channel giveMeRandomChannel() {
        return rabbitChannels.get(random.nextInt(rabbitChannels.size()));
    }
}
