package com.malkos.poppin.entities;

public class ShippingAddressPojo {
	
	private String internalId;
	private String email;
	
	public ShippingAddressPojo(String label, String email){
		this.internalId = label;
		this.email = email;
	}
	
	public String getInternalId() {
		return internalId;
	}
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
