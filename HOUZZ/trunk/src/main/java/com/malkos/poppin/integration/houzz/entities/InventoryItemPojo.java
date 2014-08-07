package com.malkos.poppin.integration.houzz.entities;

public abstract class InventoryItemPojo {
	private String internalId;
	
	public String getInternalId(){
		return this.internalId;
	}
	public void setInternalId(String internalId){
		 this.internalId = internalId; 
	}
}
