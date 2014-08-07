package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="lineitemintegrationidentifier")
public class LineItemIntegrationIdentifierDAO {
	@Id
	private int IdLineitemintegrationidentifiercol;
	
	private String vendorSKU;
	
	private String modelNum;
	private String itemInternalId;
	
	@ManyToOne
	@JoinColumn(name="retailerId")
	private RetailerDAO retailer;
	
	public String getVendorSKU() {
		return vendorSKU;
	}
	public void setVendorSKU(String vendorSKU) {
		this.vendorSKU = vendorSKU;
	}
	public String getModelNum() {
		return modelNum;
	}
	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}
	public String getItemInternalId() {
		return itemInternalId;
	}
	public void setItemInternalId(String itemInternalId) {
		this.itemInternalId = itemInternalId;
	}
	public RetailerDAO getRetailer() {
		return retailer;
	}
	public void setRetailer(RetailerDAO retailer) {
		this.retailer = retailer;
	}
}
