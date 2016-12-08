package com.ericsson.eiffel.remrem.publish.service;

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

    public PublishResultItem(String id, int status_code, String result, String message) {
        super();
        this.id = id;
        this.statusCode = status_code;
        this.result = result;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus_code() {
        return statusCode;
    }

    public void setStatus_code(int status_code) {
        this.statusCode = status_code;
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
        return ("id : " + getId() + " , " + "status_code : " + getStatus_code() + " , " + "result : " + getResult() + " , " + "message: " + getMessage() );
    }
}
