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
            String jsonArray = "[{\"id\":\"4ce1e9e1-21c4-458f-b8d1-ef26b82a5634\",\"statusCode\":200,\"result\":\"SUCCESS\",\"message\":\"Event sent successfully\"}]";
            assertEquals(jsonArray, jarray.toString());
        }
    }
    
    
    @Test
    public void testSingleSuccessfulEvent() throws Exception {
        String body = "{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a0','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";

        given().header("Authorization", credentials)
        .contentType("application/json").body(body).when().post("/producer/msg").then()
                .statusCode(HttpStatus.SC_OK)
                .body("events[0].id", Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a0"))
                .body("events[0].status_code", Matchers.equalTo(200)).body("events[0].result", Matchers.equalTo(PropertiesConfig.SUCCESS))
                .body("events[0].message", Matchers.equalTo("Event sent successfully"));
    }
    
    //This test case works when the RabbitMQ is down.
    /*@Test
    public void testServerForSingleEvent() throws Exception {
        String body = "{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a0','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";

        given().header("Authorization", credentials).contentType("application/json").body(body).when().post("/producer/msg").then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body("events[0].id", Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a0"))
                .body("events[0].status_code", Matchers.equalTo(500)).body("events[0].result", Matchers.equalTo("Internal Server Error"))
                .body("events[0].message", Matchers.equalTo("Possible to try again later when server is up"));
    }*/
    
    @Test
    public void testFailSingleEvent() throws Exception {
        String body = "{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";

        given()
                 .header("Authorization", credentials)
                .contentType("application/json").body(body).when().post("/producer/msg").then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("events[0].id", Matchers.equalTo(null))
                .body("events[0].status_code", Matchers.equalTo(400))
                .body("events[0].result", Matchers.equalTo("Bad Request")).body("events[0].message", Matchers
                        .equalTo("Invalid event content, client need to fix problem in event before submitting again"));
    }
    
    @Test
    public void testSendMultipleEvents() throws Exception {
        String body = "[{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a1','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a3','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}]";
        given().header("Authorization", credentials)
        .contentType("application/json")

        .body(body).when().post("/producer/msg").then().statusCode(HttpStatus.SC_OK).body("events[0].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a1"), "events[0].status_code",
                Matchers.equalTo(200), "events[0].result", Matchers.equalTo(PropertiesConfig.SUCCESS), "events[0].message",
                Matchers.equalTo("Event sent successfully"), "events[1].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a2"), "events[1].status_code",
                Matchers.equalTo(200), "events[1].result", Matchers.equalTo(PropertiesConfig.SUCCESS), "events[1].message",
                Matchers.equalTo("Event sent successfully"), "events[2].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a3"), "events[2].status_code",
                Matchers.equalTo(200), "events[2].result", Matchers.equalTo(PropertiesConfig.SUCCESS), "events[2].message",
                Matchers.equalTo("Event sent successfully"));
    }
    
    //This test case will work when the RabbitMQ is down
    /*@Test
    public void testMultipleRMQDownEvents() throws Exception {
        String body = "[{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a1','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a3','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}]";
        given().header("Authorization", credentials)
        .contentType("application/json")

        .body(body).when().post("/producer/msg").then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("events[0].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a1"), "events[0].status_code",
                Matchers.equalTo(500), "events[0].result", Matchers.equalTo("Internal Server Error"), "events[0].message",
                Matchers.equalTo("Possible to try again later when server is up"), "events[1].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a2"), "events[1].status_code",
                Matchers.equalTo(500), "events[1].result", Matchers.equalTo("Internal Server Error"), "events[1].message",
                Matchers.equalTo("Possible to try again later when server is up"), "events[2].id",
                Matchers.equalTo("9cdd0f68-df85-44b0-88bd-fc4163ac90a3"), "events[2].status_code",
                Matchers.equalTo(500), "events[2].result", Matchers.equalTo("Internal Server Error"), "events[2].message",
                Matchers.equalTo("Possible to try again later when server is up"));
    }*/
    
    @Test
    public void testFailMultipleEvents() throws Exception {
        String body = "[{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','optionalParameters':{}}}}}]";
        given().header("Authorization", credentials)
        .contentType("application/json")

        .body(body).when().post("/producer/msg?mp=eiffel3").then().statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("events[0].id",Matchers.equalTo(null), "events[0].status_code",
                Matchers.equalTo(400), "events[0].result", Matchers.equalTo("Bad Request"), "events[0].message",
                Matchers.equalTo("Invalid event content, client need to fix problem in event before submitting again"), "events[1].id",
                Matchers.equalTo(null), "events[1].status_code",
                Matchers.equalTo(400), "events[1].result", Matchers.equalTo("Bad Request"), "events[1].message",
                Matchers.equalTo("Invalid event content, client need to fix problem in event before submitting again"), "events[2].id",
                Matchers.equalTo(null), "events[2].status_code",
                Matchers.equalTo(400), "events[2].result", Matchers.equalTo("Bad Request"), "events[2].message",
                Matchers.equalTo("Invalid event content, client need to fix problem in event before submitting again"));
    }
    
    @Test
    public void sendMultipleInvalidEvents() throws Exception {
        String body = "[{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a3','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}]";
        given().header("Authorization", credentials).contentType("application/json").body(body).when()
                .post("/producer/msg").then().statusCode(HttpStatus.SC_MULTI_STATUS).body("events[0].status_code",
                        Matchers.equalTo(HttpStatus.SC_BAD_REQUEST), "events[1].status_code",
                        Matchers.equalTo(HttpStatus.SC_OK), "events[2].status_code",
                        Matchers.equalTo(HttpStatus.SC_OK));
    }

    //This will work when the RabbitMQ is down
    /*@Test
    public void sendServiceUnavailable() throws Exception {
        String body = "[{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventId':'c597aa70-8733-4930-a425-ea8992ae6c6f','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventId':'c597aa70-8733-4930-a425-ea8992ae6c6f','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventId':'50acee77-24ef-45d1-9ec4-2b4453cd6387','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventId':'50acee77-24ef-45d1-9ec4-2b4453cd6387','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventId':'14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventId':'14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','optionalParameters':{}}}}}]";
        given().header("Authorization", credentials).contentType("application/json").body(body).when()
                .post("/producer/msg?mp=eiffel3").then().statusCode(HttpStatus.SC_SERVICE_UNAVAILABLE)
                .body("events[0].status_code", Matchers.equalTo(HttpStatus.SC_BAD_REQUEST), "events[1].status_code",
                        Matchers.equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR), "events[2].status_code",
                        Matchers.equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    }*/
}
