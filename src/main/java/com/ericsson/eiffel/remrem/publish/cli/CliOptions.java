package com.ericsson.eiffel.remrem.publish.cli;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.helper.RemremJarHelper;

public class CliOptions {
	static private Options options=null;
	static private CommandLine commandLine;
	
    // Used for testing purposes
    private static ArrayList<Integer> testErrorCodes = new ArrayList<>();

    public static ArrayList<Integer> getErrorCodes() {
        return testErrorCodes;
    }

    public static void addErrorCode(int errorCode) {
        testErrorCodes.add(errorCode);
    }

    public static void cleanErrorCodes() {
        testErrorCodes.clear();
    }
	public static CommandLine getCommandLine() {
		return commandLine;
	}

	/**
     * Creates the options needed by command line interface
     * @return the options this CLI can handle
     */
    public static void createCLIOptions() {
        options = new Options();
        options.addOption("h", "help", false, "show help.");
        options.addOptionGroup(createContentGroup());
        options.addOptionGroup(createRoutingKeyGroup());
        options.addOption("mb", "message_bus", true, "host of message bus to use, default is 127.0.0.1");
        options.addOption("en", "exchange_name", true, "exchange name, default is amq.direct");
        options.addOption("np", "non_persistent", false, "remove persistence from message sending");
        options.addOption("port", "port", true, "port to connect to message bus");
    }
  
    private static Option createJsonOption() {
    	return new Option("json", "json_content", true, "event content in json string. The value can also be a dash(-) and the json will be read from the output of other programs if piped.");
    }
    
    private static Option createFileOption() {
    	return new Option("f", "content_file", true, "event content file");
    }
    
    private static Option createRoutingKeyOption() {
    	return new Option("rk", "routing_key", true, "routing key, mandatory");
    }

    private static Option createJarPathOption() {
        return new Option("jp", "jar_path", true,
                "path to find protocol definition jar files, e.g. C:/Users/xyz/Desktop/eiffel3messaging.jar");
    }
    private static OptionGroup createContentGroup() {
    	OptionGroup group = new OptionGroup();
    	group.addOption(createFileOption());
    	group.addOption(createJsonOption());
    	group.addOption(createJarPathOption());
    	group.setRequired(true);
    	return group;
    }

    public static void handleJarPath(){
        if (commandLine.hasOption("jp")) {
            String jarPath = commandLine.getOptionValue("jp");
            System.out.println("JarPath :: " +jarPath);
            try {
                RemremJarHelper.addJarsToClassPath(jarPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while loading jars from the path mentioned, MESSAGE :: " + e.getMessage());
            }
        }
    }
    private static OptionGroup createRoutingKeyGroup() {
    	OptionGroup group = new OptionGroup();
    	group.addOption(createRoutingKeyOption());
    	group.setRequired(true);
    	return group;
    }
    
    /**
     * Parse the given arguments and act on them
     * @param args command line arguments
     * @return if the service should start or not
     */
    public static void parse(String[] args) {
    	createCLIOptions();
        CommandLineParser parser = new DefaultParser(); 
        try {
            commandLine = parser.parse(options, args, true); 
            CliOptions.handleMessageBusOptions();
        } catch (Exception e) {
        	e.printStackTrace();
            help();
        }
    }    
    
    /**
     * Checks if any options that CLI can handle have been passed
     * @return true if any valid options have been 
     * 			passed as arguments otherwise false
     */
    public static boolean hasParsedOptions() {
    	if (commandLine == null)
    		return false;
    	
    	Option[] existingOptions = commandLine.getOptions(); 
    	return existingOptions.length > 0;
    }

    /**
     * Prints the help for this application and exits.
     * @param options the options to print usage help for
     */
    public static void help() {
    	CliOptions.clearSystemProperties();
        // This prints out some help    	
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("java -jar", options);
        System.exit(1);
    }
    
    /**
     * Sets the system properties with values passed for 
     * message bus host and exchange name
     * @param commandLine command line arguments
     */
    public static void handleMessageBusOptions(){
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
     * Remove the system properties added by this application 
     */
    public static void clearSystemProperties() {
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
}
