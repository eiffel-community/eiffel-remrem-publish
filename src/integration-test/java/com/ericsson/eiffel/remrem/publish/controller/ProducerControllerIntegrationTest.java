package com.ericsson.eiffel.remrem.publish.controller;

import static com.jayway.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.ericsson.eiffel.remrem.publish.App;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageServiceRMQImpl;
import com.jayway.restassured.RestAssured;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebIntegrationTest({ "server.port=0", "management.port=0" })
public class ProducerControllerIntegrationTest {
    
    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void sendSingleInvalid() throws Exception {
        given().contentType("application/json").body("[\"test\"]").when().post("/producer/msg?rk=test").then()
                .statusCode(HttpStatus.SC_OK)
                .body("events[0].status_code", Matchers.equalTo(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    public void sendMultipleInvalid() throws Exception {
        given().contentType("application/json").body("[\"test1\", \"test2\", \"test3\" ]").when()
                .post("/producer/msg?rk=test").then().statusCode(HttpStatus.SC_OK).body("events[0].status_code",
                        Matchers.equalTo(HttpStatus.SC_BAD_REQUEST), "events[1].status_code",
                        Matchers.equalTo(HttpStatus.SC_BAD_REQUEST), "events[2].status_code",
                        Matchers.equalTo(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    public void sendSingle() throws Exception {
        given().contentType("application/json")
                .body("{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}")
                .when().post("/producer/msg?rk=test").then().statusCode(HttpStatus.SC_OK)
                .body("events[0].status_code", Matchers.equalTo(HttpStatus.SC_OK));
    }

    @Test
    public void sendMultiple() throws Exception {
        given().contentType("application/json")
                .body("[{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a1','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a2','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','id':'9cdd0f68-df85-44b0-88bd-fc4163ac90a3','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}},{'data':{'outcome':{'conclusion':'TIMED_OUT','description':'Compilation timed out.'},'persistentLogs':[{'name':'firstLog','uri':'http://myHost.com/firstLog'},{'name':'otherLog','uri':'isbn:0-486-27557-4'}]},'links':{'activityExecution':'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1','flowContext':'flowContext','causes':['cause1','cause2']},'meta':{'domainId':'example.domain','type':'eiffelactivityfinished','version':'0.1.7','time':1478780245184,'tags':['tag1','tag2'],'source':{'host':'host','name':'name','uri':'http://java.sun.com/j2se/1.3/','serializer':{'groupId':'G','artifactId':'A','version':'V'}}}}]")
                .when().post("/producer/msg?rk=test").then().statusCode(HttpStatus.SC_OK).body("events[0].status_code",
                        Matchers.equalTo(HttpStatus.SC_BAD_REQUEST), "events[1].status_code",
                        Matchers.equalTo(HttpStatus.SC_OK), "events[2].status_code", Matchers.equalTo(HttpStatus.SC_OK),
                        "events[3].status_code", Matchers.equalTo(HttpStatus.SC_OK));
    }

}
