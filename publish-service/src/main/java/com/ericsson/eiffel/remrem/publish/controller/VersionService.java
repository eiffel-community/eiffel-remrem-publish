/*
    Copyright 2017 Ericsson AB.
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
package com.ericsson.eiffel.remrem.publish.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.google.gson.JsonParser;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will search in all registered jars and their manifest file for
 * attribute Remrem-Version-Key. It will return a list with all versions found.
 */
public class VersionService {

    private static final String WEB_INF = "WEB-INF";
    private static final String VERSION = "version";
    private static final String META_INF_MANIFEST_MF = "META-INF/MANIFEST.MF";
    private static final String REMREM_VERSION_KEY = "remremVersionKey";
    private static final String IS_ENDPOINT_VERSION = "isEndpointVersion";
    private static final String ENDPOINT_VERSION = "endpointVersions";
    private static final String SERVICE_VERSION = "serviceVersion";
    private Logger log = (Logger) LoggerFactory.getLogger(VersionService.class);
    JsonParser parser = new JsonParser();
    Map<String, Map<String, String>> versions = new HashMap<>();
    Map<String, String> endpointVersions = new HashMap<String, String>();
    Map<String, String> serviceVersion = new HashMap<String, String>();
    /**
     * This method will load and parse the MINIFEST files to get the version of
     * the loaded messaging protocols. It is required to define the versions as
     * mainifest attributes in the build.gradle or pom.xml files using
     * attributes "remremVersionKey" and "isEndpointVersion" to specify the type
     * of the protocol or service and if it is endpoint or not respectively.
     * Example for build.gradle: manifest { attributes('remremVersionKey':
     * 'semanticsVersion') attributes('semanticsVersion': version)
     * attributes('isEndpointVersion': 'true') }
     * 
     * @return a map containing the protocol and service types with their
     * versions {"endpointVersions" : {"semanticsVersion" : "1.1.1"},
     * "serviceVersion": {"remremGenerateVersion": "0.1.1"}}
     */
    public Map<String, Map<String, String>> getMessagingVersions() {
        Enumeration<?> resEnum;
        
        try {
            resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL) resEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes mainAttribs = manifest.getMainAttributes();
                        String versionKey = mainAttribs.getValue(REMREM_VERSION_KEY);
                        if (versionKey != null) {
                            String version = mainAttribs.getValue(versionKey);
                            if (version != null) {
                                
                                if (mainAttribs.getValue(IS_ENDPOINT_VERSION) != null) {
                                    if(endpointVersions.get(versionKey) == null) {
                                        endpointVersions.put(versionKey, version);
                                    }
                                } else {
                                    serviceVersion.put(versionKey, version);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Silently ignore wrong manifests on classpath?
                    log.debug("Ignore wrong manifests on classpath ",e.getMessage());
                }
            }
            if(serviceVersion.isEmpty()){
                serviceVersion=getServiceVersion();
            }
            versions.put(ENDPOINT_VERSION, endpointVersions);
            versions.put(SERVICE_VERSION, serviceVersion);
        } catch (IOException e1) {
            // Silently ignore wrong manifests on classpath?
            log.debug("Ignore wrong manifests on classpath ",e1.getMessage());
        }
        return versions;
    }
    
    /**
     * this method will parse manifest file of current project.
     *
     * @return map containing the version of current project.
     */
    public Map<String, String> getServiceVersion() {
        String resourcesPath = this.getClass().getClassLoader().getResource("").getPath();
        String manifestPath = resourcesPath.substring(0, resourcesPath.lastIndexOf(WEB_INF)).concat(META_INF_MANIFEST_MF);
        try {
            Manifest manifest = new Manifest(new FileInputStream(manifestPath));
            Attributes mainAttribs = manifest.getMainAttributes();
            String versionKey = mainAttribs.getValue(REMREM_VERSION_KEY);
            if (versionKey != null) {
                String version = mainAttribs.getValue(versionKey);
                if (version != null) {
                    serviceVersion.put(VERSION, version);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceVersion;
    }
}

