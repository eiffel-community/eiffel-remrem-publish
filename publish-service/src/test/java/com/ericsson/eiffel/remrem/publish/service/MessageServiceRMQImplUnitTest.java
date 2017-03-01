package com.ericsson.eiffel.remrem.publish.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class MessageServiceRMQImplUnitTest {
    
    @Autowired
    private MsgService msgServices[];
    
    @Autowired
    @Qualifier("messageServiceRMQImpl")
    MessageService messageService;
    
    @Autowired @Qualifier("rmqHelper") 
    RMQHelper rmqHelper;
    
    @Test public void sendNormal() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "test");
        messageService.send(map, map);
    }
    
    @Test public void testSingleSuccessfulEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/integration-test/resources/EiffelActivityFinishedEvent.json"));
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService("eiffelsemantics", msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        String Expected="[{\"id\":\"1afffd13-04ae-4638-97f1-aaeed78a28c7\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }
    
    @Test public void testSingleFailedEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/integration-test/resources/Invalid_EiffelActivityFinishedEvent.json"));
        MsgService msgService = PublishUtils.getMessageService("eiffelsemantics", msgServices);
        JsonArray jarray = new JsonArray();
        SendResult result = messageService.send(body, msgService, "test");
        String Expected="[{\"id\":null,\"status_code\":400,\"result\":\"Bad Request\",\"message\":\"Invalid event content, client need to fix problem in event before submitting again\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }
    
    @Test public void testMultipleFailedEvents() throws Exception {
        String body = FileUtils.readFileToString(new File("src/integration-test/resources/MultipleInvalidEvents.json"));
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService("eiffelsemantics", msgServices);  
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        String Expected="[{\"id\":null,\"status_code\":400,\"result\":\"Bad Request\",\"message\":\"Invalid event content, client need to fix problem in event before submitting again\"},{\"id\":null,\"status_code\":503,\"result\":\"Service Unavailable\",\"message\":\"Please check previous event and try again later\"},{\"id\":null,\"status_code\":503,\"result\":\"Service Unavailable\",\"message\":\"Please check previous event and try again later\"},{\"id\":null,\"status_code\":503,\"result\":\"Service Unavailable\",\"message\":\"Please check previous event and try again later\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }
      @Test public void testMultipleSuccessfulEvents() throws Exception {
        String body = FileUtils.readFileToString(new File("src/integration-test/resources/MultipleValidEvents.json"));
        String Expected="[{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a1\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"},{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a2\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"},{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a3\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService("eiffelsemantics", msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());       
    }

    @Test
    public void testRabbitMQConnection() {
        try {
            assertTrue(rmqHelper.rabbitConnection.isOpen());
            rmqHelper.rabbitConnection.close();
            assertFalse(rmqHelper.rabbitConnection.isOpen());
            rmqHelper.send("eiffelxxx", "Test message");
            assertTrue(rmqHelper.rabbitConnection.isOpen());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            fail(e.getMessage().toString());
        }
    }

    @Test
    public void testRoutingKey() throws Exception {
        MsgService msgService = PublishUtils.getMessageService("", msgServices);
        String routingKey;
        if (msgService != null) {
            File file = new File("src/integration-test/resources/EiffelActivityFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            routingKey = PublishUtils.prepareRoutingKey(msgService, json.getAsJsonObject(), rmqHelper, "fem001");
            assertEquals("eiffel.activity.finished.notag.eiffelxxx.fem001", routingKey);
        }
    }
}
