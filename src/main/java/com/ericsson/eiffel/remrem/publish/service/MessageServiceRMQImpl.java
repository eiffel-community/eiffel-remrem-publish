package com.ericsson.eiffel.remrem.publish.service;

import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("messageServiceRMQImpl") @Slf4j public class MessageServiceRMQImpl
    implements MessageService {

    private static final String SUCCEED = "succeed";
    @Autowired @Qualifier("rmqHelper") RMQHelper rmqHelper;

    @Override public List<SendResult> send(String routingKey, List<String> msgs) {
        List<SendResult> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(msgs)) {
            for (String msg : msgs) {
                results.add(sendMessage(routingKey, msg));
            }
        }
        return results;
    }

    public List<SendResult> send(String jsonContent, String routingKey) {
    	JsonParser parser = new JsonParser();
    	JsonElement json = parser.parse(jsonContent);
    	return send(routingKey, json);
    }
    
    public List<SendResult> send(String routingKey, JsonElement json) {
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
    	
        List<SendResult> results = send(routingKey, msgs);
    	return results;
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
    
    public void cleanUp() {
        if (rmqHelper != null)
            try {
                rmqHelper.cleanUp();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}
