package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.PurchaseOrderType;

@Entity
@Table(name="PurchaseOrder")
public class PurchaseOrderDAO {
	@Id
	@Column(name="IdPurchaseOrder")
	@GeneratedValue
	private int idPurchaseOrder;
	
	private String poNumber;
	
	@Enumerated(EnumType.STRING)
	private PurchaseOrderStatus processingStatus;
	
	@Enumerated(EnumType.STRING)
	private PurchaseOrderType purchaseOrderType;
	
	private String salesOrderNsInternald;
	private String exceptionDescription;
	
	private boolean isAsnGenerated;
	private boolean isInvoiceMessagesGenerated;
	
	@ManyToOne
	@JoinColumn(name="retailerId")
	private RetailerDAO retailer;
	
	@ManyToOne
	@JoinColumn(name="messageId")
	private IncomingMessageDAO incomingMessageDao;
	
	public int getIdPurchaseOrder() {
		return idPurchaseOrder;
	}

	public void setIdPurchaseOrder(int idPurchaseOrder) {
		this.idPurchaseOrder = idPurchaseOrder;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public IncomingMessageDAO getIncomingMessageDao() {
		return incomingMessageDao;
	}

	public void setIncomingMessageDao(IncomingMessageDAO incomingMessageDao) {
		this.incomingMessageDao = incomingMessageDao;
	}

	public PurchaseOrderStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(PurchaseOrderStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	public PurchaseOrderType getPoType() {
		return purchaseOrderType;
	}

	public void setPoType(PurchaseOrderType poType) {
		this.purchaseOrderType = poType;
	}

	public String getSalesOrderNsInternald() {
		return salesOrderNsInternald;
	}
	public void setSalesOrderNsInternald(String salesOrderNsInternald) {
		this.salesOrderNsInternald = salesOrderNsInternald;
	}

	public String getExceptionDescription() {
		return exceptionDescription;
	}

	public void setExceptionDescription(String exceptionDescription) {
		this.exceptionDescription = exceptionDescription;
	}

	public RetailerDAO getRetailer() {
		return retailer;
	}

	public void setRetailer(RetailerDAO retailer) {
		this.retailer = retailer;
	}

	/**
	 * @return the isAsnGenerated
	 */
	public boolean isAsnGenerated() {
		return isAsnGenerated;
	}

	/**
	 * @param isAsnGenerated the isAsnGenerated to set
	 */
	public void setAsnGenerated(boolean isAsnGenerated) {
		this.isAsnGenerated = isAsnGenerated;
	}

	/**
	 * @return the isInvoiceMessageGenerated
	 */
	public boolean isInvoiceMessageGenerated() {
		return isInvoiceMessagesGenerated;
	}

	/**
	 * @param isInvoiceMessageGenerated the isInvoiceMessageGenerated to set
	 */
	public void setInvoiceMessageGenerated(boolean isInvoiceMessageGenerated) {
		this.isInvoiceMessagesGenerated = isInvoiceMessageGenerated;
	}
}
