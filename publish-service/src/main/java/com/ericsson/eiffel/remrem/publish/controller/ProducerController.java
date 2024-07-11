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

import com.google.gson.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.EventTemplateHandler;
import com.ericsson.eiffel.remrem.publish.service.GenerateURLTemplate;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.fasterxml.jackson.databind.JsonNode;

import ch.qos.logback.classic.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@ComponentScan("com.ericsson.eiffel.remrem")
@RestController
@RequestMapping("/*")
@Api(value = "REMReM Publish Service", description = "REST API for publishing Eiffel messages to message bus")
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

    private RestTemplate restTemplate = new RestTemplate();

    private JsonParser parser = new JsonParser();

    private Logger log = (Logger) LoggerFactory.getLogger(ProducerController.class);

    public final String JSON_STATUS_RESULT = "result";

    public final String JSON_EVENT_MESSAGE_FIELD = "status message";

    public final String JSON_EVENT_STATUS_VALUE = "status";

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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ApiOperation(value = "To publish eiffel event to message bus", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Event sent successfully"),
            @ApiResponse(code = 400, message = "Invalid event content"),
            @ApiResponse(code = 404, message = "RabbitMq properties not found"),
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 503, message = "Service Unavailable") })
    @RequestMapping(value = "/producer/msg", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity send(
            @ApiParam(value = "message protocol", required = true) @RequestParam(value = "mp") final String msgProtocol,
            @ApiParam(value = "user domain") @RequestParam(value = "ud", required = false) final String userDomain,
            @ApiParam(value = "tag") @RequestParam(value = "tag", required = false) final String tag,
            @ApiParam(value = "routing key") @RequestParam(value = "rk", required = false) final String routingKey,
            @ApiParam(value = "eiffel event", required = true) @RequestBody final JsonElement body) {
        if(isAuthenticationEnabled) {
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
                return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        synchronized (this) {
            SendResult result = messageService.send(body, msgService, userDomain, tag, routingKey);
            log.info("HTTP Status: {}",  messageService.getHttpStatus().value());
            return new ResponseEntity(result, messageService.getHttpStatus());
        }
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ApiOperation(value = "To generate and publish eiffel event to message bus", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Event sent successfully"),
            @ApiResponse(code = 400, message = "Invalid event content"),
            @ApiResponse(code = 404, message = "RabbitMq properties not found"),
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 503, message = "Message protocol is invalid") })
    @RequestMapping(value = "/generateAndPublish", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity generateAndPublish(@ApiParam(value = "message protocol", required = true) @RequestParam(value = "mp") final String msgProtocol,
                                             @ApiParam(value = "message type", required = true) @RequestParam("msgType") final String msgType,
                                             @ApiParam(value = "user domain") @RequestParam(value = "ud", required = false) final String userDomain,
                                             @ApiParam(value = "tag") @RequestParam(value = "tag", required = false) final String tag,
                                             @ApiParam(value = "routing key") @RequestParam(value = "rk", required = false) final String routingKey,
                                             @ApiParam(value = "parse data") @RequestParam(value = "parseData", required = false, defaultValue = "false") final Boolean parseData,
                                             @ApiParam(value = "ER lookup result multiple found, Generate will fail") @RequestParam(value = "failIfMultipleFound", required = false, defaultValue = "false") final Boolean failIfMultipleFound,
                                             @ApiParam(value = "ER lookup result none found, Generate will fail") @RequestParam(value = "failIfNoneFound", required = false, defaultValue = "false") final Boolean failIfNoneFound,
                                             @ApiParam(value = "Determines if external ER's should be used to compile the results of query.Use true to use External ER's.") @RequestParam(value = "lookupInExternalERs", required = false, defaultValue = "false") final Boolean lookupInExternalERs,
                                             @ApiParam(value = "The maximum number of events returned from a lookup. If more events are found "
                                                     + "they will be disregarded. The order of the events is undefined, which means that what events are "
                                                     + "disregarded is also undefined.") @RequestParam(value = "lookupLimit", required = false, defaultValue = "1") final int lookupLimit,
                                             @ApiParam(value = "okToLeaveOutInvalidOptionalFields true will remove the optional "
                                                     + "event fields from the input event data that does not validate successfully, "
                                                     + "and add those removed field information into customData/remremGenerateFailures") @RequestParam(value = "okToLeaveOutInvalidOptionalFields", required = false, defaultValue = "false")  final Boolean okToLeaveOutInvalidOptionalFields,
                                             @ApiParam(value = "JSON message", required = true) @RequestBody final String body){

        try {
            JsonElement bodyJson = JsonParser.parseString(body);
            return generateAndPublish(msgProtocol, msgType, userDomain, tag, routingKey, parseData, failIfMultipleFound,
                    failIfNoneFound, lookupInExternalERs, lookupLimit, okToLeaveOutInvalidOptionalFields, bodyJson);
        } catch (JsonSyntaxException e) {
            JsonObject errorResponse = new JsonObject();
            log.error("Unexpected exception caught due to parse json data", e.getMessage());
            String exceptionMessage = e.getMessage();
            errorResponse.addProperty("status code", HttpStatus.BAD_REQUEST.value());
            errorResponse.addProperty(JSON_STATUS_RESULT, "fatal");
            errorResponse.addProperty(JSON_EVENT_MESSAGE_FIELD, "Invalid JSON parse data format due to: " + exceptionMessage);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity generateAndPublish(final String msgProtocol, final String msgType, final String userDomain, final String tag, final String routingKey,
                                             final Boolean parseData, final Boolean failIfMultipleFound, final Boolean failIfNoneFound, final Boolean lookupInExternalERs,
                                             final int lookupLimit, final Boolean okToLeaveOutInvalidOptionalFields, final JsonElement bodyJson) {
        if (isAuthenticationEnabled) {
            logUserName();
        }

        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);
        List<JsonObject> events = new ArrayList<>();
        if (bodyJson.isJsonObject()) {
            events.add(bodyJson.getAsJsonObject());
        } else if (bodyJson.isJsonArray()) {
            for (JsonElement element : bodyJson.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    events.add(element.getAsJsonObject());
                } else {
                    return new ResponseEntity<>("Invalid, array events must be a JSON object", HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return new ResponseEntity<>("Invalid, event is neither in the form of JSON object nor in the JSON array", HttpStatus.BAD_REQUEST);
        }
        List<Map<String, Object>> responseEvents = new ArrayList<>();
        EnumSet<HttpStatus> getStatus = EnumSet.of(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.UNAUTHORIZED,
                HttpStatus.NOT_ACCEPTABLE, HttpStatus.EXPECTATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        Map<String, Object> eventResponse;
        try {
            String bodyJsonOut = null;
            if (parseData) {
                EventTemplateHandler eventTemplateHandler = new EventTemplateHandler();
                StringBuffer parsedTempaltes = new StringBuffer();
                parsedTempaltes.append("[");
                for (JsonElement eventJson : events) {
                    // -- parse params in incoming request -> body -------------
                    JsonNode parsedTemplate = eventTemplateHandler.eventTemplateParser(eventJson.toString(), msgType);
                    if (parsedTempaltes.length() > 1) {
                        parsedTempaltes.append(",");
                    }
                    parsedTempaltes.append(parsedTemplate.toString());
                }
                parsedTempaltes.append("]");
                bodyJsonOut = parsedTempaltes.toString();
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

            String responseBody = null;
            if (bodyJson.isJsonObject()) {
                responseBody = "[" + response.getBody() + "]";
            } else if (bodyJson.isJsonArray()) {
                responseBody = response.getBody();
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("The result from REMReM Generate is: " + response.getStatusCodeValue());
                log.debug("mp: " + msgProtocol);
                log.debug("body: " + responseBody);
                log.debug("user domain suffix: " + userDomain + " tag: " + tag + " routing key: " + routingKey);

                if (msgService != null && msgProtocol != null) {
                    rmqHelper.rabbitMqPropertiesInit(msgProtocol);
                }
                JsonElement element = JsonParser.parseString(responseBody);

                for (int i = 0; i < element.getAsJsonArray().size(); i++) {
                    eventResponse = new HashMap<>();
                    JsonElement jsonResponseEvent = element.getAsJsonArray().get(i);
                    if (isValid(jsonResponseEvent)) {
                        synchronized (this) {
                            JsonObject validResponse = jsonResponseEvent.getAsJsonObject();
                            String validResponseBody = validResponse.toString();
                            SendResult result = messageService.send(validResponseBody, msgService, userDomain, tag, routingKey);
                            eventResponse.put(JSON_STATUS_RESULT, result);
                        }
                    } else {
                        eventResponse.put(JSON_EVENT_STATUS_VALUE, HttpStatus.BAD_REQUEST);
                        eventResponse.put("like", HttpStatus.ACCEPTED);
                        eventResponse.put(JSON_EVENT_MESSAGE_FIELD, jsonResponseEvent);
                    }
                    responseEvents.add(eventResponse);
                }
            } else {
                return response;
            }
        } catch (RemRemPublishException e) {
            eventResponse = new HashMap<>();
            eventResponse.put(JSON_EVENT_MESSAGE_FIELD, e.getMessage());
            return new ResponseEntity(eventResponse, HttpStatus.NOT_FOUND);
        } catch (HttpStatusCodeException e) {
            JsonElement element = JsonParser.parseString(e.getResponseBodyAsString());

            for (int i = 0; i < element.getAsJsonArray().size(); i++) {
                eventResponse = new HashMap<>();
                JsonElement JsonResponseEvent = element.getAsJsonArray().get(i);
                if (isValid(JsonResponseEvent)) {
                    synchronized (this) {
                        JsonObject validResponse = JsonResponseEvent.getAsJsonObject();
                        String validResponseBody = validResponse.toString();
                        SendResult result = messageService.send(validResponseBody, msgService, userDomain, tag, routingKey);
                        eventResponse.put(JSON_STATUS_RESULT, result);
                    }
                } else {
                    eventResponse.put(JSON_EVENT_MESSAGE_FIELD, JsonResponseEvent);
                }
                responseEvents.add(eventResponse);
            }
            return new ResponseEntity<>(responseEvents, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseEvents, HttpStatus.OK);
    }

    /**
     * This helper method check the json event is valid or not
     */

    private  Boolean isValid(JsonElement jsonObject) {
        try {
            return jsonObject.getAsJsonObject().has("meta") && jsonObject.getAsJsonObject().has("data") &&
                    jsonObject.getAsJsonObject().has("links") && !jsonObject.getAsJsonObject().has("Status code");
        } catch (Exception e) {
            log.error("Error while validating event: ", e.getMessage());
            return false;
        }
    }

    /**
     * @return this method returns the current version of publish and all loaded
     *         protocols.
     */
    @ApiOperation(value = "To get versions of publish and all loaded protocols", response = String.class)
    @RequestMapping(value = "/versions", method = RequestMethod.GET)
    public JsonElement getVersions() {
        JsonParser parser = new JsonParser();
        Map<String, Map<String, String>> versions = new VersionService().getMessagingVersions();
        return parser.parse(versions.toString());
    }

}
