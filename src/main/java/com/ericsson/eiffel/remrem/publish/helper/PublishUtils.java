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
                if (service instanceof SemanticsService) {
                    return service;
                }
            }
        }
        System.out.println("No protocol service has been found registered.");
        log.error("No protocol service has been found registered.");
        // CLIOptions.exit(CLIExitCodes.MESSAGE_PROTOCOL_NOT_FOUND);
        return null;
    }
    
    /**
     * Method returns the routing key from the messaging service based on the json event type.
     * @param msgService the Messaging service.
     * @param json the eiffel event
     * @param userDomain is optional parameter, If user provide this it will add to the domainId.
     * @return routing key or null if routing key not available
     */
    public static String prepareRoutingKey(MsgService msgService, JsonObject json, RMQHelper rmqHelper,
            String userDomain) {
        String domainId = "";
        if (msgService != null && StringUtils.isNotEmpty(msgService.getFamily(json))
                && StringUtils.isNotEmpty(msgService.getType(json))
                && StringUtils.isNotEmpty(rmqHelper.getDomainId())) {
            domainId = rmqHelper.getDomainId();
            if (StringUtils.isNotEmpty(userDomain)) {
                domainId = domainId + DOT + userDomain;
            }
            return msgService.getFamily(json) + DOT + msgService.getType(json) + DOT + "notag" + DOT + domainId;
        }
        return null;
    }

}
