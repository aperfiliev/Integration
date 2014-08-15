package com.malkos.poppin.integration.houzz.services.impl;

import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistanceManagerProvider;
import com.malkos.poppin.integration.houzz.persistence.PersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.integration.houzz.services.IFilesStorageService;
import com.malkos.poppin.integration.houzz.transport.IRemoteFilesOperator;
import com.malkos.poppin.integration.houzz.transport.RemoteFilesOperator;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;

public class FilesStorageService implements IFilesStorageService{
	
	private static Logger logger = LoggerFactory.getLogger(FilesStorageService.class);	
	private static IPersistenceManager persistenceManager = PersistanceManagerProvider.getInstance();
	private static IRemoteFilesOperator filesOperator = new RemoteFilesOperator();

	@Override
	public List<OutgoingMessageDAO> pushFilesToStorage(List<OutgoingMessageDAO> houzzFiles, RetailerAbstract retailer) {	
		List<OutgoingMessageDAO> result = new ArrayList<>();
		if (houzzFiles!=null && !houzzFiles.isEmpty()){
			logger.info("Sending outgoing messages to retailer storage.");
			if (retailer.isNeedAdditionalLogging()){
				retailer.getLogger().addMessage("Sending outgoing messages to Olapic storage...");
			}
			try {
				result = filesOperator.pushFilesToFilesStorage(houzzFiles, retailer);
				//3
				logger.info("Updating DB. Setting outgoing messages status as SENT for just sent messages.");
				persistenceManager.updateOutgoingMessagesStatuses(result);
				if (retailer.isNeedAdditionalLogging()){
					retailer.getLogger().addMessage("Olapic outgoing messages were successfully sent to storage");
				}
			} catch (NullPointerException e) {
				ErrorsCollector.addCommonPushFilesErrorMessage("Couldn't open connection with Ftp server. Skip PushFilesFlow.");
			}
		}
		else {
			logger.info("There are no new files to send to retailer storage.");
		}	
		return result;
	}
}
