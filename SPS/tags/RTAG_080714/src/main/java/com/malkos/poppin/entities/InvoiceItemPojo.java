package com.malkos.poppin.entities;

public class InvoiceItemPojo {
	private String vendorlineNumber;
	
	private String UPC;		
	private double invoiceQty;
	private double unitPrice;
	private String itemInternalId;
	private String merchantSKU;
	private String trackingNumber;
	private double taxAmount=0;
	
	public String getUPC() {
		return UPC;
	}
	public void setUPC(String uPC) {
		UPC = uPC;
	}	
	public double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public String getVendorlineNumber() {
		return vendorlineNumber;
	}
	public void setVendorlineNumber(String vendorlineNumber) {
		this.vendorlineNumber = vendorlineNumber;
	}
	
	public String getItemInternalId() {
		return this.itemInternalId;
	}
	public void setItemInternalId(String itemInternalId){
		this.itemInternalId = itemInternalId;
	}
	
	public double getInvoiceQty() {
		return invoiceQty;
	}
	public void setInvoiceQty(double invoiceQty) {
		this.invoiceQty = invoiceQty;
	}
	public String getMerchantSKU() {
		return merchantSKU;
	}
	public void setMerchantSKU(String merchantSKU) {
		this.merchantSKU = merchantSKU;
	}
	public String getTrackingNumber() {
		return trackingNumber;
	}
	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}
	public double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(double taxAmount) {
		this.taxAmount = taxAmount;
	}
}
