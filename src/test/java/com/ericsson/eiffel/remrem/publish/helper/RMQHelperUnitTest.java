package com.ericsson.eiffel.remrem.publish.helper;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.rabbitmq.client.Connection;


public class RMQHelperUnitTest {
	
	private static final String mBusHost= "HostA";
	private static final Integer mBusPort= 1234;
	private static final String exchangeName= "EN1";
	private static final String cliMode= "True";
	private static final String testMode= "True";
	private static final String tlsVer= "1.2";
	private static final String usePersistence= "1.2";
	
	@InjectMocks
	RMQHelper rmqHelper;
	
	@Mock RMQBeanConnectionFactory factory;
    @Mock Connection mockConnection;
    @Mock com.rabbitmq.client.Channel mockChannel;
    
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(factory).useSslProtocol();
        Mockito.when(factory.newConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.createChannel()).thenReturn(mockChannel);
    	initProperties();
    	rmqHelper.init();
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
    }
    
    @Test public void getHostTest() {
    	assertTrue(rmqHelper.getHost().equals(mBusHost));
    }

    @Test public void setHostTest() {
    	String host = "HostA"; 
    	rmqHelper.setHost(host);
    	assertTrue(rmqHelper.getHost().equals(host));
    }

    @Test public void getPortTest() {
    	assertTrue(rmqHelper.getPort().equals(mBusPort));
    }
    
    @Test public void setPortTest() {
    	Integer portNumber = 5678; 
    	rmqHelper.setPort(portNumber);
    	assertTrue(rmqHelper.getPort().equals(portNumber));
    }
    
    @Test public void getTlsVersionTest() {
    	assertTrue(rmqHelper.getTlsVer().equals(tlsVer));
    }
    
    @Test public void setTlsVersionTest() {
    	String tlsVersion = "1.1"; 
    	rmqHelper.setTlsVer(tlsVersion);
    	assertTrue(rmqHelper.getTlsVer().equals(tlsVersion));
    }
    
    @Test public void getExchangeNameTest() {
    	assertTrue(rmqHelper.getExchangeName().equals(exchangeName));
    }
    
    @Test public void setExchangeNameTest() {
    	String exchangeNameTest = "exchangeNameA"; 
    	rmqHelper.setExchangeName(exchangeNameTest);
    	assertTrue(rmqHelper.getExchangeName().equals(exchangeNameTest));
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
    	Method setValueMethod;
		try {
			setValueMethod = RMQHelper.class.getDeclaredMethod("setValues");
			// Make private setValues() method visible with Reflection, so method can be tested.
	    	setValueMethod.setAccessible(true);
	    	try {
				setValueMethod.invoke(rmqHelperTest);
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

    	assertTrue(rmqHelperTest.getHost().equals(host));
    	assertTrue(rmqHelperTest.getPort().equals(portNumber));
    	assertTrue(rmqHelperTest.getTlsVer().equals(tlsVersion));
    	assertTrue(rmqHelperTest.getExchangeName().equals(exchangeNameTest));
    }
}
