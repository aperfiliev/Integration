package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="fulfillmentlineitem")
public class FulfillmentLineItemDAO {
	@Id	
	@GeneratedValue
	private int idFulfillmentLineItem;
	
	private String vendorLineNumber;	
	private String UPC;
	private String itemNumber;
	private double orderQuantity;
	private double shipQuantity;
	private double unitPrice;
	private String itemInternalId;	
	private String trackingNumber;
	
	@ManyToOne
	@JoinColumn(name="packageId")
	private PackageDAO packageDAO;

	public int getIdFulfillmentLineItem() {
		return idFulfillmentLineItem;
	}

	public void setIdFulfillmentLineItem(int idFulfillmentLineItem) {
		this.idFulfillmentLineItem = idFulfillmentLineItem;
	}

	public String getVendorLineNumber() {
		return vendorLineNumber;
	}

	public void setVendorLineNumber(String vendorLineNumber) {
		this.vendorLineNumber = vendorLineNumber;
	}

	public String getUPC() {
		return UPC;
	}

	public void setUPC(String uPC) {
		UPC = uPC;
	}

	public String getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}

	public double getOrderQuantity() {
		return orderQuantity;
	}

	public void setOrderQuantity(double orderQuantity) {
		this.orderQuantity = orderQuantity;
	}

	public double getShipQuantity() {
		return shipQuantity;
	}

	public void setShipQuantity(double shipQuantity) {
		this.shipQuantity = shipQuantity;
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

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public PackageDAO getPackageDAO() {
		return packageDAO;
	}

	public void setPackageDAO(PackageDAO packageDAO) {
		this.packageDAO = packageDAO;
	}
}
