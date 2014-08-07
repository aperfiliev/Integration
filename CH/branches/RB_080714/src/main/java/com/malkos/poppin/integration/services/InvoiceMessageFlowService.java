package com.malkos.poppin.integration.services;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.malkos.poppin.entities.CustomerShppingAddressPojo;
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
import com.netsuite.webservices.transactions.sales_2012_1.SalesOrder;

public class InvoiceMessageFlowService implements IInvoiceMessageService  {
	
public static final Logger logger = LoggerFactory.getLogger(InvoiceMessageFlowService.class);
	
	private INetsuiteOperationsManager netsuiteOperationsManager;
	
	@Autowired
	IPersistenceManager persistanceManager;	
	
	private EncryptionManager encManager; 

	@Override
	public void processOrderInvoicesFromPoppin() {
		String invoiceMessage = retrieveOrderInvoicesFromPoppin();
		if (!invoiceMessage.isEmpty()){			
			persistanceManager.persistOutgoingMessages(saveInvoiceMessage(invoiceMessage));
		}
	}

	private String retrieveOrderInvoicesFromPoppin() {
		logger.info("Retrieving order invoices from poppin.");
		logger.info("Searching for PO with staples confirmation accepted status in DB.");
		List<String> poIds = persistanceManager.getPoIdsWithConfirmationAcceptedStatus();
		List<SalesOrder> records = new ArrayList<SalesOrder>();
		List<String> poNumbersToUpdate = new ArrayList<String>();
		Map<String, String> itemInternalIdToItemNumberMap = new HashMap<String, String>();
		Map<String, CustomerShppingAddressPojo> salesOrderIdToShippingAddressMap = new HashMap<String, CustomerShppingAddressPojo>();
		String output = "";
		//2.
		if(!poIds.isEmpty())
		{
			try {
				logger.info("Retrieving Sales Orders with billed status from Poppin. It might take some time. Please wait...");
				//records = netsuiteManager.getBilledSalesOrdersFromPoppin(poIds);
				records = getNetsuiteOperationsManager().getBilledSalesOrdersFromPoppinAdvanced(poIds);
				itemInternalIdToItemNumberMap = getNetsuiteOperationsManager().getItemInternalIdToItemNumberMap(records);
				salesOrderIdToShippingAddressMap = getNetsuiteOperationsManager().getsalesOrderIdToShippingAddressMap(records);
			} catch (NetsuiteOperationException e ) {
				records.clear();
				String errorMessage = "Failed to retrieve pendingBilled/billed sales orders. Reason : " + e.getMessage();
				logger.warn(errorMessage);
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				CHIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(), e.getRequestDetails());
				ErrorsCollector.addCommonErrorMessage(error);
			}
			if(!records.isEmpty()){
				try {
					output = XmlParserUtil.convertSalesOrderListToOrderInvoicesMessage(records, itemInternalIdToItemNumberMap, salesOrderIdToShippingAddressMap);
					for (SalesOrder so : records) {
						if (so.getOtherRefNum() != null){
							poNumbersToUpdate.add(so.getOtherRefNum());
						}
					}
					persistanceManager.updatePoWithConfirmationAcceptedStatus(poNumbersToUpdate);
					logger.info("Orders invoices message has been created properly.");
					
					//updating db
					//persitenceManager.updatePoWithPendingConfirmationStatus(poNumbersToUpdate);
				} catch (ParserConfigurationException | TransformerException ex) {
					String errorMessage = "Failed to create Orders invoices message properly. Reason : " + ex.getMessage();
					logger.warn(errorMessage);
					//ErrorsCollector.addCommonErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
				}
			}
			else{
				logger.info("There are no sales orders in billed state . So there is nothing to add in Orders Invoices message.");
			}
		}
		else{
			logger.info("There are no sales orders in pending billed or billed state in application DB.");
		}
		return output;
	}
	
	private List<OutgoingMessageDAO> saveInvoiceMessage(String orderInvoicesXml) {
		List<OutgoingMessageDAO> messageList = new ArrayList<>();
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		logger.info("Saving order invoices");

		//String pathTempFile = "xmlsource/encrypted/confirm.pgp";
		String todayNow = new SimpleDateFormat(properties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
		String pathTempFile = properties.STAPLES_INVOICE_MESSAGE_PREFIX + todayNow +  ".pgp";

		// 1. Encrypt the file with EncryptionManager with CommerceHub
		// public
		// key
		logger.info("1. Encrypt the file with EncryptionManager.");
		try {
			encManager = new EncryptionManager();
			byte[] encrypted = encManager.encrypt(new ByteArrayInputStream(
					orderInvoicesXml.getBytes()));
			String messagePath = properties.getInvoiceEncryptedPath() +  pathTempFile;
			FileOutputStream str = new FileOutputStream(messagePath);

			IOUtils.write(encrypted, str);
			logger.info(str.toString());
			str.close();
			OutgoingMessageDAO omDAO = new OutgoingMessageDAO();
			omDAO.setMessagePath(messagePath);
			omDAO.setMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
			omDAO.setMessageType(MessageType.ORDER_INVOICE);
			messageList.add(omDAO);
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
