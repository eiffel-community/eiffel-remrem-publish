package com.ericsson.eiffel.remrem.publish.cli;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CLI {
	private Options options=null;

	public CLI() {
		options = createCLIOptions();
	}

	/**
	 * Creates the options needed by command line interface
	 * @return
	 */
	private static Options createCLIOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "show help.");
		options.addOption("f", "content_file", true, "event content file");
		return options;
	}
	
	/**
	 * Prints the help for this application and exits.
	 * @param options
	 */
	private static void help(Options options) {
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("java -jar", options);
		System.exit(0);
	}
	
	/**
	 * Parse the given arguments
	 * @param args
	 * @return
	 */
	public boolean parse(String[] args) {
		CommandLineParser parser = new DefaultParser(); 
		boolean startService = true;
		try {
			CommandLine commandLine = parser.parse(options, args);
			Option[] existingOptions = commandLine.getOptions(); 
			if (existingOptions.length > 0) {
				startService = false;
			}

			if (commandLine.hasOption("h")) {
				help(options);
			}
			
			if (commandLine.hasOption("f")) {
				String filePath = commandLine.getOptionValue("f");
				handleContentFile(filePath);
			}
		} catch (Exception e) {
			help(options);
		}
		return startService;
	}

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
			msgService.cleanUp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
