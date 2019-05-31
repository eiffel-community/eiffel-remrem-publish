package com.ericsson.eiffel.remrem.publish.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

	public String getGenerateServerUri() {
		return generateServerUri;
	}

	public void setGenerateServerUri(String generateServerUri) {
		this.generateServerUri = generateServerUri;
	}

	public String getGenerateServerPath() {
		return generateServerPath;
	}

	public void setGenerateServerPath(String generateServerPath) {
		this.generateServerPath = generateServerPath;
	}

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
