package com.malkos.poppin.transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.utils.ErrorMessageWrapper;
import com.malkos.poppin.utils.ErrorsCollector;

public class RemoteFilesOperator implements IRemoteFilesOperator{

	
	private static Logger logger = LoggerFactory.getLogger(RemoteFilesOperator.class);
	
	private String chIncomingFaDir;
	private String chInventoryUpdateDir;
	private String chOrderInvoiceDir;
	private String chShippingConfirmationDir;
	private String chOutgoingFilesDir;
	
	@Autowired
	private IRemoteFileManager remoteFileManager;
	
	@Override
	public List<OutgoingMessageDAO> pushFilesToFilesStorage(List<OutgoingMessageDAO> outgoingMessages) {
		List<OutgoingMessageDAO> sentMessagessIdList = new ArrayList<OutgoingMessageDAO>();		
		try {
			logger.info("Opening connection with SPS.");
			remoteFileManager.openSession();
			for(OutgoingMessageDAO omDAO : outgoingMessages){
				String messageDir = null;
				switch (omDAO.getMessageType()){
					case FUNCTIONAL_ACKNOWLEDGEMENT: 
						messageDir = getChIncomingFaDir();
						break;
					case INVENTORY_UPDATE:
						messageDir = getChInventoryUpdateDir();
						break;
					case ORDER_INVOICE:
						messageDir = getChOrderInvoiceDir();
						break;
					case SHIPPING_CONFIRMATION:
						messageDir = getChShippingConfirmationDir();
						break;
					default: break;	
				}
				File fileToSend = new File(omDAO.getMessagePath());
				InputStream streamToSend = new FileInputStream(fileToSend);
				logger.info("Sending " + fileToSend.getPath() + " to SPS.");
				boolean isSucceed = false;
				try {
					isSucceed = remoteFileManager.sendFileContent(streamToSend, messageDir + "/" + fileToSend.getName());					
				} catch (Exception e) {
					String errorMessage = "Could not send " + fileToSend.getPath() + " to CH. Reason :" + e.getMessage();
					logger.warn(errorMessage);
					CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
					ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
				}
				finally{
					if(null != streamToSend)
						streamToSend.close();
					if (isSucceed){
						sentMessagessIdList.add(omDAO);
					}
				}
			}
			
		} catch (Exception e) {
			String errorMessage = "Could not send outgoing messages to CH. Reason :" + e.getMessage();
			logger.warn(errorMessage);
			//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
			CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
			ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
		}
		finally{
			try {
				remoteFileManager.closeSession();
			} catch (IOException e) {
				String errorMessage = "Could not close connection to remote file manager. Reason: " + e.getMessage();
				logger.warn(errorMessage);
				//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
				CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
				ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
			}
		}
		return sentMessagessIdList;
	}

	@Override
	public Map<String, ByteArrayInputStream> pullFilesFromFileStorage() {
		Map<String, ByteArrayInputStream> incomingFilesStreamList = new HashMap<String, ByteArrayInputStream>();
		try {
			logger.info("Opening connection with CH.");
			remoteFileManager.openSession();
			remoteFileManager.setWorkingDirectory(getChOutgoingFilesDir() + "/");
			List<String> fileList = remoteFileManager.getDirectoryFilesNames();	
			int poFilesCounter=0;
			for(String fileName : fileList){
				String formattedFileName = GlobalProperties.STAPLES_PO_MESSAGE_PREFIX +  new SimpleDateFormat(GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date()) + "_" + poFilesCounter+".pgp";
				logger.info("Pulling " + fileName + " from CH.");
				InputStream incomingFileStream = remoteFileManager.getFileContent(fileName);
				try {
					byte[] readedBytes = IOUtils.toByteArray(incomingFileStream);
					ByteArrayInputStream bytesStream = new ByteArrayInputStream(readedBytes);
					incomingFilesStreamList.put(formattedFileName, bytesStream);				
				} catch (Exception e) {
					String errorMessage = "Could not read file " + fileName + " from remote file manager. Reason: " + e.getMessage();
					logger.error(errorMessage);
					//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
					CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
					ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
				}
				finally{
					incomingFileStream.close();
				}
				poFilesCounter++;
			}
		
		} catch (Exception e) {
			String errorMessage = "Could not retrieve files from remote file manager. Reason: " + e.getMessage();
			logger.warn(errorMessage);
			//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
			CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
			ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
		}
		finally{
			try {
				remoteFileManager.closeSession();
			} catch (IOException e) {
				String errorMessage = "Could not close connection to remote file manager. Reason: " + e.getMessage();
				logger.warn(errorMessage);
				//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
				CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
				ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
			}
		}
		return incomingFilesStreamList;
	}

	@Override
	public Set<String> saveFilesToLocalFileSystem(Map<String, ByteArrayInputStream> fileNameToFileStreamMap) {
		Set<String> incomingMessagesPathsSet = new HashSet<>();
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		for(Entry<String, ByteArrayInputStream> entry : fileNameToFileStreamMap.entrySet()){
			logger.info("Saving " + entry.getKey());
			FileOutputStream poFileOutputStream = null;
			try {
				String filePath = properties.getPurchaseOrderEncryptedPath() + File.separator + entry.getKey();
				poFileOutputStream = new FileOutputStream(filePath);
				IOUtils.copy(entry.getValue(), poFileOutputStream);
				incomingMessagesPathsSet.add(filePath);
			} catch (Exception e) {
				String errorMessage = "Could not save file " + entry.getKey() + " on file system. Reason : " + e.getMessage();
				logger.warn(errorMessage);
				logger.info("Removing failed file from collection to not process it anymore.");
				fileNameToFileStreamMap.remove(entry.getKey());
				//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
				CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
				ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
			}
			finally{
				try {
					if(null != poFileOutputStream)
						poFileOutputStream.close();	
				} catch (IOException e) {
					String errorMessage = "Could not close file output stream for PO file. Reason : " + e.getMessage();
					logger.warn(errorMessage);
					//ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(errorMessage);
					CHIntegrationError error =  ErrorMessageWrapper.wrapCommonError(errorMessage);
					ErrorsCollector.addCommonRemoteFileOperatorErrorMessage(error);
				}
			}
		}
		return incomingMessagesPathsSet;
	}

	public String getChIncomingFaDir() {
		return chIncomingFaDir;
	}

	public void setChIncomingFaDir(String chIncomingFaDir) {
		this.chIncomingFaDir = chIncomingFaDir;
	}

	public String getChInventoryUpdateDir() {
		return chInventoryUpdateDir;
	}

	public void setChInventoryUpdateDir(String chInventoryUpdateDir) {
		this.chInventoryUpdateDir = chInventoryUpdateDir;
	}

	public String getChOrderInvoiceDir() {
		return chOrderInvoiceDir;
	}

	public void setChOrderInvoiceDir(String chOrderInvoiceDir) {
		this.chOrderInvoiceDir = chOrderInvoiceDir;
	}

	public String getChShippingConfirmationDir() {
		return chShippingConfirmationDir;
	}

	public void setChShippingConfirmationDir(String chShippingConfirmationDir) {
		this.chShippingConfirmationDir = chShippingConfirmationDir;
	}

	public String getChOutgoingFilesDir() {
		return chOutgoingFilesDir;
	}

	public void setChOutgoingFilesDir(String chOutgoingFilesDir) {
		this.chOutgoingFilesDir = chOutgoingFilesDir;
	}

}
