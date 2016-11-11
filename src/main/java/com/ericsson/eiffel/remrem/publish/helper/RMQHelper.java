package com.ericsson.eiffel.remrem.publish.helper;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.google.gson.JsonElement;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;

@Component("rmqHelper") @Slf4j public class RMQHelper {

    
	private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    @Value("${rabbitmq.port}") private Integer port;
    @Value("${rabbitmq.user}") private String user;
    @Value("${rabbitmq.password}") private String password;
    private boolean usePersitance = true;
    private Connection rabbitConnection;
    private List<Channel> rabbitChannels;

    public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	@PostConstruct public void init() {
        log.info("RMQHelper init ...");
        initCli();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);  
            
            if (port != null) {
            	factory.setPort(port);
            	log.info("Port is: " + port);
            } else {
            	log.info("Using default rabbit mq port.");
            }
            //factory.useSslProtocol();
            log.info("Host adress: " + host);
            log.info("Exchange is: " + exchangeName);
            rabbitConnection = factory.newConnection();
            rabbitChannels = new ArrayList<>();            
            
            for (int i = 0; i < CHANNEL_COUNT; i++) {
                rabbitChannels.add(rabbitConnection.createChannel());
            }
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage(), e);
//        } catch (KeyManagementException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
    }

    private void initCli() {
    	setValues();
    }
    
    private void setValues() {
    	String passedHost = System.getProperty(PropertiesConfig.MESSAGE_BUS_HOST); 
    	if (passedHost != null) {
    		host = passedHost;
    	}
    	
    	Integer passedPort = Integer.getInteger(PropertiesConfig.MESSAGE_BUS_PORT); 
    	if (passedPort != null) {
    		port = passedPort;
    	}
    	
    	String passedExchange = System.getProperty(PropertiesConfig.EXCHANGE_NAME); 
    	if (passedExchange != null) {
    		exchangeName = passedExchange;
    	}
    	usePersitance = Boolean.getBoolean(PropertiesConfig.USE_PERSISTENCE);
    }
    
    @PreDestroy
    public void cleanUp() throws IOException {
        log.info("RMQHelper cleanUp ...");
        if (rabbitConnection!=null){
            rabbitConnection.close();
            rabbitConnection = null;
        } else {
            log.warn("rabbitConnection is null when cleanUp");
        }
    }

    public void send(String routingKey, String msg) throws IOException {
    	try {
    		Channel channel = giveMeRandomChannel();
    		channel.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                    // Beware that proper synchronization is needed here                   
                    if (cause.isInitiatedByApplication()) {
                        log.debug("Shutdown is initiated by application. Ignoring it.");
                    } else {                    	
                        log.error("Shutdown is NOT initiated by application.");
                        log.error(cause.getMessage());
                        boolean cliMode = Boolean.getBoolean(PropertiesConfig.CLI_MODE);
                        if (cliMode) {
                        	System.exit(-3);
                        }
                    }
                }
            });
    		
    		BasicProperties msgProps = MessageProperties.BASIC;
    		if (usePersitance)
    			msgProps = MessageProperties.PERSISTENT_BASIC;

            channel.basicPublish(exchangeName, routingKey, msgProps, msg.getBytes());
            log.info("Published message with size {} bytes on exchange '{}' with routing key '{}'", msg.getBytes().length, exchangeName, routingKey);
		} catch (Exception e) {
			 log.error("REMREM Publish: Failed to send message: " + e.getMessage(), e);
		}
    }


    private Channel giveMeRandomChannel() {
        return rabbitChannels.get(random.nextInt(rabbitChannels.size()));
    }
    
    public String getEventId(JsonElement json){
    	// Eiffel 1.0
    	if (json.getAsJsonObject().has(PropertiesConfig.EIFFEL_MESSAGE_VERSIONS) && json.getAsJsonObject().getAsJsonObject(PropertiesConfig.EIFFEL_MESSAGE_VERSIONS).entrySet().size()>0){
    	        Set<Entry<String, JsonElement>> entrySet = json.getAsJsonObject().getAsJsonObject(PropertiesConfig.EIFFEL_MESSAGE_VERSIONS).entrySet();
    	        for(Map.Entry<String,JsonElement> entry : entrySet){
    	        	if(json.getAsJsonObject().getAsJsonObject(PropertiesConfig.EIFFEL_MESSAGE_VERSIONS).getAsJsonObject(entry.getKey()).has(PropertiesConfig.EVENT_ID))
    	        	return json.getAsJsonObject().getAsJsonObject(PropertiesConfig.EIFFEL_MESSAGE_VERSIONS).getAsJsonObject(entry.getKey()).get(PropertiesConfig.EVENT_ID).getAsString();
    	        }
		}
    	
    	// Eiffel 2.0
    	if (json.getAsJsonObject().has(PropertiesConfig.META) && json.getAsJsonObject().getAsJsonObject(PropertiesConfig.META).has(PropertiesConfig.ID)){
    		return json.getAsJsonObject().getAsJsonObject(PropertiesConfig.META).get(PropertiesConfig.ID).getAsString();
		}
		return null;
    }
    
}
