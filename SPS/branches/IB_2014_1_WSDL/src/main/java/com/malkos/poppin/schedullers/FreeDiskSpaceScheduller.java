package com.malkos.poppin.schedullers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.util.NotificationEmailSender;



public class FreeDiskSpaceScheduller {
	
private static Logger logger = LoggerFactory.getLogger(FreeDiskSpaceScheduller.class);
private static GlobalProperties properties;

static{
	properties = GlobalPropertiesProvider.getGlobalProperties();	
}

@Autowired
private NotificationEmailSender notificationSender;
@Autowired
PersistenceManager persistenceManager;


	public void processTask(){			
		logger.info("Free Disk Space Flow execution started ...");
		List<String> errorsList = new ArrayList<>();
		List<String> directoriesToDelete = new ArrayList<>();
		String messagesDirectoryRootPath = properties.getMessagesDirectoryRoot();
		String requestsResponseDirectoryPath = messagesDirectoryRootPath + File.separator + properties.getRequestsResoinsesDirectoryName();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -14);
		for (int  i = 0; i< 30; i++){			
			String year = Integer.toString(calendar.get(Calendar.YEAR));
			String month = new SimpleDateFormat("M").format(calendar.getTime());
			String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
			String directory = requestsResponseDirectoryPath + File.separator + year + File.separator + month + File.separator + day;	
			directoriesToDelete.add(directory);
			calendar.add(Calendar.DATE, -1);
		}	
		try{				
			for (String directory:directoriesToDelete){
				try{
					FileUtils.deleteDirectory(new File(directory));
				} catch (Exception ex){
					String errorMessage = "Couldn't remove directory '"+directory+"'. Reason: "+ex.getMessage()+". Please remove it manually.";
					errorsList.add(errorMessage);
				}				
			}
			StringBuilder sBuilder = new StringBuilder();
			
			if(! errorsList.isEmpty()){
				int count = 1;
				sBuilder.append("The following common errors has been appeared during processing the Free Disk Space flow : \r\n");				
				for(String message : errorsList){
					sBuilder.append(count+". "+message + "\r\n");
					count++;
				}
			}			
			if(sBuilder.length() > 0){	
				try {
					notificationSender.sendEmail(sBuilder.toString(), null);
				} catch (Exception e1) {
					logger.error(e1.getMessage());
					persistenceManager.persistNotificationEmail(sBuilder.toString(), null);
				}				
			}
		}catch(Exception e){
			String errorMessage = "An unexpected error has occurred while executing the Free Disk Space flow. Error message : \r\n" + e.getMessage();
			logger.warn(errorMessage);
			if(logger.isDebugEnabled())
				logger.debug(e.getMessage(), e);
			try {
				notificationSender.sendEmail(errorMessage, null);
			} catch (Exception e1) {
				logger.error(e1.getMessage());
				persistenceManager.persistNotificationEmail(errorMessage, null);
			}
		}finally{
			logger.info("Releasing the occuped resources.");			
		}
	}
}
