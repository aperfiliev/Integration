package com.malkos.poppin.integration.services;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.encryption.EncryptionManager;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.MessageType;
import com.malkos.poppin.entities.OutgoingMessageStatus;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteNullResponseException;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.utils.ErrorMessageWrapper;
import com.malkos.poppin.utils.ErrorsCollector;
import com.malkos.poppin.utils.XmlParserUtil;
import com.netsuite.webservices.transactions.sales_2014_2.SalesOrder;

public class ShippingConfirmationFlowService implements IShippingConfirmationFlowService{
	public static final Logger logger = LoggerFactory.getLogger(ShippingConfirmationFlowService.class);
	
	private INetsuiteOperationsManager netsuiteOperationsManager;
	
	@Autowired
	IPersistenceManager persistanceManager;	
	
	private EncryptionManager encManager; 
	
	@Override
	public void processShippingConfirmations() {
		String shippingConfirmation = retrieveShippingConfirmationsFromPoppin();
		if (!shippingConfirmation.isEmpty()){			
			persistanceManager.persistOutgoingMessages(saveShippingConfirmations(shippingConfirmation));
		}
	}
	
	private String retrieveShippingConfirmationsFromPoppin() {
		logger.info("Poppin Client starts retrieving Shipping Confirmations. It might take some time. Please wait...");
		
		//1. get poIds with pending_confirmation status
		//2. get sales orders with pendingBilled/billed status for ids from 1 p.
		//3. create XML string confirmation message for ordres retrieved in 2 p. 
		
		//1.
		logger.info("Searching for PO with pending confirmation status in DB.");
		List<String> poIds = persistanceManager.getPoIdsWithPendingConfirmationStatus();
		
		List<SalesOrder> records = new ArrayList<SalesOrder>();
		List<String> poNumbersToUpdate = new ArrayList<String>();
		//2.
		if(!poIds.isEmpty())
		{
			try {
				logger.info("Retrieving Shipping confirmations from poppin. It might take some time. Please wait...");
				records = getNetsuiteOperationsManager().getPendingBilledOrBilledSalesOrdersFromPoppin(poIds);
			} catch (/*NetsuiteNullResponseException |*/ NetsuiteOperationException e) {
				records.clear();
				String errorMessage = "Failed to retrieve pendingBilled/billed sales orders. Reason : " + e.getMessage();
				CHIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),e.getRequestDetails());
				logger.warn(errorMessage);
				ErrorsCollector.addCommonErrorMessage(error);
			}
		}
		else{
			logger.info("There are no sales orders in pending billed or billed state in application DB.");
		}
		//3.
		String output = "";
		if(!records.isEmpty()){
			try {
				output = XmlParserUtil.convertSalesOrderListToConfirmationMessage(records);
				for (SalesOrder so : records) {
					if (so.getOtherRefNum() != null){
						poNumbersToUpdate.add(so.getOtherRefNum());
					}
				}
				logger.info("Confirmation message has been created properly.");
				
				//updating db
				persistanceManager.updatePoWithPendingConfirmationStatus(poNumbersToUpdate);
			} catch (ParserConfigurationException | TransformerException ex) {
				String errorMessage = "Failed to create Confirmation message properly. Reason : " + ex.getMessage();
				logger.warn(errorMessage);
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			}
		}
		else{
			logger.info("There are no sales orders in pending billed or billed state . So there is nothing to add in Confirmation message message.");
		}
		return output;
	}

	private List<OutgoingMessageDAO> saveShippingConfirmations(String confirmationXml) {
		logger.info("Saving confirmation messages ");
		List<OutgoingMessageDAO> messageList = new ArrayList<>();
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		//String pathTempFile = "xmlsource/encrypted/confirm.pgp";
		String todayNow = new SimpleDateFormat(properties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
		String pathTempFile = properties.STAPLES_CONFIRM_MESSAGE_PREFIX + todayNow +  ".pgp";

		// 1. Encrypt the file with EncryptionManager with CommerceHub
		// public
		// key
		logger.info("1. Encrypt the file with EncryptionManager.");
		try {
			encManager = new EncryptionManager();
			byte[] encrypted = encManager.encrypt(new ByteArrayInputStream(
					confirmationXml.getBytes()));
			String messagePath = properties.getConfirmationEncryptedPath() +  pathTempFile;
			FileOutputStream str = new FileOutputStream(messagePath);
			IOUtils.write(encrypted, str);
			str.close();
			OutgoingMessageDAO omDAO = new OutgoingMessageDAO();
			omDAO.setMessagePath(messagePath);
			omDAO.setMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
			omDAO.setMessageType(MessageType.SHIPPING_CONFIRMATION);
			messageList.add(omDAO);
			logger.info(str.toString());
		} catch (Exception ex) {
			String errorMessage = ex.getMessage();
			logger.info(errorMessage);
			//ErrorsCollector.addCommonErrorMessage(errorMessage);
			ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
		}
		return messageList;
	}

	
	public INetsuiteOperationsManager getNetsuiteOperationsManager() {
		return netsuiteOperationsManager;
	}

	public void setNetsuiteOperationsManager(INetsuiteOperationsManager netsuiteOperationsManager) {
		this.netsuiteOperationsManager = netsuiteOperationsManager;
	}
}
