package com.malkos.poppin.integration.services.impl;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.IntegrationBootstraper;
import com.malkos.poppin.integration.services.IFilesStorageService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.transport.IRemoteFilesOperator;

public class FilesStorageService implements IFilesStorageService{

	@Autowired
	IRemoteFilesOperator filesOperator;
	
	@Autowired 
	IPersistenceManager persistenceManager;
	
	private static Logger logger = LoggerFactory.getLogger(FilesStorageService.class);
	
	@Override
	public void pullFilesFromStorage() {
		Map<String, ByteArrayInputStream> fileNameToFileStreamMap = filesOperator.pullFilesFromFileStorage();
		Set<String> incomingMessagesPaths = filesOperator.saveFilesToLocalFileSystem(fileNameToFileStreamMap);
		logger.info("Persisting incoming messages to local DB.");
		persistenceManager.persistIncomingMessages(incomingMessagesPaths);
	}

	@Override
	public void pushFilesToStorage() {
		//1. retrieve records/files from DB/filesystem
		//2. send files to SPS SFTP
		//3. update outgoing messages status in DB
		//1
		logger.info("Getting ourgoing messages to send them to SPS.");
		List<OutgoingMessageDAO> outgoingMessages = persistenceManager.getOutgoingMessagesToSend();
		//2
		logger.info("Sending ourgoing messages to send them to SPS.");
		List<OutgoingMessageDAO> sentMessages = filesOperator.pushFilesToFilesStorage(outgoingMessages);
		//3
		logger.info("Updating DB. Setting outgoing messages status as SENT for just sent messages.");
		persistenceManager.updateOutgoingMessagesStatuses(sentMessages);
	}
}
