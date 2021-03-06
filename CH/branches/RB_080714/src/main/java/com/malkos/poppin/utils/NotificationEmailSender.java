package com.malkos.poppin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;

public class NotificationEmailSender {	
	private static GlobalProperties properties;
	static{
		properties = GlobalPropertiesProvider.getGlobalProperties();	
	}	
	
	@Autowired
	private JavaMailSender mailSender;
	
	public void sendEmail(String text, List<String> attachments) throws AddressException, MessagingException{
		MimeMessage message = mailSender.createMimeMessage();		
		message.setFrom(new InternetAddress(properties.getNotificationEmailFrom()));
        message.setRecipients(Message.RecipientType.TO, properties.getNotificationEmailTo());
        String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());		
        message.setSubject(properties.getNotificationEmailSubject()+ " - " + todayNow);        
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setText(text);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messagePart);
        if (attachments != null){
	        for (String attach:attachments){
	          	FileDataSource fileDataSource = new FileDataSource(attach);
	            MimeBodyPart attachmentPart = new MimeBodyPart();
	            attachmentPart.setDataHandler(new DataHandler(fileDataSource));
	            attachmentPart.setFileName(fileDataSource.getName());             
	            multipart.addBodyPart(attachmentPart); 
	        }
        }
        message.setContent(multipart);			
        mailSender.send(message);
	}
}
