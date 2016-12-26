package com.ericsson.eiffel.remrem.publish.helper;


import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.rabbitmq.client.AMQP.BasicProperties;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

@Component("rmqHelper") public class RMQHelper {

    private static final String FALSE = "false";
	@Inject
	RMQBeanConnectionFactory factory;

    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    @Value("${rabbitmq.port}") private Integer port;
    @Value("${rabbitmq.tls}") private String tlsVer;
    @Value("${rabbitmq.user}") private String user;
    @Value("${rabbitmq.password}") private String password;
    @Value("${rabbitmq.domainId}") private String domainId;
    private boolean usePersitance = true;
    private Connection rabbitConnection;
    private List<Channel> rabbitChannels;
    
    Logger log = (Logger) LoggerFactory.getLogger(RMQHelper.class);

    public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
    public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getTlsVer() {
		return tlsVer;
	}

	public void setTlsVer(String tlsVer) {
		this.tlsVer = tlsVer;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

	@PostConstruct public void init() {
	    handleLogging();
        log.info("RMQHelper init ...");
        initCli();
        try {
            //ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);  

            if (port != null) {
            	factory.setPort(port);
            	log.info("Port is: " + port);
            } else {
            	log.info("Using default rabbit mq port.");
            }
            
            log.info("Host adress: " + host);
            log.info("Exchange is: " + exchangeName);

            if (tlsVer != null && !tlsVer.isEmpty()) {
                if (tlsVer.contains("default")) {
                	log.info("Using default TLS version connection to RabbitMQ.");
                	factory.useSslProtocol();
                }
                else {
                	log.info("Using TLS version " + tlsVer + " connection to RabbitMQ.");
                	factory.useSslProtocol("TLSv" + tlsVer);
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

        String passedDomain = System.getProperty(PropertiesConfig.DOMAIN_ID);
        if (passedDomain != null) {
            domainId = passedDomain;
        }

    	String passedTlsVer = System.getProperty(PropertiesConfig.TLS); 
    	if (passedTlsVer != null) {
    		tlsVer = passedTlsVer;
    	}
    	
    	String passedExchange = System.getProperty(PropertiesConfig.EXCHANGE_NAME);
    	if (passedExchange != null) {
    		exchangeName = passedExchange;
    	}
    	usePersitance = Boolean.getBoolean(PropertiesConfig.USE_PERSISTENCE);
    }
    
    private void handleLogging() {
        String debug = System.getProperty(PropertiesConfig.DEBUG);
        log.setLevel(Level.ALL);
        if (FALSE.equals(debug)) {
            System.setProperty("logging.level.root", "OFF");
            log.setLevel(Level.OFF);
        }
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
	    }


    private Channel giveMeRandomChannel() {
        return rabbitChannels.get(random.nextInt(rabbitChannels.size()));
    }
}
