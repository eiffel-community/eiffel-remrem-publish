package com.ericsson.eiffel.remrem.publish.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for generating URL template of REMReM Generate Service
 *
 */
public class GenerateURLTemplate {

    private String url;
    private Map<String, String> map;

    public GenerateURLTemplate(final String mp, final String msgType, final String generateServerHost,
                               final String generateServerPort, final String generateServerAppName) {
        this.url = "http://{generateServerHost}:{generateServerPort}/{generateServerAppName}/{mp}?msgType={msgType}";
        this.map = new HashMap<>();
        this.map.put("mp", mp);
        this.map.put("msgType", msgType);
        this.map.put("generateServerHost", generateServerHost);
        this.map.put("generateServerPort", generateServerPort);
        this.map.put("generateServerAppName", generateServerAppName);
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getMap() {
        return map;
    }

}
