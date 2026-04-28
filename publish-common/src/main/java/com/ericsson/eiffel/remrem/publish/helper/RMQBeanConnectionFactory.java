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

import ch.qos.logback.classic.Logger;
import com.rabbitmq.client.Connection;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.ConnectionFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;

/**
 * Spring-managed RabbitMQ ConnectionFactory that supports automatic SSL context reloading.
 * <p>
 * Extends RabbitMQ's {@link ConnectionFactory} to integrate with Spring's dependency injection
 * and automatically update SSL context when certificates are reloaded without requiring
 * application restart or manual connection recreation.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Spring bean with prototype scope for flexible instantiation</li>
 *   <li>Automatic SSL context updates via {@link SSLContextReloader} integration</li>
 *   <li>Tracks whether the latest SSL context is in use</li>
 *   <li>Seamless certificate rotation for RabbitMQ connections</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * &#64;Autowired
 * private RMQBeanConnectionFactory connectionFactory;
 * 
 * Connection connection = connectionFactory.newConnection();
 * </pre>
 *
 * <h2>SSL Context Reload Behavior</h2>
 * When certificates are reloaded:
 * <ol>
 *   <li>SSLContextReloader notifies this factory via listener callback</li>
 *   <li>Factory updates its SSL context using {@link #useSslProtocol(SSLContext)}</li>
 *   <li>New connections automatically use the updated certificates</li>
 *   <li>Existing connections remain unaffected until recreated</li>
 * </ol>
 *
 * @see SSLContextReloader
 * @see SSLContextReloadListener
 */
@Component
@Scope("prototype")
public class RMQBeanConnectionFactory extends ConnectionFactory {
    private Logger log = (Logger) LoggerFactory.getLogger(RMQBeanConnectionFactory.class);

    @Autowired
    private SSLContextReloader contextReloader;

    // At the very beginning a default SSL context, created at startup of JVM, is used.
    private boolean latestContextUsed = true;

    public RMQBeanConnectionFactory() {
        super();
    }

    /**
     * Initializes the factory by registering an SSL context reload listener.
     * This method is automatically called by Spring after dependency injection.
     */
    @PostConstruct
    private void init() {
        if (contextReloader == null) {
            log.warn("SSLContextReloader is null; will not be able to handle certificate reloads!");
            return;
        }

        // Register a listener that will be called when a new SSL context has been loaded.
        contextReloader.addListener(new SSLContextReloadAdapter() {
            @Override
            public void onContextReloaded(SSLContext sslContext) {
                log.debug("A new SSL context has been set");
                // A new SSL context has been loaded, but no new connection has been
                // created yet.
                RMQBeanConnectionFactory.this.useSslProtocol(sslContext);

                latestContextUsed = false;
            }
        });
    }

    /**
     * Creates a new RabbitMQ connection using the current SSL context.
     * Marks the latest SSL context as used when a new connection is created.
     *
     * @return a new RabbitMQ connection
     * @throws IOException if an I/O error occurs during connection creation
     * @throws TimeoutException if connection creation times out
     */
    @Override
    public Connection newConnection() throws IOException, TimeoutException {
        log.debug("Creating a new connection");
        Connection connection = super.newConnection();
        latestContextUsed = true;
        return connection;
    }

    /**
     * Determines if the latest SSL context is used.
     *
     * @return True if the latest SSL context is used, otherwise false.
     */
    public boolean isLatestContextUsed() {
        return latestContextUsed;
    }
}
