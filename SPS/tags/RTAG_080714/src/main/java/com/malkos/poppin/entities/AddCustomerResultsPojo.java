package com.malkos.poppin.entities;

public class AddCustomerResultsPojo {
	
	private String internalId;
	private String shippingAddressInternalId;
	private String billingAddressInternalId;
	
	boolean readyForMapping = true;
	
	public String getProblemDescription() {
		return problemDescription;
	}
	public void setProblemDescription(String problemDescription) {
		this.problemDescription = problemDescription;
	}
	private String problemDescription = "";
	
	public String getInternalId() {
		return internalId;
	}
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	public String getShippingAddressInternalId() {
		return shippingAddressInternalId;
	}
	public void setShippingAddressInternalId(String shippingAddressInternalId) {
		this.shippingAddressInternalId = shippingAddressInternalId;
	}
	public boolean isReadyForMapping() {
		return readyForMapping;
	}
	public void setReadyForMapping(boolean readyForMapping) {
		this.readyForMapping = readyForMapping;
	}
	public String getBillingAddressInternalId() {
		return billingAddressInternalId;
	}
	public void setBillingAddressInternalId(String billingAddressInternalId) {
		this.billingAddressInternalId = billingAddressInternalId;
	}
}
