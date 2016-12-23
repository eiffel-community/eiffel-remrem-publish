package com.ericsson.eiffel.remrem.publish.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.cli.CliOptions;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class MessageServiceRMQImplUnitTest {

    @Autowired
    private MsgService msgServices[];
    
    @Autowired
    @Qualifier("messageServiceRMQImpl")
    MessageService messageService;
    
    @Autowired @Qualifier("rmqHelper") RMQHelper rmqHelper;
    
    @Test public void sendNormal() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "test");
        messageService.send(map, map);
    }
    
    @Test public void testSingleSuccessfulEvent() throws Exception {
        String body ="{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a0','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";
        
        MsgService msgService = PublishUtils.getMessageService(null, msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        Assert.assertEquals("9cdd0f68-df85-44b0-88bd-fc4163ac90a0", result.getEvents().get(0).getId());
        Assert.assertEquals(200, result.getEvents().get(0).getStatusCode());
        Assert.assertEquals("SUCCESS", result.getEvents().get(0).getResult());
        Assert.assertEquals("Event sent successfully", result.getEvents().get(0).getMessage());
    }
    
    @Test public void testSingleFailedEvent() throws Exception {
        String body ="{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}";
        
        MsgService msgService = PublishUtils.getMessageService(null, msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        Assert.assertEquals(null, result.getEvents().get(0).getId());
        Assert.assertEquals(400, result.getEvents().get(0).getStatusCode());
        Assert.assertEquals("Bad Request", result.getEvents().get(0).getResult());
        Assert.assertEquals("Invalid event content, client need to fix problem in event before submitting again", result.getEvents().get(0).getMessage());
    }
    @Test public void testMultipleFailedEvent() throws Exception {
        String body ="[{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:08.464Z','eventType':'EiffelJobFinishedEvent','inputEventIds':['c597aa70-8733-4930-a425-ea8992ae6c6f'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'resultCode':'SUCCESS','logReferences':{},'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/10/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:07.457Z','eventType':'EiffelJobStartedEvent','inputEventIds':['50acee77-24ef-45d1-9ec4-2b4453cd6387','14d38a89-ca6d-4d06-acdc-5a2cfb8c52f7'],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','jobExecutionNumber':10,'optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.558Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'e71c530b-97e1-42fa-b1bb-ce01bdd53f34','optionalParameters':{}}}}},{'eiffelMessageVersions':{'3.21.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','triggerCause':{'type':'MANUAL','description':'Started by user anonymous'},'logReferences':{},'flowContext':'','optionalParameters':{}},'eventSource':{'hostName':'SE00208242','pid':10344,'name':'testComponent','url':'http://localhost:8080/job/IMPMessage/'}},'2.3.39.0.5':{'domainId':'TCS','eventTime':'2016-09-28T11:51:06.780Z','eventType':'EiffelJobQueuedEvent','inputEventIds':[],'eventData':{'jobInstance':'IMPMessage','jobExecutionId':'f011f05e-e371-45aa-961a-22fc585b4bc2','optionalParameters':{}}}}}]";
        
        MsgService msgService = PublishUtils.getMessageService(null, msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        
        Assert.assertEquals(400, result.getEvents().get(0).getStatusCode());
        Assert.assertEquals("Bad Request", result.getEvents().get(0).getResult());
        Assert.assertEquals("Invalid event content, client need to fix problem in event before submitting again", result.getEvents().get(0).getMessage());
       
        Assert.assertEquals(503, result.getEvents().get(1).getStatusCode());
        Assert.assertEquals("Service Unavailable", result.getEvents().get(1).getResult());
        Assert.assertEquals("Please check previous event and try again later", result.getEvents().get(1).getMessage());
       
        Assert.assertEquals(503, result.getEvents().get(2).getStatusCode());
        Assert.assertEquals("Service Unavailable", result.getEvents().get(2).getResult());
        Assert.assertEquals("Please check previous event and try again later", result.getEvents().get(2).getMessage());
        
        Assert.assertEquals(503, result.getEvents().get(3).getStatusCode());
        Assert.assertEquals("Service Unavailable", result.getEvents().get(3).getResult());
        Assert.assertEquals("Please check previous event and try again later", result.getEvents().get(3).getMessage());
    }
    @Test public void testMultipleSuccessfulEvent() throws Exception {
        String body ="[{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a1','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a3','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}]";
        
        MsgService msgService = PublishUtils.getMessageService(null, msgServices);
        SendResult result = messageService.send(body, msgService, "test");
        Assert.assertNotNull(result);
        Assert.assertEquals("9cdd0f68-df85-44b0-88bd-fc4163ac90a1", result.getEvents().get(0).getId());
        Assert.assertEquals(200, result.getEvents().get(0).getStatusCode());
        Assert.assertEquals("SUCCESS", result.getEvents().get(0).getResult());
        Assert.assertEquals("Event sent successfully", result.getEvents().get(0).getMessage());
        Assert.assertEquals("9cdd0f68-df85-44b0-88bd-fc4163ac90a2", result.getEvents().get(1).getId());
        Assert.assertEquals(200, result.getEvents().get(1).getStatusCode());
        Assert.assertEquals("SUCCESS", result.getEvents().get(1).getResult());
        Assert.assertEquals("Event sent successfully", result.getEvents().get(1).getMessage());
        Assert.assertEquals("9cdd0f68-df85-44b0-88bd-fc4163ac90a3", result.getEvents().get(2).getId());
        Assert.assertEquals(200, result.getEvents().get(2).getStatusCode());
        Assert.assertEquals("SUCCESS", result.getEvents().get(2).getResult());
        Assert.assertEquals("Event sent successfully", result.getEvents().get(2).getMessage());
       
    }
}
