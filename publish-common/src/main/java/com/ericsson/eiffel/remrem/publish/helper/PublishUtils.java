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
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class PublishUtils {

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
     * @param tag is optional parameter, If user provide this it will add to the tag.
     * @param routingKey is optional parameter, If user provide this it will add to the routingKey.
     * @return routing key or returns "" if host, exchange and domainId not available.
    */
    public static String getRoutingKey(MsgService msgService, JsonObject json, RMQHelper rmqHelper, String userDomainSuffix, String tag, String routingKey) {
        String protocol = msgService.getServiceName();
        Boolean cliMode = Boolean.getBoolean(PropertiesConfig.CLI_MODE);
        RabbitMqProperties rabbitMqProperties = rmqHelper.rabbitMqPropertiesMap.get(protocol);
        String domainId = rabbitMqProperties.getDomainId();
        if (rabbitMqProperties != null && rabbitMqProperties.getExchangeName() != null && rabbitMqProperties.getHost() != null
                && (cliMode || (!cliMode && StringUtils.isNotBlank(domainId)))) {
            return StringUtils.defaultIfBlank(routingKey, msgService.generateRoutingKey(json, tag, domainId, userDomainSuffix));
        }
        return "";
    }
}
