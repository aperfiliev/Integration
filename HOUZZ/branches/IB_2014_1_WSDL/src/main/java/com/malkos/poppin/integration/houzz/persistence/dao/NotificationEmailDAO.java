package com.malkos.poppin.integration.houzz.persistence.dao;

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

import com.malkos.poppin.integration.houzz.entities.enums.EmailMessageStatus;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;

@Entity
@Table(name="notificationemail")
public class NotificationEmailDAO {
	@Id	
	@GeneratedValue
	@Column(name="idnotificationemail")
	private int idNotificationEmail;
	
	private String message;	
	@Lob
	private Blob attachments;
	
	@Enumerated(EnumType.STRING)
	private EmailMessageStatus status;
	
	@Enumerated(EnumType.STRING)	
	private RetailerEnum retailer;

	private String subject;
	private String recepients;
	
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

	public RetailerEnum getRetailer() {
		return retailer;
	}

	public void setRetailer(RetailerEnum retailer) {
		this.retailer = retailer;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getRecepients() {
		return recepients;
	}

	public void setRecepients(String recepients) {
		this.recepients = recepients;
	}	
	
}
