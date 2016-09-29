package com.ericsson.eiffel.remrem.publish;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.eiffel.remrem.publish.cli.CliOptions;
import com.ericsson.eiffel.remrem.publish.config.SpringLoggingInitializer;

@SpringBootApplication  
@ComponentScan("com.ericsson.eiffel.remrem")
public class App extends SpringBootServletInitializer {
	 public static void main(String[] args) {
	    	startService(args);
	    }
	    
	    private static void startService(String[] args) {	
	    	CliOptions.parse(args);
	    	SpringApplication application = new SpringApplication(App.class);
	    	application.addInitializers(new SpringLoggingInitializer());
	    	application.setBannerMode(Banner.Mode.OFF);
	    	application.setLogStartupInfo(false);
	    	// We do not start web service if any arguments are passed
	    	if (CliOptions.hasParsedOptions())
	    		application.setWebEnvironment(false);
	        ApplicationContext ctx = application.run(args);
	    }
}