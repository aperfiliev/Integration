package com.malkos.poppin.integration.houzz.entities;

public class HouzzInventoryItemPojo extends InventoryItemPojo {	
	private String sku;
	private double qtyAvailable;
	private double price;
	private boolean inactive;
	private boolean wrongConfigured = false;
	private IInventoryItemPropertiesCorrector propertiesCorrector;
	
	public HouzzInventoryItemPojo(){
		propertiesCorrector = new HouzzInventoryItemPropertiesCorrector();
	}		
	public String getSKU() {
		return sku;
	}
	public void setSKU(String sku) {
		this.sku = sku;
	}
	public double getQtyAvailable() {
		return qtyAvailable;
	}
	public void setQtyAvailable(double qtyAvailable) {
		this.qtyAvailable = qtyAvailable;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}	
	public HouzzItemCorrectedProperties getCorrectedProperties(){
		return propertiesCorrector.correctItemProperties(this);
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
}
