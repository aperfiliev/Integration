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
import com.malkos.poppin.integration.services.IInvoiceMessageFlowService;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.util.EnvironmentInitializer;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.NotificationEmailSender;

public class OrderInvoicesFlowScheduller {
	@Autowired
	IInvoiceMessageFlowService invoiceMessageService;
	
	@Autowired
	private NotificationEmailSender notificationSender;
	@Autowired
	PersistenceManager persistenceManager;
	
	private static final int MAXIMUM_ATTEMPT = 20;
	
	private static Logger logger = LoggerFactory.getLogger(OrderInvoicesFlowScheduller.class);
	
	public void processTask(){
		if(SchedulledTasksSynchronizer.getEnvironmentIsReady()){
			logger.info("Invoice flow task scheduller : Initiating purchase order flow.");
			if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){
				logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");
				
				int attemtsToGetResource = 1;
				while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
					try {
						logger.info("************Invoice flow Attempt to get recources: #" + attemtsToGetResource+ " **************");
						Thread.sleep(10000);
						if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning() == false){
							logger.info("***********Invoice flow  : finally got the recources. Starting to run the task.************");
							break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					attemtsToGetResource++;
				}	
				if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){	
					logger.warn("***********Invoice flow : Has waited to much time for another task to release the resources: skipping this run. ************");
					return;	
				}
			}
			SchedulledTasksSynchronizer.setIsAnotherTaskRunning(true);
			try{
				EnvironmentInitializer.initializeDirectoriesEnvironment();
				invoiceMessageService.retrieveReadyForInvoiceOrders();
				
				List<String> attachments = new ArrayList<>();
				StringBuilder sBuilder = new StringBuilder();
				
				if(ErrorsCollector.hasCommonErrors()){
					int count = 1;
					sBuilder.append("The following common errors has been appeared during processing the Order Invoice flow : \r\n");													
					List<SPSIntegrationError> errorList = ErrorsCollector.getCommonErrorMessages();
					for(SPSIntegrationError message : errorList){
						if (message.getAttachmentsList()!=null){
							attachments.addAll(message.getAttachmentsList());
						}						
						sBuilder.append(count + ". " +message.getErrorMessage() + "\r\n");
						count++;
					}	
				}
				ErrorsCollector.cleanErrors();
				if(sBuilder.length() > 0){
					try {
						notificationSender.sendEmail(sBuilder.toString(), attachments);	
					} catch (MessagingException | MailSendException | MailAuthenticationException e){
						logger.error(e.getMessage());
						persistenceManager.persistNotificationEmail(sBuilder.toString(), attachments);
					}	
				}			
			}catch(Exception e){
				String errorMessage = "An unexpected error has occurred while executing the Order Invoice flow. Error message : \r\n" + e.getMessage();
				logger.warn(errorMessage);
				if(logger.isDebugEnabled())
					logger.debug(e.getMessage(), e);
				try {
					notificationSender.sendEmail(errorMessage, null);
				} catch (MessagingException | MailSendException | MailAuthenticationException e1) {
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
