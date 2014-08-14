package com.malkos.poppin.entities;

public class PackagePojo {
	private String packageDescription;
	private String trackingNumber;
	private Double packageWeight;
	private int itemQty=0;
	
	public String getPackageDescription() {
		return packageDescription;
	}
	public void setPackageDescription(String packageDescription) {
		this.packageDescription = packageDescription;
	}
	public String getTrackingNumber() {
		return trackingNumber;
	}
	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}
	public Double getPackageWeight() {
		return packageWeight;
	}
	public void setPackageWeight(Double packageWeight) {
		this.packageWeight = packageWeight;
	}
	public int getItemQty() {
		return itemQty;
	}
	public void setItemQty(int itemQty) {
		this.itemQty = itemQty;
	}
}
