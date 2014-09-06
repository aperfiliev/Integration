package com.malkos.poppin.persistence.dao;

import java.sql.Blob;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.type.BlobType;

import com.malkos.poppin.entities.enums.EmailMessageStatus;
import com.malkos.poppin.entities.enums.OutgoingMessageStatus;
import com.malkos.poppin.entities.enums.OutgoingMessageType;

@Entity
@Table(name="notificationemails")
public class NotificationEmailDAO {
	@Id	
	@GeneratedValue
	private int idNotificationEmail;
	
	private String message;	
	@Lob
	private Blob attachments;
	
	@Enumerated(EnumType.STRING)
	private EmailMessageStatus status;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Blob getAttachments() {
		return attachments;
	}

	public void setAttachments(Blob attachments) {
		this.attachments = attachments;
	}

	public int getIdNotificationEmail() {
		return idNotificationEmail;
	}

	public void setIdNotificationEmail(int idNotificationEmail) {
		this.idNotificationEmail = idNotificationEmail;
	}

	public EmailMessageStatus getStatus() {
		return status;
	}

	public void setStatus(EmailMessageStatus status) {
		this.status = status;
	}	
	
}
