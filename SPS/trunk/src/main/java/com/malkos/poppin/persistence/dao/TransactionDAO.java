package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.malkos.poppin.entities.enums.TransactionType;

@Entity
@Table(name="transaction")
public class TransactionDAO {
	@Id
	@GeneratedValue
    private int idTransaction;
	
	@Enumerated(EnumType.STRING)
    private TransactionType transactionType;
	
    private String transactionInternalId;
    
    @ManyToOne
	@JoinColumn(name="purchaseOrderId")
	private PurchaseOrderDAO purchaseOrder;   

	public int getIdTransaction() {
		return idTransaction;
	}

	public void setIdTransaction(int idTransaction) {
		this.idTransaction = idTransaction;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public String getTransactionInternalId() {
		return transactionInternalId;
	}

	public void setTransactionInternalId(String transactionInternalId) {
		this.transactionInternalId = transactionInternalId;
	}

	public PurchaseOrderDAO getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(PurchaseOrderDAO purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}
}
