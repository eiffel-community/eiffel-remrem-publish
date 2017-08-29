package com.ericsson.eiffel.remrem.publish.config;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.remrem.publish.helper.RMQBeanConnectionFactory;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import ch.qos.logback.classic.Logger;

public class RabbitMqProperties {

    private RMQBeanConnectionFactory factory;
    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    private boolean usePersitance = true;

    private String host;
    private String exchangeName;
    private Integer port;
    private String tlsVer;
    private String user;
    private String password;
    private String domainId;
    private Connection rabbitConnection;

    private List<Channel> rabbitChannels;

    Logger log = (Logger) LoggerFactory.getLogger(RMQHelper.class);

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public RMQBeanConnectionFactory getFactory() {
        return factory;
    }

    public void setFactory(RMQBeanConnectionFactory factory) {
        this.factory = factory;
    }

    public Connection getRabbitConnection() {
        return rabbitConnection;
    }

    public void setRabbitConnection(Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
    }

    public void init() {
        log.info("RabbitMqProperties init ...");
        initCli();

        try {
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

            createRabbitMqConnection();

        } catch (KeyManagementException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);            
        }
    }

    /**
     * This method is used to create Rabbitmq connection and channels
     */
    public void createRabbitMqConnection() {
        try {
            rabbitConnection = factory.newConnection();
            log.info("Connected to RabbitMQ.");
            rabbitChannels = new ArrayList<>();
            for (int i = 0; i < CHANNEL_COUNT; i++) {
                rabbitChannels.add(rabbitConnection.createChannel());
            }
        } catch (IOException | TimeoutException e) {
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

    /****
     * This method is used to publish the message to RabbitMQ
     * @param routingKey
     * @param msg is Eiffel Event
     * @throws IOException
     */
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
        log.info("Published message with size {} bytes on exchange '{}' with routing key '{}'", msg.getBytes().length,
                exchangeName, routingKey);
    }

    /***
     * This method is used to give random channel
     * @return channel
     */
    private Channel giveMeRandomChannel() {
        if ((rabbitConnection == null || !rabbitConnection.isOpen())) {
            createRabbitMqConnection();
        }
        return rabbitChannels.get(random.nextInt(rabbitChannels.size()));
    }
}
