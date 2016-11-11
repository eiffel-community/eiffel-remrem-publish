package com.ericsson.eiffel.remrem.publish.service;

import java.util.List;

public class SendResult {
    private List<PublishResult> events;

    public SendResult(List<PublishResult> msg) {
        this.events = msg;
    }

    public SendResult() {
    }

    public void setEvents(List<PublishResult> events) {
        this.events = events;
    }
    public List<PublishResult> getEvents() {
        return events;
    }

	public void setEvents(List<ResultEvent> events) {
		this.events = events;
	}

	public List<ResultEvent> getEvents() {
		return events;
	}
}
