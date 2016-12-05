package com.ericsson.eiffel.remrem.publish.service;

public class SendResult {
    private String msg;

    public String getMsg() {
		return msg;
	}

	public SendResult(String msg) {
        this.msg = msg;
    }

    public SendResult() {
    }
}
