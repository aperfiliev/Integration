package com.malkos.poppin.integration.houzz.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="lineitemintegrationidentifier")
public class LineItemIntegrationIdentifierDAO {
	@Id
	private int IdLineitemintegrationidentifiercol;
	
	private String sku;	
	private String itemInternalId;
	
	public String getSKU() {
		return sku;
	}
	public void setSKU(String sku) {
		this.sku = sku;
	}	
	public String getItemInternalId() {
		return itemInternalId;
	}
	public void setItemInternalId(String itemInternalId) {
		this.itemInternalId = itemInternalId;
	}	
}
