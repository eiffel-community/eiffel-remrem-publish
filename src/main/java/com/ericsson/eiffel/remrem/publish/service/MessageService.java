package com.ericsson.eiffel.remrem.publish.service;


import java.util.List;

import com.google.gson.JsonElement;

public interface MessageService {
    List<SendResult> send(String routingKey, List<String> msgs);
    List<SendResult> send(String routingKey, String jsonContent);
    List<SendResult> send(String routingKey, JsonElement jsonContent);
    public void cleanUp();
}
