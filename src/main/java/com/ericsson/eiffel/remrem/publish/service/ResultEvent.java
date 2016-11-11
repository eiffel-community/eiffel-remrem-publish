package com.ericsson.eiffel.remrem.publish.service;

public class ResultEvent {

	private String id;
	private int status_code;
	private String result;
	private String message;
	
	
	
	public ResultEvent(String id, int status_code, String result, String message) {
		super();
		this.id = id;
		this.status_code = status_code;
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
		return status_code;
	}
	public void setStatus_code(int status_code) {
		this.status_code = status_code;
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

	@Override
	public String toString() {
		return "{id=" + id + ", status_code=" + status_code + ", result=" + result + ", message=" + message
				+ "}";
	}
	
	
}
