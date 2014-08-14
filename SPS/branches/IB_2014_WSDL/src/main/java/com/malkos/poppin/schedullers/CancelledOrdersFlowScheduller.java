package com.malkos.poppin.schedullers;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.malkos.poppin.bootstrap.SchedulledTasksSynchronizer;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.integration.services.ICanceledOrdersFlowService;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.util.EnvironmentInitializer;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.NotificationEmailSender;

public class CancelledOrdersFlowScheduller {
	@Autowired
	ICanceledOrdersFlowService cancelledOrdersFlowService;
	
	@Autowired
	private NotificationEmailSender notificationSender;
	@Autowired
	PersistenceManager persistenceManager;
	
	private static final int MAXIMUM_ATTEMPT = 20;
	
	private static Logger logger = LoggerFactory.getLogger(CancelledOrdersFlowScheduller.class);
	
	public void processTask(){
		if(SchedulledTasksSynchronizer.getEnvironmentIsReady()){
			logger.info("Cancelled/Closed orders processing task scheduller : Initiating flow.");
			if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){
				logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");
				
				int attemtsToGetResource = 1;
				while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
					try {
						logger.info("************Cancelled/Closed orders flow Attempt to get recources: #" + attemtsToGetResource+ " **************");
						Thread.sleep(10000);
						if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning() == false){
							logger.info("***********Cancelled/Closed orders flow  : finally got the recources. Starting to run the task.************");
							break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					attemtsToGetResource++;
				}	
				if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){	
					logger.warn("***********Cancelled/Closed orders: Has waited to much time for another task to release the resources: skipping this run. ************");
					return;	
				}
			}
			SchedulledTasksSynchronizer.setIsAnotherTaskRunning(true);
			try{
				EnvironmentInitializer.initializeDirectoriesEnvironment();
				cancelledOrdersFlowService.processCancelledOrders();				
				if(ErrorsCollector.hasCommonErrors()){
					StringBuilder sBuilder = new StringBuilder();
					List<SPSIntegrationError> errorList = ErrorsCollector.getCommonErrorMessages();
					int messageCounter = 1;
					List<String> attachments = new ArrayList<>();
					for(SPSIntegrationError message : errorList){
						if (message.getAttachmentsList()!=null){
							attachments.addAll(message.getAttachmentsList());
						}						
						sBuilder.append(messageCounter + ". " +message.getErrorMessage() + "\r\n");
						messageCounter++;
					}					
					ErrorsCollector.cleanErrors();
					String errorMessage = "ERROR(s) has ocurred while executing the Cancelled/closed orders processing flow. Error message(s):\r\n" + sBuilder.toString();
					try {
						notificationSender.sendEmail(errorMessage, attachments);	
					} catch (Exception e){
						logger.error(e.getMessage());
						persistenceManager.persistNotificationEmail(errorMessage, attachments);
					}
				}
			}catch(Exception e){
				String errorMessage = "An unexpected error has occurred while executing the Cancelled/closed orders processing flow. Error message : \r\n" + e.getMessage();
				logger.warn(errorMessage);
				if(logger.isDebugEnabled())
					logger.debug(e.getMessage(), e);
				try {
					notificationSender.sendEmail(errorMessage, null);
				} catch (Exception e1) {
					logger.error(e1.getMessage());
					persistenceManager.persistNotificationEmail(errorMessage, null);
				}
			}
			finally{
				logger.info("Releasing the occuped resources.");
				ErrorsCollector.cleanErrors();
				SchedulledTasksSynchronizer.setIsAnotherTaskRunning(false);
			}
		}
		else{
			logger.info("Working environemt is not ready. It might take from 1 to 5 minutes to initialize the environment so skipping this run.");
		}
	}
}

