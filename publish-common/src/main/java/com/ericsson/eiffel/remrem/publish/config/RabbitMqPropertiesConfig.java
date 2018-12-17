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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;

@Component
public class RabbitMqPropertiesConfig {

    @Autowired
    Environment env;

    @Value("${eiffelsemantics.rabbitmq.host}")
    private String rabbitmqHost;
    
    @Value("${eiffelsemantics.rabbitmq.port}")
    private String rabbitmqPort;

    @Value("${eiffelsemantics.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${eiffelsemantics.rabbitmq.password}")
    private String rabbitmqPassword;
    
    @Value("${eiffelsemantics.rabbitmq.tls}")
    private String rabbitmqTls;
    
    @Value("${eiffelsemantics.rabbitmq.exchangeName}")
    private String rabbitmqExchangeName;
    
    @Value("${eiffelsemantics.rabbitmq.domainId}")
    private String rabbitmqDomainId;
    
    
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
            String protocolName = "eiffelsemantics";
            if (rabbitMqPropertiesMap.get(protocolName) == null) {
                rabbitMqPropertiesMap.put(protocolName, new RabbitMqProperties());
            }
            rabbitMqPropertiesMap.get(protocolName).setHost(rabbitmqHost);
            rabbitMqPropertiesMap.get(protocolName).setPort(Integer.getInteger(rabbitmqPort));
            rabbitMqPropertiesMap.get(protocolName).setUsername(rabbitmqUsername);
            rabbitMqPropertiesMap.get(protocolName).setPassword(rabbitmqPassword);
            rabbitMqPropertiesMap.get(protocolName).setTlsVer(rabbitmqTls);
            rabbitMqPropertiesMap.get(protocolName).setExchangeName(rabbitmqExchangeName);
            rabbitMqPropertiesMap.get(protocolName).setDomainId(rabbitmqDomainId);
            System.out.println("RABBIT MQ MAP: " + rabbitMqPropertiesMap.toString());
            
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
