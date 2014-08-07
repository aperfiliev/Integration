package com.malkos.poppin.entities;

import java.lang.reflect.Method;

public class OrderItemPojo {
	private int lineItemId;
	/*private int orderLineNumber;*/
	private int merchantLineNumber;
	private int qtyOrdered;
	private String unitOfMeasure;
	private String UPC;
	private String description;
	private String merchantSKU;
	private String vendorSKU;
	private Double unitPrice;
	private Double unitCost;
	private Float lineMerchandise;
	private String shippingCode;
		
	private String vendorDescription;
	private int lineReqDelvDate;
	private String vendorMessage;
	private String nameValuePair;
	/*private String factoryOrderNumber;*/
	private int subUnitQty;
	private String modelNum;
	
	
	public int getLineItemId() {
		return lineItemId;
	}
	public void setLineItemId(int lineItemId) {
		this.lineItemId = lineItemId;
	}
	public int getQtyOrdered() {
		return qtyOrdered;
	}
	public void setQtyOrdered(int qtyOrdered) {
		this.qtyOrdered = qtyOrdered;
	}
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	public String getUPC() {
		return UPC;
	}
	public void setUPC(String uPC) {
		UPC = uPC;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMerchantSKU() {
		return merchantSKU;
	}
	public void setMerchantSKU(String merchantSKU) {
		this.merchantSKU = merchantSKU;
	}
	public String getVendorSKU() {
		return vendorSKU;
	}
	public void setVendorSKU(String vendorSKU) {
		this.vendorSKU = vendorSKU;
	}
	public Double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public Double getUnitCost() {
		return unitCost;
	}
	public void setUnitCost(Double unitCost) {
		this.unitCost = unitCost;
	}
	public Float getLineMerchandise() {
		return lineMerchandise;
	}
	public void setLineMerchandise(Float lineMerchandise) {
		this.lineMerchandise = lineMerchandise;
	}
	public String getShippingCode() {
		return shippingCode;
	}
	public void setShippingCode(String shippingCode) {
		this.shippingCode = shippingCode;
	}
	public String getVendorDescription() {
		return vendorDescription;
	}
	public void setVendorDescription(String vendorDescription) {
		this.vendorDescription = vendorDescription;
	}
	public int getLineReqDelvDate() {
		return lineReqDelvDate;
	}
	public void setLineReqDelvDate(int lineReqDelvDate) {
		this.lineReqDelvDate = lineReqDelvDate;
	}
	public String getVendorMessage() {
		return vendorMessage;
	}
	public void setVendorMessage(String vendorMessage) {
		this.vendorMessage = vendorMessage;
	}
	public String getNameValuePair() {
		return nameValuePair;
	}
	public void setNameValuePair(String nameValuePair) {
		this.nameValuePair = nameValuePair;
	}
	public int getSubUnitQty() {
		return subUnitQty;
	}
	public void setSubUnitQty(int subUnitQty) {
		this.subUnitQty = subUnitQty;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			Class c = Class.forName(this.getClass().getName());
			Method m[] = c.getDeclaredMethods();
			Object oo;

			for (int i = 0; i < m.length; i++)
				if (m[i].getName().startsWith("get")) {
					oo = m[i].invoke(this, null);
					sb.append("[" + m[i].getName().substring(3) + "]: "
							+ String.valueOf(oo) + ", \n");
				}
			sb.append("\n\n\n");
		} catch (Throwable e) {
			System.err.println(e);
		}
		return sb.toString();
	}
	public int getMerchantLineNumber() {
		return merchantLineNumber;
	}
	public void setMerchantLineNumber(int merchantLineNumber) {
		this.merchantLineNumber = merchantLineNumber;
	}
	public String getModelNum() {
		return modelNum;
	}
	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}
}
