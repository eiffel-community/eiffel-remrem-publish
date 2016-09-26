package com.ericsson.eiffel.remrem.publish.cli;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
    private Options options=null;

    public CLI() {
        options = createCLIOptions();
    }

    /**
     * Creates the options needed by command line interface
     * @return the options this CLI can handle
     */
    private static Options createCLIOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "show help.");
        options.addOption("f", "content_file", true, "event content file, mandatory");
        options.addOption("mb", "message_bus", true, "host of message bus to use, default is 127.0.0.1");
        options.addOption("en", "exchange_name", true, "exchange name, default is eiffel.poc");
        options.addOption("rk", "routing_key", true, "routing key, mandatory");
        options.addOption("np", "non_persistent", false, "remove persistence from message sending");
        
        return options;
    }
    
    /**
     * Prints the help for this application and exits.
     * @param options the options to print usage help for
     */
    private static void help(Options options) {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("java -jar", options);
        System.exit(1);
    }
    
    /**
     * Parse the given arguments and act on them
     * @param args command line arguments
     * @return if the service should start or not
     */
    public boolean parse(String[] args) {
        CommandLineParser parser = new DefaultParser(); 
        boolean startService = true;
        try {
            CommandLine commandLine = parser.parse(options, args);
            Option[] existingOptions = commandLine.getOptions(); 
            if (existingOptions.length > 0) {
                startService = false;
                handleOptions(commandLine);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            help(options);
        }
        return startService;
    }
    
    /**
     * Delegates actions depending on the passed arguments
     * @param commandLine command line arguments
     */
    private void handleOptions(CommandLine commandLine) {
    	handleMessageBusOptions(commandLine);
    	if (commandLine.hasOption("h")) {
    		System.out.println("You passed help flag.");
    		clearSystemProperties();
    		help(options);
    	} else if (commandLine.hasOption("f") && commandLine.hasOption("rk")) {
            String filePath = commandLine.getOptionValue("f");
            String routingKey = commandLine.getOptionValue("rk");
            handleContentFile(filePath, routingKey);
        } else {
        	System.out.println("Missing arguments, please review your arguments" + 
        						" and check if any mandatory argument is missing");
        	clearSystemProperties();
        	help(options);
        }    
    }
    
    /**
     * Sets the system properties with values passed for 
     * message bus host and exchange name
     * @param commandLine command line arguments
     */
    private void handleMessageBusOptions(CommandLine commandLine){
    	if (commandLine.hasOption("mb")) {
    		String messageBusHost = commandLine.getOptionValue("mb");
    		String key = PropertiesConfig.MESSAGE_BUSS_HOST;
    		System.setProperty(key, messageBusHost);
    	}
    	
    	if (commandLine.hasOption("en")) {
    		String exchangeName = commandLine.getOptionValue("en");
    		String key = PropertiesConfig.EXCHANGE_NAME;
    		System.setProperty(key, exchangeName);
    	}
    	
    	String usePersistance = "true";
    	if (commandLine.hasOption("np")) {
    		usePersistance = "false";    		
    	}
    	String key = PropertiesConfig.USE_PERSISTENCE;
		System.setProperty(key, usePersistance);
    }

    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContentFile(String filePath, String routingKey) {
        JsonParser parser = new JsonParser();
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String fileContent = new String(fileBytes);
            JsonArray bodyJson = parser.parse(fileContent).getAsJsonArray();
            List<String> msgs = new ArrayList<>();
            for (JsonElement obj : bodyJson) {
                msgs.add(obj.toString());
            }
            MessageService msgService = new MessageServiceRMQImpl();
            List<SendResult> results = msgService.send(routingKey, msgs);
            msgService.cleanUp();
            clearSystemProperties();
            for(SendResult result : results)
            	System.out.println(result.getMsg());
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
    	String key = PropertiesConfig.MESSAGE_BUSS_HOST;
    	System.clearProperty(key);
    	key = PropertiesConfig.EXCHANGE_NAME;
    	System.clearProperty(key);
    }

	@Override
	public void run(String... args) throws Exception {
		parse(args);		
	}

}