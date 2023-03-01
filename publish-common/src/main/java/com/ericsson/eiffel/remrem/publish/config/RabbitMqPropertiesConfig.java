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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.helper.RabbitMqProperties;
import com.ericsson.eiffel.remrem.publish.service.GenerateURLTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

@Component
public class RabbitMqPropertiesConfig {

    static Logger log = (Logger) LoggerFactory.getLogger(RabbitMqPropertiesConfig.class);

    @Autowired
    Environment env;

    @Value("${rabbitmq.instances.jsonlist:#{null}}")
    private String rabbitmqInstancesJsonListContent;

    @Value("${jasypt.encryptor.jasyptKeyFilePath:#{null}}")
    private String jasyptKeyFilePath;

    @Value("${semanticsRoutingkeyTypeOverrideFilepath:#{null}}")
    private String semanticsRoutingkeyTypeOverrideFilepath;

    private Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();

    @Autowired
    private GenerateURLTemplate generateURLTemplate;
    private static final String GENERATE_SERVER_URI = "generate.server.uri";
    private static final String GENERATE_SERVER_PATH = "generate.server.contextpath";

    private static final String PROPERTY_MP = "mp";
    private static final String PROPERTY_HOST = "host";
    private static final String PROPERTY_EXCHANGE_NAME = "exchangeName";
    private static final String PROPERTY_PORT = "port";
    private static final String PROPERTY_TLSVER = "tls";
    private static final String PROPERTY_VIRTUAL_HOST = "virtualHost";
    private static final String PROPERTY_USERNAME = "username";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String PROPERTY_DOMAINID = "domainId";
    private static final String PROPERTY_CHANNELS_COUNT = "channelsCount";
    private static final String PROPERTY_CREATE_EXCHANGE_IF_NOTEXISTING = "createExchangeIfNotExisting";
    private static final String PROPERTY_TCP_TIMEOUT = "tcpTimeOut";
    private static final String PROPERTY_WAIT_FOR_CONFIRMS_TIMEOUT = "waitForConfirmsTimeOut";

    /***
     * This method is used to get the RabbitMq property value.
     *
     * @return RabbitMq property or null if the property name was not found
     */
    String getPropertyAsText(JsonNode node, String property) {
        JsonNode jsonNode = node.get(property);
        return jsonNode != null ? jsonNode.asText() : null;
     }

    /***
     * This method is used to get the RabbitMq property value.
     *
     * @return RabbitMq property or null if the property name was not found
     */
    Boolean getPropertyAsBoolean(JsonNode node, String property) {
        JsonNode jsonNode = node.get(property);
        return jsonNode != null ? jsonNode.asBoolean() : null;
     }

    /***
     * This method is used to give RabbitMq properties based on protocol
     *
     * @return protocol specific RabbitMq properties in map
     */
    public Map<String, RabbitMqProperties> getRabbitMqProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        readSpringProperties();

        loadGenerateConfigurationBasedOnSystemProperties();
        return rabbitMqPropertiesMap;
    }

    /***
     * Reads Spring Properties and writes RabbitMq properties to RabbitMq
     * instances properties map object.
     */
    private void readSpringProperties() {
        JsonNode rabbitmqInstancesJsonListJsonArray = null;
        String jasyptKey = null;
        final ObjectMapper objMapper = new ObjectMapper();
        if (jasyptKeyFilePath != null && !jasyptKeyFilePath.equals("\"\"") && !jasyptKeyFilePath.equals("''")) {
            log.info("Initiating Jasypt Key File");
            jasyptKey = readJasyptKeyFile(jasyptKeyFilePath);
        }

        try {
            rabbitmqInstancesJsonListJsonArray = objMapper.readTree(rabbitmqInstancesJsonListContent);

            for (int i = 0; i < rabbitmqInstancesJsonListJsonArray.size(); i++) {
                JsonNode rabbitmqInstanceObject = rabbitmqInstancesJsonListJsonArray.get(i);
                String protocol = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_MP);
                log.info("Configuring RabbitMq instance for Eiffel message protocol: " + protocol);

                RabbitMqProperties rabbitMqProperties = new RabbitMqProperties();
                rabbitMqProperties.setHost(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_HOST));
                rabbitMqProperties.setPort(Integer.parseInt(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_PORT)));
                String virtualHost = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_VIRTUAL_HOST);
                if(virtualHost != null) {
                    rabbitMqProperties.setVirtualHost(virtualHost);
                }
                rabbitMqProperties.setUsername(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_USERNAME));
                String rabbitMqPassword = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_PASSWORD);
                if (rabbitMqPassword.startsWith("{ENC(") && rabbitMqPassword.endsWith("}")) {
                    rabbitMqPassword = rabbitMqPassword.substring(1, rabbitMqPassword.length() - 1);
                    rabbitMqProperties.setPassword(DecryptionUtils.decryptString(rabbitMqPassword, jasyptKey));
                }
                else{
                    rabbitMqProperties.setPassword(rabbitMqPassword);
                }
                rabbitMqProperties.setTlsVer(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_TLSVER));
                rabbitMqProperties.setExchangeName(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_EXCHANGE_NAME));
                rabbitMqProperties.setCreateExchangeIfNotExisting(getPropertyAsBoolean(rabbitmqInstanceObject, PROPERTY_CREATE_EXCHANGE_IF_NOTEXISTING));
                rabbitMqProperties.setDomainId(getPropertyAsText(rabbitmqInstanceObject, PROPERTY_DOMAINID));
                String channelsCount = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_CHANNELS_COUNT);
                if (channelsCount != null) {
                    rabbitMqProperties.setChannelsCount(
                            Integer.parseInt(channelsCount));
                }
                rabbitMqProperties.setRoutingkeyTypeOverrideFilePath(semanticsRoutingkeyTypeOverrideFilepath);
                String waitForConfirmsTimeOut = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_WAIT_FOR_CONFIRMS_TIMEOUT);
                if (waitForConfirmsTimeOut != null) {
                    rabbitMqProperties.setWaitForConfirmsTimeOut(Long.parseLong(waitForConfirmsTimeOut));
                }
                String tcpTimeOut = getPropertyAsText(rabbitmqInstanceObject, PROPERTY_TCP_TIMEOUT);
                if (tcpTimeOut != null) {
                    rabbitMqProperties.setTcpTimeOut(
                            Integer.parseInt(tcpTimeOut));
                }
                rabbitMqPropertiesMap.put(protocol, rabbitMqProperties);
            }
        } catch (Exception e) {
            log.error("Failure when initiating RabbitMq Java Spring properties: " + e.getMessage(), e);
        }
    }

    /**
     * To read the jasypt key from jasypt.key file
     *
     * @param jasyptKeyFilePath
     *            file path in which jasypt key is stored
     * @return jasypt key fetched from the file
     */
    public static String readJasyptKeyFile(final String jasyptKeyFilePath) {
        String jasyptKey = "";
        final FileInputStream file;
        try {
            if (StringUtils.isNotBlank(jasyptKeyFilePath)) {
                file = new FileInputStream(jasyptKeyFilePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(file));
                jasyptKey = reader.readLine();
                if(jasyptKey == null) {
                    return "";
                }
            }
        } catch (IOException e) {
            log.error("Could not read the jasypt key from the jasypt key file path: " + e.getMessage(), e);
        }
        return jasyptKey;
    }

    /***
     * This method is used to load generate server configuration from JAVA_OPTS.
     */
    private void loadGenerateConfigurationBasedOnSystemProperties() {
        if (StringUtils.isBlank(generateURLTemplate.getGenerateServerUri())) {
            generateURLTemplate.setGenerateServerUri(System.getProperty(GENERATE_SERVER_URI));
        }

        if (StringUtils.isBlank(generateURLTemplate.getGenerateServerContextPath())) {
            generateURLTemplate.setGenerateServerPath(System.getProperty(GENERATE_SERVER_PATH));
        }
    }
}
