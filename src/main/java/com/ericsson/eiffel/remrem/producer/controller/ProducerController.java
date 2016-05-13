package com.ericsson.eiffel.remrem.producer.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.ericsson.eiffel.remrem.producer.helper.ResponseHelper;
import com.ericsson.eiffel.remrem.producer.service.MessageService;
import com.ericsson.eiffel.remrem.producer.service.SendResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j @RestController @RequestMapping("/producer") public class ProducerController {

    @Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;
    @Autowired @Qualifier("responseHelper") ResponseHelper responseHelper;

    @RequestMapping(value = "/msg", method = RequestMethod.POST) @ResponseBody
    public List<String> send(@RequestParam(value = "rk", required = true) String routingKey,
        @RequestBody JsonArray body) {
        log.debug("routingKey: " + routingKey);
        log.debug("body: " + body);

        List<String> msgs = new ArrayList<>();
        for (JsonElement obj : body) {
            msgs.add(obj.toString());
        }
        List<SendResult> results = messageService.send(routingKey, msgs);
        return responseHelper.convert(results);
    }
}
