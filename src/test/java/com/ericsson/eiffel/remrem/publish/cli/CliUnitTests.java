package com.ericsson.eiffel.remrem.publish.cli;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.ericsson.eiffel.remrem.publish.cli.CLIExitCodes;
import com.ericsson.eiffel.remrem.publish.cli.CLI;
import com.ericsson.eiffel.remrem.publish.cli.CliOptions;
import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
//import com.ericsson.eiffel.remrem.shared.MsgService;


public class CliUnitTests {
    private PrintStream console;
    private ByteArrayOutputStream bytes;
	
//    @Mock
//    private MsgService msgService;	

    private CLI cli;
	
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        String key = PropertiesConfig.TEST_MODE;
        System.setProperty(key, "true");
        // Switch std out to another stream in case
        // we need to check output
        bytes   = new ByteArrayOutputStream();		  
        console = System.out;
        System.setOut(new PrintStream(bytes));
//        MsgService[] msgServices = {msgService};
//        cli = new CLI(msgServices);
        cli = new CLI();
        
//        Mockito.when(msgService.generateMsg(
//	                Mockito.anyString(),
//	                Mockito.anyObject()
//	        )).thenReturn("{ \"service\":\"msgService\" }");
    }
	
    @After
    public void tearDown() {
        System.clearProperty(PropertiesConfig.TEST_MODE);
        System.setOut(console);
        // reset error code since it is static
        CliOptions.cleanErrorCodes();
    }
    
    @Test
    public void testRunCliOptionsHFlag() throws Exception {	
        String[] args = {"-h"};
        CliOptions.parse(args);
        cli.run(args);		
        int code = 0;
        assertTrue(CliOptions.getErrorCodes().contains(code));		
    }
    
    @Test
    public void testRunCliOptionsMissingFlags() throws Exception {	
        String[] args = {""};
        CliOptions.parse(args);
        cli.run(args);		
        int code = CLIExitCodes.CLI_MISSING_OPTION_EXCEPTION;
        assertTrue(CliOptions.getErrorCodes().contains(code));		
    }
}
