package com.ericsson.eiffel.remrem.producer.service;


import java.util.List;

public interface MessageService {
    List<SendResult> send(String routingKey, List<String> msgs);
}
