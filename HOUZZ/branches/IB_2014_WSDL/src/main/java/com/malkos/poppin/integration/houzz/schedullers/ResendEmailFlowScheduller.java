package com.malkos.poppin.integration.houzz.schedullers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.EmailMessageStatus;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.integration.houzz.util.MailMessagingService;

public class ResendEmailFlowScheduller implements Job{

	private static Logger logger = LoggerFactory.getLogger(ResendEmailFlowScheduller.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {	
		IPersistenceManager persistenceManager = new PersistenceManager();
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
				RetailerAbstract retailer = RetailerManager.get_retailer(neDao.getRetailer());
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
					MailMessagingService.sendMultipartMessage(InternetAddress.parse(neDao.getRecepients()), neDao.getSubject(), neDao.getMessage(), retailer, attachmentList);
					neDao.setStatus(EmailMessageStatus.SENT);				
					sentEmails.add(neDao);
				} catch (MessagingException e){
					logger.error(e.getMessage());
				}
			}
		}
		persistenceManager.updateNotificationEmails(sentEmails);
		logger.info("Releasing the occuped resources.");	
	}

}
