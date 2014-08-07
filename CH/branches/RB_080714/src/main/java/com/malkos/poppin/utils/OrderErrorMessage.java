package com.malkos.poppin.utils;

import java.util.List;

import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;

public class OrderErrorMessage {
	private String poNumber;
	private String mbNumber;
	private String mbFile;
	private String poOrderDate;
	private String errorDetails;
	private String itemId;
	private String requestFilePath;
	private String responseFilePath;
	private String requestType;
	private List<String> vendorSKU;
	private String nsUsername;
	private String errorId;
	private String requestDateTime;	
	
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public String getMbNumber() {
		return mbNumber;
	}
	public void setMbNumber(String mbNumber) {
		this.mbNumber = mbNumber;
	}
	public String getMbFile() {
		return mbFile;
	}
	public void setMbFile(String mbFile) {
		this.mbFile = mbFile;
	}
	public String getPoOrderDate() {
		return poOrderDate;
	}
	public void setPoOrderDate(String poOrderDate) {
		this.poOrderDate = poOrderDate;
	}
	public String getErrorDetails() {
		return errorDetails;
	}
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	
	@Override
	public String toString() {
		return "Order day : " + poOrderDate + "\r\n" +
			   "BATCH # : " + mbNumber + "\r\n" +
			   "PO # : " + poNumber + "\r\n" +
			   "Partner: Staples.com\r\n" +
			   "BATCH FILE PATH # : " + mbFile + "\r\n" +
			   "ERROR MESSAGE : " + errorDetails;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemURL() {
		if (itemId!=null){
			String result;
			if (GlobalPropertiesProvider.getGlobalProperties().getEnvironment().equals("sandbox")){
				result = "https://system.sandbox.netsuite.com/app/common/item/item.nl?id="+itemId;
			} else {
				result = "https://system.netsuite.com/app/common/item/item.nl?id="+itemId;
			}
			return result;
		}
		return null;
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
	public String getVendorSKU() {
		String result ="";
		for (String number: vendorSKU){
			result+="Invalid VendorPartNumber : "+number+ "\r\n";
		}
		if (!result.isEmpty()){
			return result;
		} else {
			return null;
		}
	}
	public void setVendorSKU(List<String> vendorSKU) {
		this.vendorSKU = vendorSKU;
	}
	public String getNsUsername() {
		return nsUsername;
	}
	public void setNsUsername(String nsUsername) {
		this.nsUsername = nsUsername;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getErrorId() {
		return errorId;
	}
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	public String getRequestDateTime() {
		return requestDateTime;
	}
	public void setRequestDateTime(String requestDateTime) {
		this.requestDateTime = requestDateTime;
	}
	
}
