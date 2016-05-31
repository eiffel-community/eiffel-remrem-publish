package com.ericsson.eiffel.remrem.publish.service;


import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

public interface MessageService {
    ListenableFuture<List<SendResult>> send(String routingKey, List<String> msgs);
}
