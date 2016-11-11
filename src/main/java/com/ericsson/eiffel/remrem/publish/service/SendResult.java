package com.ericsson.eiffel.remrem.publish.service;

import java.util.List;

public class SendResult {
    private List<ResultEvent> events;

    public SendResult(List<ResultEvent> msg) {
        this.events = msg;
    }

    public SendResult() {
    }

	public void setEvents(List<ResultEvent> events) {
		this.events = events;
	}

	public List<ResultEvent> getEvents() {
		return events;
	}
}
