package com.ericsson.eiffel.remrem.publish.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@RestController @RequestMapping("/producer") public class ProducerController {
    
    @Autowired
    private MsgService msgServices[] ;
    @Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;

     @RequestMapping(value = "/msg", method = RequestMethod.POST)
     @ResponseBody
        public SendResult send(@RequestParam(value = "mp", required = false) String msgProtocol,
                @RequestBody JsonElement body) { 
            MsgService msgService = PublishUtils.getMessageService(msgProtocol,msgServices);
            System.out.println("MsgService is :" + msgService);
            
            //log.debug("routingKey: " + routingKey);
            //log.debug("body: " + body);
            return messageService.send(body, msgService);
        }}
