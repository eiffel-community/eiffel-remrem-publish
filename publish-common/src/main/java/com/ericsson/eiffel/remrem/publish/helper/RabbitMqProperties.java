package com.ericsson.eiffel.remrem.publish.helper;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
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

    private RMQBeanConnectionFactory factory = new RMQBeanConnectionFactory();
    private static final int CHANNEL_COUNT = 100;
    private static final Random random = new Random();
    private boolean usePersitance = true;

    private String host;
    private String exchangeName;
    private Integer port;
    private String tlsVer;
    private String username;
    private String password;
    private String domainId;
    private Connection rabbitConnection;
    private String protocol;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String user) {
        this.username = user;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Connection getRabbitConnection() {
        return rabbitConnection;
    }

    public void setRabbitConnection(Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
    }

    public void init() {
        log.info("RabbitMqProperties init ...");
        if (Boolean.getBoolean(PropertiesConfig.CLI_MODE)) {
            initCli();
        } else {
            initService();
        }

        madatoryParametersCheck();
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

            if((username != null && !username.isEmpty()) && (password != null && !password.isEmpty())) {
                factory.setUsername(username);
                factory.setPassword(password);
            }

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

    private void initService() {
        if (host == null) {
            host = getValuesFromSystemProperties(protocol + ".rabbitmq.host");
        }

        if (port == null) {
            port = Integer.getInteger(getValuesFromSystemProperties(protocol + ".rabbitmq.port"));
        }

        if (domainId == null) {
            domainId = getValuesFromSystemProperties(protocol + ".rabbitmq.domainId");
        }

        if (tlsVer == null) {
            tlsVer = getValuesFromSystemProperties(protocol + ".rabbitmq.tls");
        }

        if (exchangeName == null) {
            exchangeName = getValuesFromSystemProperties(protocol + ".rabbitmq.exchangeName");
        }

        if (username == null) {
            username = getValuesFromSystemProperties(protocol + ".rabbitmq.username");
        }

        if (password == null) {
            password = getValuesFromSystemProperties(protocol + ".rabbitmq.password");
        }
    }

    private void setValues() {
        host = getValuesFromSystemProperties(PropertiesConfig.MESSAGE_BUS_HOST);
        port = Integer.getInteger(getValuesFromSystemProperties(PropertiesConfig.MESSAGE_BUS_PORT));
        domainId = getValuesFromSystemProperties(PropertiesConfig.DOMAIN_ID);
        tlsVer = getValuesFromSystemProperties(PropertiesConfig.TLS);
        exchangeName = getValuesFromSystemProperties(PropertiesConfig.EXCHANGE_NAME);
        usePersitance = Boolean.getBoolean(PropertiesConfig.USE_PERSISTENCE);
    }

    private String getValuesFromSystemProperties(String propertyName) {
        return System.getProperty(propertyName);
    }

    /****
     * This method is used to check mandatory RabbitMQ properties.
     */
    private void madatoryParametersCheck() {
        if(host == null || exchangeName == null || domainId == null) {
            if (Boolean.getBoolean(PropertiesConfig.CLI_MODE)) {
                System.err.println("Mandatory RabbitMq properties missing");
                System.exit(-1);
            }
        }
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
