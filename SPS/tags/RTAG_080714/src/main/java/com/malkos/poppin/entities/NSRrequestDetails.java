package com.malkos.poppin.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.util.SoapMessageType;

public class NSRrequestDetails {
	private String requestFilePath;
	private String responseFilePath;
	private Date requestDateTime;
	private SoapMessageType requestType;
	private String nsUsername;
	private String errorId;
	
	public NSRrequestDetails(){		
	}
	
	public NSRrequestDetails(String requestFilePath, String responseFilePath, Date requestDateTime, SoapMessageType requestType){
		this.requestFilePath = requestFilePath;
		this.responseFilePath = responseFilePath;
		this.requestDateTime = requestDateTime;
		this.requestType = requestType;
		this.nsUsername = GlobalPropertiesProvider.getGlobalProperties().getNsUsername();
	}
	
	public String getRequestFilePath() {
		return requestFilePath;
	}
	public void setRequestFilePath(String requestFilePath) {
		this.requestFilePath = requestFilePath;
	}
	public String getResponseFilePath() {
		return responseFilePath;
	}
	public void setResponseFilePath(String responseFilePath) {
		this.responseFilePath = responseFilePath;
	}
	public String getRequestDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
		if (requestDateTime!=null){
			return dateFormat.format(requestDateTime);
		}
		return null;
	}
	public void setRequestDateTime(Date requestDateTime) {
		this.requestDateTime = requestDateTime;
	}
	public SoapMessageType getRequestType() {
		return requestType;
	}
	public void setRequestType(SoapMessageType requestType) {
		this.requestType = requestType;
	}
	public String getNsUsername() {
		return nsUsername;
	}
	public void setNsUsername(String nsUsername) {
		this.nsUsername = nsUsername;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	public String getRequestTypeAsString(){
		return requestType.toString();
	}
}
