package com.malkos.poppin.documents;

public class PoDocumentItem {
	private String vendorlineNumber;
	private String popMapperNum;
	private String UPC;
	//private String itemNumber;
	private double orderQty;
	private double unitPrice;
	private String itemInternalId;
	private String merchantSKU;
	public String getVendorlineNumber() {
		return vendorlineNumber;
	}
	public void setVendorlineNumber(String vendorlineNumber) {
		this.vendorlineNumber = vendorlineNumber;
	}
	public String getPopMapperNum() {
		return popMapperNum;
	}
	public void setPopMapperNum(String popMapperNum) {
		this.popMapperNum = popMapperNum;
	}
	public String getUPC() {
		return UPC;
	}
	public void setUPC(String uPC) {
		UPC = uPC;
	}
	public double getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(double orderQty) {
		this.orderQty = orderQty;
	}
	public double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public String getItemInternalId() {
		return itemInternalId;
	}
	public void setItemInternalId(String itemInternalId) {
		this.itemInternalId = itemInternalId;
	}
	public String getMerchantSKU() {
		return merchantSKU;
	}
	public void setMerchantSKU(String merchantSKU) {
		this.merchantSKU = merchantSKU;
	}
}
