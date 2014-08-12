package com.malkos.poppin.entities;

import java.util.List;

public class CHIntegrationError {
	private String errorMessage;
	private List<String> attachmentsList;
	
	public CHIntegrationError(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public CHIntegrationError() {}

	public boolean hasAttachments(){
		if ((attachmentsList!=null)&&(!attachmentsList.isEmpty())) return true;
		return false;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public List<String> getAttachmentsList() {
		return attachmentsList;
	}
	public void setAttachmentsList(List<String> attachmentsList) {
		this.attachmentsList = attachmentsList;
	}
}