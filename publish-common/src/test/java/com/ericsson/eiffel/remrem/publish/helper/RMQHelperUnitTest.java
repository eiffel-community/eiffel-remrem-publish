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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.config.RabbitMqPropertiesConfig;
import com.rabbitmq.client.Connection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RabbitMqProperties.class, RMQHelper.class })
public class RMQHelperUnitTest {

    private static final String mBusHost= "HostA";
    private static final Integer mBusPort= 1234;
    private static final String exchangeName= "EN1";
    private static final String cliMode= "True";
    private static final String testMode= "True";
    private static final String tlsVer= "1.2";
    private static final String usePersistence= "1.2";
    private static final String domainId= "eiffelxxx";
    private static final Integer channelsCount= 1;
    private String protocol = "eiffelsemantics";
    private String createExchange = "true";

    @InjectMocks
    RMQHelper rmqHelper;

    @Mock RMQBeanConnectionFactory factory;
    @Mock Connection mockConnection;
    @Mock com.rabbitmq.client.Channel mockChannel;
    @Mock RabbitMqPropertiesConfig rabbitMqPropertiesConfig;
    RabbitMqProperties rabbitMqProperties = new RabbitMqProperties();

    Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(factory).useSslProtocol();
        Mockito.when(factory.newConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
        PowerMockito.whenNew(RabbitMqProperties.class).withNoArguments().thenReturn(rabbitMqProperties);
        initProperties();
        rabbitMqProperties.setFactory(factory);
        rmqHelper.rabbitMqPropertiesInit(protocol);
    }

    @After public void tearDown() throws Exception {
        cleanProperties();
        rmqHelper.cleanUp();
    }

    private void initProperties() {
        String key = PropertiesConfig.MESSAGE_BUS_HOST;
        System.setProperty(key, mBusHost);
        key = PropertiesConfig.MESSAGE_BUS_PORT;
        System.setProperty(key, Integer.toString(mBusPort));
        key = PropertiesConfig.EXCHANGE_NAME;
        System.setProperty(key, exchangeName);
        key = PropertiesConfig.CLI_MODE;
        System.setProperty(key, cliMode);
        key = PropertiesConfig.TEST_MODE;
        System.setProperty(key, testMode);
        key = PropertiesConfig.TLS;
        System.setProperty(key, tlsVer);
        key = PropertiesConfig.USE_PERSISTENCE;
        System.setProperty(key, usePersistence);
        key = PropertiesConfig.CREATE_EXCHANGE_IF_NOT_EXISTING;
        System.setProperty(key, createExchange);
        key = PropertiesConfig.DOMAIN_ID;
        System.setProperty(key, domainId);
        key = PropertiesConfig.CHANNELS_COUNT;
        System.setProperty(key, Integer.toString(channelsCount));
    }

    private void cleanProperties() {
        String key = PropertiesConfig.MESSAGE_BUS_HOST;
        System.clearProperty(key);
        key = PropertiesConfig.MESSAGE_BUS_PORT;
        System.clearProperty(key);
        key = PropertiesConfig.EXCHANGE_NAME;
        System.clearProperty(key);
        key = PropertiesConfig.CLI_MODE;
        System.clearProperty(key);
        key = PropertiesConfig.TEST_MODE;
        System.clearProperty(key);
        key = PropertiesConfig.TLS;
        System.clearProperty(key);
        key = PropertiesConfig.USE_PERSISTENCE;
        System.clearProperty(key);
        key = PropertiesConfig.CREATE_EXCHANGE_IF_NOT_EXISTING;
        System.setProperty(key, createExchange);
        key = PropertiesConfig.DOMAIN_ID;
        System.clearProperty(key);
        key = PropertiesConfig.CHANNELS_COUNT;
        System.clearProperty(key);
    }

    @Test public void getHostTest() {
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getHost().equals(mBusHost));
    }

    @Test public void setHostTest() {
        String host = "HostA";
        rmqHelper.rabbitMqPropertiesMap.get(protocol).setHost(host);
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getHost().equals(host));
    }

    @Test
    public void testConnection() {
        assertNull(rmqHelper.rabbitMqPropertiesMap.get(protocol).getRabbitConnection());
        rmqHelper.rabbitMqPropertiesMap.get(protocol).createRabbitMqConnection();
        assertNotNull(rmqHelper.rabbitMqPropertiesMap.get(protocol).getRabbitConnection());
    }

    @Test public void getPortTest() {
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getPort().equals(mBusPort));
    }

    @Test public void setPortTest() {
        Integer portNumber = 5678;
        rmqHelper.rabbitMqPropertiesMap.get(protocol).setPort(portNumber);
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getPort().equals(portNumber));
    }

    @Test public void getTlsVersionTest() {
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getTlsVer().equals(tlsVer));
    }

    @Test public void setTlsVersionTest() {
        String tlsVersion = "1.1";
        rmqHelper.rabbitMqPropertiesMap.get(protocol).setTlsVer(tlsVersion);
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getTlsVer().equals(tlsVersion));
    }

    @Test public void getExchangeNameTest() {
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getExchangeName().equals(exchangeName));
    }

    @Test public void setExchangeNameTest() {
        String exchangeNameTest = "exchangeNameA";
        rmqHelper.rabbitMqPropertiesMap.get(protocol).setExchangeName(exchangeNameTest);
        assertTrue(rmqHelper.rabbitMqPropertiesMap.get(protocol).getExchangeName().equals(exchangeNameTest));
    }

    @Test public void setValuesTest() {
        cleanProperties();

        String host = "HOSTC";
        Integer portNumber = 1928;
        String tlsVersion = "1.0";
        String exchangeNameTest = "EN2";
        String usePersistenceTest = "false";

        String key = PropertiesConfig.MESSAGE_BUS_HOST;
        System.setProperty(key, host);
        key = PropertiesConfig.MESSAGE_BUS_PORT;
        System.setProperty(key, Integer.toString(portNumber));
        key = PropertiesConfig.TLS;
        System.setProperty(key, tlsVersion);
        key = PropertiesConfig.EXCHANGE_NAME;
        System.setProperty(key, exchangeNameTest);
        key = PropertiesConfig.USE_PERSISTENCE;
        System.setProperty(key, usePersistenceTest);

        RMQHelper rmqHelperTest = new RMQHelper();
        RabbitMqProperties rabbitMqProperties = new RabbitMqProperties();
        Map<String, RabbitMqProperties> rabbitMqPropertiesMap = new HashMap<String, RabbitMqProperties>();
        Method setValueMethod;
        try {
            setValueMethod = RabbitMqProperties.class.getDeclaredMethod("setValues");
            // Make private setValues() method visible with Reflection, so method can be tested.
            setValueMethod.setAccessible(true);
            try {
                setValueMethod.invoke(rabbitMqProperties);
                rabbitMqPropertiesMap.put(protocol,rabbitMqProperties);
                rmqHelperTest.setRabbitMqPropertiesMap(rabbitMqPropertiesMap);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                assertTrue(false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                assertTrue(false);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (SecurityException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        assertTrue(rmqHelperTest.rabbitMqPropertiesMap.get(protocol).getHost().equals(host));
        assertTrue(rmqHelperTest.rabbitMqPropertiesMap.get(protocol).getPort().equals(portNumber));
        assertTrue(rmqHelperTest.rabbitMqPropertiesMap.get(protocol).getTlsVer().equals(tlsVersion));
        assertTrue(rmqHelperTest.rabbitMqPropertiesMap.get(protocol).getExchangeName().equals(exchangeNameTest));
    }

    @Test
    public void testJasyptFileSuccess() throws IOException {
        String jasyptPath = "src/test/resources/jasypt.key";
        String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptPath);
        assertEquals("docker", jasyptKey);
    }

    @Test
    public void testJasyptFileWithEmptyKey() {
        String jasyptPath = "src/test/resources/emptyJasypt.key";
        String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptPath);
        assertEquals("", jasyptKey);
    }

    @Test
    public void testJasyptFileFailure() throws IOException {
        String jasyptPath = "src/test/jasypt.key";
        String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptPath);
        assertEquals("", jasyptKey);
    }
}
