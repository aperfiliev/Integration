package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="vendorskutomodelnummap")
public class VendorSkuToModelNumMapDAO {
	@Id
	private long vendorSku;
	
	private String modelNum;
	
	private String itemInternalId;
	
	public long getVendorSku() {
		return vendorSku;
	}
	public void setVendorSku(long vendorSku) {
		this.vendorSku = vendorSku;
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
}
