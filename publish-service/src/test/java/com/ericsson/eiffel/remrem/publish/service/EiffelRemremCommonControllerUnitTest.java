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
package com.ericsson.eiffel.remrem.publish.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.service.GenerateURLTemplate;
import com.ericsson.eiffel.remrem.publish.controller.ProducerController;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@ExtendWith(SpringExtension.class)
public class EiffelRemremCommonControllerUnitTest {

    @Mock
    RestTemplate restTemplate;

    @Spy
    GenerateURLTemplate generateURLTemplate;

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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResponseEntity responseOptionsFailed = new ResponseEntity("Link specific options could not be fulfilled", HttpStatus.UNPROCESSABLE_ENTITY);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResponseEntity responseMultipleFound = new ResponseEntity("Muliple event ids found with ERLookup properties", HttpStatus.EXPECTATION_FAILED);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResponseEntity responseNoneFound = new ResponseEntity("No event id found with ERLookup properties", HttpStatus.NOT_ACCEPTABLE);

    @Mock
    JsonElement body;

    private MsgService[] msgServices = new MsgService[2];

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        unit.setRestTemplate(restTemplate);

        File file = new File("src/test/resources/EiffelActivityFinishedEvent.json");
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

        ResponseEntity<?> elem = unit.generateAndPublish("eiffelsemantics", "eiffelactivityfinished", "", "", "",false,
                null, null, true, 1, false, body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.OK);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRestTemplateCallFail() throws Exception {

        String inCorrectURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(inCorrectURL), Mockito.<HttpEntity<String>>any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseBad);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffel3", "eiffelactivityfinished", "", "", "",false,
                null, null, true, 1, false, body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void testGenerateURLTemplate() throws Exception {
        String mp = "eiffelsemantics";
        String msgType = "eiffelactivityfinished";
        String generateServerUri = null;
        String generateServerContextPath = "";

        Map<String, String> map = new HashMap<>();
        map.put("mp", mp);
        map.put("msgType", msgType);

        String correctURL = generateServerUri + "/" + generateServerContextPath + "/{mp}?msgType={msgType}";

        Map<String, String> mapTest = generateURLTemplate.getMap(mp, msgType);

        assertEquals(generateURLTemplate.getUrl(), correctURL);
        assertEquals(mapTest.get("mp"), map.get("mp"));
        assertEquals(mapTest.get("msgType"), map.get("msgType"));

        assertEquals(generateURLTemplate.getUrl(), correctURL);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testErLookupFailedWithOptions() throws Exception {
        String correctURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(correctURL), Mockito.<HttpEntity<String>> any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseOptionsFailed);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffelsemantics", "eiffelactivityfinished", "", "", "", false,
                false, false, true, 1, false, body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testErLookupFailedWithMultipleFound() throws Exception {
        String correctURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(correctURL), Mockito.<HttpEntity<String>> any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseMultipleFound);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffelsemantics", "eiffelactivityfinished", "", "", "", false,
                false, false, true, 1, false, body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.EXPECTATION_FAILED);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testErLookupFailedWithNoneFound() throws Exception {
        String correctURL = "/{mp}?msgType={msgType}";
        when(restTemplate.postForEntity(Mockito.contains(correctURL), Mockito.<HttpEntity<String>> any(),
                Mockito.eq(String.class), Mockito.anyMap())).thenReturn(responseNoneFound);

        ResponseEntity<?> elem = unit.generateAndPublish("eiffelsemantics", "eiffelactivityfinished", "", "", "", false,
                false, false, true, 1, false, body.getAsJsonObject());
        assertEquals(elem.getStatusCode(), HttpStatus.NOT_ACCEPTABLE);

    }

}
