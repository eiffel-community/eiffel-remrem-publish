package com.ericsson.eiffel.remrem.publish.service;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SendResult {
    
    @SerializedName("events")
    private List<PublishResultItem> events;

    public SendResult(List<PublishResultItem> msg) {
        this.events = msg;
    }

    public SendResult() {
    }

    public void setEvents(List<PublishResultItem> events) {
        this.events = events;
    }

    public List<PublishResultItem> getEvents() {
        return events;
    }}
