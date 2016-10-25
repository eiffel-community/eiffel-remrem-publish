package com.ericsson.eiffel.remrem.publish.config;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.jmx.EndpointMBeanExporter;
import org.springframework.boot.autoconfigure.logging.AutoConfigurationReportLoggingInitializer;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.ericsson.eiffel.remrem.publish.App;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class SpringLoggingInitializer implements ApplicationContextInitializer {

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextInitializer#initialize(org.springframework.context.ConfigurableApplicationContext)
	 * 
	 * We need to turn off Spring logging since we want write the generated message to console. 
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Class[] loggers = {SpringApplication.class, App.class, ConfigFileApplicationListener.class, EndpointMBeanExporter.class,
				AutoConfigurationReportLoggingInitializer.class};
		Logger log = (Logger) LoggerFactory.getLogger("ROOT");
		log.setLevel(Level.INFO);
		for (Class logger : loggers) {
			log = (Logger) LoggerFactory.getLogger(logger);
			log.setLevel(Level.INFO);
		}
	}
}
