/*
    Copyright 2019 Ericsson AB.
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

    @Value("${generate.server.uri:{null}}")
    private String generateServerUri;

    @Value("${generate.server.path:{null}}")
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