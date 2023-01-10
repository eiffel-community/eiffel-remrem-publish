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


import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

import org.mockito.MockitoAnnotations;

import com.ericsson.eiffel.remrem.publish.config.PropertiesConfig;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.exception.RemRemPublishException;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.PublishResultItem;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonArray;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PublishUtils.class)
public class CliUnitTests {
    private PrintStream console;
    private ByteArrayOutputStream bytes;

    @Mock
    MsgService eiffelsemanticsMsgService;

    @Mock
    MessageService messageService;

    @Mock
    RMQHelper rmqHelper;

    @Mock
    SendResult res;

    @Mock
    PublishResultItem resultItem;

    @InjectMocks
    private CLI cli;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        String key = PropertiesConfig.TEST_MODE;
        System.setProperty(key, "true");

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

    @Test
    public void testCreateExchangeDisable() throws Exception {
        PowerMockito.mockStatic(PublishUtils.class);
        Mockito.when(PublishUtils.getMessageService(Mockito.eq("eiffelsemantics"), Mockito.any()))
                .thenReturn(eiffelsemanticsMsgService);
        Mockito.when(eiffelsemanticsMsgService.getServiceName()).thenReturn("eiffelsemantics");
        Mockito.doThrow(new RemRemPublishException("message")).when(rmqHelper)
                .rabbitMqPropertiesInit(Mockito.anyString());
        File file = new File("src/test/resources/publishMessages.json");
        if (file.exists()) {
            System.out.println("fileExist");
        }
        String[] args = { "-f", "src/test/resources/publishMessages.json", "-mb", "127.0.0.1", "-exchange_name",
                "1test", "-ce", "false", "-mp", "eiffelsemantics" };
        CliOptions.parse(args);
        cli.run(args);
        int code = CLIExitCodes.HANDLE_CONTENT_FAILED;
        assertTrue(CliOptions.getErrorCodes().contains(code));
    }

    @Test
    public void testCreateExchangeEnable() throws Exception {
        PowerMockito.mockStatic(PublishUtils.class);
        Mockito.when(PublishUtils.getMessageService(Mockito.eq("eiffelsemantics"), Mockito.any()))
                .thenReturn(eiffelsemanticsMsgService);
        Mockito.when(eiffelsemanticsMsgService.getServiceName()).thenReturn("eiffelsemantics");
        Mockito.when(messageService.send(Mockito.anyString(), Mockito.any(), Mockito.isNull(), Mockito.isNull(),
                Mockito.isNull())).thenReturn(res);
        JsonArray jarray = new JsonArray();
        List<PublishResultItem> results = new ArrayList<>();
        jarray.add(results.add(resultItem));
        Mockito.when(res.getEvents()).thenReturn(results);
        File file = new File("src/test/resources/publishMessages.json");
        if (file.exists()) {
            System.out.println("fileExist");
        }
        String[] args = { "-f", "src/test/resources/publishMessages.json", "-mb", "127.0.0.1", "-exchange_name",
                "1test", "-ce", "true", "-mp", "eiffelsemantics" };
        CliOptions.parse(args);
        cli.run(args);
        assertTrue(CliOptions.getErrorCodes().isEmpty());
    }
}
