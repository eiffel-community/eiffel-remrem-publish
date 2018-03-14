package com.ericsson.eiffel.remrem.publish.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for generating URL template of REMReM Generate Service
 *
 */
@Component
public class GenerateURLTemplate {

    @Value("${generate.server.host}")
    private String generateServerHost;

    @Value("${generate.server.port}")
    private String generateServerPort;

    @Value("${generate.server.appName}")
    private String generateServerAppName;

    private String url;
    private Map<String, String> map = new HashMap<>();

    public void generate(final String mp, final String msgType) {
        url = "http://{generateServerHost}:{generateServerPort}/{generateServerAppName}/{mp}?msgType={msgType}";
        map.put("mp", mp);
        map.put("msgType", msgType);
        map.put("generateServerHost", generateServerHost);
        map.put("generateServerPort", generateServerPort);
        map.put("generateServerAppName", generateServerAppName);
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getMap() {
        return map;
    }

}
