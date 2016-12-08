package com.ericsson.eiffel.remrem.publish.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
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

    /*
     * (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.util.Map, java.util.Map)
     */
    @Override
    public SendResult send(Map<String, String> routingKeyMap, Map<String, String> msgs) {
        List<PublishResultItem> results = new ArrayList<>();
        SendResult sendResult = null;
        PublishResultItem event = null;
        if (!CollectionUtils.isEmpty(msgs)) {
            for (Map.Entry<String, String> entry : msgs.entrySet()) {
                String message = sendMessage(routingKeyMap.get(entry.getKey()), entry.getValue());
                if (PropertiesConfig.SUCCESS.equals(message)) {
                    event = new PublishResultItem(entry.getKey(), 200, PropertiesConfig.SUCCESS, null);
                } else {
                    event = new PublishResultItem(entry.getKey(), 400, PropertiesConfig.INVALID_MESSAGE,
                            PropertiesConfig.INVALID_EVENT_CONTENT);
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
    public SendResult send(String jsonContent, MsgService msgService, String userDomainSuffix) {
        
        JsonParser parser = new JsonParser();
        try {
            JsonElement json = parser.parse(jsonContent);
            Map<String, String> map = new HashMap<>();
            Map<String, String> routingKeyMap = new HashMap<>();
            String eventId = msgService.getEventId(json.getAsJsonObject());
            if (eventId != null) {
                map.put(eventId, json.toString());
                routingKeyMap.put(eventId, PublishUtils.prepareRoutingKey(msgService, json.getAsJsonObject(), rmqHelper, userDomainSuffix)) ;
            } else {
                List<PublishResultItem> events = new ArrayList<>();
                createFailureResult(events);
                return new SendResult(events);
            }
            return send(routingKeyMap, map);
        } catch (final JsonSyntaxException e) {
            String resultMsg = "Could not parse JSON.";
            if (e.getCause() != null) {
                resultMsg = resultMsg + " Cause: " + e.getCause().getMessage();
            }
            log.error(resultMsg, e.getMessage());
            List<PublishResultItem> events = new ArrayList<>();
            createFailureResult(events);
            return new SendResult(events);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(com.google.gson.JsonElement, com.ericsson.eiffel.remrem.protocol.MsgService, java.lang.String)
     */
    @Override
    public SendResult send(JsonElement json, MsgService msgService, String userDomainSuffix) {
        
        Map<String, String> map = new HashMap<>();
        Map<String, String> routingKeyMap = new HashMap<>();
        List<PublishResultItem> events = new ArrayList<>();
        if (json == null) {
            createFailureResult(events);
        }
        if (json.isJsonArray()) {
            JsonArray bodyJson = json.getAsJsonArray();
            for (JsonElement obj : bodyJson) {
                getAndCheckEvent(msgService, map, events, obj,routingKeyMap,userDomainSuffix);
            }
        } else {
            getAndCheckEvent(msgService, map, events, json,routingKeyMap,userDomainSuffix);
        }

        if (map.size() > 0) {
            SendResult result = send(routingKeyMap, map);
            events.addAll(result.getEvents());
            result.setEvents(events);
            return result;
        } else {
            SendResult result = new SendResult();
            result.setEvents(events);
            return result;
        }
    }
    
    private String sendMessage(String routingKey, String msg) {
        String resultMsg = PropertiesConfig.SUCCESS;
        instantiateRmqHelper();
        try {
            rmqHelper.send(routingKey, msg);
        } catch (Exception e) {
           log.error(e.getMessage(), e);
            resultMsg = "Failed to send message:" + msg;
        }
        return resultMsg;
    }
    
    private void instantiateRmqHelper() {
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
     * @param routeKeyMap contains the eventId and routing key of that event
     */
    private void getAndCheckEvent(MsgService msgService, Map<String, String> map, List<PublishResultItem> events,
            JsonElement obj, Map<String, String> routeKeyMap, String userDomainSuffix) {
        String eventId = msgService.getEventId(obj.getAsJsonObject());
        if (eventId != null) {
            routeKeyMap.put(eventId, PublishUtils.prepareRoutingKey(msgService, obj.getAsJsonObject(), rmqHelper, userDomainSuffix)) ;
            map.put(eventId, obj.toString());
        } else {
            createFailureResult(events);
        }
    }

    /**
     * Method returns result for the failure event.
     * @param events for list the eiffel events results
     */
    private void createFailureResult(List<PublishResultItem> events) {
        PublishResultItem event = new PublishResultItem(null, 400, PropertiesConfig.INVALID_MESSAGE,
                PropertiesConfig.INVALID_EVENT_CONTENT);
        events.add(event);
    }
}
