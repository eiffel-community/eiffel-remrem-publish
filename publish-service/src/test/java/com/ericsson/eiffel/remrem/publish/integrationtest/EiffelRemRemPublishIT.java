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
package com.ericsson.eiffel.remrem.publish.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.PublishResultItem;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;

@ActiveProfiles("integration-test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class EiffelRemRemPublishIT {
    @Value("${local.server.port}")
    int port;
    @Autowired
    private MsgService[] msgServices;
    @Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;
    private String credentials = "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes());
    private String domainId= "True";
    private String exchangeName= "EN1";
    private String host= "HostA";
    private String protocol = "eiffelsemantics";
    @Autowired
    RMQHelper rmqHelper;

    @Before
    public void setUp() throws RemRemPublishException {
        RestAssured.port = port;
        rmqHelper.rabbitMqPropertiesInit(protocol);
        RabbitMqProperties rabbitmqProtocolProperties = rmqHelper.getRabbitMqPropertiesMap().get(protocol);
        rabbitmqProtocolProperties.setHost(host);
        rabbitmqProtocolProperties.setExchangeName(exchangeName);
        rabbitmqProtocolProperties.setDomainId(domainId);
    }

    @Test
    public void testJsonOutput() throws Exception {
        MsgService msgService = PublishUtils.getMessageService("eiffel3", msgServices);
        if (msgService != null) {
            JsonArray jarray = new JsonArray();
            String jsonString = FileUtils.readFileToString(new File("src/test/resources/EiffelJobFinishedEvent.json"));
            SendResult results = messageService.send(jsonString, msgService, "fem001", null, null);
            for (PublishResultItem result : results.getEvents()) {
                jarray.add(result.toJsonObject());
            }
            String jsonArray = "[{\"id\":\"2930b40e-79dc-453c-b178-bc2bbde593ba\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
            assertEquals(jsonArray, jarray.toString());
        }
    }
    
    @Test
    public void testFailSingleEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/test/resources/Invalid_EiffelActivityFinishedEvent.json"));

        given().header("Authorization", credentials)
               .contentType("application/json").body(body).when().post("/producer/msg?mp=eiffelsemantics").then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body("events[0].status_code", Matchers.equalTo(400))
               .body("events[0].result", Matchers.equalTo(PropertiesConfig.INVALID_MESSAGE)).body("events[0].message", Matchers
                        .equalTo("Invalid event content, client need to fix problem in event before submitting again"));
    }

    @Test
    public void testGenerateRoutingKey() throws Exception {
        MsgService messageService = PublishUtils.getMessageService("", msgServices);
        if (messageService != null) {
            File file = new File("src/test/resources/EiffelActivityFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            String routingKey = messageService.generateRoutingKey(json.getAsJsonObject(), null, null, null);
            assertEquals("eiffel.activity.EiffelActivityFinishedEvent.notag.eiffeltest", routingKey);
        }
    }
 }