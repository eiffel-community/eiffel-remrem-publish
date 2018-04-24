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

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventTemplateHandler {
    Logger log = (Logger) LoggerFactory.getLogger(EventTemplateHandler.class);

    // Paths in Semantics JAR
    private static final String EVENT_TEMPLATE_PATH = "templates/";
    private static final String EVENT_SCHEMA_PATH = "schemas/input/";

    private static final String REGEXP_END_DIGITS = "\\[\\d+\\]$";

    private final Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

    // eventTemplateParser
    public JsonNode eventTemplateParser(String jsonData , String eventName){
        JsonNode updatedJson = null;
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = null;
        try {
            String eventTemplate = accessFileInSemanticJar(EVENT_TEMPLATE_PATH + eventName.toLowerCase() + ".json");

            rootNode = mapper.readTree(jsonData);
            updatedJson = mapper.readValue(eventTemplate, JsonNode.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        // For each key/value pair for parsing to template
        Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            // Parse values to template
            // Check if POJO required for update in event template
            Pattern p = Pattern.compile(REGEXP_END_DIGITS);  // if ends with [d+]
            Matcher m = p.matcher(field.getKey());

            String myKey = "$." + templateParamHandler(field.getKey());

                if(field.getValue().toString().equals("\"<%DELETE%>\"")){
                    updatedJson = jsonPathHandlerDelete(updatedJson, myKey);
                }else if (m.find()) {
                    String myValue = field.getValue().toString();
                    try {
                        // Fetch Class name in Event Schema
                        String eventSchema = accessFileInSemanticJar(EVENT_SCHEMA_PATH + eventName + ".json");
                        // Filter javatype from Event Schema = class name
                        JsonNode jsonFromSchema = JsonPath.using(configuration).parse(eventSchema.toString()).read(schemaClassPathHandler(field.getKey().replaceAll(REGEXP_END_DIGITS, "")));
                        String myClassName = jsonFromSchema.toString().replace("[", "").replace("]", "").replace("\"", "");  // Ex ["com.ericsson.eiffel.semantics.events.PersistentLog"] to com.ericsson.eiffel.semantics.events.PersistentLog
                        // Initiate Class via reflection and map values - POJO
                        Class myClass = Class.forName(myClassName);
                        Object mapped2Pojo = mapper.readValue(myValue, myClass);
                        updatedJson = jsonPathHandlerSet(updatedJson, myKey, mapped2Pojo);
                    } catch (ClassNotFoundException e) {
                        // No POJO required for adding new item in Array (ie no key/value pairs)
                        updatedJson = jsonPathHandlerSet(updatedJson, myKey, myValue.toString().replace("\"", ""));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                } else {  // No POJO needed for update
                    Object myValue = field.getValue();
                    updatedJson = jsonPathHandlerSet(updatedJson, myKey, myValue);
                }
        } // while
        return updatedJson;
    }

    private JsonNode jsonPathHandlerAdd(JsonNode updatedJson, String jsonKey, Object pojo){
        updatedJson = JsonPath.using(configuration).parse(updatedJson.toString()).add(jsonKey, pojo).json();
        return updatedJson;
    }

    private JsonNode jsonPathHandlerSet(JsonNode updatedJson, String jsonKey, Object JsonValue){
        updatedJson = JsonPath.using(configuration).parse(updatedJson.toString()).set(jsonKey, JsonValue).json();
        return updatedJson;
    }

    private JsonNode jsonPathHandlerDelete(JsonNode updatedJson, String jsonkey){
        updatedJson = JsonPath.using(configuration).parse(updatedJson.toString()).delete(jsonkey).json();
        return updatedJson;
    }

    private String templateParamHandler(String jsonKey){
        String[] strArray = jsonKey.split("\\.");
        Pattern p = Pattern.compile("links\\[\\d+\\]$");  // if ends with [d+]
        Matcher m = p.matcher(strArray[0]);
        try {
            if (strArray != null && strArray.length >0 && strArray[0].equals("meta")) {
                jsonKey = "msgParams." + jsonKey;
            } else if (strArray != null && strArray.length >0 && strArray[0].equals("data") || m.find()) {
                jsonKey = "eventParams." + jsonKey;
            } else {
                throw new IllegalArgumentException("jsonKey in data to be parsed is not valid : " + jsonKey);
            }
        }catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("jsonKey in data to be parsed is not valid : " + jsonKey);
        }
      return jsonKey;
    }

    private String schemaClassPathHandler(String jsonkey){
        String[] strArray = jsonkey.split("\\.");
        jsonkey = "";
        for (String s: strArray) {
            jsonkey = jsonkey + s+"[*].";
        }
        jsonkey = "$.properties." + jsonkey + "javaType";
        return jsonkey;
    }

    private String accessFileInSemanticJar(String path) {
        String result="";
        InputStream input = EventTemplateHandler.class.getResourceAsStream(path);
        if (input == null) {
            input = EventTemplateHandler.class.getClassLoader().getResourceAsStream(path);
            try {
                result= IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (NullPointerException e){
                throw new NullPointerException("Can not find path: " + path);
            }
        }
        return result;
    }
} // class EventTemplateHandler
