package com.malkos.poppin.transport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.ErrorMessageWraped;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;

public class RemoteFilesOperator implements IRemoteFilesOperator{

	@Autowired
	IRemoteFileManager remoteFileManager;
	
	private static Logger logger = LoggerFactory.getLogger(RemoteFilesOperator.class);
	private static GlobalProperties globalProperties;
	
	static{
		globalProperties = GlobalPropertiesProvider.getGlobalProperties();	
	}
	
	@Override
	public Map<String, ByteArrayInputStream> pullFilesFromFileStorage() {
		Map<String, ByteArrayInputStream> incomingFilesStreamList = new HashMap<String, ByteArrayInputStream>();
		try {
			logger.info("Opening connection with SPS.");
			remoteFileManager.openSession();
			remoteFileManager.setWorkingDirectory(globalProperties.getSpsOutgoingFilesDirectoryName() + "/");
			List<String> fileList = remoteFileManager.getDirectoryFilesNames();
			for(String fileName : fileList){
				logger.info("Pulling " + fileName + " from SPS.");
				InputStream incomingFileStream = remoteFileManager.getFileContent(fileName);
				try {
					byte[] readedBytes = IOUtils.toByteArray(incomingFileStream);
					ByteArrayInputStream bytesStream = new ByteArrayInputStream(readedBytes);
					incomingFilesStreamList.put(fileName, bytesStream);
					logger.info("deleting " + fileName + " from SPS.");
					remoteFileManager.deleteFileContent(fileName);
				} catch (Exception e) {
					String errorMessage = "Could not read file " + fileName + " from remote file manager. Reason : " + e.getMessage();
					logger.error(errorMessage);
					SPSIntegrationError error = new SPSIntegrationError();
					error.setErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(error);
				}
				finally{
					incomingFileStream.close();
				}
			}
		
		} catch (Exception e) {
			String errorMessage = "Could not retrieve files from remote file manager. Reason: " + e.getMessage();
			logger.warn(errorMessage);			
			SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage, null);			
			ErrorsCollector.addCommonErrorMessage(error);
		}
		finally{
			try {
				remoteFileManager.closeSession();
			} catch (IOException e) {
				String errorMessage = "Could not close connection to remote file manager. Reason: " + e.getMessage();
				logger.warn(errorMessage);
				SPSIntegrationError error = new SPSIntegrationError();
				error.setErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}
		}
		return incomingFilesStreamList;
	}

	@Override
	public Set<String> saveFilesToLocalFileSystem(Map<String, ByteArrayInputStream> incomingPoFileNameToStreamMap) {
		Set<String> incomingMessagesPathsSet = new HashSet<>();
		for(Entry<String, ByteArrayInputStream> entry : incomingPoFileNameToStreamMap.entrySet()){
			logger.info("Saving " + entry.getKey());
			FileOutputStream poFileOutputStream = null;
			try {
				String filePath = globalProperties.getCurrentIncomingMessagesDirectory() + File.separator + entry.getKey();
				poFileOutputStream = new FileOutputStream(globalProperties.getCurrentIncomingMessagesDirectory() + File.separator + entry.getKey());
				IOUtils.copy(entry.getValue(), poFileOutputStream);
				incomingMessagesPathsSet.add(filePath);
			} catch (Exception e) {
				String errorMessage = "Could not save file " + entry.getKey() + " on file system. Reason : " + e.getMessage();
				logger.warn(errorMessage);
				logger.info("Removing failed file from collection to not process it anymore.");
				incomingPoFileNameToStreamMap.remove(entry.getKey());
				SPSIntegrationError error = new SPSIntegrationError();
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
					SPSIntegrationError error = new SPSIntegrationError();
					error.setErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(error);
				}
			}
		}
		return incomingMessagesPathsSet;
	}

	@Override
	public List<OutgoingMessageDAO> pushFilesToFilesStorage(List<OutgoingMessageDAO> outgoingMessages) {
		List<OutgoingMessageDAO> sentMessagessIdList = new ArrayList<OutgoingMessageDAO>();
		try {
			logger.info("Opening connection with SPS.");
			remoteFileManager.openSession();
			for(OutgoingMessageDAO omDAO : outgoingMessages){
				File fileToSend = new File(omDAO.getOutgoingMessagePath());
				InputStream streamToSend = new FileInputStream(fileToSend);
				logger.info("Sending " + fileToSend.getPath() + " to SPS.");
				try {
					remoteFileManager.sendFileContent(streamToSend, globalProperties.getSpsIncomingFilesDirectoryName() + "/" + fileToSend.getName());
					sentMessagessIdList.add(omDAO);
				} catch (Exception e) {
					String errorMessage = "Could not send " + fileToSend.getPath() + " to SPS. Reason :" + e.getMessage();
					SPSIntegrationError error = new SPSIntegrationError();
					error.setErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(error);
				}
				finally{
					if(null != streamToSend)
						streamToSend.close();
				}
			}
			
		} catch (Exception e) {
			String errorMessage = "Could not send outgoing messages to SPS. Reason :" + e.getMessage();
			logger.warn(errorMessage);			 
			SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage,null);			
			ErrorsCollector.addCommonErrorMessage(error);			
		}
		finally{
			try {
				remoteFileManager.closeSession();
			} catch (IOException e) {
				String errorMessage = "Could not close connection to remote file manager. Reason: " + e.getMessage();
				logger.warn(errorMessage);
				SPSIntegrationError error = new SPSIntegrationError();
				error.setErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}
		}
		return sentMessagessIdList;
	}

}
