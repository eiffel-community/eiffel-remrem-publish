package com.ericsson.eiffel.remrem.publish.service;


import java.util.Map;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.google.gson.JsonElement;

public interface MessageService {
    /**
     * @param routingKey
     * @param msgs the list with messages to be sent
     * @return list of results containing send response messages after sending the messages 
     */
    SendResult send(Map<String, String> routekeyMap, Map<String, String> msgs);
    
    /**
     * @param routingKey
     * @param jsonContent the json string containing the messages to be send, 
     * 		if several messages in the json string then they need to be comma separated and
     * 		surrounded with brackets
     * 
     * @return list of results containing send response messages after sending the messages in jsonContent
     */
    public SendResult send(String jsonContent, MsgService msgService);
    
    /**
     * @param routingKey
     * @param jsonContent a json object containing the messages to be send
     * @return list of results containing send response messages after sending the messages in jsonContent
     */
    public SendResult send(JsonElement json, MsgService msgService);
    
    /**
     * Does the cleanup like closing open connections
     */
    public void cleanUp(); 
    }
