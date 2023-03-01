/*
    Copyright 2018 Ericsson AB.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.exception.NackException;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ch.qos.logback.classic.Logger;

@Service("messageServiceRMQImpl") public class MessageServiceRMQImpl
    implements MessageService {

    @Autowired @Qualifier("rmqHelper") RMQHelper rmqHelper;
    
    Logger log = (Logger) LoggerFactory.getLogger(MessageServiceRMQImpl.class);
    
    /*Variables handles status codes*/
    List<Integer> statusCodes;
    List<JsonElement> errorItems;
    List<PublishResultItem> resultList;
    boolean checkEventStatus;
    /*
     * (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.util.Map, java.util.Map)
     */
    @Override
    public SendResult send(Map<String, String> routingKeyMap, Map<String, String> msgs, MsgService msgService) {
        List<PublishResultItem> results = new CopyOnWriteArrayList<>();
        SendResult sendResult = null;
        PublishResultItem event = null;
        if (!CollectionUtils.isEmpty(msgs)) {
            for (Map.Entry<String, String> entry : msgs.entrySet()) {
                String message, entryKey = entry.getKey();
                try {
                    message = sendMessage(routingKeyMap.get(entryKey), entry.getValue(), msgService);
                if (PropertiesConfig.SUCCESS.equals(message)) {
                    event = new PublishResultItem(entryKey, HttpStatus.OK.value(), PropertiesConfig.SUCCESS,
                            PropertiesConfig.SUCCESS_MESSAGE);
                } else {
                    event = new PublishResultItem(entryKey,  HttpStatus.INTERNAL_SERVER_ERROR.value(), PropertiesConfig.SERVER_DOWN,
                            PropertiesConfig.SERVER_DOWN_MESSAGE);
                    checkEventStatus = false;
                }
                } catch (NackException e) {
                    event = new PublishResultItem(entryKey, HttpStatus.INTERNAL_SERVER_ERROR.value(), PropertiesConfig.SERVER_DOWN,
                            PropertiesConfig.MESSAGE_NACK);
                } catch (TimeoutException e) {
                    event = new PublishResultItem(entryKey, HttpStatus.GATEWAY_TIMEOUT.value(), PropertiesConfig.GATEWAY_TIMEOUT,
                            PropertiesConfig.TIMEOUT_WAITING_FOR_ACK);
                } catch (RemRemPublishException e) {
                    event = new PublishResultItem(entryKey, HttpStatus.INTERNAL_SERVER_ERROR.value(), PropertiesConfig.SERVER_DOWN,
                            e.getMessage());
                } catch (IllegalArgumentException e) {
                    event = new PublishResultItem(entryKey, HttpStatus.BAD_REQUEST.value(), PropertiesConfig.INVALID_MESSAGE,
                            e.getMessage());
	        }catch (IOException e) {
                    event = new PublishResultItem(entryKey, HttpStatus.INTERNAL_SERVER_ERROR.value(), PropertiesConfig.SERVER_DOWN,
                            PropertiesConfig.SERVER_DOWN_MESSAGE);
                }
                results.add(event);
            }
            sendResult = new SendResult(results);
        }
        return sendResult;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.String, com.ericsson.eiffel.remrem.protocol.MsgService, java.lang.String)
     */
    @Override
    public SendResult send(String jsonContent, MsgService msgService, String userDomainSuffix, String tag, String routingKey) {

        JsonParser parser = new JsonParser();
        try {
            JsonElement json = parser.parse(jsonContent);
            if (json.isJsonArray()) {
                return send(json, msgService, userDomainSuffix, tag, routingKey);
            } else {
                Map<String, String> map = new HashMap<>();
                Map<String, String> routingKeyMap = new HashMap<>();
                String eventId = msgService.getEventId(json.getAsJsonObject());
                if (StringUtils.isNotBlank(eventId)) {
                    String routing_key = PublishUtils.getRoutingKey(msgService, json.getAsJsonObject(), rmqHelper, userDomainSuffix, tag, routingKey);
                    if (StringUtils.isNotBlank(routing_key)) {
                        map.put(eventId, json.toString());
                        routingKeyMap.put(eventId, routing_key);
                    } else if (routing_key == null) {
                        List<PublishResultItem> resultItemList = new CopyOnWriteArrayList<>();
                        routingKeyGenerationFailure(resultItemList);
                        return new SendResult(resultItemList);
                    } else {
                        List<PublishResultItem> resultItemList = new CopyOnWriteArrayList<>();
                        PublishResultItem resultItem = rabbitmqConfigurationNotFound(msgService);
                        resultItemList.add(resultItem);
                        return new SendResult(resultItemList);
                    }
                } else {
                    List<PublishResultItem> resultItemList = new CopyOnWriteArrayList<>();
                    createFailureResult(resultItemList);
                    return new SendResult(resultItemList);
                }
                return send(routingKeyMap, map, msgService);
            }
        } catch (final JsonSyntaxException e) {
            String resultMsg = "Could not parse JSON.";
            if (e.getCause() != null) {
                resultMsg = resultMsg + " Cause: " + e.getCause().getMessage();
            }
            log.error(resultMsg, e.getMessage());
            List<PublishResultItem> resultItemList = new CopyOnWriteArrayList<>();
            createFailureResult(resultItemList);
            return new SendResult(resultItemList);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(com.google.gson.JsonElement, com.ericsson.eiffel.remrem.protocol.MsgService, java.lang.String)
    */
    @Override
    public SendResult send(JsonElement json, MsgService msgService, String userDomainSuffix, String tag, String routingKey) {
        Map<String, String> map = new HashMap<>();
        Map<String, String> routingKeyMap = new HashMap<>();
        SendResult result;
        resultList = new CopyOnWriteArrayList<PublishResultItem>();
        if (json == null) {
            createFailureResult(resultList);
        }
        if (json.isJsonArray()) {
            statusCodes = new CopyOnWriteArrayList<Integer>();
            checkEventStatus = true;
            JsonArray bodyJson = json.getAsJsonArray();
            for (JsonElement obj : bodyJson) {
                String eventId = msgService.getEventId(obj.getAsJsonObject());
                if (StringUtils.isNotEmpty(eventId) && checkEventStatus) {
                    String routing_key = getAndCheckEvent(msgService, map, resultList, obj, routingKeyMap,
                            userDomainSuffix, tag, routingKey);
                    if (StringUtils.isNotBlank(routing_key)) {
                        result = send(obj.toString(), msgService, userDomainSuffix, tag, routing_key);
                        resultList.addAll(result.getEvents());
                        int statusCode = result.getEvents().get(0).getStatusCode();
                        if (!statusCodes.contains(statusCode))
                            statusCodes.add(statusCode);
                    } else if (routing_key == null) {
                        routingKeyGenerationFailure(resultList);
                        errorItems = new CopyOnWriteArrayList<JsonElement>();
                        int statusCode = resultList.get(0).getStatusCode();
                        statusCodes.add(statusCode);
                        errorItems.add(obj);
                        checkEventStatus = false;
                    } else {
                        PublishResultItem resultItem = rabbitmqConfigurationNotFound(msgService);
                        resultList.add(resultItem);
                        int statusCode = resultItem.getStatusCode();
                        statusCodes.add(statusCode);
                        break;
                    }
                } else {
                    if (!checkEventStatus) {
                        addUnsuccessfulResultItem(obj);
                        int statusCode = resultList.get(0).getStatusCode();
                        statusCodes.add(statusCode);
                    } else {
                        createFailureResult(resultList);
                        errorItems = new CopyOnWriteArrayList<JsonElement>();
                        int statusCode = resultList.get(0).getStatusCode();
                        statusCodes.add(statusCode);
                        errorItems.add(obj);
                        checkEventStatus = false;
                    }
                }
            }
        } else {
            statusCodes = new CopyOnWriteArrayList<Integer>();
            result = send(json.toString(), msgService, userDomainSuffix, tag, routingKey);
            resultList.addAll(result.getEvents());
            int statusCode = result.getEvents().get(0).getStatusCode();
            if (!statusCodes.contains(statusCode))
                statusCodes.add(statusCode);
        }
        result = new SendResult();
        result.setEvents(resultList);
        return result;
    }

    private String sendMessage(String routingKey, String msg, MsgService msgService) throws IOException,NackException, TimeoutException, RemRemPublishException {
        String resultMsg = PropertiesConfig.SUCCESS;
        try {
            instantiateRmqHelper();
        } catch (RemRemPublishException e) {
            log.error("RemRemPublishException occurred::" + e.getMessage());
        }
        rmqHelper.send(routingKey, msg, msgService);
        return resultMsg;
    }
    
    private void instantiateRmqHelper() throws RemRemPublishException {
        if (rmqHelper == null) {
            rmqHelper = new RMQHelper();
            rmqHelper.init();
        }
    }
    
    @Override public void cleanUp() {
        if (rmqHelper != null)
            try {
                rmqHelper.cleanUp();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
    }

    /**
     * Method get the eventId from the messaging service. And checks the eventId.
     * @param msgService Messaging service.
     * @param map contains the eventId and event in string format.
     * @param events for list the eiffel events results
     * @param obj the eiffel event 
     * @param routingKeyMap contains the eventId and routing key of that event
     */
    private String getAndCheckEvent(MsgService msgService, Map<String, String> map, List<PublishResultItem> events,
            JsonElement obj, Map<String, String> routingKeyMap, String userDomainSuffix, String tag, String routingKey) {
        String eventId = msgService.getEventId(obj.getAsJsonObject());
        String routing_key = (eventId != null)
                ? PublishUtils.getRoutingKey(msgService, obj.getAsJsonObject(), rmqHelper, userDomainSuffix, tag, routingKey) : null;
        if (eventId != null && routing_key != null && !routing_key.isEmpty()) {
            routingKeyMap.put(eventId, routing_key);
            map.put(eventId, obj.toString());
        }
        return routing_key;
    }

    /**
     * Method returns result for the failure event.
     * @param events for list the eiffel events results
     */
    private void createFailureResult(List<PublishResultItem> resultItemList) {
        PublishResultItem resultItem = new PublishResultItem(null, 400, PropertiesConfig.INVALID_MESSAGE,
                PropertiesConfig.INVALID_EVENT_CONTENT);
        resultItemList.add(resultItem);
    }

    /**
     * This method returns result for Missing RabbitMq properties in configuration file
     * @param msgService
     * @return PublishResultItem for 404
     */
    private PublishResultItem rabbitmqConfigurationNotFound(MsgService msgService) {
        PublishResultItem event = new PublishResultItem(null, 404, PropertiesConfig.RABBITMQ_PROPERTIES_NOT_FOUND,
                PropertiesConfig.RABBITMQ_PROPERTIES_NOT_FOUND_CONTENT+msgService.getServiceName());
        return event;
    }

    private void routingKeyGenerationFailure(List<PublishResultItem> resultItemList) {
        PublishResultItem resultItem = new PublishResultItem(null, 500, PropertiesConfig.SERVER_DOWN,
                PropertiesConfig.ROUTING_KEY_GENERATION_FAILED_CONTENT);
        resultItemList.add(resultItem);
    }

    private void addUnsuccessfulResultItem(JsonElement obj) {
        PublishResultItem event = new PublishResultItem(null, 503, PropertiesConfig.SERVICE_UNAVAILABLE,
                PropertiesConfig.UNSUCCESSFUL_EVENT_CONTENT);
        resultList.add(event);
    }
    
    /**
     * Method returns the Http response code for the events.
     */
    public HttpStatus getHttpStatus() {
        if (statusCodes.size() > 1) {
            return HttpStatus.MULTI_STATUS;
        } else {
            return HttpStatus.valueOf(statusCodes.get(0));
            
        }
    }
}
