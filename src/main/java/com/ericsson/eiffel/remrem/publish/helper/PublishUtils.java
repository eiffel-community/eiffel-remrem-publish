package com.ericsson.eiffel.remrem.publish.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.semantics.SemanticsService;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class PublishUtils {

    private static final String DOT = ".";
    static Logger log = (Logger) LoggerFactory.getLogger(PublishUtils.class);
    /**
     * Method returns the MsgService based on the mp(message protocol) from the list of MsgService beans. 
     * @param mp(message protocol) Specifies which service we consider from the list of MsgService beans
     * @param msgServices List of msgService beans or instances
     * @return msgService or null if message service not available.
     */
    public static MsgService getMessageService(String mp, MsgService msgServices[]) {
        if (StringUtils.isNotBlank(mp)) {
            for (MsgService service : msgServices) {
                if (service.getServiceName().equals(mp)) {
                    return service;
                }
            }
        }
        else {
            for (MsgService service : msgServices) {
                //if (service instanceof SemanticsService) {
                    return service;
               // }
            }
        }
        log.error("No protocol service has been found registered.");
        return null;
    }
    
    /**
     * Method returns the routing key from the messaging service based on the json event type.
     * @param msgService the Messaging service.
     * @param json the eiffel event
     * @param userDomainSuffix is optional parameter, If user provide this it will add to the domainId.
     * @return routing key or returns domain id if family and type not available.
     */
    public static String prepareRoutingKey(MsgService msgService, JsonObject json, RMQHelper rmqHelper,
            String userDomainSuffix) {
        String domainId = rmqHelper.getDomainId();
        if (StringUtils.isNotEmpty(userDomainSuffix)) {
            domainId = domainId + DOT + userDomainSuffix;
        }
        if (msgService != null && StringUtils.isNotEmpty(msgService.getFamily(json))
                && StringUtils.isNotEmpty(msgService.getType(json))) {
            return msgService.getFamily(json) + DOT + msgService.getType(json) + DOT + "notag" + DOT + domainId;
        }
        return domainId;
    }

}
