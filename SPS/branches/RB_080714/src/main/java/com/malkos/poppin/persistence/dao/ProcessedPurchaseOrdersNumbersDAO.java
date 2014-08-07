package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="poppinprocessedpurcaseorders")
public class ProcessedPurchaseOrdersNumbersDAO {
	@Id
	@Column(name="idPoppinProcessedPurcaseOrder")
	@GeneratedValue
	private int idProcessedPurcaseOrder;
	
	@Column(name="purchaseOrderNumber")
	private String processedPurchaseOrderNumber;
	
	private long salesOrderInternalIdNumber;
	

	public int getIdProcessedPurcaseOrder() {
		return idProcessedPurcaseOrder;
	}

	public void setIdProcessedPurcaseOrder(int idProcessedPurcaseOrder) {
		this.idProcessedPurcaseOrder = idProcessedPurcaseOrder;
	}

	public String getProcessedPurchaseOrderNumber() {
		return processedPurchaseOrderNumber;
	}

	public void setProcessedPurchaseOrderNumber(String processedPurchaseOrderNumber) {
		this.processedPurchaseOrderNumber = processedPurchaseOrderNumber;
	}

	public long getSalesOrderInternalIdNumber() {
		return salesOrderInternalIdNumber;
	}
	public void setSalesOrderInternalIdNumber(long salesOrderInternalIdNumber) {
		this.salesOrderInternalIdNumber = salesOrderInternalIdNumber;
	}
}
