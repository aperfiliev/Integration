package com.malkos.poppin.entities;

import com.netsuite.webservices.platform.messages_2013_1.WriteResponseList;

public class WriteResponseListWrapped {
	private NSRrequestDetails requestDeatils;
	private WriteResponseList writeResponseList;
	
	public WriteResponseListWrapped(NSRrequestDetails requestDeatils, WriteResponseList writeResponseList){
		this.writeResponseList = writeResponseList;
		this.requestDeatils = requestDeatils;
	}
	
	public NSRrequestDetails getRequestDeatils() {
		return requestDeatils;
	}
	public void setRequestDeatils(NSRrequestDetails requestDeatils) {
		this.requestDeatils = requestDeatils;
	}
	public WriteResponseList getWriteResponseList() {
		return writeResponseList;
	}
	public void setWriteResponseList(WriteResponseList writeResponseList) {
		this.writeResponseList = writeResponseList;
	}
	
}
