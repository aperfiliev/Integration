package com.malkos.poppin.persistence.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="MessageBatch")
public class MessageBatchDAO {
	@Id
	@Column(name="IdMessageBatch")
	private long MessageBatchId;
	
	private String encryptedFilePath;
	private String decryptedFilePath;
	
	@ManyToOne
	@JoinColumn(name="idIncomingMessage")
	private IncomingMessageDAO incomingMessage;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateRecieved;
	public long getMessageBatchId() {
		return MessageBatchId;
	}
	public void setMessageBatchId(long messageBatchId) {
		MessageBatchId = messageBatchId;
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
	public Date getDateRecieved() {
		return dateRecieved;
	}
	public void setDateRecieved(Date dateRecieved) {
		this.dateRecieved = dateRecieved;
	}
	public IncomingMessageDAO getIncomingMessage() {
		return incomingMessage;
	}
	public void setIncomingMessage(IncomingMessageDAO incomingMessage) {
		this.incomingMessage = incomingMessage;
	}
}
