package com.ericsson.eiffel.remrem.publish.helper;


import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

@Component("rmqHelper") @Slf4j public class RMQHelper {

    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    @Value("${rabbitmq.port}") private Integer port;
    private boolean usePersitance = false;
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
        if (host == null || exchangeName == null || port == null) {
            Yaml yaml = new Yaml();
            try {
                String fileName = "application.yml";
                ClassLoader classLoader = getClass().getClassLoader(); 
                InputStream ios = classLoader.getResourceAsStream(fileName);

                // Parse the YAML file and return the output as a series of Maps and Lists
                Map<String,Object> result = (Map<String,Object>)yaml.load(ios);
                Map<String,Object> rmq = (Map<String,Object>)result.get("rabbitmq");
                if (host == null)
                	host = (String)rmq.get("host");
                if (port == null)
                	port = (Integer)rmq.get("port");
                Map<String,Object> rmqExchange = (Map<String,Object>)rmq.get("exchange");
                if (exchangeName == null)
                	exchangeName = (String)rmqExchange.get("name");
                int i = 2;
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
    }
    
    private void setValues() {
    	if (host == null) {
    		host = System.getProperty(PropertiesConfig.MESSAGE_BUS_HOST);
    	}
    	if (port == null) {
    		port = Integer.getInteger(PropertiesConfig.MESSAGE_BUS_PORT);
    	}
    	if (exchangeName == null) {
    		exchangeName = System.getProperty(PropertiesConfig.EXCHANGE_NAME);
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
		} catch (Exception e) {
			// TODO: handle exception
		}
    }


    private Channel giveMeRandomChannel() {
        return rabbitChannels.get(random.nextInt(rabbitChannels.size()));
    }
}
