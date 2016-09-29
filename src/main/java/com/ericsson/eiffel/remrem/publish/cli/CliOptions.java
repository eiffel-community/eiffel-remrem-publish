package com.ericsson.eiffel.remrem.publish.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CliOptions {
	static private Options options=null;
	static private CommandLine commandLine;
	
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
        options.addOption("f", "content_file", true, "event content file");
        options.addOption("json", "json_content", true, "event content in json string");
        options.addOption("mb", "message_bus", true, "host of message bus to use, default is 127.0.0.1");
        options.addOption("en", "exchange_name", true, "exchange name, default is eiffel.poc");
        options.addOption("rk", "routing_key", true, "routing key, mandatory");
        options.addOption("np", "non_persistent", false, "remove persistence from message sending");
        options.addOption("port", "port", true, "port to connect to message bus");
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
            commandLine = parser.parse(options, args); 
        } catch (Exception e) {
        	e.printStackTrace();
            help();
        }
    }
    
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
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("java -jar", options);
        System.exit(1);
    }
}
