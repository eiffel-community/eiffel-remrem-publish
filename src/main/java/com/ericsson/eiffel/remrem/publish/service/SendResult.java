package com.ericsson.eiffel.remrem.publish.service;

import lombok.Getter;

public class SendResult {
    @Getter private String msg;

    public SendResult(String msg) {
        this.msg = msg;
    }

    public SendResult() {
    }
}
