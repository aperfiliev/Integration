package com.malkos.poppin.entities;

import java.util.ArrayList;
import java.util.List;

public class OrderErrorDetails {
	private List<String> errorVendorPartNumbers;
	private String requestFilePath;
	private String responseFilePath;
	private String requestType;
	private String requestTime;
	public OrderErrorDetails(){
		errorVendorPartNumbers = new ArrayList<>();
	}
	
	public List<String> getErrorVendorPartNumbers() {
		return errorVendorPartNumbers;
	}
	public void setErrorVendorPartNumbers(List<String> errorVendorPartNumbers) {
		this.errorVendorPartNumbers = errorVendorPartNumbers;
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
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
}
