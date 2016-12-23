package com.ericsson.eiffel.remrem.publish.controller;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.util.Base64;

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
            String jsonString = "{'eiffelMessageVersions': { '3.21.37.0.4': { 'domainId': 'testdomain', 'eventId': '4ce1e9e1-21c4-458f-b8d1-ef26b82a5634', 'eventTime': '2016-09-01T08:23:57.894Z', 'eventType': 'EiffelJobFinishedEvent', 'inputEventIds': [], 'eventData': { 'jobInstance': 'MySuperGreatJob', 'jobExecutionId': '81e06fbc-1247-4446-9426-3381f9a1bda2', 'jobExecutionNumber': 731, 'resultCode': 'SUCCESS', 'resultDetails': { 'key': 0, 'description': 'The Result was Successful' }, 'logReferences': {}, 'flowContext': '', 'optionalParameters': {} } }, '2.3.37.0.4': { 'domainId': 'testdomain', 'eventId': '4ce1e9e1-21c4-458f-b8d1-ef26b82a5634', 'eventTime': '2016-09-01T08:23:57.894Z', 'eventType': 'EiffelJobFinishedEvent', 'inputEventIds': [], 'eventData': { 'jobInstance': 'MySuperGreatJob', 'jobExecutionId': '81e06fbc-1247-4446-9426-3381f9a1bda2', 'jobExecutionNumber': 731, 'resultCode': 'SUCCESS', 'logReferences': {}, 'optionalParameters': {} } } } }";
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(jsonString);
            String family = messageService.getFamily(json.getAsJsonObject());
            assertEquals("job", family);
        }
    }

    @Test
    public void testJsonOutput() throws Exception {
        MsgService msgService = PublishUtils.getMessageService("eiffel3", msgServices);
        if (msgService != null) {
            JsonArray jarray = new JsonArray();
            String jsonString = "{'eiffelMessageVersions': { '3.21.37.0.4': { 'domainId': 'testdomain', 'eventId': '4ce1e9e1-21c4-458f-b8d1-ef26b82a5634', 'eventTime': '2016-09-01T08:23:57.894Z', 'eventType': 'EiffelJobFinishedEvent', 'inputEventIds': [], 'eventData': { 'jobInstance': 'MySuperGreatJob', 'jobExecutionId': '81e06fbc-1247-4446-9426-3381f9a1bda2', 'jobExecutionNumber': 731, 'resultCode': 'SUCCESS', 'resultDetails': { 'key': 0, 'description': 'The Result was Successful' }, 'logReferences': {}, 'flowContext': '', 'optionalParameters': {} } }, '2.3.37.0.4': { 'domainId': 'testdomain', 'eventId': '4ce1e9e1-21c4-458f-b8d1-ef26b82a5634', 'eventTime': '2016-09-01T08:23:57.894Z', 'eventType': 'EiffelJobFinishedEvent', 'inputEventIds': [], 'eventData': { 'jobInstance': 'MySuperGreatJob', 'jobExecutionId': '81e06fbc-1247-4446-9426-3381f9a1bda2', 'jobExecutionNumber': 731, 'resultCode': 'SUCCESS', 'logReferences': {}, 'optionalParameters': {} } } } }";
            SendResult results = messageService.send(jsonString, msgService, "fem001");
            for (PublishResultItem result : results.getEvents()) {
                jarray.add(result.toJsonObject());
            }
            String jsonArray = "[{\"id\":\"4ce1e9e1-21c4-458f-b8d1-ef26b82a5634\",\"status_code\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
            assertEquals(jsonArray, jarray.toString());
        }
    }
    
    @Test
    public void testFailSingleEvent() throws Exception {
        String body = "{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";

        given().header("Authorization", credentials)
               .contentType("application/json").body(body).when().post("/producer/msg").then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body("events[0].status_code", Matchers.equalTo(400))
               .body("events[0].result", Matchers.equalTo(PropertiesConfig.INVALID_MESSAGE)).body("events[0].message", Matchers
                        .equalTo("Invalid event content, client need to fix problem in event before submitting again"));
    }
    
 }