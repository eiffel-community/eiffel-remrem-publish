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

    public void generate(String mp, String msgType, String userDomain, String routingKey, String tag,  String generateServerHost, String generateServerPort) {

        url = "http://{generateServerHost}:{generateServerPort}/{mp}?msgType={msgType}";

        map = new HashMap<String, String>();
        map.put("mp", mp);
        map.put("msgType", msgType);
        map.put("generateServerHost", generateServerHost);
        map.put("generateServerPort", generateServerPort);
        
        if (userDomain == null) {userDomain = "";}
        if (tag == null) {tag = "";}
        if (routingKey == null) {routingKey = "";}
        
        
        

        if (!userDomain.isEmpty()) {
            url += "&ud={ud}";
            map.put("ud", userDomain);
        }

        if (!tag.isEmpty()) {
            url += "&tag={tag}";
            map.put("tag", tag);
        }

        if (!routingKey.isEmpty()) {
            url += "&rk={rk}";
            map.put("rk", routingKey);
        }
    }

}
