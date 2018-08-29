/*
    Copyright 2018 Ericsson AB.
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
