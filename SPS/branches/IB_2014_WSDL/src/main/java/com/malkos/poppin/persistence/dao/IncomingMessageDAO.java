package com.malkos.poppin.persistence.dao;

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

import com.malkos.poppin.entities.enums.IncomingMessageStatus;
import com.malkos.poppin.entities.enums.OutgoingMessageStatus;

@Entity
@Table(name="incomingmessages")
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class IncomingMessageDAO {
	@Id
	@Column(name="IdIncomingMessage")
	@GeneratedValue
	private int idIncomingMessage;
	
	private String messagePath;
	
	@Temporal(TemporalType.DATE)
	private Date recievedDate;
	
	@Enumerated(EnumType.STRING)
	private IncomingMessageStatus incomingMessageStatus;
	
	public int getIdIncomingMessage() {
		return idIncomingMessage;
	}
	public void setIdIncomingMessage(int idIncomingMessage) {
		this.idIncomingMessage = idIncomingMessage;
	}
	public String getMessagePath() {
		return messagePath;
	}
	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;
	}
	public Date getRecievedDate() {
		return recievedDate;
	}
	public void setRecievedDate(Date recievedDate) {
		this.recievedDate = recievedDate;
	}
	public IncomingMessageStatus getIncomingMessageStatus() {
		return incomingMessageStatus;
	}
	public void setIncomingMessageStatus(IncomingMessageStatus incomingMessageStatus) {
		this.incomingMessageStatus = incomingMessageStatus;
	}
}
