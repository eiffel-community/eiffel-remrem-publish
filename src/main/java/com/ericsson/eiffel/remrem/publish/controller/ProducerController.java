package com.ericsson.eiffel.remrem.publish.controller;

import com.ericsson.eiffel.remrem.publish.helper.ResponseHelper;
import com.ericsson.eiffel.remrem.publish.service.MessageService;
import com.ericsson.eiffel.remrem.publish.service.SendResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j @RestController @RequestMapping("/producer") public class ProducerController {

    @Autowired @Qualifier("messageServiceRMQImpl") MessageService messageService;
    @Autowired @Qualifier("responseHelper") ResponseHelper responseHelper;

    @RequestMapping(value = "/msg", method = RequestMethod.POST) @ResponseBody
    public DeferredResult<ResponseEntity<?>> send(@RequestParam(value = "rk", required = true) String routingKey,
        @RequestBody JsonArray body) {

        // This prevents string formation... performance wise it is better this way
        if (log.isDebugEnabled()){
            log.debug("routingKey: " + routingKey);
            log.debug("body: " + body);
        }


        List<String> msgs = new ArrayList<>();
        for (JsonElement obj : body) {
            msgs.add(obj.toString());
        }

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        ListenableFuture<List<SendResult>> repositoryListDto = messageService.send(routingKey, msgs);
        repositoryListDto.addCallback(
            new ListenableFutureCallback<List<SendResult>>() {
                @Override
                public void onSuccess(List<SendResult> result) {
                    ResponseEntity<List<SendResult>> responseEntity =
                        new ResponseEntity<>(result, HttpStatus.OK);
                    deferredResult.setResult(responseEntity);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("Failed to fetch result from remote service", t);
                    ResponseEntity<Void> responseEntity =
                        new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                    deferredResult.setResult(responseEntity);
                }
            }
        );
        return deferredResult;
        //return responseHelper.convert(results);
    }
}
