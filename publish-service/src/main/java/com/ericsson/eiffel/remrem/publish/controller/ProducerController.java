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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.ericsson.eiffel.remrem.protocol.ValidationResult;
import com.ericsson.eiffel.remrem.publish.service.*;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
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

    @Value("${maxSizeOfInputArray:250}")
    private int maxSizeOfInputArray;

    private RestTemplate restTemplate = new RestTemplate();

    private JsonParser parser = new JsonParser();

    private Logger log = (Logger) LoggerFactory.getLogger(ProducerController.class);

    public final String JSON_STATUS_RESULT = "result";

    public final String JSON_EVENT_MESSAGE_FIELD = "status message";

    public final String JSON_STATUS_CODE = "status code";

    public static final String META = "meta";

    public static final String DATA = "data";

    public static final String LINKS = "links";

    public static final String JSON_FATAL_STATUS = "FATAL";

    public static final String JSON_ERROR_STATUS = "FAIL";

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
                return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }

        //here add check for limitation for events in array is fetched from REMReM property and checked during publishing.
        if (body.isJsonArray() && (body.getAsJsonArray().size() > maxSizeOfInputArray)) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                "The number of events in the input array is too high: " + body.getAsJsonArray().size() + " > "
                        + maxSizeOfInputArray + "; you can modify the property 'maxSizeOfInputArray' to increase it.");
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
    @ApiOperation(value = "To publish eiffel event to message bus", response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Event sent successfully"),
            @ApiResponse(code = 400, message = "Invalid event content"),
            @ApiResponse(code = 404, message = "RabbitMq properties not found"),
            @ApiResponse(code = 500, message = "Internal server error"),
            @ApiResponse(code = 503, message = "Service Unavailable")})
    @RequestMapping(value = "/producer/msg", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity send(
            @ApiParam(value = "message protocol", required = true) @RequestParam(value = "mp") final String msgProtocol,
            @ApiParam(value = "user domain") @RequestParam(value = "ud", required = false) final String userDomain,
            @ApiParam(value = "tag") @RequestParam(value = "tag", required = false) final String tag,
            @ApiParam(value = "routing key") @RequestParam(value = "rk", required = false) final String routingKey,
            @ApiParam(value = "eiffel event", required = true) @RequestBody final String body) {
        try {
            JsonElement inputBody = JsonParser.parseString(body);
            return send(msgProtocol, userDomain, tag, routingKey, inputBody);
        } catch (JsonSyntaxException e) {
            String exceptionMessage = e.getMessage();
            log.error("Cannot parse the following JSON data:\n" + body + "\n\n" + exceptionMessage);
            // TODO Disable CodeQL rule java/error-message-exposure. The message is sent to user to
            // TODO show where exactly JSON parser encountered an issue, i.e. line and column.
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_FATAL_STATUS,
                    "Invalid JSON data: " + exceptionMessage);
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
            String exceptionMessage = e.getMessage();
            log.error("Unexpected exception caught due to parsed json data", exceptionMessage);
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_FATAL_STATUS,
                    "Invalid JSON parse data format due to: " + exceptionMessage);
        }
    }

    private boolean eventTypeExists(@NonNull MsgService msgService, String eventType) {
        Collection<String> supportedEventTypes = msgService.getSupportedEventTypes();
        return supportedEventTypes != null && supportedEventTypes.contains(eventType);
    }

    /**
     * Ensure attribute value is properly URL encoded.
     *
     * @param attribute Attribute name.
     * @param value Attribute value. It's converted to string using toString().
     * @return "&attribute=[URL encoded value]"
     * @throws UnsupportedEncodingException
     */
    private String appendAttributeAndValue(String attribute, Object value)
            throws UnsupportedEncodingException {
        return "&" + attribute + "="
                + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString());
    }

    /**
     * Guarantees that given value is not null.
     *
     * @param b a value
     * @return b if non-null, Boolean.FALSE otherwise.
     */
    private Boolean ensureValueNonNull(Boolean b) {
        return b != null ? b : Boolean.FALSE;
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

        if (StringUtils.isEmpty(msgProtocol)) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_FATAL_STATUS, "Value of parameter 'msgProtocol' is empty");
        }

        if (StringUtils.isEmpty(msgType)) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_FATAL_STATUS, "Value of parameter 'msgType' is empty");
        }

        MsgService msgService;
        if (StringUtils.isEmpty(msgProtocol) ||
                ((msgService = PublishUtils.getMessageService(msgProtocol, msgServices)) == null)) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                    "No protocol service has been found registered");
        }
        List<JsonObject> events = new ArrayList<>();
        if (bodyJson.isJsonObject()) {
            events.add(getAsJsonObject(bodyJson));
        } else if (bodyJson.isJsonArray()) {
            JsonArray bodyJsonArray = bodyJson.getAsJsonArray();
            //here add check for limitation for events in array is fetched from REMReM property and checked during publishing.
            if (bodyJsonArray.size() > maxSizeOfInputArray) {
                return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                        "The number of events in the input array is too high: " + bodyJsonArray.size() + " > "
                                + maxSizeOfInputArray + "; you can modify the property 'maxSizeOfInpuArray' to increase it.");
            }
            for (JsonElement element : bodyJsonArray) {
                if (element.isJsonObject()) {
                    events.add(getAsJsonObject(element));
                } else {
                    return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                            "Invalid, array events must be a JSON object");
                }
            }
        } else {
            return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                    "Invalid, event is neither in the form of JSON object nor in the JSON array");
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
                        return createResponseEntity(HttpStatus.BAD_REQUEST, JSON_ERROR_STATUS,
                            "Unknown event type '" + msgType + "'");
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
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(bodyJsonOut, headers);

            String generatedUrl = generateURLTemplate.getUrl()
                + appendAttributeAndValue("failIfMultipleFound", ensureValueNonNull(failIfMultipleFound))
                + appendAttributeAndValue("failIfNoneFound", ensureValueNonNull(failIfNoneFound))
                + appendAttributeAndValue("lookupInExternalERs", ensureValueNonNull(lookupInExternalERs))
                + appendAttributeAndValue("lookupLimit", lookupLimit)
                + appendAttributeAndValue("okToLeaveOutInvalidOptionalFields", ensureValueNonNull(okToLeaveOutInvalidOptionalFields));

            ResponseEntity<String> response = restTemplate.postForEntity(generatedUrl,
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
        }
        catch (UnsupportedEncodingException e) {
            return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, JSON_FATAL_STATUS, e.getMessage());
        }
        catch (RemRemPublishException e) {
            return createResponseEntity(HttpStatus.NOT_FOUND, JSON_ERROR_STATUS, e.getMessage());
        }
        catch (HttpStatusCodeException e) {
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

        // Status here is the status returned from generate service,
        // except BAD_REQUEST which already handled above.
        return new ResponseEntity<>(responseEvents, responseStatus);
    }

    /**
     * To display response in browser or application
     * @param status response code for the HTTP request
     * @param responseMessage the message according to response
     * @param resultMessage whatever the result this message gives you idea about that
     * @param errorResponse is to collect all the responses here.
     * @return ResponseEntity
     */
    public ResponseEntity<JsonObject> createResponseEntity(HttpStatus status, String responseMessage, String resultMessage,
                                                           JsonObject errorResponse) {
        initializeResponse(status, responseMessage, resultMessage, errorResponse);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * To display response in browser or application
     * @param status response code for the HTTP request
     * @param responseMessage the message according to response
     * @param resultMessage whatever the result this message gives you idea about that
     * @return ResponseEntity
     */
    public ResponseEntity<JsonObject> createResponseEntity(HttpStatus status, String responseMessage, String resultMessage) {
        return createResponseEntity(status, responseMessage, resultMessage, new JsonObject());
    }

    /**
     * To initialize in the @{createResponseEntity} method
     * @param status response code for the HTTP request
     * @param resultMessage whatever the result this message gives you idea about that
     * @param errorResponse is to collect all the responses here.
     */
    public void initializeResponse(HttpStatus status, String errorMessage, String resultMessage, JsonObject errorResponse) {
        errorResponse.addProperty(JSON_STATUS_CODE, status.value());
        errorResponse.addProperty(JSON_STATUS_RESULT, resultMessage);
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
    @ApiOperation(value = "To get versions of publish and all loaded protocols", response = String.class)
    @RequestMapping(value = "/versions", method = RequestMethod.GET)
    public JsonElement getVersions() {
        JsonParser parser = new JsonParser();
        Map<String, Map<String, String>> versions = new VersionService().getMessagingVersions();
        return parser.parse(versions.toString());
    }
}
