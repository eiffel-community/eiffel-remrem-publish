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

    @Value("${generate.server.uri}")
    private String generateServerUri;

    @Value("${generate.server.path}")
    private String generateServerPath;

    public String getUrl() {
        return generateServerUri + generateServerPath + "/{mp}?msgType={msgType}";
    }

    public Map<String, String> getMap(final String mp, final String msgType) {
        Map<String, String> map = new HashMap<>();
        map.put("mp", mp);
        map.put("msgType", msgType);
        return map;
    }

}
