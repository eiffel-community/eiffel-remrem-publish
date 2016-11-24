package com.ericsson.eiffel.remrem.publish.helper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.eiffel.remrem.publish.cli.CliOptions;
import com.ericsson.eiffel.remrem.publish.listener.DirectoryWatchService;
import com.ericsson.eiffel.remrem.publish.listener.SimpleJarDirectoryWatchService;

@Component("jarHelper")
public class RemremJarHelper {

    @Value("${jar.path}")
    private String jarPath;

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    @PostConstruct
    public void init() {
        if (CliOptions.getCommandLine() == null) {
            System.out.println("Adding the jars present in " + jarPath);
            addJarsToClassPath(jarPath);
            lookupForJarFileChanges();
        } else if (!CliOptions.getCommandLine().hasOption("jp")) {
            System.out.println("Adding the jars present in " + jarPath);
            addJarsToClassPath(jarPath);
        }
    }

    private void lookupForJarFileChanges() {
        try {
            final DirectoryWatchService jarPathListener = new SimpleJarDirectoryWatchService();
            String jarPath = getJarPath();
            System.out.println("Listening to the changes in Jar Path " + jarPath);
            if (jarPath != null) {
                System.out.println("Creating path listener");
                jarPathListener.register(new DirectoryWatchService.OnFileChangeListener() {
                    @Override
                    public final void onFileCreate(final String filePath) {
                        addJarsToClassPath(filePath);
                    }

                    @Override
                    public final void onFileModify(final String filePath) {
                        addJarsToClassPath(filePath);
                    }

                    @Override
                    public final void onFileDelete(final String filePath) {
                        addJarsToClassPath(filePath);
                    }
                }, jarPath);
                jarPathListener.start();
            }
        } catch (IOException e) {
            System.out.println("Failed to listen to changes in jars from : " + jarPath);
            System.out.println(e.getMessage() + ":" + e.getCause());
        }

    }

    public static void addJarsToClassPath(String jarPath) {
        if (jarPath != null) {
            File f = new File(jarPath);
            try {
                URL u = f.toURI().toURL();
                URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Class<?> urlClass = URLClassLoader.class;
                Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
                method.setAccessible(true);
                method.invoke(urlClassLoader, new Object[] { u });
            } catch (MalformedURLException e) {
                System.out.println("Invalid jarPath ... " + jarPath);
                System.out.println(e.getMessage() + ":" + e.getCause());
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                System.out.println("Failed to load jars from : " + jarPath);
                System.out.println(e.getMessage() + ":" + e.getCause());
            }
        }
    }
}