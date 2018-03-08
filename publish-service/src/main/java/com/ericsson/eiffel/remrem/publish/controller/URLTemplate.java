package com.ericsson.eiffel.remrem.publish.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for generating URL template
 *
 */
public class URLTemplate {

    private String url;
    private Map<String, String> map = new HashMap<String, String>();

    public String getUrl() {
        return url;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void generate(String mp, String msgType, String generateServerHost, String generateServerPort, String generateServerAppName) {

        url = "http://{generateServerHost}:{generateServerPort}/{generateServerAppName}/{mp}?msgType={msgType}";

        map = new HashMap<>();
        map.put("mp", mp);
        map.put("msgType", msgType);
        map.put("generateServerHost", generateServerHost);
        map.put("generateServerPort", generateServerPort);
        map.put("generateServerAppName", generateServerAppName);

    }

}
