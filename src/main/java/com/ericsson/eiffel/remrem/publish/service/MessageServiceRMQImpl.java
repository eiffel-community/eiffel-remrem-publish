package com.ericsson.eiffel.remrem.publish.service;

import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("messageServiceRMQImpl") public class MessageServiceRMQImpl
    implements MessageService {

    private static final String SUCCEED = "succeed";
    @Autowired @Qualifier("rmqHelper") RMQHelper rmqHelper;
    
    Logger log = (Logger) LoggerFactory.getLogger(MessageServiceRMQImpl.class);

    /* (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.String, java.util.List)
     */
    @Override public List<SendResult> send(String routingKey, List<String> msgs) {
        List<SendResult> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(msgs)) {
            for (String msg : msgs) {
                results.add(sendMessage(routingKey, msg));
            }
        }
        return results;
    }

    /* (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.String, java.lang.String)
     */
    @Override public List<SendResult> send(String routingKey, String jsonContent) {
    	JsonParser parser = new JsonParser();
    	try {
    	JsonElement json = parser.parse(jsonContent);
        return send(routingKey, json);
    	} catch (final JsonSyntaxException e){
            String resultMsg = "Could not parse JSON.";
            if (e.getCause() != null) {
                resultMsg = resultMsg + " Cause: " + e.getCause().getMessage();
            }
            log.error(resultMsg, e.getMessage());
            List<SendResult> results = new ArrayList<>();
            results.add(new SendResult(resultMsg));
            return results;
    	}
    }
    
    /* (non-Javadoc)
     * @see com.ericsson.eiffel.remrem.publish.service.MessageService#send(java.lang.String, com.google.gson.JsonElement)
     */
    @Override public List<SendResult> send(String routingKey, JsonElement json) {
    	List<String> msgs = new ArrayList<>();
    	
    	if (json == null) {    		
            String resultMsg = "Invalid json content.";
            log.error(resultMsg);
            List<SendResult> results = new ArrayList<>();
            results.add(new SendResult(resultMsg));
            return results;
    	}

    	if (json.isJsonArray()) {
    		JsonArray bodyJson = json.getAsJsonArray();

    		for (JsonElement obj : bodyJson) {
    			msgs.add(obj.toString());
    		}
    	} else {
    		msgs.add(json.toString());
    	}
    	
        return send(routingKey, msgs);
    }
    
    private SendResult sendMessage(String routingKey, String msg) {
        String resultMsg = SUCCEED;
        instantiateRmqHelper();
        try {
            rmqHelper.send(routingKey, msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultMsg = "Failed to send message:" + msg;
        }
        return new SendResult(resultMsg);
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
}
