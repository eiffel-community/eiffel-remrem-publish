/*
    Copyright 2018 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.ericsson.eiffel.remrem.publish.controller;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ericsson.eiffel.remrem.protocol.MsgService;

@RunWith(SpringRunner.class)
public class EiffelRemremCommonControllerUnitTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    ProducerController unit = new ProducerController();

    @Mock
    MsgService service;

    @Mock
    MsgService service2;

    @Mock
    MsgService msgService;

    @Mock
    MessageService messageService;

    @Mock
    RMQHelper rmqHelper;

    SendResult res = mock(SendResult.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResponseEntity responseOK = new ResponseEntity("ok", HttpStatus.OK);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResponseEntity responseBad = new ResponseEntity("ok", HttpStatus.BAD_REQUEST);

    @Mock
    JsonElement body;

    private MsgService[] msgServices = new MsgService[2];

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        unit.setRestTemplate(restTemplate);

        File file = new File("src/integration-test/resources/EiffelActivityFinishedEvent.json");
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(new FileReader(file)).getAsJsonObject();

        msgServices[0] = service;
        msgServices[1] = service2;
        unit.setMsgServices(msgServices);

        when(service.getServiceName()).thenReturn("eiffelsemantics");
        when(service2.getServiceName()).thenReturn("eiffelsemantics");
        when(messageService.getHttpStatus()).thenReturn(HttpStatus.OK);

        when(messageService.send(Matchers.anyString(), Matchers.any(MsgService.class), Matchers.anyString(),
                Matchers.anyString(), Matchers.anyString())).thenReturn(res);

        when(body.getAsJsonObject()).thenReturn(json);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRestTemplateCallSuccess() throws Exception {
        String correctURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(correctURL), Mockito.<HttpEntity<String>>any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseOK);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffelsemantics", "eiffelactivityfinished", "", "", "",
                body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.OK);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRestTemplateCallFail() throws Exception {

        String inCorrectURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(inCorrectURL), Mockito.<HttpEntity<String>>any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseBad);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffel3", "eiffelactivityfinished", "", "", "",
                body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void testURLTemplate() throws Exception {

        String mp = "eiffeldemantics";
        String msgType = "eiffelactivityfinished";

        Map<String, String> map = new HashMap<>();
        map.put("mp", mp);
        map.put("msgType", msgType);

        String generateServerHost = "localhost";
        String generateServerPort = "8987";
        String generateServerAppName = "generate";
        String correctURL = "http://{generateServerHost}:{generateServerPort}/{generateServerAppName}/{mp}?msgType={msgType}";

        GenerateURLTemplate urlT = new GenerateURLTemplate(mp, msgType, generateServerHost, generateServerPort, generateServerAppName);
        Map<String, String> mapTest = urlT.getMap();

        assertEquals(mapTest.get("mp"), map.get("mp"));
        assertEquals(mapTest.get("msgType"), map.get("msgType"));

        assertEquals(urlT.getUrl(), correctURL);

    }

}
