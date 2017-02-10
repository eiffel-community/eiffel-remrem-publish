package com.ericsson.eiffel.remrem.publish.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.eiffel.remrem.protocol.MsgService;
import com.ericsson.eiffel.remrem.publish.helper.PublishUtils;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonElement;

import ch.qos.logback.classic.Logger;

@RestController
@RequestMapping("/producer")
public class ProducerController {

    @Autowired
    private MsgService msgServices[];
    @Autowired
    @Qualifier("messageServiceRMQImpl")
    MessageService messageService;
    Logger log = (Logger) LoggerFactory.getLogger(ProducerController.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/msg", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity send(@RequestParam(value = "mp", required = false) String msgProtocol,
            @RequestParam(value = "ud", required = false) String userDomain, @RequestBody JsonElement body) {
        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);

        log.debug("mp: " + msgProtocol);
        log.debug("body: " + body);
        SendResult result = messageService.send(body, msgService, userDomain);
        return new ResponseEntity(result, messageService.getHttpStatus());
    }
}
