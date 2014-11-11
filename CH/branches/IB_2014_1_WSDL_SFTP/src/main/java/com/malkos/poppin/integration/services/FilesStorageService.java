package com.malkos.poppin.integration.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.integration.services.IFilesStorageService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.transport.IRemoteFilesOperator;
import com.malkos.poppin.utils.ErrorsCollector;

public class FilesStorageService implements IFilesStorageService{

	@Autowired
	IRemoteFilesOperator filesOperator;
	
	@Autowired 
	IPersistenceManager persistenceManager;
	
	private static Logger logger = LoggerFactory.getLogger(FilesStorageService.class);
	
	@Override
	public void pullFilesFromStorage() {
		Map<String, ByteArrayInputStream> fileNameToFileStreamMap = filesOperator.pullFilesFromFileStorage();
		if (!fileNameToFileStreamMap.isEmpty()){
			Set<String> incomingMessagesPaths = saveFilesToLocalFileSystem(fileNameToFileStreamMap);
			logger.info("Persisting incoming messages to local DB.");
			persistenceManager.persistIncomingMessages(incomingMessagesPaths);
		} else {
			logger.info("There are no new PO's in CH FTP storage");
		}		
	}

	private Set<String> saveFilesToLocalFileSystem(Map<String, ByteArrayInputStream> incomingPoFileNameToStreamMap) {
		Set<String> incomingMessagesPathsSet = new HashSet<>();
		for(Entry<String, ByteArrayInputStream> entry : incomingPoFileNameToStreamMap.entrySet()){
			logger.info("Saving " + entry.getKey());
			FileOutputStream poFileOutputStream = null;
			try {
				String filePath = GlobalPropertiesProvider.getGlobalProperties().getPurchaseOrderDecryptedPath() + File.separator + entry.getKey();
				poFileOutputStream = new FileOutputStream(filePath);
				IOUtils.copy(entry.getValue(), poFileOutputStream);
				incomingMessagesPathsSet.add(filePath);
			} catch (Exception e) {
				String errorMessage = "Could not save file " + entry.getKey() + " on file system. Reason : " + e.getMessage();
				logger.warn(errorMessage);
				logger.info("Removing failed file from collection to not process it anymore.");
				incomingPoFileNameToStreamMap.remove(entry.getKey());
				CHIntegrationError error = new CHIntegrationError();
				error.setErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}
			finally{
				try {
					if(null != poFileOutputStream)
						poFileOutputStream.close();	
				} catch (IOException e) {
					String errorMessage = "Could not close file output stream for PO file. Reason : " + e.getMessage();
					logger.warn(errorMessage);
					CHIntegrationError error = new CHIntegrationError();
					error.setErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(error);
				}
			}
		}
		return incomingMessagesPathsSet;
	}
	
	
	@Override
	public void pushFilesToStorage() {
		//1. retrieve records/files from DB/filesystem
		//2. send files to SPS SFTP
		//3. update outgoing messages status in DB
		//1
		logger.info("Getting ourgoing messages to send them to CH.");
		List<OutgoingMessageDAO> outgoingMessages = persistenceManager.getOutgoingMessagesToSend();
		//2
		if (!outgoingMessages.isEmpty()){
			try{
				logger.info("Sending ourgoing messages to send them to SPS.");
				List<OutgoingMessageDAO> sentMessages = filesOperator.pushFilesToFilesStorage(outgoingMessages);
				//3
				logger.info("Updating DB. Setting outgoing messages status as SENT for just sent messages.");
				persistenceManager.updateOutgoingMessagesStatuses(sentMessages);
			} catch (NullPointerException e){
				//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage("Couldn't open connection to CH FTP server");
				ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(new CHIntegrationError("Couldn't open connection to CH FTP server"));
			}			
		}		
	}
}
