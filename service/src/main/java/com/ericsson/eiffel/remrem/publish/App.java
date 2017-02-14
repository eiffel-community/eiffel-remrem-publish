package com.ericsson.eiffel.remrem.publish;

import java.util.List;

import org.springframework.boot.Banner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.eiffel.remrem.publish.config.SpringLoggingInitializer;

@SpringBootApplication  
@ComponentScan("com.ericsson.eiffel.remrem")
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