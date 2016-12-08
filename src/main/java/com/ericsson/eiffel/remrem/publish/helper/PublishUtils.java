package com.ericsson.eiffel.remrem.publish.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.semantics.SemanticsService;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;

public class PublishUtils {

    static Logger log = (Logger) LoggerFactory.getLogger(PublishUtils.class);
    /**
     * Method returns the MsgService based on the mp(message protocol) from the list of MsgService beans. 
     * @param mp
     * @param msgServices
     * @return
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
     * 
     * @param msgService
     * @param json
     * @return
     */
    public static String prepareRoutingKey(MsgService msgService, JsonObject json,RMQHelper rmqHelper) {
        if (msgService != null && StringUtils.isNotEmpty(msgService.getFamily(json))
                && StringUtils.isNotEmpty(msgService.getType(json))
                && StringUtils.isNotEmpty(rmqHelper.getDomainId())) {
            return msgService.getFamily(json) + "." + msgService.getType(json) + "." + "notag" + "."
                    + rmqHelper.getDomainId();
        }
        return null;
    }

}
