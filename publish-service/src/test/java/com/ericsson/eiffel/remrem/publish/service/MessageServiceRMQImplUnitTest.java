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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;
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

    @Autowired
    @Qualifier("rmqHelper") 
    RMQHelper rmqHelper;
    private static final String protocol = "eiffelsemantics";
    private static final String host= "0.0.0.0";
    private static final String exchangeName= "amq.direct";
    private static final String domainId= "eiffelxxx";
    private boolean createExchangeIfNotExisting = true;

    @PostConstruct public void setUp() throws Exception {
        rmqHelper.getRabbitMqPropertiesMap().put(protocol, new RabbitMqProperties());
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setProtocol(protocol);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setHost(host);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setExchangeName(exchangeName);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setDomainId(domainId);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).init();
    }

    /**
     * This test case is used to check and create an exchange based on
     * createExchangeIfNotExisting value If createExchangeIfNotExisting is true and
     * exchange is not available then exchange is created. If it fails to create an
     * exchange it will throw an Exception.
     */
    @Test
    public void testCreateExchangeIfNotExistingEnable() throws RemRemPublishException {
        boolean ExceptionOccured = false;
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setExchangeName("nonexistexchangename");
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setCreateExchangeIfNotExisting(createExchangeIfNotExisting);
        try {
            rmqHelper.getRabbitMqPropertiesMap().get(protocol).init();
        } catch (RemRemPublishException e) {
            ExceptionOccured = true;
        } finally {
            rmqHelper.getRabbitMqPropertiesMap().get(protocol).setExchangeName(exchangeName);
            rmqHelper.getRabbitMqPropertiesMap().get(protocol).init();
        }
        assertFalse("An exception occured while creating a exchange", ExceptionOccured);
    }

    /**
     * This test case is used to check whether it will throw
     * RemRemPublishException when CreateExchangeIfNotExisting is false and
     * exchange is not available.
     */
    @Test(expected = RemRemPublishException.class)
    public void testCreateExchangeIfNotExistingDisable() throws RemRemPublishException  {
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setExchangeName("test76888");
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setCreateExchangeIfNotExisting(false);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).init();

        rmqHelper.getRabbitMqPropertiesMap().get(protocol).setExchangeName(exchangeName);
        rmqHelper.getRabbitMqPropertiesMap().get(protocol).init();

    }

    @Test public void sendNormal() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        map.put("test", "test");
        messageService.send(map, map, msgService);
    }

    @Test public void testSingleSuccessfulEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/test/resources/EiffelActivityFinishedEvent.json"));
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        SendResult result = messageService.send(body, msgService, "test", null, null);
        String Expected="[{\"id\":\"0238a8bd-9bdf-4161-aeff-b00eccf92983\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }

    @Test public void testSingleFailedEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/test/resources/Invalid_EiffelActivityFinishedEvent.json"));
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        JsonArray jarray = new JsonArray();
        SendResult result = messageService.send(body, msgService, "test", null, null);
        String Expected="[{\"id\":null,\"status_code\":400,\"result\":\"Bad Request\",\"message\":\"Invalid event content, client need to fix problem in event before submitting again\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }

    @Test public void testMultipleFailedEvents() throws Exception {
        String body = FileUtils.readFileToString(new File("src/test/resources/MultipleInvalidEvents.json"));
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        SendResult result = messageService.send(body, msgService, "test", null, null);
        Assert.assertNotNull(result);
        String Expected="[{\"id\":null,\"status_code\":400,\"result\":\"Bad Request\",\"message\":\"Invalid event content, client need to fix problem in event before submitting again\"},{\"id\":null,\"status_code\":503,\"result\":\"Service Unavailable\",\"message\":\"Please check previous event and try again later\"},{\"id\":null,\"status_code\":503,\"result\":\"Service Unavailable\",\"message\":\"Please check previous event and try again later\"}]";
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());
    }

    @Test public void testMultipleSuccessfulEvents() throws Exception {
        String body = FileUtils.readFileToString(new File("src/test/resources/MultipleValidEvents.json"));
        String Expected="[{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a1\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"},{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a2\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"},{\"id\":\"9cdd0f68-df85-44b0-88bd-fc4163ac90a3\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
        JsonArray jarray = new JsonArray();
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        SendResult result = messageService.send(body, msgService, "test", null, null);
        Assert.assertNotNull(result);
        for (PublishResultItem results : result.getEvents()) {
            jarray.add(results.toJsonObject());
        }
        assertEquals(Expected, jarray.toString());       
    }

    @Test
    public void testRabbitMQConnection() {
        try {
            if(rmqHelper.getRabbitMqPropertiesMap().get(protocol) != null) {
                rmqHelper.getRabbitMqPropertiesMap().get(protocol).createRabbitMqConnection();
                MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
                rmqHelper.send("eiffelxxx", "Test message", msgService);
                assertTrue(rmqHelper.getRabbitMqPropertiesMap().get(protocol).getRabbitConnection().isOpen());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            fail(e.getMessage().toString());
        }
    }

    @Test
    public void testRoutingKey() throws Exception {
        MsgService msgService = PublishUtils.getMessageService(protocol, msgServices);
        String routingKey;
        if (msgService != null) {
            File file = new File("src/test/resources/EiffelActivityFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            routingKey = PublishUtils.getRoutingKey(msgService, json.getAsJsonObject(), rmqHelper, "fem001", null, null);
            if(routingKey != null) {
                assertEquals("eiffel.activity.finished.notag.eiffeltest.fem001", routingKey);
            }
        }
    }
}
