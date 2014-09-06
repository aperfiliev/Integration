package com.malkos.poppin.entities;

import java.util.ArrayList;
import java.util.List;

public class MessageBatchTransfer {
	
	private int messageBatchId;
	private String encryptedFilePath;
	private String decryptedFilePath;
	private List<PurchaseOrderPojo> purchaseOrders = new ArrayList<PurchaseOrderPojo>();;
	private long idIncomingMessage;
	
	public MessageBatchTransfer(int messageBatchId, String encryptedFilePath,
			String decryptedFilePath) {
		super();
		this.messageBatchId = messageBatchId;
		this.encryptedFilePath = encryptedFilePath;
		this.decryptedFilePath = decryptedFilePath;
	}
	public MessageBatchTransfer(String tempBatchNumber) {
		this.messageBatchId = Integer.parseInt(tempBatchNumber);
	}

	public int getMessageBatchId() {
		return messageBatchId;
	}

	public void setMessageBatchId(int messageBatchId) {
		this.messageBatchId = messageBatchId;
	}

	public String getEncryptedFilePath() {
		return encryptedFilePath;
	}

	public void setEncryptedFilePath(String encryptedFilePath) {
		this.encryptedFilePath = encryptedFilePath;
	}

	public String getDecryptedFilePath() {
		return decryptedFilePath;
	}

	public void setDecryptedFilePath(String decryptedFilePath) {
		this.decryptedFilePath = decryptedFilePath;
	}

	public List<PurchaseOrderPojo> getPurchaseOrders() {
		return purchaseOrders;
	}

	public void setPurchaseOrders(List<PurchaseOrderPojo> purchaseOrders) {
		this.purchaseOrders = purchaseOrders;
	}
	public long getIdIncomingMessage() {
		return idIncomingMessage;
	}
	public void setIdIncomingMessage(long idIncomingMessage) {
		this.idIncomingMessage = idIncomingMessage;
	}
}
