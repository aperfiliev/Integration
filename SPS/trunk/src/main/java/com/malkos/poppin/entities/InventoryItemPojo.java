package com.malkos.poppin.entities;

public class InventoryItemPojo {
	private String nsInternalId;
	private String popSKU;
	private double qtyAvailable;	
	
	public String getNsInternalId() {
		return nsInternalId;
	}
	public void setNsInternalId(String nsInternalId) {
		this.nsInternalId = nsInternalId;
	}
	public String getPopSKU() {
		return popSKU;
	}
	public void setPopSKU(String popSKU) {
		this.popSKU = popSKU;
	}
	public double getQtyAvailable() {
		return qtyAvailable;
	}
	public void setQtyAvailable(double qtyAvailable) {
		this.qtyAvailable = qtyAvailable;
	}	
}
