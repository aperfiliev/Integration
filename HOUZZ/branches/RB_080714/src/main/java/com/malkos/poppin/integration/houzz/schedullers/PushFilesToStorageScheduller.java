package com.malkos.poppin.integration.houzz.schedullers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.bootstrap.SchedulledTasksSynchronizer;
import com.malkos.poppin.integration.houzz.entities.IntegrationError;
import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.integration.houzz.services.IFilesStorageService;
import com.malkos.poppin.integration.houzz.services.IInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.services.impl.FilesStorageService;
import com.malkos.poppin.integration.houzz.services.impl.HouzzInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.util.EnvironmentInitializer;
import com.malkos.poppin.integration.houzz.util.ErrorMessageWrapper;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.malkos.poppin.integration.houzz.util.MailMessagingService;

public class PushFilesToStorageScheduller implements Job {

private static Logger logger = LoggerFactory.getLogger(PushFilesToStorageScheduller.class);

private final static int MAXIMUM_ATTEMPT = 20;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {		
		logger.info("Push Files To storage flow task scheduller : Initiating Push Files flow.");
		if(SchedulledTasksSynchronizer.getIsRemoteFileManagerConnectionBusy()){
			logger.warn("-----Another task is executing now. Will make an attempt to get recources to execute the task. -----");			
			int attemtsToGetResource = 1;
			while(attemtsToGetResource  <= MAXIMUM_ATTEMPT){
				try {
					logger.info("************Houzz push files Attempt to get recources: #" + attemtsToGetResource+ " **************");
					Thread.sleep(10000);
					if(SchedulledTasksSynchronizer.getIsRemoteFileManagerConnectionBusy() == false){
						logger.info("***********Houzz push files flow  : finally got the recources. Starting to run the task.************");
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				attemtsToGetResource++;
			}	
			if(SchedulledTasksSynchronizer.getIsRemoteFileManagerConnectionBusy()){	
				logger.warn("***********Push files flow : Has waited to much time for another task to release the resources: skipping this run. ************");
				return;	
			}
		}
		SchedulledTasksSynchronizer.setIsRemoteFileManagerConnectionBusy(true);
		EnvironmentInitializer.initializeDirectoriesEnvironment();
		List<OutgoingMessageDAO> messagesToSend = new ArrayList<>();
		IPersistenceManager persistenceManager = new PersistenceManager();
		try{
			persistenceManager = new PersistenceManager();
			messagesToSend = persistenceManager.getOutgoingMessagesToSend();	
		}
		catch (Exception e){
			String errorMessage = "An unexpected error has occurred while executing the Push Files flow. Error message : \r\n" + e.getMessage();
			logger.warn(errorMessage);
			if(logger.isDebugEnabled())
				logger.debug(e.getMessage(), e);
			String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
			Collection<RetailerAbstract> retailers = RetailerManager.get_retailers();
			for (RetailerAbstract retailer:retailers){
				String subject = retailer.getNotificationEmailSubject() + " - " + todayNow;
				try {					
					MailMessagingService.sendMessage(InternetAddress.parse(retailer.getNotificationEmailsTo()),subject, errorMessage, retailer);
				} catch (MessagingException e1) {
					try{
						persistenceManager.persistNotificationEmail(errorMessage,subject,retailer.getNotificationEmailsTo(), null, retailer.getIdentifier());
					} catch (Exception e2){
						logger.error(e2.getMessage());
						e2.printStackTrace();
					}
				}
			}
			return;
		}	
		
		for (RetailerAbstract retailer : RetailerManager.get_retailers()){
			try{				
				IFilesStorageService fileStorageService = new FilesStorageService();
				List<OutgoingMessageDAO> filteredList = filterMessages(messagesToSend, retailer);
				if ((filteredList!=null)&&(!filteredList.isEmpty())){
					List<OutgoingMessageDAO> sentMessages= fileStorageService.pushFilesToStorage(filteredList,retailer);								
					StringBuilder sBuilder = new StringBuilder();
					if(ErrorsCollector.hasCommonPushFilesErrors()){
						int count = 1;
						sBuilder.append("The following common errors has been appeared during processing the Push Files flow : \r\n");
						List<String> errorList = ErrorsCollector.getCommonPushFilesErrorMessages();
						for(String message : errorList){
							sBuilder.append(count+". "+message + "\r\n");
							count++;
						}
					}	else {
						if ((retailer.isNeedSendFileConfirmation())&&(!sentMessages.isEmpty())){
							String todayNow = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
							try {
								List<String> attachments = new ArrayList<>();
								for (OutgoingMessageDAO omDAO:sentMessages){
									attachments.add(omDAO.getOutgoingMessagePath());
								}
								MailMessagingService.sendMultipartMessage(InternetAddress.parse(retailer.getInventoryNotificationEmailsTo()),retailer.getInventoryConfirmationEmailSubject() + " - " + todayNow, retailer.getInventoryConfirmationEmailContent(),retailer,attachments);
							} catch (Exception ex){
								List<String> attachments = new ArrayList<>();
							    for (OutgoingMessageDAO omDAO:sentMessages){
							    	attachments.add(omDAO.getOutgoingMessagePath());
							    }
								persistenceManager.persistNotificationEmail(retailer.getInventoryConfirmationEmailContent(),retailer.getInventoryConfirmationEmailSubject() + " - " + todayNow, retailer.getInventoryNotificationEmailsTo(), attachments, retailer.getIdentifier());
								NSRrequestDetails details = new NSRrequestDetails();
								details.setRetailer(retailer.getIdentifier().toString());
								IntegrationError error = ErrorMessageWrapper.wrapCommonError(ex.getMessage(),details);
								todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
								persistenceManager.persistNotificationEmail(error.getErrorMessage(), retailer.getNotificationEmailSubject() + " - "+todayNow, retailer.getNotificationEmailsTo(), error.getAttachmentsList(), retailer.getIdentifier());	
							}							
						}
					}
					if(sBuilder.length() > 0){	
						if (retailer.isNeedAdditionalLogging()){
							retailer.getLogger().addError(sBuilder.toString());
						}
						String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
						try{
							MailMessagingService.sendMessage(InternetAddress.parse(retailer.getNotificationEmailsTo()),retailer.getNotificationEmailSubject() + " - " + todayNow, sBuilder.toString(),retailer);
						} catch (Exception e1){
							try{
								persistenceManager.persistNotificationEmail(sBuilder.toString(),retailer.getNotificationEmailSubject() + " - " + todayNow,retailer.getNotificationEmailsTo(),null, retailer.getIdentifier());
							} catch (Exception e2){
								logger.error(e2.getMessage());
								e2.printStackTrace();
							}
						}
					}
				}
			}catch(Exception e){
				ErrorsCollector.addCommonPushFilesErrorMessage(e.getMessage());
				String errorMessage = "An unexpected error has occurred while executing the Push Files flow. Error message : \r\n" + e.getMessage();
				if (retailer.isNeedAdditionalLogging()){
					retailer.getLogger().addError(errorMessage);
				}
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
				ErrorsCollector.cleanPushFilesErrors();				
			}
		}		
		logger.info("Releasing the occuped resources.");	
		SchedulledTasksSynchronizer.setIsRemoteFileManagerConnectionBusy(false);
	}
	
	private List<OutgoingMessageDAO> filterMessages(List<OutgoingMessageDAO> messagesToSend,RetailerAbstract retailer){
		List<OutgoingMessageDAO> result = new ArrayList<>();
		for (OutgoingMessageDAO message: messagesToSend){
			if (message.getRetailer()==retailer.getIdentifier()){
				result.add(message);
			}
		}
		return result;
	}
}
