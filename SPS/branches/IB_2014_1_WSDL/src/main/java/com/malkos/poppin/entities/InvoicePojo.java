package com.malkos.poppin.entities;

import java.util.Date;
import java.util.List;

import com.malkos.poppin.entities.enums.PurchaseOrderType;

public class InvoicePojo {

	private List<InvoiceItemPojo> orderItems;
	
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
	private String invoiceNumber;
	private boolean isProcessingClosed=false;
	private Date invoiceDate;
	private List<PackagePojo> packageList;
	
	private PurchaseOrderType purchaseOrderType;
	private double taxTotal=0;
	private double totalSalesOrderAmount;
	
	public List<InvoiceItemPojo> getItemList() {
		return orderItems;
	}
	public void setItemList(List<InvoiceItemPojo> orderItems) {
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
	public String getNsInternalId() {
		return nsInternalId;
	}
	public void setNsInternalId(String nsInternalId) {
		this.nsInternalId = nsInternalId;
	}
	
	public int getPurchaseOrderId() {
		return purchaseOrderId;
	}
	public void setPurchaseOrderId(int purchaseOrderId) {
		this.purchaseOrderId = purchaseOrderId;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public boolean isProcessingClosed() {
		return isProcessingClosed;
	}
	public void setProcessingClosed(boolean isSoProcessingClosed) {
		this.isProcessingClosed = isSoProcessingClosed;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public List<PackagePojo> getPackageList() {
		return packageList;
	}
	public void setPackageList(List<PackagePojo> packageList) {
		this.packageList = packageList;
	}
	public String getShipmentTrackingNumbers() {
		if ((null!=packageList)&&(packageList.size()>0)){
			StringBuilder builder = new StringBuilder();
			int counter=0;
			for (PackagePojo packagePojo:packageList){
				if (counter==packageList.size()-1){
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
		if ((null!=packageList)&&(packageList.size()>0)){						
			for (PackagePojo packagePojo:packageList){
				weight+=packagePojo.getPackageWeight();
			}			
		}
		return weight;
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
	public double getTaxTotal() {
		return taxTotal;
	}
	public void setTaxTotal(double taxTotal) {
		this.taxTotal = taxTotal;
	}
	public double getTotalSalesOrderAmount() {
		return totalSalesOrderAmount;
	}
	public void setTotalSalesOrderAmount(double totalSalesOrderAmount) {
		this.totalSalesOrderAmount = totalSalesOrderAmount;
	}
}
