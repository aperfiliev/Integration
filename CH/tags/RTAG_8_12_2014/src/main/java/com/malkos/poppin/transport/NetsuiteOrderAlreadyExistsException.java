package com.malkos.poppin.transport;

import com.malkos.poppin.entities.NSRrequestDetails;

public class NetsuiteOrderAlreadyExistsException extends NetsuiteOperationException {
	public NetsuiteOrderAlreadyExistsException(String message,  NSRrequestDetails details) {
		super(message, details);		
	}

}
