/*
    Copyright 2017 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
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
     * @param msgService
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
    SendResult send(Map<String, String> routingKeyMap, Map<String, String> msgs, MsgService msgService);
    
    /**
     * 
     * @param jsonContent the json string containing the messages to be send, 
     *      if several messages in the json string then they need to be comma separated and
     *      surrounded with brackets
     * @param msgService
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @param tag is optional parameter, If user provide this it will add to the tag if routingKey is not specified by the user.
     * @param routingKey is optional parameter, If user provide this it will add to the routingKey otherwise generate routingKey based on message type.
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
 
    public SendResult send(String jsonContent, MsgService msgService, String userDomainSuffix, String tag, String routingKey);
    
    /**
     * @param jsonContent a json object containing the messages to be send
     * @param msgService
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @param tag is optional parameter, If user provide this it will add to the tag if routingKey is not specified by the user.
     * @param routingKey is optional parameter, If user provide this it will add to the routingKey otherwise generate routingKey based on message type.
     * @return SendResult which contains response including eventId, statusCode, message and result.
     */
    public SendResult send(JsonElement jsonContent, MsgService msgService, String userDomainSuffix, String tag, String routingKey);
    
    /**
     * Does the cleanup like closing open connections
     */
    public void cleanUp(); 
    
    /**
     * Implemented Status code for the response
     */
    public HttpStatus getHttpStatus();

}
