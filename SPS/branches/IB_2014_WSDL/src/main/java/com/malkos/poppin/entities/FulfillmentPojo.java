package com.malkos.poppin.entities;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.text.StrBuilder;

import com.malkos.poppin.entities.enums.PurchaseOrderType;


public class FulfillmentPojo {
	
	
	private List<FulfillmentItemPojo> orderItems;		
	private List<PackagePojo> packageItems;
	
	private String shipToName;
	private String shipToAddress1;
	private String shipToAddress2;	
	private String shipToCity;
	private String shipToState;
	private String shipToPostalCode;
	private String shipToCountry;
	
	private String incomingMessagePath;
	
	private String nsInternalId;
	private String departmentNsInternalId;
	private String salesorderNsInternalId;
	private Date shipDate;	
	private String soTransactionId;
	private String poNumber;
	private Date poDate;
	private int purchaseOrderId;
	private boolean isProcessingClosed;
	
	private PurchaseOrderType purchaseOrderType;
	
	private String exceptionDescription;
	
	
	public List<FulfillmentItemPojo> getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(List<FulfillmentItemPojo> orderItems) {
		this.orderItems = orderItems;
	}
	public String getShipToName() {
		return shipToName;
	}
	public void setShipToName(String shipToName) {
		this.shipToName = shipToName;
	}
	public String getShipToAddress1() {
		return shipToAddress1;
	}
	public void setShipToAddress1(String shipToAddress1) {
		this.shipToAddress1 = shipToAddress1;
	}
	public String getShipToAddress2() {
		return shipToAddress2;
	}
	public void setShipToAddress2(String shipToAddress2) {
		this.shipToAddress2 = shipToAddress2;
	}
	public String getShipToCity() {
		return shipToCity;
	}
	public void setShipToCity(String shipToCity) {
		this.shipToCity = shipToCity;
	}
	public String getShipToState() {
		return shipToState;
	}
	public void setShipToState(String shipToState) {
		this.shipToState = shipToState;
	}
	public String getShipToPostalCode() {
		return shipToPostalCode;
	}
	public void setShipToPostalCode(String shipToPostalCode) {
		this.shipToPostalCode = shipToPostalCode;
	}
	public String getShipToCountry() {
		return shipToCountry;
	}
	public void setShipToCountry(String shipToCountry) {
		this.shipToCountry = shipToCountry;
	}
	public String getIncomingMessagePath() {
		return incomingMessagePath;
	}
	public void setIncomingMessagePath(String incomingMessagePath) {
		this.incomingMessagePath = incomingMessagePath;
	}
	public String getDepartmentNsInternalId() {
		return departmentNsInternalId;
	}
	public void setDepartmentNsInternalId(String departmentNsInternalId) {
		this.departmentNsInternalId = departmentNsInternalId;
	}
	public String getSalesorderNsInternalId() {
		return salesorderNsInternalId;
	}
	public void setSalesorderNsInternalId(String salesorderNsInternalId) {
		this.salesorderNsInternalId = salesorderNsInternalId;
	}
	public Date getShipDate() {
		return shipDate;
	}
	public void setShipDate(Date shipDate) {
		this.shipDate = shipDate;
	}
	public String getSoTransactionId() {
		return soTransactionId;
	}
	public void setSoTransactionId(String soTransactionId) {
		this.soTransactionId = soTransactionId;
	}
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public Date getPoDate() {
		return poDate;
	}
	public void setPoDate(Date poDate) {
		this.poDate = poDate;
	}
	public List<PackagePojo> getPackageItems() {
		return packageItems;
	}
	public void setPackageItems(List<PackagePojo> packageItems) {
		this.packageItems = packageItems;
	}
	public String getNsInternalId() {
		return nsInternalId;
	}
	public void setNsInternalId(String nsInternalId) {
		this.nsInternalId = nsInternalId;
	}
	
	public String getShipmentTrackingNumbers() {
		if ((null!=packageItems)&&(packageItems.size()>0)){
			StringBuilder builder = new StringBuilder();
			int counter=0;
			for (PackagePojo packagePojo:packageItems){
				if (counter==packageItems.size()-1){
					builder.append(packagePojo.getTrackingNumber());
				}
				else {
					builder.append(packagePojo.getTrackingNumber());
					builder.append(" ");
				}
				counter++;
			}
			return builder.toString();
		}
		else return new String();		
	}	
	
	public Double getShipmentWeight(){
		Double weight = new Double(0);
		if ((null!=packageItems)&&(packageItems.size()>0)){						
			for (PackagePojo packagePojo:packageItems){
				weight+=packagePojo.getPackageWeight();
			}			
		}
		return weight;
	}
	public int getPurchaseOrderId() {
		return purchaseOrderId;
	}
	public void setPurchaseOrderId(int purchaseOrderId) {
		this.purchaseOrderId = purchaseOrderId;
	}
	public boolean isProcessingClosed() {
		return isProcessingClosed;
	}
	public void setProcessingClosed(boolean isProcessingClosed) {
		this.isProcessingClosed = isProcessingClosed;
	}
	/**
	 * @return the purchaseOrderType
	 */
	public PurchaseOrderType getPurchaseOrderType() {
		return purchaseOrderType;
	}
	/**
	 * @param purchaseOrderType the purchaseOrderType to set
	 */
	public void setPurchaseOrderType(PurchaseOrderType purchaseOrderType) {
		this.purchaseOrderType = purchaseOrderType;
	}
	public String getExceptionDescription() {
		return exceptionDescription;
	}
	public void setExceptionDescription(String exceptionDescription) {
		this.exceptionDescription = exceptionDescription;
	}	
}
