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
    public void testEventParserWithEventEiffelActivityFinishedEvent() {
        try {
            testParser("EiffelActivityFinishedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelSourceChangeCreatedEvent() {
        try {
            testParser("EiffelSourceChangeCreatedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelSourceChangeSubmittedEvent() {
        try {
            testParser("EiffelSourceChangeSubmittedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }



    @Test
    public void testEventParserWithEventEiffelActivityTriggeredEvent() {
        try {
            testParser("EiffelActivityTriggeredEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelActivityStartedEvent() {
        try {
            testParser("EiffelActivityStartedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelActivityCanceledEvent() {
        try {
            testParser("EiffelActivityCanceledEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelArtifactCreatedEvent() {
        try {
            testParser("EiffelArtifactCreatedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseTriggeredEvent() {
        try {
            testParser("EiffelTestCaseTriggeredEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseStartedEvent() {
        try {
            testParser("EiffelTestCaseStartedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelTestCaseFinishedEvent() {
        try {
            testParser("EiffelTestCaseFinishedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelConfidenceLevelModifiedEvent() {
        try {
            testParser("EiffelConfidenceLevelModifiedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelAnnouncementPublishedEvent() {
        try {
            testParser("EiffelAnnouncementPublishedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }



    @Test
    public void testEventParserWithEventEiffelCompositionDefinedEvent() {
        try {
            testParser("EiffelCompositionDefinedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelArtifactReusedEvent() {
        try {
            testParser("EiffelArtifactReusedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelEnvironmentDefinedEvent() {
        try {
            testParser("EiffelEnvironmentDefinedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelFlowContextDefinedEvent() {
        try {
            testParser("EiffelFlowContextDefinedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelIssueVerifiedEvent() {
        try {
            testParser("EiffelIssueVerifiedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelServiceAllocatedEvent() {
        try {
            testParser("EiffelServiceAllocatedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelTestCaseCanceledEvent() {
        try {
            testParser("EiffelTestCaseCanceledEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelTestExecutionRecipeCollectionCreatedEvent() {
        try {
            testParser("EiffelTestExecutionRecipeCollectionCreatedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelTestSuiteFinishedEvent() {
        try {
            testParser("EiffelTestSuiteFinishedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelTestSuiteStartedEvent() {
        try {
            testParser("EiffelTestSuiteStartedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelAlertAcknowledgedEvent() {
        try {
            testParser("EiffelAlertAcknowledgedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelAlertCeasedEvent() {
        try {
            testParser("EiffelAlertCeasedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelAlertRaisedEvent() {
        try {
            testParser("EiffelAlertRaisedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelArtifactDeployedEvent() {
        try {
            testParser("EiffelArtifactDeployedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelServiceDeployedEvent() {
        try {
            testParser("EiffelServiceDeployedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @Test
    public void testEventParserWithEventEiffelServiceDiscontinuedEvent() {
        try {
            testParser("EiffelServiceDiscontinuedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelServiceReturnedEvent() {
        try {
            testParser("EiffelServiceReturnedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelServiceStartedEvent() {
        try {
            testParser("EiffelServiceStartedEvent");
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }


    @Test
    public void testEventParserWithEventEiffelServiceStoppedEvent() {
        try {
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
