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
package com.ericsson.eiffel.remrem.publish;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import com.ericsson.eiffel.remrem.publish.config.SpringLoggingInitializer;

@SpringBootApplication
@ComponentScan("com.ericsson.eiffel.remrem")
@EnableAutoConfiguration(exclude = { JacksonAutoConfiguration.class })
@PropertySource(value = "file:${catalina.home}/conf/config.properties", ignoreResourceNotFound = true)
public class App extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(App.class);
        application.addInitializers(new SpringLoggingInitializer());
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);
        application.setWebEnvironment(true);
        ApplicationContext ctx = application.run(args);
    }
}