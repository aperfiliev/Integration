package com.malkos.poppin.entities;

public class AddSalesOrderResultPojo {
	String soInternalId;
	boolean addedSuccessifully = true;
	String problemDescription = "";
	private NSRrequestDetails requestDetails;
	
	private AddCustomerResultsPojo addCustomerResultsPojo;
	
	public String getSoInternalId() {
		return soInternalId;
	}
	public void setSoInternalId(String soInternalId) {
		this.soInternalId = soInternalId;
	}
	public boolean isAddedSuccessifully() {
		return addedSuccessifully;
	}
	public void setAddedSuccessifully(boolean addedSuccessifully) {
		this.addedSuccessifully = addedSuccessifully;
	}
	public String getProblemDescription() {
		return problemDescription;
	}
	public void setProblemDescription(String problemDescription) {
		this.problemDescription = problemDescription;
	}
	public AddCustomerResultsPojo getAddCustomerResultsPojo() {
		return addCustomerResultsPojo;
	}
	public void setAddCustomerResultsPojo(AddCustomerResultsPojo addCustomerResultsPojo) {
		this.addCustomerResultsPojo = addCustomerResultsPojo;
	}
	public NSRrequestDetails getRequestDetails() {
		return requestDetails;
	}
	public void setRequestDetails(NSRrequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}	
}
