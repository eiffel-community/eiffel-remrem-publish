package com.ericsson.eiffel.remrem.publish.service;


import java.util.Map;

import com.google.gson.JsonElement;

public interface MessageService {
    /**
     * @param routingKey
     * @param msgs the list with messages to be sent
     * @return list of results containing send response messages after sending the messages 
     */
    SendResult send(String routingKey, Map<String, String> map);
    
    /**
     * @param routingKey
     * @param jsonContent the json string containing the messages to be send, 
     * 		if several messages in the json string then they need to be comma separated and
     * 		surrounded with brackets
     * 
     * @return list of results containing send response messages after sending the messages in jsonContent
     */
    SendResult send(String routingKey, String jsonContent);
    
    /**
     * @param routingKey
     * @param jsonContent a json object containing the messages to be send
     * @return list of results containing send response messages after sending the messages in jsonContent
     */
    SendResult send(String routingKey, JsonElement jsonContent);
    
    /**
     * Does the cleanup like closing open connections
     */
    public void cleanUp(); 
    }
