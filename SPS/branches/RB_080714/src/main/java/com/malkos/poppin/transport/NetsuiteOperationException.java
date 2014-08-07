package com.malkos.poppin.transport;

import java.util.Date;

import com.malkos.poppin.entities.NSRrequestDetails;

public class NetsuiteOperationException extends Exception {
	
	private NSRrequestDetails requestDetails;
	
	public NetsuiteOperationException(String errorMessage, NSRrequestDetails requestDetails){
		super(errorMessage);
		this.requestDetails = requestDetails;		
	}

	public NSRrequestDetails getRequestDetails() {
		return requestDetails;
	}

	public void setRequestDetails(NSRrequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}	
}

