package com.ericsson.eiffel.remrem.publish.controller;

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
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
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
public class ProducerControllerIntegrationTest {
    @Value("${local.server.port}")
    int port;
    @Autowired
    private MsgService[] msgServices;
    @Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;
    private String credentials = "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes());

    @Before
    public void setUp() {
        RestAssured.port = port;
    }
    
    @Test
    public void testGetFamily() throws Exception {
        MsgService messageService = PublishUtils.getMessageService("eiffel3", msgServices);
        if (messageService != null) {
            File file = new File("src/integration-test/resources/EiffelJobFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            String family = messageService.getFamily(json.getAsJsonObject());
            assertEquals("job", family);
        }
    }

    @Test
    public void testJsonOutput() throws Exception {
        MsgService msgService = PublishUtils.getMessageService("eiffel3", msgServices);
        if (msgService != null) {
            JsonArray jarray = new JsonArray();
            String jsonString = FileUtils.readFileToString(new File("src/integration-test/resources/EiffelJobFinishedEvent.json"));
            SendResult results = messageService.send(jsonString, msgService, "fem001");
            for (PublishResultItem result : results.getEvents()) {
                jarray.add(result.toJsonObject());
            }
            String jsonArray = "[{\"id\":\"2930b40e-79dc-453c-b178-bc2bbde593ba\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
            assertEquals(jsonArray, jarray.toString());
        }
    }
    
    @Test
    public void testFailSingleEvent() throws Exception {
        String body = FileUtils.readFileToString(new File("src/integration-test/resources/Invalid_EiffelActivityFinishedEvent.json"));

        given().header("Authorization", credentials)
               .contentType("application/json").body(body).when().post("/producer/msg").then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body("events[0].status_code", Matchers.equalTo(400))
               .body("events[0].result", Matchers.equalTo(PropertiesConfig.INVALID_MESSAGE)).body("events[0].message", Matchers
                        .equalTo("Invalid event content, client need to fix problem in event before submitting again"));
    }

    @Test
    public void testGetFamilyRoutingKey() throws Exception {
        MsgService messageService = PublishUtils.getMessageService("", msgServices);
        if (messageService != null) {
            File file = new File("src/integration-test/resources/EiffelActivityFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            String family = messageService.getFamily(json.getAsJsonObject());
            assertEquals("activity", family);
        }
    }

    @Test
    public void testGetTypeRoutingKey() throws Exception {
        MsgService messageService = PublishUtils.getMessageService("", msgServices);
        if (messageService != null) {
            File file = new File("src/integration-test/resources/EiffelActivityFinishedEvent.json");
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(new FileReader(file)).getAsJsonObject();
            String type = messageService.getType(json.getAsJsonObject());
            assertEquals("finished", type);
        }
    }
 }