package com.ericsson.eiffel.remrem.publish.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import org.apache.commons.cli.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;

/**
 * Class for interpreting the passed arguments from command line.
 * Parse method returns true, meaning we need to start the service afterwards, if no argument
 * is given. The same method returns false, meaning we do not start the service afterwards, if any
 * argument is given. If an argument is given that it is not recognized we print help
 * 
 * This class also uses System Properties to pass some arguments to underlying service. It is important to
 * choose properties names that are difficult to be matched by the system
 * 
 * @author evasiba
 *
 */
@Component
@ComponentScan(basePackages = "com.ericsson.eiffel.remrem")
public class CLI implements CommandLineRunner{
    
	@Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;
	
    /**
     * Delegates actions depending on the passed arguments
     * @param commandLine command line arguments
     */
    private void handleOptions() {
    	CommandLine commandLine = CliOptions.getCommandLine();    	
    	if (commandLine.hasOption("h")) {
    		System.out.println("You passed help flag.");
    		CliOptions.clearSystemProperties();
    		CliOptions.help();
    	} else if (commandLine.hasOption("f")) {
            String filePath = commandLine.getOptionValue("f");
            handleContentFile(filePath);
        } else if (commandLine.hasOption("json")) {
            String content = getJsonString(commandLine);
            handleContent(content);
        } else {
        	System.out.println("Missing arguments, please review your arguments" + 
        						" and check if any mandatory argument is missing");
        	CliOptions.clearSystemProperties();
        	CliOptions.help();
        }    
    }
    
    private String getJsonString(CommandLine commandLine) {
    	String jsonContent = commandLine.getOptionValue("json");
    	
    	if (jsonContent.equals("-")) {
    		try {
    			InputStreamReader isReader = new InputStreamReader(System.in);
    			BufferedReader bufReader = new BufferedReader(isReader);
    			jsonContent =  bufReader.readLine();
    		} catch (Exception e) {
    			  e.printStackTrace();
    	          System.exit(-5);
    		}
    		
    	}
    	return jsonContent;
    }
    
    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContentFile(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String fileContent = new String(fileBytes);
            handleContent(fileContent);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContent(String content) {
        try {
        	String routingKey = CliOptions.getCommandLine().getOptionValue("rk");
            List<SendResult> results = messageService.send(content, routingKey);
            for(SendResult result : results)
            	System.out.println(result.getMsg());
            messageService.cleanUp();
            CliOptions.clearSystemProperties();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(-1);
        }
    }      

	@Override
	public void run(String... args) throws Exception {
		if (CliOptions.hasParsedOptions())
			handleOptions();
		boolean cliMode = Boolean.getBoolean(PropertiesConfig.CLI_MODE);
        if (cliMode) 
        	CliOptions.help();
	}
}