/*
    Copyright 2018 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
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
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import com.ericsson.eiffel.remrem.publish.config.SpringLoggingInitializer;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.PublishResultItem;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
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
@SpringBootApplication
@ComponentScan(basePackages = "com.ericsson.eiffel.remrem")
public class CLI implements CommandLineRunner{

    @Autowired
    @Qualifier("messageServiceRMQImpl")
    MessageService messageService;

    @Autowired
    private MsgService[] msgServices;

    @Autowired
    RMQHelper rmqHelper;

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
        String exchangeName = CliOptions.getCommandLine().getOptionValue("en");
        try {
            String msgProtocol = CliOptions.getCommandLine().getOptionValue("mp");
            MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);
            if(msgService != null) {
                rmqHelper.rabbitMqPropertiesInit(msgService.getServiceName());
                SendResult results = messageService.send(content, msgService, CliOptions.getCommandLine().getOptionValue("ud"), 
                        CliOptions.getCommandLine().getOptionValue("tag"), CliOptions.getCommandLine().getOptionValue("rk"));
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
        }catch (RemRemPublishException e) {
            JsonArray errorResponse = new JsonArray();
            PublishResultItem result = new PublishResultItem(null, 404,
                 null,e.getMessage());
            errorResponse.add(result.toJsonObject());
            System.err.println(new GsonBuilder().setPrettyPrinting().create().toJson(errorResponse));
            CliOptions.exit(CLIExitCodes.HANDLE_CONTENT_FAILED);
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

    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(CLI.class);
        application.addInitializers(new SpringLoggingInitializer());
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        CliOptions.parse(args);
        application.run(args);
    }
}