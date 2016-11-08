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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

@Component("rmqHelper") @Slf4j public class RMQHelper {

    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    @Value("${rabbitmq.port}") private Integer port;
    @Value("${rabbitmq.tls}") private String tls_ver;
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
	
  public String getTlsVer() {
		return tls_ver;
	}

	public void setTlsVer(String tls_ver) {
		this.tls_ver = tls_ver;
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
            
            log.info("Host adress: " + host);
            log.info("Exchange is: " + exchangeName);
            // "TLSv1.2"
            if (tls_ver != null && !tls_ver.isEmpty()) {
                if (tls_ver.contains("default")) {
                    log.info("Using default TLS version connection to RabbitMQ.");
                    factory.useSslProtocol();
                }
                else {
                    log.info("Using TLS version " + tls_ver + " connection to RabbitMQ.");
                    factory.useSslProtocol("TLSv" + tls_ver);
                }
            }
            else{
            	log.info("Using standard connection method to RabbitMQ.");
            }

            rabbitConnection = factory.newConnection();
            rabbitChannels = new ArrayList<>();            
            
            for (int i = 0; i < CHANNEL_COUNT; i++) {
                rabbitChannels.add(rabbitConnection.createChannel());
            }
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage(), e);
        } catch (KeyManagementException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
        	log.error(e.getMessage(), e);            
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

    	String passedTlsVer = System.getProperty(PropertiesConfig.TLS); 
    	if (passedTlsVer != null) {
    		tls_ver = passedTlsVer;
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
}
