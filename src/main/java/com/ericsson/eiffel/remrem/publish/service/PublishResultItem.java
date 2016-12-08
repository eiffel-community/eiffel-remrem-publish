package com.ericsson.eiffel.remrem.publish.service;

import com.google.gson.annotations.SerializedName;

public class PublishResultItem {

    @SerializedName("id")
    private String id;
    
    @SerializedName("statusCode")
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

    public void setStatus_code(int statusCode) {
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
    
    public String toString()
    {
        return ("id : " + getId() + " , " + "statusCode : " + getStatusCode() + " , " + "result : " + getResult() + " , " + "message: " + getMessage() );
    }
}
