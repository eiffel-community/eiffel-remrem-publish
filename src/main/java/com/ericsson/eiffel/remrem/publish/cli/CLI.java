package com.ericsson.eiffel.remrem.publish.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.MessageServiceRMQImpl;
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
    
    /**
     * Delegates actions depending on the passed arguments
     * @param commandLine command line arguments
     */
    private void handleOptions() {
    	CommandLine commandLine = CliOptions.getCommandLine();
    	handleMessageBusOptions(commandLine);
    	if (commandLine.hasOption("h")) {
    		System.out.println("You passed help flag.");
    		clearSystemProperties();
    		CliOptions.help();
    	} else if (commandLine.hasOption("f") && commandLine.hasOption("rk")) {
            String filePath = commandLine.getOptionValue("f");
            String routingKey = commandLine.getOptionValue("rk");
            handleContentFile(filePath, routingKey);
        } else if (commandLine.hasOption("json") && commandLine.hasOption("rk")) {
            String content = getJsonString(commandLine);
            String routingKey = commandLine.getOptionValue("rk");
            handleContent(content, routingKey);
        } else {
        	System.out.println("Missing arguments, please review your arguments" + 
        						" and check if any mandatory argument is missing");
        	clearSystemProperties();
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
     * Sets the system properties with values passed for 
     * message bus host and exchange name
     * @param commandLine command line arguments
     */
    private void handleMessageBusOptions(CommandLine commandLine){
    	if (commandLine.hasOption("mb")) {
    		String messageBusHost = commandLine.getOptionValue("mb");
    		String key = PropertiesConfig.MESSAGE_BUS_HOST;
    		System.setProperty(key, messageBusHost);
    	}
    	
    	if (commandLine.hasOption("en")) {
    		String exchangeName = commandLine.getOptionValue("en");
    		String key = PropertiesConfig.EXCHANGE_NAME;
    		System.setProperty(key, exchangeName);
    	}
    	
    	if (commandLine.hasOption("port")) {
    		String exchangeName = commandLine.getOptionValue("port");
    		String key = PropertiesConfig.MESSAGE_BUS_PORT;
    		System.setProperty(key, exchangeName);
    	}
    	
    	String usePersistance = "true";
    	if (commandLine.hasOption("np")) {
    		usePersistance = "false";    		
    	}
    	String key = PropertiesConfig.USE_PERSISTENCE;
		System.setProperty(key, usePersistance);
		key = PropertiesConfig.CLI_MODE;
		System.setProperty(key, "true");
    }

    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContentFile(String filePath, String routingKey) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String fileContent = new String(fileBytes);
            handleContent(fileContent, routingKey);
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
    public void handleContent(String content, String routingKey) {
        try {
            MessageService msgService = new MessageServiceRMQImpl();
            
            List<SendResult> results = msgService.send(content, routingKey);
            for(SendResult result : results)
            	System.out.println(result.getMsg());
            msgService.cleanUp();
            clearSystemProperties();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /**
     * Remove the system properties add by this application 
     */
    private void clearSystemProperties() {
    	String key = PropertiesConfig.MESSAGE_BUS_HOST;
    	System.clearProperty(key);
    	key = PropertiesConfig.EXCHANGE_NAME;
    	System.clearProperty(key);
    	key = PropertiesConfig.USE_PERSISTENCE;
    	System.clearProperty(key);
    	key = PropertiesConfig.CLI_MODE;
    	System.clearProperty(key);
    	key = PropertiesConfig.MESSAGE_BUS_PORT;
    	System.clearProperty(key);
    }

	@Override
	public void run(String... args) throws Exception {
		if (CliOptions.hasParsedOptions())
			handleOptions();
		System.exit(0);
	}

}