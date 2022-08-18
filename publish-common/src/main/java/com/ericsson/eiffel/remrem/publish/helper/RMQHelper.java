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
package com.ericsson.eiffel.remrem.publish.helper;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.config.RabbitMqPropertiesConfig;
import com.ericsson.eiffel.remrem.publish.exception.NackException;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Component("rmqHelper") public class RMQHelper {

    private static final String FALSE = "false";

    @Autowired
    RabbitMqPropertiesConfig rabbitMqPropertiesConfig;

    Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();

    Logger log = (Logger) LoggerFactory.getLogger(RMQHelper.class);

    public Map<String, RabbitMqProperties> getRabbitMqPropertiesMap() {
        return rabbitMqPropertiesMap;
    }

    public void setRabbitMqPropertiesMap(Map<String, RabbitMqProperties> rabbitMqPropertiesMap) {
        this.rabbitMqPropertiesMap = rabbitMqPropertiesMap;
    }

    @PostConstruct
    public void init() throws RemRemPublishException {
        if (!Boolean.getBoolean(PropertiesConfig.CLI_MODE)) {
            log.info("RMQHelper init ...");
            rabbitMqPropertiesMap = rabbitMqPropertiesConfig.getRabbitMqProperties();
            for (String protocol : rabbitMqPropertiesMap.keySet()) {
                protocolInit(protocol);
            }
        }
    }

    /**
     * This method is used to set protocol specific RabbitMQ properties
     * @param protocol name
     * @throws RemRemPublishException
     */
    public void rabbitMqPropertiesInit(String protocol) throws RemRemPublishException{
        if(!rabbitMqPropertiesMap.containsKey(protocol)) {
            rabbitMqPropertiesMap.put(protocol, new RabbitMqProperties());
            protocolInit(protocol);
        }
    }

    /**
     * This method is used to set the values of protocol and initialize the RabbitMq properties
     * @param protocol name
     * @throws RemRemPublishException
     */
    private void protocolInit(String protocol) throws RemRemPublishException {
        RabbitMqProperties rabbitmqProtocolProperties = rabbitMqPropertiesMap.get(protocol);
        rabbitmqProtocolProperties.setProtocol(protocol);
        rabbitmqProtocolProperties.init();
    }

    public void send(String routingKey, String msg, MsgService msgService) throws IOException, NackException, TimeoutException, RemRemPublishException, IllegalArgumentException {
        String protocol = msgService.getServiceName();
        RabbitMqProperties rabbitmqProtocolProperties = rabbitMqPropertiesMap.get(protocol);
        if (rabbitmqProtocolProperties != null) {
            rabbitmqProtocolProperties.send(routingKey, msg);
        } else {
            log.error("RabbitMq properties not configured for the protocol " + protocol);
        }
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        log.info("RMQHelper cleanUp ...");
        for(String protocol : rabbitMqPropertiesMap.keySet()) {
            RabbitMqProperties rabbitmqProtocolProperties = rabbitMqPropertiesMap.get(protocol);
            if (rabbitmqProtocolProperties.getRabbitConnection() != null) {
                rabbitmqProtocolProperties.getRabbitConnection().close();
                rabbitmqProtocolProperties.setRabbitConnection(null);
            } else {
                log.warn("rabbitConnection is null when cleanUp");
            }
        }
    }

    @PostConstruct
    private void handleLogging() {
        String debug = System.getProperty(PropertiesConfig.DEBUG);
        log.setLevel(Level.ALL);
        if (FALSE.equals(debug)) {
            System.setProperty("logging.level.root", "OFF");
            log.setLevel(Level.OFF);
        }
    }

}
