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
package com.ericsson.eiffel.remrem.publish.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

@Component
public class RabbitMqPropertiesConfig {

    Logger log = (Logger) LoggerFactory.getLogger(RabbitMqPropertiesConfig.class);

    @Autowired
    Environment env;

    @Value("${rabbitmq.instances.jsonlist:{null}}")
    private String rabbitmqInstancesJsonListContent;

    @Value("${jasypt.encryptor.password:{null}}")
    private String jasyptPassword;

    private Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();

    /***
     * This method is used to give RabbitMq properties based on protocol
     * 
     * @return protocol specific RabbitMq properties in map
     */
    public Map<String, RabbitMqProperties> getRabbitMqProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        readCatalinaProperties(map);

        if (map.isEmpty()) {
            log.info("Catalina Properties configuration not provided. Trying to initiate Spring properties instead.");
            readSpringProperties();
        } else {
            log.info("Catalina Properties configuration provided. Populating Rabbitmq properties to rabbitMqPropertiesMap object.");
            populateRabbitMqConfigurationsBasedOnCatalinaProperties(map);

        }
        return rabbitMqPropertiesMap;
    }

    /***
     * Reads catalina properties to a map object.
     * 
     * @param map
     *            RabbitMq instances map object.
     */
    private void readCatalinaProperties(Map<String, Object> map) {
        String catalina_home = System.getProperty("catalina.home").replace('\\', '/');
        for (Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
            PropertySource propertySource = (PropertySource) it.next();
            if (propertySource instanceof MapPropertySource) {
                if (propertySource.getName().contains("[file:" + catalina_home + "/conf/config.properties]")) {
                    map.putAll(((MapPropertySource) propertySource).getSource());
                }
            }
        }
    }

    /***
     * Reads Spring Properties and writes RabbitMq properties to RabbitMq instances properties map object.
     */
    private void readSpringProperties() {
        JsonNode rabbitmqInstancesJsonListJsonArray = null;
        final ObjectMapper objMapper = new ObjectMapper();
        try {
            rabbitmqInstancesJsonListJsonArray = objMapper.readTree(rabbitmqInstancesJsonListContent);

            for (int i = 0; i < rabbitmqInstancesJsonListJsonArray.size(); i++) {
                JsonNode rabbitmqInstanceObject = rabbitmqInstancesJsonListJsonArray.get(i);
                String protocol = rabbitmqInstanceObject.get("mp").asText();
                log.info("Configuring RabbitMq instance for Eiffel message protocol: " + protocol);

                RabbitMqProperties rabbitMqProperties = new RabbitMqProperties();
                rabbitMqProperties.setHost(rabbitmqInstanceObject.get("host").asText());
                rabbitMqProperties.setPort(Integer.parseInt(rabbitmqInstanceObject.get("port").asText()));
                rabbitMqProperties.setUsername(rabbitmqInstanceObject.get("username").asText());
                rabbitMqProperties.setPassword(DecryptionUtils.decryptString(rabbitmqInstanceObject.get("password").asText(), jasyptPassword));
                rabbitMqProperties.setTlsVer(rabbitmqInstanceObject.get("tls").asText());
                rabbitMqProperties.setExchangeName(rabbitmqInstanceObject.get("exchangeName").asText());
                rabbitMqProperties.setDomainId(rabbitmqInstanceObject.get("domainId").asText());

                rabbitMqPropertiesMap.put(protocol, rabbitMqProperties);
            }
        } catch (Exception e) {
            log.error("Failure when initiating RabbitMq Java Spring properties: " + e.getMessage(), e);
        }
    }

    /***
     * Writes RabbitMq catalina properties to RabbitMq instances properties map object.
     * 
     * @param map
     *            RabbitMq instances map object.
     */
    private void populateRabbitMqConfigurationsBasedOnCatalinaProperties(Map<String, Object> map) {
        final String jasyptSecretPassword = (String) map.get("jasypt.encryptor.password");
        for (Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            if (key.contains("rabbitmq")) {
                String protocol = key.split("\\.")[0];
                if (rabbitMqPropertiesMap.get(protocol) == null) {
                    rabbitMqPropertiesMap.put(protocol, new RabbitMqProperties());
                }
                if (key.contains("rabbitmq.host")) {
                    rabbitMqPropertiesMap.get(protocol).setHost(entry.getValue().toString());
                } else if (key.contains("rabbitmq.port")) {
                    rabbitMqPropertiesMap.get(protocol).setPort(Integer.getInteger(entry.getValue().toString()));
                } else if (key.contains("rabbitmq.username")) {
                    rabbitMqPropertiesMap.get(protocol).setUsername(entry.getValue().toString());
                } else if (key.contains("rabbitmq.password")) {
                    rabbitMqPropertiesMap.get(protocol).setPassword(DecryptionUtils.decryptString(entry.getValue().toString(), jasyptSecretPassword));
                } else if (key.contains("rabbitmq.tls")) {
                    rabbitMqPropertiesMap.get(protocol).setTlsVer(entry.getValue().toString());
                } else if (key.contains("rabbitmq.exchangeName")) {
                    rabbitMqPropertiesMap.get(protocol).setExchangeName(entry.getValue().toString());
                } else if (key.contains("rabbitmq.domainId")) {
                    rabbitMqPropertiesMap.get(protocol).setDomainId(entry.getValue().toString());
                }
            }
        }
    }
}
