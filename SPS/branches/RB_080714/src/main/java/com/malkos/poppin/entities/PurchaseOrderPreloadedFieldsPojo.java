package com.malkos.poppin.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseOrderPreloadedFieldsPojo {
	private List<OrderItemPojo> orderItems;
	private String poNumber;
	private Date poDate;
	private String departmentNsInternalId;
	private String salesOrderNsInternalId;	
	private Date shipDate;		
	private String invoiceNumber;
	private String soTransactionId;	
	private boolean isASNGenerated=false;
	

	public String getDepartmentNsInternalId() {
		return departmentNsInternalId;
	}
	

	public List<OrderItemPojo> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItemPojo> orderItems) {
		this.orderItems = orderItems;
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

	public void setDepartmentNsInternalId(String departmentNsInternalId) {
		this.departmentNsInternalId = departmentNsInternalId;
		
	}

	public String getSalesOrderNsInternalId() {
		return salesOrderNsInternalId;
	}

	public void setSalesOrderNsInternalId(String salesOrderNsInternalId) {
		this.salesOrderNsInternalId = salesOrderNsInternalId;
	}		

	public void setShipDate(Date shipDate) {
		this.shipDate = shipDate;
	}
	public Date getShipDate() {
		return this.shipDate;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public void setSalesOrderTransactionId(String soTransactionId) {
		this.soTransactionId = soTransactionId;
		
	}
	public String getSalesOrderTransactionId() {
		return this.soTransactionId;
		
	}	
	
	public boolean isASNGenerated() {
		return isASNGenerated;
	}

	public void setASNGenerated(boolean isASNGenerated) {
		this.isASNGenerated = isASNGenerated;
	}

}
