package com.ericsson.eiffel.remrem.publish.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.eiffel.remrem.publish.App;
import com.ericsson.eiffel.remrem.publish.cli.CliOptions;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
public class JarPathImplUnitTest {

    @Test
    public void testClassLoadingFromJarPath() {
        String jarFile = "src/test/resources/semantics.jar";
        String[] args = { "-rk", "Test", "-jp", jarFile };
        CliOptions.parse(args);
        CliOptions.handleJarPath();
        try {
            Class c = Class.forName("com.ericsson.eiffel.remrem.semantics.EiffelEventType");
            System.out.println("Here Class Name : "+c.getName());
            System.out.println("Here Class Method : "+c.getMethods().length);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testJarLoading() throws Exception {
        String jarFile = "src/test/resources/sample.jar";
        String[] args = { "-rk", "Test", "-jp", jarFile };
        CliOptions.parse(args);
        assertTrue(CliOptions.getErrorCodes().isEmpty());
    }

}