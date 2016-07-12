package com.ericsson.eiffel.remrem.publish.cli;

import java.io.IOException;
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
 * @author evasiba
 *
 */
public class CLI {
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
        options.addOption("f", "content_file", true, "event content file");
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
        System.exit(0);
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
        if (commandLine.hasOption("f")) {
            String filePath = commandLine.getOptionValue("f");
            handleContentFile(filePath);
        } else {
        	help(options);
        }    
    }

    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContentFile(String filePath) {
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
            List<SendResult> results = msgService.send("test", msgs);
            for(SendResult result : results)
            	System.out.println(result.getMsg());
            msgService.cleanUp();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}