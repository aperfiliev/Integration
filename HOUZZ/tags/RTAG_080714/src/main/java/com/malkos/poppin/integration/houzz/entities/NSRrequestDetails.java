package com.malkos.poppin.integration.houzz.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;

public class NSRrequestDetails {
	private String requestFilePath;
	private String responseFilePath;
	private Date requestDateTime;
	private String requestType;
	private String nsUsername;
	private String errorId;
	private String retailer;
	
	public NSRrequestDetails(){		
	}
	
	public NSRrequestDetails(String requestFilePath, String responseFilePath, Date requestDateTime){
		this.requestFilePath = requestFilePath;
		this.responseFilePath = responseFilePath;
		this.requestDateTime = requestDateTime;
		this.requestType = "SEARCH";
		this.nsUsername = GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigEmail();
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
		if (requestDateTime!=null){
			return dateFormat.format(requestDateTime);
		}
		return null;
	}
	public void setRequestDateTime(Date requestDateTime) {
		this.requestDateTime = requestDateTime;
	}
	public String getRequestType() {
		return requestType;
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

	public String getRetailer() {
		return retailer;
	}

	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}
}
