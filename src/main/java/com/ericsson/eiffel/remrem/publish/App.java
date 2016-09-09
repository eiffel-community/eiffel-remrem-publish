package com.ericsson.eiffel.remrem.publish;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.ericsson.eiffel.remrem.publish.cli.CLI;

import java.util.Arrays;

@SpringBootApplication 
@Slf4j 
public class App extends SpringBootServletInitializer {
    public static void main(String[] args) {
        
        // CLI class checks if arguments are passed to application
        // and if so we do not start the service but act based on 
        // passed arguments. If no arguments are passed the server
        // will be started
        CLI cli = new CLI();
        boolean needsStartService = cli.parse(args);

        if (needsStartService) {
            startService(args);
        }
    }
    
    private static void startService(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);

        log.info("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            log.info(beanName);
        }
    }
}