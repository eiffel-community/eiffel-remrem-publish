/*
    Copyright 2017 Ericsson AB.
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
package com.ericsson.eiffel.remrem.publish.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.semantics.SemanticsService;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class PublishUtils {

    private static final String DOT = ".";
    static Logger log = (Logger) LoggerFactory.getLogger(PublishUtils.class);
    /**
     * Method returns the MsgService based on the mp(message protocol) from the list of MsgService beans. 
     * @param mp(message protocol) Specifies which service we consider from the list of MsgService beans
     * @param msgServices List of msgService beans or instances
     * @return msgService or null if message service not available.
     */
    public static MsgService getMessageService(String mp, MsgService msgServices[]) {
        if (StringUtils.isNotBlank(mp)) {
            for (MsgService service : msgServices) {
                if (service.getServiceName().equals(mp)) {
                    return service;
                }
            }
        }
        else {
            for (MsgService service : msgServices) {
                //if (service instanceof SemanticsService) {
                    return service;
               // }
            }
        }
        log.error("No protocol service has been found registered.");
        return null;
    }
    
    /**
     * Method returns the routing key from the messaging service based on the json event type.
     * @param msgService the Messaging service.
     * @param json the eiffel event
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @return routing key or returns null if family and type not available.
     */
    public static String prepareRoutingKey(MsgService msgService, JsonObject json, RMQHelper rmqHelper,
            String userDomainSuffix) {
        String protocol = msgService.getServiceName();
        RabbitMqProperties rabbitMqProperties = rmqHelper.rabbitMqPropertiesMap.get(protocol);
        if (rabbitMqProperties != null && rabbitMqProperties.getDomainId() != null && rabbitMqProperties.getExchangeName() != null && rabbitMqProperties.getHost() != null) {
            String domainId = rabbitMqProperties.getDomainId();
            String family = msgService.getFamily(json);
            String type = msgService.getType(json);
            if (StringUtils.isNotEmpty(userDomainSuffix)) {
                domainId = domainId + DOT + userDomainSuffix;
            }
            if (msgService != null && StringUtils.isNotEmpty(family) && StringUtils.isNotEmpty(type)) {
                if (msgService instanceof SemanticsService)
                    return PropertiesConfig.PROTOCOL + DOT + family + DOT + type + DOT + "notag" + DOT + domainId;
                else
                    return family + DOT + type + DOT + "notag" + DOT + domainId;
            }
        } else {
            if (Boolean.getBoolean(PropertiesConfig.CLI_MODE)) {
                log.error("RabbitMq properties not configured for protocol " + protocol);
                System.exit(-1);
            }
            return "";
        }
        return null;
    }

}
