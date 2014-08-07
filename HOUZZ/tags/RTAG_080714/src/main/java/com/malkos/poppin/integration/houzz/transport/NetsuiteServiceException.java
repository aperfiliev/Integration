package com.malkos.poppin.integration.houzz.transport;

import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;

public class NetsuiteServiceException extends Exception {
	private NSRrequestDetails requestDetails;
	public NetsuiteServiceException(String message, NSRrequestDetails details){
		super(message);
		this.setRequestDetails(details);
	}
	public NSRrequestDetails getRequestDetails() {
		return requestDetails;
	}
	public void setRequestDetails(NSRrequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}
}
