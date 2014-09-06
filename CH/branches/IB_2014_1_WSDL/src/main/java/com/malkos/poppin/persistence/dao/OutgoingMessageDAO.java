package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.malkos.poppin.entities.OutgoingMessageStatus;
import com.malkos.poppin.entities.MessageType;

@Entity
@Table(name="outgoingmessages")
public class OutgoingMessageDAO {
	@Id
	@GeneratedValue
	private long idOutgoingMessages;
	
	private String messagePath;
	
	@Enumerated(EnumType.STRING)
	private MessageType messageType;
	
	@Enumerated(EnumType.STRING)
	private OutgoingMessageStatus messageStatus;

	public long getIdOutgoingMessages() {
		return idOutgoingMessages;
	}

	public void setIdOutgoingMessages(long idOutgoingMessages) {
		this.idOutgoingMessages = idOutgoingMessages;
	}

	public String getMessagePath() {
		return messagePath;
	}

	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public OutgoingMessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(OutgoingMessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}
	
	
}
