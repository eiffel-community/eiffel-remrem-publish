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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Base64;

import static com.jayway.restassured.RestAssured.given;

@ActiveProfiles("integration-test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProducerControllerIntegrationTest {
    @Value("${local.server.port}")
    int port;

    private String credentials = "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes());

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testUnauthenticatedNotAllowed() throws Exception {
        given()
                .contentType("application/json")
                .body("[\"test\"]")
                .when()
                    .post("/producer/msg?rk=test")
                .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test public void testSendSingle() throws Exception {
        given()
                .header("Authorization", credentials)
                .contentType("application/json")
                .body("[\"test\"]")
                .when()
                    .post("/producer/msg?rk=test")
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("[0]", Matchers.equalToIgnoringCase("succeed"));
    }

    @Test public void testSendMultiple() throws Exception {
        given()
                .header("Authorization", credentials)
                .contentType("application/json")
                .body("[\"test1\", \"test2\", \"test3\" ]")
                .when()
                    .post("/producer/msg?rk=test")
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("[0]", Matchers.equalToIgnoringCase("succeed"))
                    .body("[1]", Matchers.equalToIgnoringCase("succeed"))
                    .body("[2]", Matchers.equalToIgnoringCase("succeed"))
                    .body("[3]", Matchers.nullValue());
    }

}
