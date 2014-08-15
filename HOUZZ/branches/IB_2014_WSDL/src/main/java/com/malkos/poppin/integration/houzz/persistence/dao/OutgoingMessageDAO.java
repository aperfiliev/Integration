package com.malkos.poppin.integration.houzz.persistence.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.malkos.poppin.integration.houzz.entities.enums.OutgoingMessageStatus;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;

@Entity
@Table(name="outgoingmessages")
public class OutgoingMessageDAO {
	
	@Id
	@Column(name="IdOutgoingMessage")
	@GeneratedValue
	private int IdOutgoingMessage;
	
	private String outgoingMessagePath;	
	
	@Enumerated(EnumType.STRING)
	private RetailerEnum retailer;
	
	@Enumerated(EnumType.STRING)
	private OutgoingMessageStatus outgoingMessageStatus;
	
	@Temporal(TemporalType.DATE)
	private Date createdDate;

	
	public String getOutgoingMessagePath() {
		return outgoingMessagePath;
	}

	public void setOutgoingMessagePath(String outgoingMessagePath) {
		this.outgoingMessagePath = outgoingMessagePath;
	}	

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public OutgoingMessageStatus getOutgoingMessageStatus() {
		return outgoingMessageStatus;
	}

	public void setOutgoingMessageStatus(OutgoingMessageStatus outgoingMessageStatus) {
		this.outgoingMessageStatus = outgoingMessageStatus;
	}

	public RetailerEnum getRetailer() {
		return this.retailer;
	}

	public void setRetailer(RetailerEnum retailer) {
		this.retailer = retailer;
	}	
}
