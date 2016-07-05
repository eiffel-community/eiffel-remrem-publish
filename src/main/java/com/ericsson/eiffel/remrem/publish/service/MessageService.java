package com.ericsson.eiffel.remrem.publish.service;


import java.util.List;

public interface MessageService {
    List<SendResult> send(String routingKey, List<String> msgs);
    public void cleanUp();
}
