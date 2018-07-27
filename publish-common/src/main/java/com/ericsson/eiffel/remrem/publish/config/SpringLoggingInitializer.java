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
package com.ericsson.eiffel.remrem.publish.config;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.jmx.EndpointMBean;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

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
		Class[] loggers = {SpringApplication.class, /*App.class,*/ ConfigFileApplicationListener.class, EndpointMBean.class,
		        ConditionEvaluationReportLoggingListener.class};
		Logger log = (Logger) LoggerFactory.getLogger("ROOT");
		log.setLevel(Level.ERROR);
		for (Class logger : loggers) {
			log = (Logger) LoggerFactory.getLogger(logger);
			log.setLevel(Level.ERROR);
		}
	}
}
