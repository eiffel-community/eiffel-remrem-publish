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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
public class EventTemplateHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventTemplateHandlerTest.class);
    private static final String INPUT_FILE_PATH_DATA = "src/test/resources/testDataForParsingEvents/";
    private static final String INPUT_FILE_PATH_EXPECTED_DATA = "src/test/resources/expectedParsedEvents/";


    @Test
    public void testEventParserWithEventEiffelActivityFinishedEvent() throws Exception {
            testParser("EiffelActivityFinishedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelSourceChangeCreatedEvent() throws Exception {
            testParser("EiffelSourceChangeCreatedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelSourceChangeSubmittedEvent() throws Exception {
        testParser("EiffelSourceChangeSubmittedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelActivityTriggeredEvent() throws Exception {
        testParser("EiffelActivityTriggeredEvent");
    }


    @Test
    public void testEventParserWithEventEiffelActivityStartedEvent() throws Exception {
        testParser("EiffelActivityStartedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelActivityCanceledEvent() throws Exception {
        testParser("EiffelActivityCanceledEvent");
    }

    @Test
    public void testEventParserWithEventEiffelArtifactCreatedEvent() throws Exception {
        testParser("EiffelArtifactCreatedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseTriggeredEvent() throws Exception {
        testParser("EiffelTestCaseTriggeredEvent");
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseStartedEvent() throws Exception {
        testParser("EiffelTestCaseStartedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelTestCaseFinishedEvent() throws Exception {
        testParser("EiffelTestCaseFinishedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelConfidenceLevelModifiedEvent() throws Exception {
        testParser("EiffelConfidenceLevelModifiedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelAnnouncementPublishedEvent() throws Exception {
        testParser("EiffelAnnouncementPublishedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelCompositionDefinedEvent() throws Exception {
        testParser("EiffelCompositionDefinedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelArtifactReusedEvent() throws Exception {
        testParser("EiffelArtifactReusedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelEnvironmentDefinedEvent() throws Exception {
        testParser("EiffelEnvironmentDefinedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelFlowContextDefinedEvent() throws Exception {
        testParser("EiffelFlowContextDefinedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelIssueVerifiedEvent() throws Exception {
        testParser("EiffelIssueVerifiedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelServiceAllocatedEvent() throws Exception {
        testParser("EiffelServiceAllocatedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseCanceledEvent() throws Exception {
        testParser("EiffelTestCaseCanceledEvent");
    }

    @Test
    public void testEventParserWithEventEiffelTestExecutionRecipeCollectionCreatedEvent() throws Exception {
        testParser("EiffelTestExecutionRecipeCollectionCreatedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelTestSuiteFinishedEvent() throws Exception {
        testParser("EiffelTestSuiteFinishedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelTestSuiteStartedEvent() throws Exception {
        testParser("EiffelTestSuiteStartedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelAlertAcknowledgedEvent() throws Exception {
        testParser("EiffelAlertAcknowledgedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelAlertCeasedEvent() throws Exception {
        testParser("EiffelAlertCeasedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelAlertRaisedEvent() throws Exception {
        testParser("EiffelAlertRaisedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelArtifactDeployedEvent() throws Exception {
        testParser("EiffelArtifactDeployedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelServiceDeployedEvent() throws Exception {
        testParser("EiffelServiceDeployedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelServiceDiscontinuedEvent() throws Exception {
        testParser("EiffelServiceDiscontinuedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelServiceReturnedEvent() throws Exception {
        testParser("EiffelServiceReturnedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelServiceStartedEvent() throws Exception {
        testParser("EiffelServiceStartedEvent");
    }


    @Test
    public void testEventParserWithEventEiffelServiceStoppedEvent() throws Exception {
        testParser("EiffelServiceStoppedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelArtifactPublishedEvent() throws Exception {
        testParser("EiffelArtifactPublishedEvent");
    }

    @Test
    public void testEventParserWithEventEiffelIssueDefinedEvent() throws Exception {
        testParser("EiffelIssueDefinedEvent");
    }

    private void testParser(String EventName) throws Exception {
        EventTemplateHandler eventTemplateHandler = new EventTemplateHandler();
        String dataToBeParsed = FileUtils.readFileToString(new File(INPUT_FILE_PATH_DATA + "test_data_for_parsing_" + EventName + ".json"), "UTF-8");
        String expectedDocument = FileUtils.readFileToString(new File(INPUT_FILE_PATH_EXPECTED_DATA + "expected_parsed_" + EventName + ".json"), "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJson = mapper.readTree(expectedDocument);
        try {
            JsonNode actualParsedEventJson = eventTemplateHandler.eventTemplateParser(dataToBeParsed, EventName);
        }
        catch (Throwable e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            System.err.println(sStackTrace);
            System.err.flush();
        }
        JsonNode actualParsedEventJson = eventTemplateHandler.eventTemplateParser(dataToBeParsed, EventName);

        LOG.info("expectedJsonString:    " + expectedJson.toString());
        LOG.info("actualParsedEventJson: " + actualParsedEventJson.toString());

        JSONAssert.assertEquals(expectedJson.toString(), actualParsedEventJson.toString(), JSONCompareMode.NON_EXTENSIBLE);
        JSONAssert.assertEquals(expectedJson.toString(), actualParsedEventJson.toString(), true);
    }
}

