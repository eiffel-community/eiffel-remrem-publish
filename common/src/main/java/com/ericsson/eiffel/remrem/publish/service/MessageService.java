package com.ericsson.eiffel.remrem.publish.service;


import java.util.Map;

import org.springframework.http.HttpStatus;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.google.gson.JsonElement;

public interface MessageService {
    /**
     * 
     * @param routingKeyMap
     * @param msgs the list with messages to be sent
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
    SendResult send(Map<String, String> routingKeyMap, Map<String, String> msgs);
    
    /**
     * 
     * @param jsonContent the json string containing the messages to be send, 
     *      if several messages in the json string then they need to be comma separated and
     *      surrounded with brackets
     * @param msgService
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
 
    public SendResult send(String jsonContent, MsgService msgService, String userDomainSuffix);
    
    /**
     * @param jsonContent a json object containing the messages to be send
     * @param msgService
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
    public SendResult send(JsonElement jsonContent, MsgService msgService, String userDomainSuffix);
    
    /**
     * Does the cleanup like closing open connections
     */
    public void cleanUp(); 
    
    /**
     * Implemented Status code for the response
     */
    public HttpStatus getHttpStatus();
}
