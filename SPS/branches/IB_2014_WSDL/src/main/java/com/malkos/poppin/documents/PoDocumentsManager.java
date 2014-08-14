package com.malkos.poppin.documents;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.xml.XmlParserUtil;

public class PoDocumentsManager {
	
	private static Logger logger = LoggerFactory.getLogger(PoDocumentsManager.class);
	
	private static GlobalProperties globalProperties;
	
	static{
		globalProperties = GlobalPropertiesProvider.getGlobalProperties();	
	}
	
	public List<PoDocument> loadPendingProcessingDocuments(){
		IPersistenceManager persistenceManager = new PersistenceManager();
		List<IncomingMessageDAO> incomingMessagesDAOlist = persistenceManager.loadPendingProcessingDocumentsFilePathList();
		return createDocuments(incomingMessagesDAOlist);
	}
	
	private List<PoDocument> createDocuments(List<IncomingMessageDAO> incomingMessagesDAOlist){
		List<PoDocument> createdPoDocuments = new ArrayList<PoDocument>();
		Map<IncomingMessageDAO, ByteArrayInputStream> pathToInputStreamMap = new HashMap<IncomingMessageDAO, ByteArrayInputStream>();
		for (IncomingMessageDAO incomingMessage : incomingMessagesDAOlist){			
			try {
				InputStream is = new FileInputStream(incomingMessage.getMessagePath());
				byte[] readedBytes = IOUtils.toByteArray(is);
				ByteArrayInputStream bytesStream = new ByteArrayInputStream(readedBytes);
				pathToInputStreamMap.put(incomingMessage, bytesStream);
				is.close();
			} catch (Exception e) {
				String errorMessage = "Could not read document " + incomingMessage + " from local storage. Reason : " + e.getMessage();
				logger.error(errorMessage);
				SPSIntegrationError error = new SPSIntegrationError();
				error.setErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}					
		}		
		for(Entry<IncomingMessageDAO, ByteArrayInputStream> entry : pathToInputStreamMap.entrySet()){
			String fileName = entry.getKey().getMessagePath();
			InputStream streamToParse = entry.getValue();
			try {
				streamToParse.reset();
				logger.info("Parsing and creating PODocuments from " + fileName);
				List<PoDocument> poDocList = XmlParserUtil.convertXmlStringToPurchaseOrderDocument(entry.getValue());
				for(PoDocument poDoc : poDocList){
					//poDoc.setIncomingMessagePath(globalProperties.getCurrentIncomingMessagesDirectory() + File.separator + fileName);
					poDoc.setIncomingMessageId(entry.getKey().getIdIncomingMessage());
					poDoc.setIncomingMessagePath(entry.getKey().getMessagePath());
				}
				createdPoDocuments.addAll(poDocList);
			} catch (Exception e) {
				String errorMessage = "Could not parse " + fileName + ". Reason :" + e.getMessage();
				logger.warn(errorMessage);
				SPSIntegrationError error = new SPSIntegrationError();
				error.setErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}
			finally{
				try {
					if(null != streamToParse)
						streamToParse.close();	
				} catch (IOException e) {
					String errorMessage = "Could not close file output stream for PO file. Reason : " + e.getMessage();
					logger.warn(errorMessage);
					SPSIntegrationError error = new SPSIntegrationError();
					error.setErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(error);
				}
			}
		}
		return createdPoDocuments;
	}
}
