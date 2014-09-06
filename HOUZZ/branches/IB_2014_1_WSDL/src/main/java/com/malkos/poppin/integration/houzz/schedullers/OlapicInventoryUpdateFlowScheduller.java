package com.malkos.poppin.integration.houzz.schedullers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.bootstrap.SchedulledTasksSynchronizer;
import com.malkos.poppin.integration.houzz.entities.IntegrationError;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistenceManager;
import com.malkos.poppin.integration.houzz.services.IInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.services.impl.HouzzInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.services.impl.OlapicInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.util.EnvironmentInitializer;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.malkos.poppin.integration.houzz.util.MailMessagingService;

public class OlapicInventoryUpdateFlowScheduller  implements Job{

private static Logger logger = LoggerFactory.getLogger(OlapicInventoryUpdateFlowScheduller.class);	
private static final int MAXIMUM_ATTEMPT = 20;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {			
		logger.info("Inventory Update flow task scheduller : Initiating Inventory Update flow.");		
		if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){
			logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");				
			int attemtsToGetResource = 1;
			while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
				try {
					logger.info("************Inventory Update flow Attempt to get recources: #" + attemtsToGetResource+ " **************");
					Thread.sleep(10000);
					if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning() == false){
						logger.info("***********Inventory Update flow  : finally got the recources. Starting to run the task.************");
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				attemtsToGetResource++;
			}	
			if(SchedulledTasksSynchronizer.getIsAnotherTaskRunning()){	
				logger.warn("***********Inventory Update flow : Has waited to much time for another task to release the resources: skipping this run. ************");
				return;	
			}
		}
		boolean isSucceed = false;
		int atemptsToRetry = 0;
		SchedulledTasksSynchronizer.setIsAnotherTaskRunning(true);		
		EnvironmentInitializer.initializeDirectoriesEnvironment();
		IInventoryUpdateFlowService inventoryUpdateService = new OlapicInventoryUpdateFlowService();
		IPersistenceManager persistenceManager = new PersistenceManager();
		while ((!isSucceed)&&(atemptsToRetry <= GlobalPropertiesProvider.getGlobalProperties().getInventoryUpdateAutoretryAttempts())) {
			atemptsToRetry++;
			List<String> attachments = new ArrayList<>();
			try{			
				inventoryUpdateService.updateInventory();
				StringBuilder sBuilder = new StringBuilder();
				if(ErrorsCollector.hasCommonInventoryUpdateErrors()){
					int count = 1;
					sBuilder.append("The following common errors has been appeared during processing the Inventory Update flow : \r\n");
					//sBuilder.append("Run # "+atempts);
					List<IntegrationError> errorList = ErrorsCollector.getCommonInventoryUpdateErrorMessages();					
					for(IntegrationError message : errorList){
						sBuilder.append(count+". "+message.getErrorMessage() + "\r\n");
						if ((message.getAttachmentsList()!=null)&&(!message.getAttachmentsList().isEmpty())){
							attachments.addAll(message.getAttachmentsList());
						}
						count++;
					}
				}		
				RetailerAbstract retailer = RetailerManager.get_retailer(RetailerEnum.OLAPIC);
				if(sBuilder.length() > 0){					
						String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());						
						try {							
							MailMessagingService.sendMultipartMessage(retailer.getNotificationEmailSubject() + " - " + todayNow, sBuilder.toString(), retailer,attachments);
						} catch (MessagingException e1) {
							try{
								persistenceManager.persistNotificationEmail(sBuilder.toString(),retailer.getNotificationEmailSubject() + " - " + todayNow,retailer.getNotificationEmailsTo(), attachments, retailer.getIdentifier());
							} catch (Exception e2){
								logger.error(e2.getMessage());
								e2.printStackTrace();
							}
						}
				}
			}catch(Exception e){
				RetailerAbstract retailer = RetailerManager.get_retailer(RetailerEnum.OLAPIC);
				IntegrationError error = new IntegrationError();
				error.setErrorMessage(e.getMessage());
				ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);
				String errorMessage = "An unexpected error has occurred while executing Olapic Inventory Update flow. Error message : \r\n" + e.getMessage();//+". Run # "+atempts;
				retailer.getLogger().addError(errorMessage);
				logger.warn(errorMessage);
				if(logger.isDebugEnabled())
					logger.debug(e.getMessage(), e);
				String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());				
				try {
					MailMessagingService.sendMessage(InternetAddress.parse(retailer.getNotificationEmailsTo()),retailer.getNotificationEmailSubject() + " - " + todayNow, errorMessage, retailer);
				} catch (MessagingException e1) {
					try{
						persistenceManager.persistNotificationEmail(errorMessage,retailer.getNotificationEmailSubject() + " - " + todayNow,retailer.getNotificationEmailsTo(), null, retailer.getIdentifier());
					} catch (Exception e2){
						logger.error(e2.getMessage());
						e2.printStackTrace();
					}
				}
			}finally{
				//logger.info("Run # "+atempts+". Succeed = "+isSucceed+"Errors count = "+ErrorsCollector.getCommonInventoryUpdateErrorMessages().size());				
				if (!ErrorsCollector.hasCommonInventoryUpdateErrors()){
					isSucceed = true;
					break;
				} else {
					try {
						if (GlobalPropertiesProvider.getGlobalProperties().getInventoryUpdateAutoretryAttempts() >= atemptsToRetry)
							Thread.sleep(1000*60*GlobalPropertiesProvider.getGlobalProperties().getInventoryUpdateAutoretryIntervalMinutes());
					} catch (InterruptedException e) {							
						e.printStackTrace();
					}
				}				
				ErrorsCollector.cleanInventoryUpdateErrors();				
			}	
		}
		SchedulledTasksSynchronizer.setIsAnotherTaskRunning(false);
		logger.info("Releasing the occuped resources.");
	}

}
