package com.ericsson.eiffel.remrem.publish;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.ericsson.eiffel.remrem.publish.config.SpringLoggingInitializer;

@SpringBootApplication
@ComponentScan("com.ericsson.eiffel.remrem")
@PropertySources({ @PropertySource("classpath:config.properties"),
        @PropertySource(value = "file:${catalina.home}/conf/config.properties", ignoreResourceNotFound = true) })
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