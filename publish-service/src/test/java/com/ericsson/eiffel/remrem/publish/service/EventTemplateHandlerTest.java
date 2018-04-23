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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
public class EventTemplateHandlerTest {

    private static Logger log = LoggerFactory.getLogger(EventTemplateHandlerTest.class);
    static private final String inputFilePathData = "src/test/resources/testDataForParsingEvents/";
    static private final String inputFilePathExpectedData = "src/test/resources/expectedParsedEvents/";

    @Test
    public void testEventParserWithEvents() {
        try {
            testParser("EiffelActivityFinishedEvent");
            testParser("EiffelSourceChangeCreatedEvent");
            testParser("EiffelSourceChangeSubmittedEvent");
            testParser("EiffelCompositionDefinedEvent");
            testParser("EiffelActivityTriggeredEvent");
            testParser("EiffelActivityStartedEvent");
            testParser("EiffelActivityCanceledEvent");
            testParser("EiffelArtifactCreatedEvent");
            testParser("EiffelTestCaseTriggeredEvent");
            testParser("EiffelTestCaseStartedEvent");
            testParser("EiffelTestCaseFinishedEvent");
            testParser("EiffelConfidenceLevelModifiedEvent");
            testParser("EiffelAnnouncementPublishedEvent");
            testParser("EiffelArtifactReusedEvent");
            testParser("EiffelEnvironmentDefinedEvent");
            testParser("EiffelFlowContextDefinedEvent");
            testParser("EiffelIssueVerifiedEvent");
            testParser("EiffelTestCaseCanceledEvent");
            testParser("EiffelTestExecutionRecipeCollectionCreatedEvent");
            testParser("EiffelTestSuiteFinishedEvent");
            testParser("EiffelTestSuiteStartedEvent");
            testParser("EiffelAlertAcknowledgedEvent");
            testParser("EiffelAlertCeasedEvent");
            testParser("EiffelAlertRaisedEvent");
            testParser("EiffelArtifactDeployedEvent");
            testParser("EiffelServiceAllocatedEvent");
            testParser("EiffelServiceDeployedEvent");
            testParser("EiffelServiceDiscontinuedEvent");
            testParser("EiffelServiceReturnedEvent");
            testParser("EiffelServiceStartedEvent");
            testParser("EiffelServiceStoppedEvent");

        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }

    }

    public void testParser(String EventName) {
        try {
            EventTemplateHandler eventTemplateHandler = new EventTemplateHandler();
            String dataToBeParsed = FileUtils.readFileToString(new File(inputFilePathData+"test_data_for_parsing_"+EventName+".json"), "UTF-8");
            String expectedDocument = FileUtils.readFileToString(new File(inputFilePathExpectedData+"expected_parsed_"+EventName+".json"), "UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedJson = mapper.readTree(expectedDocument);

            JsonNode actualParsedEventJson = eventTemplateHandler.eventTemplateParser(dataToBeParsed, EventName);

            System.out.println("expectedJsonString: " + expectedJson.toString());
            System.out.println("actualParsedEventJson: " + actualParsedEventJson.toString());

            JSONAssert.assertEquals(expectedJson.toString(), actualParsedEventJson.toString(), true);

        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }

    }



}
