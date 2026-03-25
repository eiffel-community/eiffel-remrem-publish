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

import java.util.*;

import com.ericsson.eiffel.remrem.protocol.ValidationResult;
import com.ericsson.eiffel.remrem.publish.service.*;
import com.google.gson.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.fasterxml.jackson.databind.JsonNode;

import ch.qos.logback.classic.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.ericsson.eiffel.remrem.publish.constants.RemRemPublishResponseExamples.*;
import static com.ericsson.eiffel.remrem.publish.constants.RemremPublishServiceConstants.*;

@ComponentScan("com.ericsson.eiffel.remrem")
@RestController
@RequestMapping("/*")
@Tag(name = "REMReM Publish Service", description = "REST API for publishing Eiffel messages to message bus")
public class ProducerController {

    @Autowired
    private MsgService msgServices[];

    @Autowired
    @Qualifier("messageServiceRMQImpl")
    private MessageService messageService;

    @Autowired
    private RMQHelper rmqHelper;

    @Autowired
    private GenerateURLTemplate generateURLTemplate;

    @Value("${activedirectory.publish.enabled}")
    private boolean isAuthenticationEnabled;

    @Value("${maxSizeOfInputArray:250}")
    private int maxSizeOfInputArray;

    private RestTemplate restTemplate = new RestTemplate();

    private JsonParser parser = new JsonParser();

    private Logger log = (Logger) LoggerFactory.getLogger(ProducerController.class);

    public void setMsgServices(MsgService[] msgServices) {
        this.msgServices = msgServices;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void logUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check if the user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Get the UserDetails object, which contains user information
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // Get the username of the authenticated user
            String username = userDetails.getUsername();
            log.info("User name: {} ", username);
        }
    }

    /**
     * This controller used as producer to send messages or event
     * @param msgProtocol
     *            message protocol (required)
     * @param userDomain
     *            user domain (required)
     * @param tag
     *            (not required)
     * @param routingKey
     *            (not required)
     * @param body
     *            (required)
     * @return A response entity which contains http status and result
     */
    public ResponseEntity send(final String msgProtocol, final String userDomain, final String tag,
                               final String routingKey, final JsonElement body) {
        if (isAuthenticationEnabled) {
            logUserName();
        }

        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);
        log.debug("mp: " + msgProtocol);
        log.debug("body: " + body);
        log.debug("user domain suffix: " + userDomain + " tag: " + tag + " Routing Key: "
                + routingKey);

        if (msgService != null && msgProtocol != null) {
            try {
                rmqHelper.rabbitMqPropertiesInit(msgProtocol);
            } catch (RemRemPublishException e) {
                return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), ResultStatus.FAIL);
            }
        }

        //here add check for limitation for events in array is fetched from REMReM property and checked during publishing.
        if (body.isJsonArray() && (body.getAsJsonArray().size() > maxSizeOfInputArray)) {
            return createResponseEntity(HttpStatus.BAD_REQUEST,
                "The number of events in the input array is too high: " + body.getAsJsonArray().size() + " > "
                        + maxSizeOfInputArray + "; you can modify the property 'maxSizeOfInputArray' to increase it.",
                    ResultStatus.FAIL);
        }

        SendResult result = messageService.send(body, msgService, userDomain, tag, routingKey);
        HttpStatus status = getHttpStatus(result);
        log.info("HTTP Status: {}", status.value());
        return new ResponseEntity(result, status);
    }

    private HttpStatus getHttpStatus(SendResult result) {
        List<PublishResultItem> events = result.getEvents();
        HttpStatus status;
        int nevents = events.size();
        if (nevents == 0) {
            return HttpStatus.BAD_REQUEST;
        }

        if (events.size() == 1) {
            return HttpStatus.valueOf(events.get(0).getStatusCode());
        }
        boolean allSuccessFull = events.stream().allMatch(event -> event.getStatusCode() == HttpStatus.OK.value());

        return allSuccessFull ? HttpStatus.OK : HttpStatus.MULTI_STATUS;
    }

    /**
     * This controller used as producer to send messages or event
     * @param msgProtocol
     *            message protocol (required)
     * @param userDomain
     *            user domain (required)
     * @param tag
     *            (not required)
     * @param routingKey
     *            (not required)
     * @param body
     *            (Here json body of string type as input because just to parse
     *            the string in to JsonElement not using JsonElement directly here.)
     * @return A response entity which contains http status and result
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Operation(summary = "To publish Eiffel event to message bus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event sent successfully",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(value = PRODUCER_RESPONSE_200_EXAMPLE)}
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid event content",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Event validation failure", value = PRODUCER_RESPONSE_400_VALIDATION_EXAMPLE),
                    @ExampleObject(name= "Input contains invalid JSON", value = PRODUCER_RESPONSE_400_INVALID_JSON_EXAMPLE),
                    @ExampleObject(name = "Invalid protocol", value = PRODUCER_RESPONSE_400_INVALID_PROTOCOL_EXAMPLE)
                }
            )
        ),
        @ApiResponse(responseCode = "404", description = "RabbitMq properties not found"),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(value = PRODUCER_RESPONSE_500_EXAMPLE),
                }
            )
        ),
        @ApiResponse(responseCode = "503", description = "Service Unavailable")
    })
    @RequestMapping(value = "/producer/msg", method = RequestMethod.POST)
    public ResponseEntity send(
        @Parameter(description = "message protocol", required = true) @RequestParam(value = "mp") final String msgProtocol,
        @Parameter(description = "user domain") @RequestParam(value = "ud", required = false) final String userDomain,
        @Parameter(description = "tag") @RequestParam(value = "tag", required = false) final String tag,
        @Parameter(description = "routing key") @RequestParam(value = "rk", required = false) final String routingKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Eiffel event",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(name = "Example event", value = PRODUCER_REQUEST_INPUT_EXAMPLE)}
            )
        ) @RequestBody String body
    ) {
        try {
            JsonElement inputBody = JsonParser.parseString(body);
            return send(msgProtocol, userDomain, tag, routingKey, inputBody);
        } catch (JsonSyntaxException e) {
            String exceptionMessage = e.getMessage();
            log.error("Cannot parse the following JSON data:\n" + body + "\n\n" + exceptionMessage);
            return createResponseEntity(HttpStatus.BAD_REQUEST,
                    "Invalid JSON data: " + exceptionMessage, ResultStatus.FATAL);
        }
    }

    /**
     * This controller provides single RemRem REST API End Point for both RemRem
     * Generate and Publish.
     * @param msgProtocol
     *            message protocol (required)
     * @param msgType
     *            message type (required)
     * @param userDomain
     *            user domain (not required)
     * @param tag
     *            (not required)
     * @param routingKey
     *            (not required)
     * @param parseData
     *            (not required, default=false)
     * @param body
     *            (Here json body of string type as input because just to parse
     *            the string in to JsonElement not using JsonElement directly here.)
     * @return A response entity which contains http status and result
     *
     * @use A typical CURL command: curl -H "Content-Type: application/json" -X POST
     *      --data "@inputGenerate_activity_finished.txt"
     *      "http://localhost:8986/generateAndPublish/?mp=eiffelsemantics&msgType=EiffelActivityFinished"
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Operation(summary = "To generate and publish Eiffel event to message bus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event sent successfully",
            content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(value = GENERATE_PUBLISH_RESPONSE_200_EXAMPLE)}
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid event content",
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Event validation failure", value = GENERATE_PUBLISH_RESPONSE_400_VALIDATION_EXAMPLE),
                    @ExampleObject(name = "Invalid protocol", value = GENERATE_PUBLISH_RESPONSE_400_INVALID_PROTOCOL_EXAMPLE),
                    @ExampleObject(name = "Request to REMReM Generate failed", value = GENERATE_PUBLISH_RESPONSE_400_BAD_REQUEST_EXAMPLE)
                }
            )
        ),
        @ApiResponse(responseCode = "404", description = "REMReM Generate not found"),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(value = GENERATE_PUBLISH_RESPONSE_500_EXAMPLE)
                }
            )
        ),
        @ApiResponse(responseCode = "503", description = "Message protocol is invalid")
    })
    @RequestMapping(value = "/generateAndPublish", method = RequestMethod.POST)
    public ResponseEntity generateAndPublish(
        @Parameter(description = "message protocol", required = true) @RequestParam(value = "mp") final String msgProtocol,
        @Parameter(description = "message type", required = true) @RequestParam("msgType") final String msgType,
        @Parameter(description = "user domain") @RequestParam(value = "ud", required = false) final String userDomain,
        @Parameter(description = "tag") @RequestParam(value = "tag", required = false) final String tag,
        @Parameter(description = "routing key") @RequestParam(value = "rk", required = false) final String routingKey,
        @Parameter(description = "parse data") @RequestParam(value = "parseData", required = false, defaultValue = "false") final Boolean parseData,
        @Parameter(description = "ER lookup result multiple found, Generate will fail") @RequestParam(value = "failIfMultipleFound", required = false, defaultValue = "false") final Boolean failIfMultipleFound,
        @Parameter(description = "ER lookup result none found, Generate will fail") @RequestParam(value = "failIfNoneFound", required = false, defaultValue = "false") final Boolean failIfNoneFound,
        @Parameter(description = "Determines if external ER's should be used to compile the results of query.Use true to use External ER's.") @RequestParam(value = "lookupInExternalERs", required = false, defaultValue = "false") final Boolean lookupInExternalERs,
        @Parameter(description = "The maximum number of events returned from a lookup. If more events are found "
             + "they will be disregarded. The order of the events is undefined, which means that what events are "
             + "disregarded is also undefined.") @RequestParam(value = "lookupLimit", required = false, defaultValue = "1") final int lookupLimit,
        @Parameter(description = "okToLeaveOutInvalidOptionalFields true will remove the optional "
             + "event fields from the input event data that does not validate successfully, "
             + "and add those removed field information into customData/remremGenerateFailures") @RequestParam(value = "okToLeaveOutInvalidOptionalFields", required = false, defaultValue = "false")  final Boolean okToLeaveOutInvalidOptionalFields,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
             description = "JSON message",
             required = true,
             content = @Content(
                 mediaType = "application/json",
                 examples = {@ExampleObject(name = "Example event", value = GENERATE_PUBLISH_REQUEST_INPUT_EXAMPLE)}
             )
        ) @RequestBody String body
    ){

        try {
            JsonElement bodyJson = JsonParser.parseString(body);
            return generateAndPublish(msgProtocol, msgType, userDomain, tag, routingKey, parseData, failIfMultipleFound,
                    failIfNoneFound, lookupInExternalERs, lookupLimit, okToLeaveOutInvalidOptionalFields, bodyJson);
        } catch (JsonSyntaxException e) {
            String exceptionMessage = e.getMessage();
            log.error("Unexpected exception caught due to parsed json data {}", exceptionMessage);
            return createResponseEntity(HttpStatus.BAD_REQUEST,
                    "Invalid JSON parse data format due to: " + exceptionMessage, ResultStatus.FATAL);
        }
    }

    private boolean eventTypeExists(@NonNull MsgService msgService, String eventType) {
        Collection<String> supportedEventTypes = msgService.getSupportedEventTypes();
        return supportedEventTypes != null && supportedEventTypes.contains(eventType);
    }

    /**
     * This controller provides single RemRem REST API End Point for both RemRem
     * Generate and Publish.
     *
     * @param msgProtocol
     *            message protocol (required)
     * @param msgType
     *            message type (required)
     * @param userDomain
     *            user domain (not required)
     * @param tag
     *            (not required)
     * @param routingKey
     *            (not required)
     * @param parseData
     *            (not required, default=false)
     * @return A response entity which contains http status and result
     *
     * @use A typical CURL command: curl -H "Content-Type: application/json" -X POST
     *      --data "@inputGenerate_activity_finished.txt"
     *      "http://localhost:8986/generateAndPublish/?mp=eiffelsemantics&msgType=EiffelActivityFinished"
     */

    public ResponseEntity generateAndPublish(final String msgProtocol, final String msgType, final String userDomain, final String tag, final String routingKey,
                                             final Boolean parseData, final Boolean failIfMultipleFound, final Boolean failIfNoneFound, final Boolean lookupInExternalERs,
                                             final int lookupLimit, final Boolean okToLeaveOutInvalidOptionalFields, final JsonElement bodyJson) {
        if (isAuthenticationEnabled) {
            logUserName();
        }

        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);
        if (msgService == null) {
            return createResponseEntity(HttpStatus.BAD_REQUEST,
                    "No protocol service has been found registered", ResultStatus.FAIL);
        }
        List<JsonObject> events = new ArrayList<>();
        if (bodyJson.isJsonObject()) {
            events.add(getAsJsonObject(bodyJson));
        } else if (bodyJson.isJsonArray()) {
            JsonArray bodyJsonArray = bodyJson.getAsJsonArray();
            //here add check for limitation for events in array is fetched from REMReM property and checked during publishing.
            if (bodyJsonArray.size() > maxSizeOfInputArray) {
                return createResponseEntity(
                        HttpStatus.BAD_REQUEST,
                        "The number of events in the input array is too high: " + bodyJsonArray.size() + " > "
                        + maxSizeOfInputArray + "; you can modify the property 'maxSizeOfInputArray' to increase it.",
                        ResultStatus.FAIL);
            }
            for (JsonElement element : bodyJsonArray) {
                if (element.isJsonObject()) {
                    events.add(getAsJsonObject(element));
                } else {
                    return createResponseEntity(HttpStatus.BAD_REQUEST,
                            "Invalid, array events must be a JSON object", ResultStatus.FAIL);
                }
            }
        } else {
            return createResponseEntity(
                    HttpStatus.BAD_REQUEST,
                    "Invalid, event is neither in the form of JSON object nor in the JSON array",
                    ResultStatus.FAIL);
        }
        List<Map<String, Object>> responseEvents;
        HttpStatus responseStatus = HttpStatus.BAD_REQUEST;
        EnumSet<HttpStatus> getStatus = EnumSet.of(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.UNAUTHORIZED,
                HttpStatus.NOT_ACCEPTABLE, HttpStatus.EXPECTATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        try {
            String bodyJsonOut;
            if (parseData) {
                EventTemplateHandler eventTemplateHandler = new EventTemplateHandler();
                StringBuffer parsedTemplates = new StringBuffer();
                parsedTemplates.append("[");
                for (JsonElement eventJson : events) {
                    // -- parse params in incoming request -> body -------------
                    if (!eventTypeExists(msgService, msgType)) {
                        return createResponseEntity(HttpStatus.BAD_REQUEST,
                            "Unknown event type '" + msgType + "'", ResultStatus.FAIL);
                    }

                    JsonNode parsedTemplate = eventTemplateHandler.eventTemplateParser(eventJson.toString(), msgType);
                    if (parsedTemplates.length() > 1) {
                        parsedTemplates.append(",");
                    }
                    parsedTemplates.append(parsedTemplate.toString());
                }
                parsedTemplates.append("]");
                bodyJsonOut = parsedTemplates.toString();
                log.info("Parsed template: " + bodyJsonOut);
            } else {
                bodyJsonOut = bodyJson.toString();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> entity = new HttpEntity<>(bodyJsonOut, headers);
            String generateUrl = generateURLTemplate.getUrl() + "&failIfMultipleFound=" + failIfMultipleFound
                    + "&failIfNoneFound=" + failIfNoneFound + "&lookupInExternalERs=" + lookupInExternalERs
                    + "&lookupLimit=" + lookupLimit + "&okToLeaveOutInvalidOptionalFields=" + okToLeaveOutInvalidOptionalFields;

            ResponseEntity<String> response = restTemplate.postForEntity(generateUrl,
                    entity, String.class, generateURLTemplate.getMap(msgProtocol, msgType));

            responseStatus = response.getStatusCode();
            String responseBody = null;
            // TODO We should not rely on bodyJson (an input string), but rather on given
            //      result, i.e. response.getBody() and check this for type (object or array).
            if (bodyJson.isJsonObject()) {
                responseBody = "[" + response.getBody() + "]";
            } else if (bodyJson.isJsonArray()) {
                responseBody = response.getBody();
            }

            if (responseStatus == HttpStatus.OK || responseStatus == HttpStatus.MULTI_STATUS) {
                log.info("The result from REMReM Generate is: " + response.getStatusCodeValue());
                log.debug("mp: " + msgProtocol);
                log.debug("body: " + responseBody);
                log.debug("user domain suffix: " + userDomain + " tag: " + tag + " routing key: " + routingKey);

                if (msgService != null && msgProtocol != null) {
                    rmqHelper.rabbitMqPropertiesInit(msgProtocol);
                }
                responseEvents = processingValidEvent(responseBody, msgProtocol, msgType, userDomain,
                        tag, routingKey, okToLeaveOutInvalidOptionalFields);
            } else {
                return response;
            }
        } catch (RemRemPublishException e) {
            String exceptionMessage = e.getMessage();
            return createResponseEntity(HttpStatus.NOT_FOUND, exceptionMessage, ResultStatus.FAIL);
        } catch (HttpStatusCodeException e) {
            String responseBody = null;
            String responseMessage = e.getResponseBodyAsString();
            if (bodyJson.isJsonObject()) {
                responseBody = "[" + responseMessage + "]";
            } else if (bodyJson.isJsonArray()) {
                responseBody = responseMessage;
            }
            responseEvents = processingValidEvent(responseBody, msgProtocol, msgType, userDomain,
                    tag, routingKey, okToLeaveOutInvalidOptionalFields);
            return new ResponseEntity<>(responseEvents, HttpStatus.BAD_REQUEST);
        }
        //Status here is the status returned from generate service, except BAD_REQUEST which already handled above
        return new ResponseEntity<>(responseEvents, responseStatus);
    }

    /**
     * Creates a ResponseEntity containing an error response with the given status, message, and result.
     *
     *  @param status status code for the HTTP request
     *  @param responseMessage the message with more details about the response
     *  @param result the result for the HTTP request
     *  @param errorResponse the existing response object to update
     * @return ResponseEntity
     */
    public ResponseEntity<JsonObject> createResponseEntity(HttpStatus status, String responseMessage, ResultStatus result,
                                                           JsonObject errorResponse) {
        initializeResponse(status, responseMessage, result, errorResponse);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Creates a ResponseEntity with the given status, message, and result.
     *
     * @param status status code for the HTTP request
     * @param responseMessage the message with more details about the response
     * @param result the result for the HTTP request
     * @return ResponseEntity
     */
    public ResponseEntity<JsonObject> createResponseEntity(HttpStatus status, String responseMessage, ResultStatus result) {
        return createResponseEntity(status, responseMessage, result, new JsonObject());
    }

    /**
     * Update the given JSON object with the provided values for
     * result and message.
     *
     * @param status the status code to put in the response
     * @param result the result to put in the response
     * @param errorMessage the error message to put in the response
     * @param errorResponse the error response object to update
     */
    public void initializeResponse(HttpStatus status, String errorMessage, ResultStatus result, JsonObject errorResponse) {
        errorResponse.addProperty(JSON_STATUS_CODE, status.value());
        errorResponse.addProperty(JSON_STATUS_RESULT, result.toString());
        errorResponse.addProperty(JSON_EVENT_MESSAGE_FIELD, errorMessage);
    }

    /**
     * This one is helper method to process or publish messages or event
     * @param eventResponseMessage
     * @param msgProtocol
     * @param userDomain
     * @param tag
     * @param routingKey
     * @return list of responses
     */
    // TODO Why public?
    public List<Map<String, Object>> processingValidEvent(String eventResponseMessage, final String msgProtocol, final String msgType,
                                                          final String userDomain, final String tag, final String routingKey, final Boolean okToLeaveOutInvalidOptionalFields) {
        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);
        List<Map<String, Object>> responseEvent = new ArrayList<>();
        Map<String, Object> eventResponse = null;

        if (eventResponseMessage == null) {
            eventResponse.put("Missing response from 'generate' service", HttpStatus.BAD_REQUEST);
            responseEvent.add(eventResponse);
            return responseEvent;
        }

        JsonArray eventArray = null;
        try {
            JsonElement eventElement = JsonParser.parseString(eventResponseMessage);
            eventArray = eventElement.getAsJsonArray();
        }
        catch (JsonSyntaxException e) {
            String errorMessage = "Cannot parse '" + eventResponseMessage + "': " + e.getMessage();
            log.error(errorMessage);
            eventResponse.put(errorMessage, HttpStatus.BAD_REQUEST);
            responseEvent.add(eventResponse);
            return responseEvent;
        }

        if (eventArray == null) {
            String errorMessage = "Processed event '" +eventResponseMessage + "' is expected in form of JSON array";
            log.error(errorMessage);
            eventResponse.put(errorMessage, HttpStatus.BAD_REQUEST);
            return responseEvent;
        }

        for (int i = 0; i < eventArray.size(); i++) {
            // TODO Having hash map here is very chaotic: key are sometimes predefined values,
            //      e.g. JSON_STATUS_RESULT, JSON_EVENT_MESSAGE_FIELD, but also a general string
            //      as in case of reporting a bad request.
            eventResponse = new HashMap<>();
            // TODO Could it be null?
            JsonObject jsonResponseEvent = eventArray.get(i).getAsJsonObject();
            ValidationResult validationResult = msgService.validateMsg(msgType, jsonResponseEvent, okToLeaveOutInvalidOptionalFields);
            if (validationResult.isValid()) {
                String validResponseBody = jsonResponseEvent.toString();
                SendResult result = messageService.send(validResponseBody, msgService, userDomain, tag, routingKey);
                eventResponse.put(JSON_STATUS_RESULT, result);
            } else {
                eventResponse.put(JSON_EVENT_MESSAGE_FIELD, jsonResponseEvent);
            }
            responseEvent.add(eventResponse);
        }
        return responseEvent;
    }

    /**
     * This helper method to get JsonObject type from JsonElement
     * @param jsonElement An event which need to convert
     * @return JsonObject converted Json of JsonObject type
     */
    public JsonObject getAsJsonObject(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;
    }

    /**
     * @return this method returns the current version of publish and all loaded
     *         protocols.
     */
    @Operation(summary = "To get versions of publish and all loaded protocols")
    @RequestMapping(value = "/versions", method = RequestMethod.GET)
    public JsonElement getVersions() {
        JsonParser parser = new JsonParser();
        Map<String, Map<String, String>> versions = new VersionService().getMessagingVersions();
        return parser.parse(versions.toString());
    }
}
