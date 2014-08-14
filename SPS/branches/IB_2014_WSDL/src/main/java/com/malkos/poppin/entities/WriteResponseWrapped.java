package com.malkos.poppin.entities;

import com.netsuite.webservices.platform.messages_2014_2.WriteResponse;

public class WriteResponseWrapped {
	private NSRrequestDetails requestDeatils;
	private WriteResponse writeResponse;
	
	public WriteResponseWrapped(NSRrequestDetails requestDeatils, WriteResponse writeResponseList){
		this.writeResponse = writeResponseList;
		this.requestDeatils = requestDeatils;
	}
	
	public NSRrequestDetails getRequestDeatils() {
		return requestDeatils;
	}
	public void setRequestDeatils(NSRrequestDetails requestDeatils) {
		this.requestDeatils = requestDeatils;
	}
	public WriteResponse getWriteResponse() {
		return writeResponse;
	}
	public void setWriteResponse(WriteResponse writeResponse) {
		this.writeResponse = writeResponse;
	}
}
