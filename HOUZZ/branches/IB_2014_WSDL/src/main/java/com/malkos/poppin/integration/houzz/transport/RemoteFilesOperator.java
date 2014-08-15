package com.malkos.poppin.integration.houzz.transport;

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

import com.malkos.poppin.integration.houzz.bootstrap.GlobalProperties;
import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;

public class RemoteFilesOperator implements IRemoteFilesOperator{

	
	private static Logger logger = LoggerFactory.getLogger(RemoteFilesOperator.class);
	private static GlobalProperties globalProperties;
	
	static{
		globalProperties = GlobalPropertiesProvider.getGlobalProperties();	
	}

	@Override
	public List<OutgoingMessageDAO> pushFilesToFilesStorage(List<OutgoingMessageDAO> outgoingMessages, RetailerAbstract retailer) {
		List<OutgoingMessageDAO> sentMessagessIdList = new ArrayList<OutgoingMessageDAO>();
		IRemoteFileManager remoteFileManager = new FTPFileManager();
		try {
			logger.info("Opening connection with retailer.");			
			for(OutgoingMessageDAO omDAO : outgoingMessages){
				remoteFileManager.openSession(retailer);
				File fileToSend = new File(omDAO.getOutgoingMessagePath());
				InputStream streamToSend = new FileInputStream(fileToSend);
				logger.info("Sending " + fileToSend.getPath() + " to storage.");
				boolean isSucceed = false;
				try {
					isSucceed = remoteFileManager.sendFileContent(streamToSend, retailer.getIncomingFilesDirName() + "/" + fileToSend.getName());	
					if (retailer.isNeedAdditionalLogging())
						retailer.getLogger().addMessage("File "+fileToSend.getName()+ " was successfully sent to Olapic storage");
				} catch (Exception e) {
					String errorMessage = "Could not send " + fileToSend.getPath() + " . Reason :" + e.getMessage();
					logger.warn(errorMessage);
					ErrorsCollector.addCommonPushFilesErrorMessage(errorMessage);
				}
				finally{
					if(null != streamToSend)
						streamToSend.close();
					if (isSucceed){
						sentMessagessIdList.add(omDAO);
					}
					try {
						remoteFileManager.closeSession();
					} catch (IOException e) {
						String errorMessage = "Could not close connection to remote file manager. Reason: " + e.getMessage();
						logger.warn(errorMessage);
						ErrorsCollector.addCommonPushFilesErrorMessage(errorMessage);
					}
				}
			}
			
		} catch (Exception e) {
			String errorMessage = "Could not send outgoing messages . Reason :" + e.getMessage();
			logger.warn(errorMessage);
			ErrorsCollector.addCommonPushFilesErrorMessage(errorMessage);
		}		
		return sentMessagessIdList;
	}

}
