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

    public void generate(String mp, String msgType, String userDomain, String routingKey, String tag) {

        url = "http://localhost:8987/{mp}?msgType={msgType}";

        map = new HashMap<String, String>();
        map.put("mp", mp);
        map.put("msgType", msgType);
        
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
