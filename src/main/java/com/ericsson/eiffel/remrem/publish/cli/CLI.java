package com.ericsson.eiffel.remrem.publish.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.PublishResultItem;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import ch.qos.logback.classic.Logger;

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
	@Autowired
    private MsgService[] msgServices;
	Logger log = (Logger) LoggerFactory.getLogger(CLI.class);
	
    /**
     * Delegates actions depending on the passed arguments
     * @param commandLine command line arguments
     */
    private void handleOptions() {
    	CommandLine commandLine = CliOptions.getCommandLine();    	
    	if (commandLine.hasOption("h")) {
    		System.out.println("You passed help flag.");
    		CliOptions.help(0);
    	} else if (commandLine.hasOption("f")) {
            String filePath = commandLine.getOptionValue("f");
            handleContentFile(filePath);
        } else if (commandLine.hasOption("json")) {
            String content = getJsonString(commandLine);
            handleContent(content);
        } else {
        	System.out.println("Missing arguments, please review your arguments" + 
        						" and check if any mandatory argument is missing");        	
        	CliOptions.help(CLIExitCodes.CLI_MISSING_OPTION_EXCEPTION);
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
    	          CliOptions.exit(CLIExitCodes.READ_JSON_FROM_CONSOLE_FAILED);
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
        } catch (final NoSuchFileException e) {
            log.debug("NoSuchFileException", e);
            System.err.println("File not found: " + e.getMessage());
            CliOptions.exit(CLIExitCodes.HANDLE_CONTENT_FILE_NOT_FOUND_FAILED);        
        } catch (Exception e) {
            System.err.println("Could not read content file. Cause: " + e.getMessage());
            CliOptions.exit(CLIExitCodes.HANDLE_CONTENT_FILE_COULD_NOT_READ_FAILED);
        }
    }
    
    /**
     * Handle event from file
     * @param filePath the path of the file where the messages reside
     */
    public void handleContent(String content) {
        try {
            MsgService msgService = PublishUtils.getMessageService(CliOptions.getCommandLine().getOptionValue("mp"),
                    msgServices);
            if (msgService != null) {
                SendResult results = messageService.send(content, msgService,CliOptions.getCommandLine().getOptionValue("ud"));
                JsonArray jarray=new JsonArray();
                for (PublishResultItem result : results.getEvents()) {
                    jarray.add(result.toJsonObject());
                }
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(jarray));
                messageService.cleanUp();
                CliOptions.clearSystemProperties();
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            log.debug("Exception: ", e);
            System.err.println("Exception: " + e.getMessage());
            CliOptions.exit(CLIExitCodes.HANDLE_CONTENT_FAILED);
        }
    }

	@Override
	public void run(String... args) throws Exception {
		if (CliOptions.hasParsedOptions())
			handleOptions();
		boolean cliMode = Boolean.getBoolean(PropertiesConfig.CLI_MODE);
        if (cliMode) 
        	CliOptions.help(CLIExitCodes.CLI_MISSING_OPTION_EXCEPTION);
	}
}