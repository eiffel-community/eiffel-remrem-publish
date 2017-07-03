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
package com.ericsson.eiffel.remrem.publish.service;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class PublishResultItem {

    @SerializedName("id")
    private String id;
    
    @SerializedName("status_code")
    private int statusCode;
    
    @SerializedName("result")
    private String result;
    
    @SerializedName("message")
    private String message;

    public PublishResultItem(String id, int statusCode, String result, String message) {
        super();
        this.id = id;
        this.statusCode = statusCode;
        this.result = result;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public JsonObject toJsonObject()
    {
        JsonObject data=new JsonObject();
        data.addProperty("id", getId());
        data.addProperty("status_code",getStatusCode());
        data.addProperty("result", getResult());
        data.addProperty("message", getMessage());
        return data;
    }
}
