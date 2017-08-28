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


import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.config.RabbitMqProperties;
import com.ericsson.eiffel.remrem.publish.config.RabbitMqPropertiesConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Component("rmqHelper") public class RMQHelper {

    @Inject
    RMQBeanConnectionFactory factory;
    private static final String FALSE = "false";

    @Autowired
    RabbitMqPropertiesConfig rabbitMqPropertiesConfig;

    Map<String, RabbitMqProperties> rabbitMqPropertiesMap;

    Logger log = (Logger) LoggerFactory.getLogger(RMQHelper.class);

    public Map<String, RabbitMqProperties> getRabbitMqPropertiesMap() {
        return rabbitMqPropertiesMap;
    }

    public void setRabbitMqPropertiesMap(Map<String, RabbitMqProperties> rabbitMqPropertiesMap) {
        this.rabbitMqPropertiesMap = rabbitMqPropertiesMap;
    }

    @PostConstruct public void init() {
        handleLogging();
        log.info("RMQHelper init ...");
        rabbitMqPropertiesMap = rabbitMqPropertiesConfig.getRabbitMqProperties();
        for(String protocol : rabbitMqPropertiesMap.keySet()) {
            rabbitMqPropertiesMap.get(protocol).setFactory(factory);
            rabbitMqPropertiesMap.get(protocol).init();
        }
    }

    public void send(String routingKey, String msg, MsgService msgService) throws IOException {
        if(rabbitMqPropertiesMap.get(msgService.getServiceName()) != null) {
            rabbitMqPropertiesMap.get(msgService.getServiceName()).send(routingKey,msg);
        } else {
            rabbitMqPropertiesMap.get("eiffelsemantics").send(routingKey,msg);
        }
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        log.info("RMQHelper cleanUp ...");
        for(String protocol : rabbitMqPropertiesMap.keySet()) {
            rabbitMqPropertiesMap.get(protocol).getRabbitConnection();
            if (rabbitMqPropertiesMap.get(protocol).getRabbitConnection() != null){
                rabbitMqPropertiesMap.get(protocol).getRabbitConnection().close();
                rabbitMqPropertiesMap.get(protocol).setRabbitConnection(null);
            } else {
                log.warn("rabbitConnection is null when cleanUp");
            }
        }
    }

    private void handleLogging() {
        String debug = System.getProperty(PropertiesConfig.DEBUG);
        log.setLevel(Level.ALL);
        if (FALSE.equals(debug)) {
            System.setProperty("logging.level.root", "OFF");
            log.setLevel(Level.OFF);
        }
    }
}
