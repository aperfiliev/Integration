package com.malkos.poppin.schedullers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.enums.EmailMessageStatus;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.util.NotificationEmailSender;


public class ResendEmailFlowScheduller {
	
private static Logger logger = LoggerFactory.getLogger(ResendEmailFlowScheduller.class);
private static GlobalProperties properties;

static{
	properties = GlobalPropertiesProvider.getGlobalProperties();	
}

@Autowired
private NotificationEmailSender notificationSender;
@Autowired
PersistenceManager persistenceManager;


	public void processTask(){			
		logger.info("Resend email Flow execution started ...");
		List<NotificationEmailDAO> emailsToSend = null;
		List<NotificationEmailDAO> sentEmails = new ArrayList<>();
		try{
			emailsToSend = persistenceManager.getPendingNotificationEmails();
		} catch (Exception e){
			logger.error(e.getMessage());
		}
		if ((emailsToSend!=null)&&(!emailsToSend.isEmpty())){
			for (NotificationEmailDAO neDao:emailsToSend){				
				String text = neDao.getMessage();
				Blob attachmentsBinary = neDao.getAttachments();
				List<String> attachmentList = null;
				if (attachmentsBinary!=null){
					try {
						InputStream is = attachmentsBinary.getBinaryStream();
						ObjectInputStream os = new ObjectInputStream(is);
						attachmentList = (List<String>) os.readObject();
					} catch (SQLException | IOException | ClassNotFoundException e) {
						logger.error(e.getMessage());
					}	
				}			
				try{
					notificationSender.sendEmail(text, attachmentList);
					neDao.setStatus(EmailMessageStatus.SENT);				
					sentEmails.add(neDao);
				} catch (Exception e){
					logger.error(e.getMessage());
				}
			}
		}
		persistenceManager.updateNotificationEmails(sentEmails);
		logger.info("Releasing the occuped resources.");
	}
}
