package com.malkos.poppin.transport;

public class NetsuiteNullResponseException extends Exception{
	private String message = "";
	private final String NULL_RESPONSE_MESSAGE = " Something is wrong with connection to Netsuite. Possible reasons: connection time out, multiple requests at one time. Also please check your connection configuration.";
	public String getExceptionMessage() {
		return message;
	}
	public NetsuiteNullResponseException(String message){
		super();
		this.message = message  + ". " + NULL_RESPONSE_MESSAGE;;
	}
	@Override
	public String getMessage() {
		return message;
	}
}
