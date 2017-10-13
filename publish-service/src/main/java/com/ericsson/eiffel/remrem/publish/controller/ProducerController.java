/*
    Copyright 2017 Ericsson AB.
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

import java.util.Map;

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
import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.ericsson.eiffel.remrem.shared.VersionService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ch.qos.logback.classic.Logger;

@RestController
@RequestMapping("/*")
public class ProducerController {

    @Autowired
    private MsgService msgServices[];
    @Autowired
    @Qualifier("messageServiceRMQImpl")
    MessageService messageService;
    @Autowired
    RMQHelper rmqHelper;
    Logger log = (Logger) LoggerFactory.getLogger(ProducerController.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/producer/msg", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity send(@RequestParam(value = "mp", required = false) String msgProtocol,
            @RequestParam(value = "ud", required = false) String userDomain, @RequestBody JsonElement body) {
        MsgService msgService = PublishUtils.getMessageService(msgProtocol, msgServices);

        log.debug("mp: " + msgProtocol);
        log.debug("body: " + body);
        if(msgService != null && msgProtocol != null) {
            rmqHelper.rabbitMqPropertiesInit(msgProtocol);
        }
        SendResult result = messageService.send(body, msgService, userDomain);
        return new ResponseEntity(result, messageService.getHttpStatus());
    }
    
    /**
     * @return this method returns the current version of publish and all loaded protocols.
     */
    @RequestMapping(value = "/versions", method = RequestMethod.GET)
    public JsonElement getVersions() {
        JsonParser parser = new JsonParser();
        Map<String, Map<String, String>> versions = new VersionService().getMessagingVersions();
        return parser.parse(versions.toString());
    }
}
