package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.entities.PurchaseOrderStatus;

@Entity
@Table(name="PurchaseOrder")
public class PurchaseOrderDAO {
	@Id
	@Column(name="IdPurchaseOrder")
	@GeneratedValue
	private int purchaseOrderId;
	
	@ManyToOne
	@JoinColumn(name="MessageBatchId")
	private MessageBatchDAO mbDao;
	
	private String details;
	
	@Enumerated(EnumType.STRING)
	private PurchaseOrderStatus status;
	
	private String purchaseOrderNumber;

	public int getPurchaseOrderId() {
		return purchaseOrderId;
	}

	public void setPurchaseOrderId(int purchaseOrderId) {
		this.purchaseOrderId = purchaseOrderId;
	}

	public MessageBatchDAO getMbDao() {
		return mbDao;
	}

	public void setMbDao(MessageBatchDAO mbDao) {
		this.mbDao = mbDao;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public PurchaseOrderStatus getStatus() {
		return status;
	}

	public void setStatus(PurchaseOrderStatus status) {
		this.status = status;
	}

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}
}
