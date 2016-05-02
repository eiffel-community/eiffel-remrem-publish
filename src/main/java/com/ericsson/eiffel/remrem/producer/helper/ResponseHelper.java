package com.ericsson.eiffel.remrem.producer.helper;

import com.ericsson.eiffel.remrem.producer.service.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component("responseHelper") public class ResponseHelper {
    public List<String> convert(List<SendResult> results) {
        List<String> responses = new ArrayList<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (SendResult result : results) {
                responses.add(result.getMsg());
            }
        }
        return responses;
    }
}
