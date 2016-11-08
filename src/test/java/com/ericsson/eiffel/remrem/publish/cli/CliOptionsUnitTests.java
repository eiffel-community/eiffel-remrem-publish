package com.ericsson.eiffel.remrem.publish.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;

public class CliOptionsUnitTests {
    private PrintStream console;
    private ByteArrayOutputStream bytes;
	
	@Before public void setUp() throws Exception {
        String key = PropertiesConfig.TEST_MODE;
        System.setProperty(key, "true");
        //Switch std out to another stream
        bytes   = new ByteArrayOutputStream();
        console = System.out;
        System.setOut(new PrintStream(bytes));
    }

    @After
    public void tearDown() {
        System.clearProperty(PropertiesConfig.TEST_MODE);
        System.setOut(console);
        // reset error code since it is static
        CliOptions.cleanErrorCodes();
    }

}
