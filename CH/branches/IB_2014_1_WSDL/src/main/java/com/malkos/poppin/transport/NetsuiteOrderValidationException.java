package com.malkos.poppin.transport;

import com.malkos.poppin.entities.NSRrequestDetails;

public class NetsuiteOrderValidationException extends NetsuiteOperationException {	
		public NetsuiteOrderValidationException(String message,  NSRrequestDetails details) {
			super(message, details);		
		}
}
