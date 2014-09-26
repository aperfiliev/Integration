package com.malkos.poppin.transport;

import com.malkos.poppin.entities.NSRrequestDetails;

public class NetsuiteOperationException extends Exception {
	private NSRrequestDetails requestDetails;
	public NetsuiteOperationException(String message, NSRrequestDetails details){
		super(message);
		this.setRequestDetails(details);
	}
	/*public NetsuiteOperationException(String message){
		super(message);
	}*/
	public NSRrequestDetails getRequestDetails() {
		return requestDetails;
	}
	public void setRequestDetails(NSRrequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}
}
