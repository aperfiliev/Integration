package com.malkos.poppin.bootstrap;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.integration.services.IPoppinDataLoaderService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.NotificationEmailSender;

public class PoppinDataLoader {
	@Autowired
	IPoppinDataLoaderService poppinDataLoaderService;	
	@Autowired
	NotificationEmailSender notificationSender;
	@Autowired 
	IPersistenceManager persistenceManager;
	
	private static Logger logger = LoggerFactory.getLogger(PoppinDataLoader.class);
	
	public void loadIntegrationRequiredPoppinData(){
		try{
			poppinDataLoaderService.loadIntegrationRequiredPoppinData();
			StringBuilder sBuilder = new StringBuilder();
			List<String> attachments = new ArrayList<String>();			
			if(ErrorsCollector.hasCommonErrors()){
				int count = 1;
				sBuilder.append("The following common errors has been appeared during processing the Poppin Data Loader flow : \r\n");				
				List<SPSIntegrationError> errorList = ErrorsCollector.getCommonErrorMessages();
				for(SPSIntegrationError message : errorList){
					if (message.hasAttachments()){
						attachments.addAll(message.getAttachmentsList());
					}
					sBuilder.append(count+". "+message.getErrorMessage() + "\r\n");
					count++;
				}
			}
			
			if(sBuilder.length() > 0){				
				try {
				    notificationSender.sendEmail(sBuilder.toString(), attachments);
				}catch (Exception ex){
					persistenceManager.persistNotificationEmail(sBuilder.toString(), attachments);
				}
			}
		} catch(Exception e){
			String errorMessage = "An unexpected error has occurred while executing the SPS push files flow. Error message : \r\n" + e.getMessage();
			logger.warn(errorMessage);
			if(logger.isDebugEnabled())
				logger.debug(e.getMessage(), e);
			try {
			    notificationSender.sendEmail(errorMessage, null);
			}catch (Exception ex){
				persistenceManager.persistNotificationEmail(errorMessage, null);
			}
		}finally{
			logger.info("Releasing the occuped resources.");
			ErrorsCollector.cleanErrors();
			SchedulledTasksSynchronizer.setIsRemoteFileManagerConnectionBusy(false);
		}
	}
	
	public void loadRequiredDataAfterDataMigration(){
		try{
			poppinDataLoaderService.loadRequiredDataAfterDataMigration();
			StringBuilder sBuilder = new StringBuilder();
			List<String> attachments = new ArrayList<String>();	
			if(ErrorsCollector.hasCommonErrors()){
				int count = 1;
				sBuilder.append("The following common errors has been appeared during processing the Poppin Data Loader flow : \r\n");
				List<SPSIntegrationError> errorList = ErrorsCollector.getCommonErrorMessages();
				for(SPSIntegrationError message : errorList){
					if (message.hasAttachments()){
						attachments.addAll(message.getAttachmentsList());
					}
					sBuilder.append(count+". "+message.getErrorMessage() + "\r\n");
					count++;
				}
			}
			
			if(sBuilder.length() > 0){
				try {
				    notificationSender.sendEmail(sBuilder.toString(), attachments);
				}catch (Exception ex){
					persistenceManager.persistNotificationEmail(sBuilder.toString(), attachments);
				}
			}
		} catch(Exception e){
			String errorMessage = "An unexpected error has occurred while executing the SPS push files flow. Error message : \r\n" + e.getMessage();
			logger.warn(errorMessage);
			if(logger.isDebugEnabled())
				logger.debug(e.getMessage(), e);
			try {
			    notificationSender.sendEmail(errorMessage, null);
			}catch (Exception ex){
				persistenceManager.persistNotificationEmail(errorMessage, null);
			}
		}finally{
			logger.info("Releasing the occuped resources.");
			ErrorsCollector.cleanErrors();
			SchedulledTasksSynchronizer.setIsRemoteFileManagerConnectionBusy(false);
		}
	}
}
