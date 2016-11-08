package com.ericsson.eiffel.remrem.publish.controller;

import com.jayway.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProducerControllerIntegrationTest {
    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Test public void sendSingle() throws Exception {
        given().contentType("application/json").body("[\"test\"]").
        when().
            post("/producer/msg?rk=test").
            then().
            statusCode(HttpStatus.SC_OK).
            body("[0]", Matchers.equalToIgnoringCase("succeed"));
    }

    @Test public void sendMultiple() throws Exception {
        given().contentType("application/json").body("[\"test1\", \"test2\", \"test3\" ]").
            when().
            post("/producer/msg?rk=test").
            then().
            
            statusCode(HttpStatus.SC_OK).
            body("[0]", Matchers.equalToIgnoringCase("succeed")).
            body("[1]", Matchers.equalToIgnoringCase("succeed")).
            body("[2]", Matchers.equalToIgnoringCase("succeed")).
            body("[3]", Matchers.nullValue());
    }

}
