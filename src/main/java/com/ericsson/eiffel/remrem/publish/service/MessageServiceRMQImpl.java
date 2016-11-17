package com.ericsson.eiffel.remrem.publish.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

@Service("messageServiceRMQImpl")
@Slf4j
public class MessageServiceRMQImpl implements MessageService {

    @Autowired
    @Qualifier("rmqHelper")
    RMQHelper rmqHelper;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.
     * String, java.util.List)
     */
    @Override
    public SendResult send(String routingKey, Map<String, String> msgs) {
        List<PublishResultItem> results = new ArrayList<>();
        SendResult sendResult = null;
        PublishResultItem event = null;
        if (!CollectionUtils.isEmpty(msgs)) {
            for (Map.Entry<String, String> entry : msgs.entrySet()) {
                String message = sendMessage(routingKey, entry.getValue());
                if (PropertiesConfig.SUCCEED.equals(message)) {
                    event = new PublishResultItem(entry.getKey(), 200, PropertiesConfig.SUCCESS, null);
                } else {
                    event = new PublishResultItem(entry.getKey(), 200, PropertiesConfig.INVALID_MESSAGE,
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
     * 
     * @see
     * com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.
     * String, java.lang.String)
     */
    @Override
    public SendResult send(String routingKey, String jsonContent) {
        JsonParser parser = new JsonParser();
        try {
            JsonElement json = parser.parse(jsonContent);
            Map<String, String> map = new HashMap<>();
            String eventId = rmqHelper.getEventId(json);
            if (eventId != null) {
                map.put(eventId, json.toString());
            } else {
                List<PublishResultItem> events = new ArrayList<>();
                createFailureResult(events);
                return new SendResult(events);
            }
            return send(routingKey, map);
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
     * 
     * @see
     * com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.
     * String, com.google.gson.JsonElement)
     */
    @Override
    public SendResult send(String routingKey, JsonElement json) {
        Map<String, String> map = new HashMap<>();
        List<PublishResultItem> events = new ArrayList<>();
        if (json == null) {
            createFailureResult(events);
        }
        if (json.isJsonArray()) {
            JsonArray bodyJson = json.getAsJsonArray();
            for (JsonElement obj : bodyJson) {
                String eventId = rmqHelper.getEventId(obj);
                if (eventId != null) {
                    map.put(eventId, obj.toString());
                } else {
                    createFailureResult(events);
                }
            }
        } else {
            String eventId = rmqHelper.getEventId(json);
            if (eventId != null) {
                map.put(eventId, json.toString());
            } else {
                createFailureResult(events);
            }
        }

        if (map.size() > 0) {
            SendResult result = send(routingKey, map);
            events.addAll(result.getEvents());
            result.setEvents(events);
            return result;
        } else {
            SendResult result = new SendResult();
            result.setEvents(events);
            return result;
        }

    }

    private void createFailureResult(List<PublishResultItem> events) {
        PublishResultItem event = new PublishResultItem(null, 400, PropertiesConfig.INVALID_MESSAGE,
                PropertiesConfig.INVALID_EVENT_CONTENT);
        events.add(event);
    }

    private String sendMessage(String routingKey, String msg) {
        String resultMsg = PropertiesConfig.SUCCEED;
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

    @Override
    public void cleanUp() {
        if (rmqHelper != null)
            try {
                rmqHelper.cleanUp();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
    }
}
