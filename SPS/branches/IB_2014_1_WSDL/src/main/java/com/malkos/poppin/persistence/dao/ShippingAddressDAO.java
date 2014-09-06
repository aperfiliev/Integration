package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="customershipaddr")
public class ShippingAddressDAO {
	@Id
	@Column(name="idshippingaddress")
	@GeneratedValue
	private int idStaplesShippingAddress;
	
	@Column(name="shippingaddresslabel")
	private String shippingAddressLabel;
	
	@Column(name="shippingaddressinternalid")
	private String shippingAddressInternalId;
	
	@Column(name="shippingaddressemail")
	private String shippingAddressemail;
	
	@Column(name="retailerId")
	private int shippingAddressRetailerId;
	
	public int getIdStaplesShippingAddress() {
		return idStaplesShippingAddress;
	}
	public void setIdStaplesShippingAddress(int idStaplesShippingAddress) {
		this.idStaplesShippingAddress = idStaplesShippingAddress;
	}
	public String getShippingAddressLabel() {
		return shippingAddressLabel;
	}
	public void setShippingAddressLabel(String shippingAddressLabel) {
		this.shippingAddressLabel = shippingAddressLabel;
	}
	public String getShippingAddressInternalId() {
		return shippingAddressInternalId;
	}
	public void setShippingAddressInternalId(String shippingAddressInternalId) {
		this.shippingAddressInternalId = shippingAddressInternalId;
	}
	public String getShippingAddressemail() {
		return shippingAddressemail;
	}
	public void setShippingAddressemail(String shippingAddressemail) {
		this.shippingAddressemail = shippingAddressemail;
	}
	public int getShippingAddressRetailerId() {
		return shippingAddressRetailerId;
	}
	public void setShippingAddressRetailerId(int shippingAddressRetailerId) {
		this.shippingAddressRetailerId = shippingAddressRetailerId;
	}

}
