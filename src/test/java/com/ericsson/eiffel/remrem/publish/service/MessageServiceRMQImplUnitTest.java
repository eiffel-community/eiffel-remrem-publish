package com.ericsson.eiffel.remrem.publish.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;

import test.config.FakeConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=FakeConfig.class)
public class MessageServiceRMQImplUnitTest {
    @InjectMocks
    MessageServiceRMQImpl unit = new MessageServiceRMQImpl();

    @Mock
    RMQHelper rmqHelper;



    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void sendNormal() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "test");
        unit.send(map, map);
    }

    @Test() public void sendWithException() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "test");
        unit.send(map, map);
        Mockito.doThrow(new IOException()).when(rmqHelper).send("test", "test");
        unit.send(map, map);
    }

}
