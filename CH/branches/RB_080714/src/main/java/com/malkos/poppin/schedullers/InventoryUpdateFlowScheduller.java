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

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.bootstrap.Synchronizer;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.integration.services.IInventoryUpdateFlowService;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.utils.EnvironmentInitializer;
import com.malkos.poppin.utils.ErrorsCollector;
import com.malkos.poppin.utils.NotificationEmailSender;


public class InventoryUpdateFlowScheduller{

private static Logger logger = LoggerFactory.getLogger(InventoryUpdateFlowScheduller.class);	
private static final int MAXIMUM_ATTEMPT = 1000;
@Autowired
//private SimpleMailMessage notificationMailMessage;
private NotificationEmailSender notificationSender;
@Autowired
private MailSender mailSender;
@Autowired
PersistenceManager persistenceManager;
@Autowired
private IInventoryUpdateFlowService inventoryUpdateService;
	
	public void processTask() {	
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		logger.info("Inventory Update flow task scheduller : Initiating Inventory Update flow.");		
		if(Synchronizer.getIsAnotherTaskRunning()){
			logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");				
			int attemtsToGetResource = 1;
			while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
				try {
					logger.info("************Inventory Update flow Attempt to get recources: #" + attemtsToGetResource+ " **************");
					Thread.sleep(10000);
					if(Synchronizer.getIsAnotherTaskRunning() == false){
						logger.info("***********Inventory Update flow  : finally got the recources. Starting to run the task.************");
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				attemtsToGetResource++;
			}	
			if(Synchronizer.getIsAnotherTaskRunning()){	
				logger.warn("***********Inventory Update flow : Has waited to much time for another task to release the resources: skipping this run. ************");
				return;	
			}
		}
		boolean isSucceed = false;
		int atemptsToRetry = 0;
		Synchronizer.setIsAnotherTaskRunning(true);		
		EnvironmentInitializer.initializeDirectoriesEnvironment();		
		while ((!isSucceed) && (atemptsToRetry <= Integer.parseInt(properties.getInventoryUpdateAutoretryAttempts()))) {
			atemptsToRetry++;		
			try{			
				inventoryUpdateService.processTask();
				StringBuilder sBuilder = new StringBuilder();
				List<String> attachments = new ArrayList<>();
				if(ErrorsCollector.hasCommonErrors()){
					int count = 1;
					sBuilder.append("The following common errors has been appeared during processing the Inventory Update flow : \r\n");
					//sBuilder.append("Run # "+atempts);
					//List<String> errorList = ErrorsCollector.getCommonErrorMessages();
					List<CHIntegrationError> errorList = ErrorsCollector.getCommonErrorMessages();
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
					} catch (MessagingException e){
						logger.error(e.getMessage());
						persistenceManager.persistNotificationEmail(sBuilder.toString(), attachments);
					}									
				}
			}catch(Exception e){
				//ErrorsCollector.addCommonErrorMessage(e.getMessage());
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(e.getMessage()));
				String errorMessage = "An unexpected error has occurred while executing the Inventory Update flow. Error message : \r\n" + e.getMessage();//+". Run # "+atempts;
				logger.warn(errorMessage);
				if(logger.isDebugEnabled())
					logger.debug(e.getMessage(), e);
				/*SimpleMailMessage mailMessage = new SimpleMailMessage(notificationMailMessage);
				mailMessage.setText(errorMessage);
				mailSender.send(mailMessage);*/
				try {
					notificationSender.sendEmail(errorMessage, null);
				} catch (MessagingException e1) {
					logger.error(e1.getMessage());
					persistenceManager.persistNotificationEmail(errorMessage, null);
				}
			}finally{
				//logger.info("Run # "+atempts+". Succeed = "+isSucceed+"Errors count = "+ErrorsCollector.getCommonInventoryUpdateErrorMessages().size());				
				if (!ErrorsCollector.hasCommonErrors()){
					isSucceed = true;
					break;
				} else {
					try {
						if (Integer.parseInt(properties.getInventoryUpdateAutoretryAttempts()) >= atemptsToRetry)
							Thread.sleep(1000*60*Integer.parseInt(properties.getInventoryUpdateAutoretryInterval()));
					} catch (InterruptedException e) {							
						e.printStackTrace();
					}
				}				
				ErrorsCollector.cleanCommonErrors();				
			}	
		}
		Synchronizer.setIsAnotherTaskRunning(false);
		logger.info("Releasing the occuped resources.");
	}

}
