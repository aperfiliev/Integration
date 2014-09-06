package com.malkos.poppin.integration.houzz.entities;

public class ErrorMessageWraped {
	private String errorMessage;
	private boolean isAttachRequest;
	private boolean isAttachResponse;
	
	public ErrorMessageWraped(String errorMessage, boolean isAttachRequest,boolean isAttachResponse){
		this.errorMessage = errorMessage;
		this.isAttachRequest = isAttachRequest;
		this.isAttachResponse = isAttachResponse;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public boolean isAttachRequest() {
		return isAttachRequest;
	}
	public void setAttachRequest(boolean isAttachRequest) {
		this.isAttachRequest = isAttachRequest;
	}
	public boolean isAttachResponse() {
		return isAttachResponse;
	}
	public void setAttachResponse(boolean isAttachResponse) {
		this.isAttachResponse = isAttachResponse;
	}
}
