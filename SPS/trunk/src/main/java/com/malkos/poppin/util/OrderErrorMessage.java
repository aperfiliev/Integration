package com.malkos.poppin.util;

import java.util.List;

import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;

public class OrderErrorMessage {
	private String poNumber;
	private String pathFile;
	private String poOrderDate;
	private String errorDetails;
	private String retailer;
	private List<String> vendorPartNumber;
	private String itemId;	
	private String fulfillmentId;	
	private String orderId;
	private String requestType;
	private String nsUsername;
	private String poTypeCode;	
	private String requestFilePath;
	private String responseFilePath;
	private String errorId;
	private String requestTime;
	
	
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public String getPathFile() {
		return pathFile;
	}
	public void setPathFile(String pathFile) {
		this.pathFile = pathFile;
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
	
	public String getRetailer() {
		return retailer;
	}
	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}
	public String getVendorPartNumber() {
		String result ="";
		for (String number: vendorPartNumber){
			result+="Invalid VendorPartNumber : "+number+ "\r\n";
		}
		if (!result.isEmpty()){
			return result;
		} else {
			return null;
		}
	}
	public void setVendorPartNumber(List<String> vendorPartNumber) {
		this.vendorPartNumber = vendorPartNumber;
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
	
	public String getFulfillmentURL() {
		if (fulfillmentId!=null){
			String result;
			if (GlobalPropertiesProvider.getGlobalProperties().getEnvironment().equals("sandbox")){
				result = "https://system.sandbox.netsuite.com/app/accounting/transactions/itemship.nl?whence=&id="+fulfillmentId;
			} else {
				result = "https://system.netsuite.com/app/accounting/transactions/itemship.nl?whence=&id="+fulfillmentId;
			}
			return result;
		}
		return null;
	}
	
	public String getOrderURL() {
		if (orderId!=null){
			String result;
			if (GlobalPropertiesProvider.getGlobalProperties().getEnvironment().equals("sandbox")){
				result = "https://system.sandbox.netsuite.com/app/accounting/transactions/salesord.nl?id="+orderId;
			} else {
				result = "https://system.netsuite.com/app/accounting/transactions/salesord.nl?id="+orderId;
			}
			return result;
		}
		return null;
	}
	
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getNsUsername() {
		return nsUsername;
	}
	public void setNsUsername(String nsUsername) {
		this.nsUsername = nsUsername;
	}
	public String getPoTypeCode() {
		return poTypeCode;
	}
	public void setPoTypeCode(String poTypeCode) {
		this.poTypeCode = poTypeCode;
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
	public String getFulfillmentId() {
		return fulfillmentId;
	}
	public void setFulfillmentId(String fulfillmentId) {
		this.fulfillmentId = fulfillmentId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getErrorId() {
		return errorId;
	}
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}	
}
