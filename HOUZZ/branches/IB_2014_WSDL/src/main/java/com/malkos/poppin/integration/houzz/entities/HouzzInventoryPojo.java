package com.malkos.poppin.integration.houzz.entities;

public abstract class HouzzInventoryPojo extends InventoryPojo {
	private String sku;	
	private double price;
	protected boolean inactive;
	private boolean wrongConfigured = false;
	protected  IInventoryItemPropertiesCorrector propertiesCorrector;
	
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public boolean isInactive() {
		return inactive;
	}
	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}
	public boolean isWrongConfigured() {
		return wrongConfigured;
	}
	public void setWrongConfigured(boolean wrongConfigured) {
		this.wrongConfigured = wrongConfigured;
	}	
	
	public HouzzItemCorrectedProperties getCorrectedProperties(){
		return propertiesCorrector.correctItemProperties(this);
	}
	
	public abstract double getQtyAvailable();
}
