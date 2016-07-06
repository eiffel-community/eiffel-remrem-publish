package com.ericsson.eiffel.remrem.publish.service;

import com.ericsson.eiffel.remrem.publish.helper.RMQHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("messageServiceRMQImpl") @Slf4j public class MessageServiceRMQImpl
    implements MessageService {

    private static final String SUCCEED = "succeed";
    @Autowired @Qualifier("rmqHelper") RMQHelper rmqHelper;

    @Override public List<SendResult> send(String routingKey, List<String> msgs) {
        List<SendResult> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(msgs)) {
            for (String msg : msgs) {
                results.add(send(routingKey, msg));
            }
        }
        return results;
    }

    private SendResult send(String routingKey, String msg) {
        String resultMsg = SUCCEED;
        instatiateRmqHelper();
        try {
            rmqHelper.send(routingKey, msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultMsg = e.getStackTrace().toString();
        }
        return new SendResult(resultMsg);
    }
    
    private void instatiateRmqHelper() {
        if (rmqHelper == null) {
            rmqHelper = new RMQHelper();
            rmqHelper.init();
        }
    }
    
    public void cleanUp() {
        if (rmqHelper != null)
            try {
                rmqHelper.cleanUp();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}
