package com.ericsson.eiffel.remrem.publish.helper;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.ConnectionFactory;

/*
 * This class make it possible to use ConnctionFactory in normal execution and
 * for unit-tests. This solves the NoBeanDefinitionExeception for the @inject ConnectionFactory.  
 */
@Component
public class RMQBeanConnectionFactory extends ConnectionFactory {

}
