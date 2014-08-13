package com.malkos.poppin.schedullers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.malkos.poppin.bootstrap.Synchronizer;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.integration.services.IFilesStorageService;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.utils.EnvironmentInitializer;
import com.malkos.poppin.utils.ErrorsCollector;
import com.malkos.poppin.utils.NotificationEmailSender;


public class PullFilesFromCHScheduller {		
		@Autowired
		IFilesStorageService filesStorageService;		
		@Autowired
		//private SimpleMailMessage notificationMailMessage;
		private NotificationEmailSender notificationSender;
		@Autowired
		private MailSender mailSender;
		@Autowired
		PersistenceManager persistenceManager;
		
		private static final int MAXIMUM_ATTEMPT = 20;
		
		private static Logger logger = LoggerFactory.getLogger(PullFilesFromCHScheduller.class);
		
		public void processTask(){			
			logger.info("CH pull files flow task scheduller : Initiating pull files flow.");
			if(Synchronizer.getIsRemoteFileManagerConnectionBusy()){
				logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");
				
				int attemtsToGetResource = 1;
				while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
					try {
						logger.info("************CH pull files Attempt to get recources: #" + attemtsToGetResource+ " **************");
						Thread.sleep(10000);
						if(Synchronizer.getIsRemoteFileManagerConnectionBusy() == false){
							logger.info("***********Ch pull files flow  : finally got the recources. Starting to run the task.************");
							break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					attemtsToGetResource++;
				}	
				if(Synchronizer.getIsRemoteFileManagerConnectionBusy()){	
					logger.warn("***********CH pull files flow : Has waited to much time for another task to release the resources: skipping this run. ************");
					return;	
				}
			}
			Synchronizer.setIsRemoteFileManagerConnectionBusy(true);
			EnvironmentInitializer.initializeDirectoriesEnvironment();
			try{				
				filesStorageService.pullFilesFromStorage();
				StringBuilder sBuilder = new StringBuilder();
				List<String> attachments = new ArrayList<>();
				
				if(ErrorsCollector.hasRemoteFileManagerErrorMessages()){
					int count = 1;
					sBuilder.append("The following common errors has been appeared during processing the CH pull files flow : \r\n");
					//List<String> errorList = ErrorsCollector.getRemoteFileOperatorErrorMessages();
					List<CHIntegrationError> errorList = ErrorsCollector.getRemoteFileOperatorErrorMessages();
					/*for(String message : errorList){
						sBuilder.append(count+". "+message + "\r\n");
						count++;
					}*/
					for(CHIntegrationError error : errorList){
						if (error.getAttachmentsList() != null){
							attachments.addAll(error.getAttachmentsList());
						}						
						sBuilder.append(count + ". " + error.getErrorMessage() + "\r\n");
						count++;
					}
				}
				
				/*if(sBuilder.length() > 0){
					SimpleMailMessage mailMessage = new SimpleMailMessage(notificationMailMessage);
					String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
					mailMessage.setSubject(mailMessage.getSubject() + " - " + todayNow);
					mailMessage.setText(sBuilder.toString());
					mailSender.send(mailMessage);
				}*/
				if(sBuilder.length() > 0){						
					try {
						notificationSender.sendEmail(sBuilder.toString(), attachments);	
					} catch (Exception e){
						logger.error(e.getMessage());
						persistenceManager.persistNotificationEmail(sBuilder.toString(), attachments);
					}									
				}
			}catch(Exception e){
				String errorMessage = "An unexpected error has occurred while executing the CH pull files flow. Error message : \r\n" + e.getMessage();
				logger.warn(errorMessage);
				if(logger.isDebugEnabled())
					logger.debug(e.getMessage(), e);
				/*SimpleMailMessage mailMessage = new SimpleMailMessage(notificationMailMessage);
				mailMessage.setText(errorMessage);
				mailSender.send(mailMessage);*/
				try {
					notificationSender.sendEmail(errorMessage, null);
				} catch (Exception e1) {
					logger.error(e1.getMessage());
					persistenceManager.persistNotificationEmail(errorMessage, null);
				}
			}finally{
				logger.info("Releasing the occuped resources.");
				ErrorsCollector.cleanRemoteFileOperatorErrors();
				Synchronizer.setIsRemoteFileManagerConnectionBusy(false);
			}			
		}
	}

