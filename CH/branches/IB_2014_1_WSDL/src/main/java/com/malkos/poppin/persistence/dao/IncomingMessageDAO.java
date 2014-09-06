package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.malkos.poppin.entities.IncomingMessageStatus;
import com.malkos.poppin.entities.OutgoingMessageStatus;

@Entity
@Table(name="incomingmessages")
public class IncomingMessageDAO {
	@Id
	@Column(name = "IdIncomingMessages")
	@GeneratedValue
	private long idIncomingMessages;
	
	private String messagePath;	
	
	@Enumerated(EnumType.STRING)
	private IncomingMessageStatus messageStatus;

	public long getIdIncomingMessages() {
		return idIncomingMessages;
	}

	public void setIdIncomingMessages(long idIncomingMessages) {
		this.idIncomingMessages = idIncomingMessages;
	}

	public String getMessagePath() {
		return messagePath;
	}

	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;
	}

	public IncomingMessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(IncomingMessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}
}
