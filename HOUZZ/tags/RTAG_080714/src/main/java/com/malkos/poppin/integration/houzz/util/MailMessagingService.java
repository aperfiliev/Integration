package com.malkos.poppin.integration.houzz.util;

import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.axis.transport.mail.MailSender;
import org.omg.CORBA.OMGVMCID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;

public class MailMessagingService {
	
	private static Logger logger = LoggerFactory.getLogger(MailMessagingService.class);
	
	public static void sendMessage(InternetAddress[] recipients, String subject,String messageContent,  RetailerAbstract retailer) throws MessagingException{
		Session session = initializeMailSession(retailer);
		try { 
			Message message = new MimeMessage(session);			
			message.setFrom(new InternetAddress(retailer.getNotificationEmailFrom()));
			message.setRecipients(Message.RecipientType.TO,	recipients);
			message.setSubject(subject);
			message.setContent(messageContent,"text/html"); 			
			Transport.send(message); 
		} catch (MessagingException e) {
			logger.info(e.getMessage());
			throw e;
		}
	}
	
	/*public static void sendInventoryNotificationEmail(String subject, String messageContent,RetailerAbstract retailer) throws AddressException, MessagingException{
		Session session = initializeMailSession(retailer);
		try { 
			Message message = new MimeMessage(session);		
			message.setFrom(new InternetAddress(retailer.getNotificationEmailFrom()));
			message.setRecipients(Message.RecipientType.TO,	InternetAddress.parse(retailer.getInventoryNotificationEmailsTo()));
			message.setSubject(subject);
			message.setContent(messageContent,"text/html"); 
			Transport.send(message); 
		} catch (MessagingException e) {
			logger.info(e.getMessage());
		}
	}*/
	private static Session initializeMailSession(RetailerAbstract retailer){
		final String username = retailer.getNotificationEmailServerSmtpUser();
		final String password =  retailer.getNotificationEmailServerStmpPassword();
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", retailer.getNotificationEmailServerHost());
		props.put("mail.smtp.port", retailer.getNotificationEmailServerPort());
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  }); 
		return session;
	}

	public static void sendMultipartMessage(InternetAddress[] address, String subject
			, String content, RetailerAbstract retailer, List<String> attachments) throws MessagingException {	
		  Session session = initializeMailSession(retailer);
		  try { 
			  MimeMessage message = new MimeMessage(session);
	          message.setFrom(new InternetAddress(retailer.getNotificationEmailFrom()));
	          message.setRecipients(Message.RecipientType.TO, address);
	          message.setSubject(subject);        
	          MimeBodyPart messagePart = new MimeBodyPart();
	          messagePart.setContent(content,"text/html");
	          //messagePart.setText(content);
	          Multipart multipart = new MimeMultipart();
	          multipart.addBodyPart(messagePart);
	          if ((attachments!=null)&&(!attachments.isEmpty())){
	        	  for (String path:attachments){
		        	  FileDataSource fileDataSource = new FileDataSource(path);
		              MimeBodyPart attachmentPart = new MimeBodyPart();
		              attachmentPart.setDataHandler(new DataHandler(fileDataSource));
		              attachmentPart.setFileName(fileDataSource.getName());             
		              multipart.addBodyPart(attachmentPart);
		          }
	          }	          
	          message.setContent(multipart);
	          Transport.send(message); 
		  } catch (MessagingException e) {
				logger.info(e.getMessage());
				throw e;
		  }
	}
	
	public static void sendMultipartMessage(String subject
			, String content, RetailerAbstract retailer, List<String> attachments) throws MessagingException {	
		  Session session = initializeMailSession(retailer);
		  try { 
			  MimeMessage message = new MimeMessage(session);
	          message.setFrom(new InternetAddress(retailer.getNotificationEmailFrom()));
	          message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(retailer.getNotificationEmailsTo()));
	          message.setSubject(subject);        
	          MimeBodyPart messagePart = new MimeBodyPart();
	          messagePart.setContent(content,"text/html");
	         // messagePart.setText(content);
	          Multipart multipart = new MimeMultipart();
	          multipart.addBodyPart(messagePart);
	          if (attachments!=null){
	        	  for (String path:attachments){
		        	  FileDataSource fileDataSource = new FileDataSource(path);
		              MimeBodyPart attachmentPart = new MimeBodyPart();
		              attachmentPart.setDataHandler(new DataHandler(fileDataSource));
		              attachmentPart.setFileName(fileDataSource.getName());             
		              multipart.addBodyPart(attachmentPart);
		          }
	          }	          
	          message.setContent(multipart);
	          Transport.send(message); 
		  } catch (MessagingException e) {
				logger.info(e.getMessage());
				throw e;
		  }
	}
	
}
