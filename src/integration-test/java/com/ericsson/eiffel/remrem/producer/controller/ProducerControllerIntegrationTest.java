package com.ericsson.eiffel.remrem.producer.controller;

import com.ericsson.eiffel.remrem.producer.App;
import com.jayway.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebIntegrationTest({"server.port=0", "management.port=0"})
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
