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
    static private final String inputFilePathData = "src/test/resources/";
    static private final String inputFilePathExpectedData = "src/test/resources/";

    @Test
    public void testParseEiffelActivityFinishedEvent() {
        try {
            String EventName = "EiffelActivityFinishedEvent";
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



    @Test
    public void testParseEiffelSourceChangeCreatedEvent() {
        try {
            String EventName = "EiffelSourceChangeCreatedEvent";
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


    @Test
    public void testParseEiffelSourceChangeSubmittedEvent() {
        try {
            String EventName = "EiffelSourceChangeSubmittedEvent";
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



    @Test
    public void testParseEiffelCompositionDefinedEvent() {
        try {
            String EventName = "EiffelCompositionDefinedEvent";
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


    @Test
    public void testParseEiffelActivityTriggeredEvent() {
        try {
            String EventName = "EiffelActivityTriggeredEvent";
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



    @Test
    public void testParseEiffelActivityStartedEvent() {
        try {
            String EventName = "EiffelActivityStartedEvent";
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


    @Test
    public void testParseEiffelActivityCanceledEvent() {
        try {
            String EventName = "EiffelActivityCanceledEvent";
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



    @Test
    public void testParseEiffelArtifactCreatedEvent() {
        try {
            String EventName = "EiffelArtifactCreatedEvent";
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



    @Test
    public void testParseEiffelTestCaseTriggeredEvent() {
        try {
            String EventName = "EiffelTestCaseTriggeredEvent";
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




    @Test
    public void testParseEiffelTestCaseStartedEvent() {
        try {
            String EventName = "EiffelTestCaseStartedEvent";
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


    @Test
    public void testParseEiffelTestCaseFinishedEvent() {
        try {
            String EventName = "EiffelTestCaseFinishedEvent";
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


    @Test
    public void testParseEiffelConfidenceLevelModifiedEvent() {
        try {
            String EventName = "EiffelConfidenceLevelModifiedEvent";
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


    @Test
    public void testParseEiffelAnnouncementPublishedEvent() {
        try {
            String EventName = "EiffelAnnouncementPublishedEvent";
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



    @Test
    public void testParseEiffelArtifactReusedEvent() {
        try {
            String EventName = "EiffelArtifactReusedEvent";
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


    @Test
    public void testParseEiffelEnvironmentDefinedEvent() {
        try {
            String EventName = "EiffelEnvironmentDefinedEvent";
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



    @Test
    public void testParseEiffelFlowContextDefinedEvent() {
        try {
            String EventName = "EiffelFlowContextDefinedEvent";
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

    @Test
    public void testParseEiffelIssueVerifiedEvent() {
        try {
            String EventName = "EiffelIssueVerifiedEvent";
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


    @Test
    public void testParseEiffelTestCaseCanceledEvent() {
        try {
            String EventName = "EiffelTestCaseCanceledEvent";
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


    @Test
    public void testParseEiffelTestExecutionRecipeCollectionCreatedEvent() {
        try {
            String EventName = "EiffelTestExecutionRecipeCollectionCreatedEvent";
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


    @Test
    public void testParseEiffelTestSuiteFinishedEvent() {
        try {
            String EventName = "EiffelTestSuiteFinishedEvent";
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


    @Test
    public void testParseEiffelTestSuiteStartedEvent() {
        try {
            String EventName = "EiffelTestSuiteStartedEvent";
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



    @Test
    public void testParseEiffelAlertAcknowledgedEvent() {
        try {
            String EventName = "EiffelAlertAcknowledgedEvent";
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


    @Test
    public void testParseEiffelAlertCeasedEvent() {
        try {
            String EventName = "EiffelAlertCeasedEvent";
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


    @Test
    public void testParseEiffelAlertRaisedEvent() {
        try {
            String EventName = "EiffelAlertRaisedEvent";
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

    @Test
    public void testParseEiffelArtifactDeployedEvent() {
        try {
            String EventName = "EiffelArtifactDeployedEvent";
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

    @Test
    public void testParseEiffelServiceAllocatedEvent() {
        try {
            String EventName = "EiffelServiceAllocatedEvent";
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


    @Test
    public void testParseEiffelServiceDeployedEvent() {
        try {
            String EventName = "EiffelServiceDeployedEvent";
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


    @Test
    public void testParseEiffelServiceDiscontinuedEvent() {
        try {
            String EventName = "EiffelServiceDiscontinuedEvent";
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


    @Test
    public void testParseEiffelServiceReturnedEvent() {
        try {
            String EventName = "EiffelServiceReturnedEvent";
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


    @Test
    public void testParseEiffelServiceStartedEvent() {
        try {
            String EventName = "EiffelServiceStartedEvent";
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

    @Test
    public void testParseEiffelServiceStoppedEvent() {
        try {
            String EventName = "EiffelServiceStoppedEvent";
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
