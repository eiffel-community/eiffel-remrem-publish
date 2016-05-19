package com.ericsson.eiffel.remrem.producer.controller;

import com.ericsson.eiffel.remrem.producer.App;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebIntegrationTest({"server.port=0", "management.port=0"})
public class ProducerControllerIntegrationTest {
    @Before public void setUp() throws Exception {

    }

    @After public void tearDown() throws Exception {

    }

    @Test public void send() throws Exception {

    }

}
