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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;

import ch.qos.logback.classic.Logger;

@Component
public class RabbitMqPropertiesConfig {

    Logger log = (Logger) LoggerFactory.getLogger(RabbitMqPropertiesConfig.class);
    
    @Autowired
    Environment env;

    @Value("${rabbitmq.instances.jsonlist}")
    private String rabbitmqInstancesJsonListContent;
    
    private Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();

    /***
     * This method is used to give RabbitMq properties based on protocol
     * @return protocol specific RabbitMq properties in map
     */
    public Map<String, RabbitMqProperties> getRabbitMqProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        String catalina_home = System.getProperty("catalina.home").replace('\\', '/');
        for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource propertySource = (PropertySource) it.next();
            if (propertySource instanceof MapPropertySource) {
                if(propertySource.getName().contains("[file:"+catalina_home+"/conf/config.properties]")) {
                    map.putAll(((MapPropertySource) propertySource).getSource());
                }
            }
        }
        if (map.isEmpty()) {
            JSONArray rabbitmqInstancesJsonListJsonArray = null;
            try {
                rabbitmqInstancesJsonListJsonArray = new JSONArray(rabbitmqInstancesJsonListContent);

                for (int i = 0; i < rabbitmqInstancesJsonListJsonArray.length(); i++) {
                    JSONObject rabbitmqInstanceObject = (JSONObject)rabbitmqInstancesJsonListJsonArray.get(i);
                    String protocol= rabbitmqInstanceObject.get("mp").toString();
                    log.info("Configuring RabbitMq instance for Eiffel message protocol: " + protocol);
                    
                    RabbitMqProperties rabbitMqProperties = new RabbitMqProperties();
                    rabbitMqProperties.setHost(rabbitmqInstanceObject.get("host").toString());
                    rabbitMqProperties.setPort(Integer.parseInt(rabbitmqInstanceObject.get("port").toString()));
                    rabbitMqProperties.setUsername(rabbitmqInstanceObject.get("username").toString());
                    rabbitMqProperties.setPassword(rabbitmqInstanceObject.get("password").toString());
                    rabbitMqProperties.setTlsVer(rabbitmqInstanceObject.get("tls").toString());
                    rabbitMqProperties.setExchangeName(rabbitmqInstanceObject.get("exchangeName").toString());
                    rabbitMqProperties.setDomainId(rabbitmqInstanceObject.get("domainId").toString());
                    
                    rabbitMqPropertiesMap.put(protocol, rabbitMqProperties);
                }
            } catch (Exception e) {
                log.error("Failure when initiating RabbitMq Java Spring properties: " + e.getMessage(), e);
            }
        } else {
            for (Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
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
                        rabbitMqPropertiesMap.get(protocol).setPassword(entry.getValue().toString());
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
        return rabbitMqPropertiesMap;
    }
}
