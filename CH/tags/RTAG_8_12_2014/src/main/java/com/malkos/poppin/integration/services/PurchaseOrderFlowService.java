package com.malkos.poppin.integration.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.bootstrap.IntegrationBootStraper;
import com.malkos.poppin.encryption.EncryptionManager;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.IncomingMessageStatus;
import com.malkos.poppin.entities.MessageBatchTransfer;
import com.malkos.poppin.entities.MessageType;
import com.malkos.poppin.entities.NSRrequestDetails;
import com.malkos.poppin.entities.OrderErrorDetails;
import com.malkos.poppin.entities.OutgoingMessageStatus;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.entities.PurchaseOrderStatus;
import com.malkos.poppin.integration.services.IPurchaseOrderFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.MessageBatchDAO;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteNullResponseException;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.transport.NetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteOrderAlreadyExistsException;
import com.malkos.poppin.transport.NetsuiteOrderValidationException;
import com.malkos.poppin.utils.ErrorMessageWrapper;
import com.malkos.poppin.utils.ErrorsCollector;
import com.malkos.poppin.utils.OrderErrorMessage;
import com.malkos.poppin.utils.OrderProcessingStep;
import com.malkos.poppin.utils.PoParsingException;
import com.malkos.poppin.utils.XmlParserUtil;


public class PurchaseOrderFlowService implements IPurchaseOrderFlowService {
	
	private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderFlowService.class);
	
	
	private INetsuiteOperationsManager netsuiteOperationsManager;
	
	@Autowired
	IPersistenceManager persistanceManager;	
	
	private EncryptionManager encManager; 

	private OrderProcessingStep processingStep;
	
	@Override
	public void processNewPurchaseOrders() {
		encManager = new EncryptionManager();
		List<PurchaseOrderPojo> generalOrderList = new ArrayList<>();
		List<PurchaseOrderPojo> ordersToSendList = new ArrayList<>();
		
		List<VendorSkuToModelNumMapDAO> inventoryList = persistanceManager.getInventoryDAO();
		
		Map<String, String> popNumToInternalIdMap = new HashMap<>();
		for (VendorSkuToModelNumMapDAO invDAO:inventoryList){
			popNumToInternalIdMap.put(invDAO.getModelNum(), invDAO.getItemInternalId());
		}		
		
		processingStep = OrderProcessingStep.BEFORE_SENDING;
		
		List<IncomingMessageDAO> sourceMessageDAOList = new ArrayList<>();
		List<PurchaseOrderDAO>  previouslyFailedOrders = persistanceManager.getPreviouslyNSprocessingFailedOrders(sourceMessageDAOList);
		if (previouslyFailedOrders.size() > 0){
			List<MessageBatchTransfer> successfullyProcessedPOs = processMessages(sourceMessageDAOList,popNumToInternalIdMap);
			List<PurchaseOrderPojo> ordersToRetry = excludeProccessedOrdersFromBatch(successfullyProcessedPOs,previouslyFailedOrders);
			ordersToSendList.addAll(ordersToRetry);
			generalOrderList.addAll(ordersToRetry);
		}	
		
		List<IncomingMessageDAO> newMessagesDAO = persistanceManager.retrieveNewIncomingMessages();		
		if (!newMessagesDAO.isEmpty()){			
			List<MessageBatchTransfer> successfullyProcessedBatches = processMessages(newMessagesDAO,popNumToInternalIdMap);			
			List<PurchaseOrderPojo> ordersInBatch =  extractOrdersPojo(successfullyProcessedBatches);
			generalOrderList.addAll(ordersInBatch);
			updateOrdersStatus(ordersInBatch);		
			persistanceManager.persistMessageBatch(successfullyProcessedBatches);
			updateOrdersStatus(ordersInBatch);
			ordersToSendList.addAll(extractOrdersToSend(ordersInBatch));
			persistanceManager.updateIncomingMessagesStatus(newMessagesDAO);
		}		
		
		if (ordersToSendList.size()>0){			
			List<PurchaseOrderPojo> successfullySentList = addSalesOrders(ordersToSendList, popNumToInternalIdMap);
			processingStep = OrderProcessingStep.AFTER_SENDING;
			updateOrdersStatus(successfullySentList);			
		}	
		collectErrors(generalOrderList);
		persistanceManager.updatePurchaseOrders(generalOrderList);
		
		List<String> faMessages = getNetsuiteOperationsManager().createFAMessage(generalOrderList);
		List<OutgoingMessageDAO> faMessagesDAOList = saveFunctionalAcknowledgments(faMessages);
		persistanceManager.persistOutgoingMessages(faMessagesDAOList);
	}
	
	private List<OutgoingMessageDAO> saveFunctionalAcknowledgments(List<String> faXmls) {
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		List<OutgoingMessageDAO> messagesList = new ArrayList<>();
		if(faXmls.isEmpty() == false){
			int faCounter = 0;			
			logger.info("There is/are " + faXmls.size() + " fa files to send to Staples.");
			for (String faXml : faXmls) {	
				String todayNow = new SimpleDateFormat(properties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
				String pathTempFile = properties.STAPLES_FA_MESSAGE_PREFIX + todayNow + "_" + faCounter + ".pgp";					
				logger.info("1. Encrypt the files with EncryptionManager");
				try {
					byte[] encrypted = encManager.encrypt(new ByteArrayInputStream(
							faXml.getBytes()));
					String filePath  = properties.getFuncAckEncryptedPath() + pathTempFile;
					FileOutputStream str = new FileOutputStream(filePath);	
					IOUtils.write(encrypted, str);
					str.close();
					OutgoingMessageDAO omDAO = new OutgoingMessageDAO();
					omDAO.setMessagePath(filePath);
					omDAO.setMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
					omDAO.setMessageType(MessageType.FUNCTIONAL_ACKNOWLEDGEMENT);
					messagesList.add(omDAO);
				} catch (Exception ex) {
					String errorMessage = ex.getMessage();
					logger.info(errorMessage);
					//ErrorsCollector.addCommonErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
				}
				faCounter++;
			}
		}
		else{
			logger.info("There is no FA files to send to Staples.");
		}
		return messagesList;
	}

	private List<PurchaseOrderPojo> excludeProccessedOrdersFromBatch(List<MessageBatchTransfer> successfullyProcessedPOs, List<PurchaseOrderDAO> previouslyFailedOrders) {
		List<PurchaseOrderPojo> ordersList = new ArrayList<>();
		for (MessageBatchTransfer mbTransfer:successfullyProcessedPOs){
			ordersList.addAll(mbTransfer.getPurchaseOrders());
		}
		Map<String, PurchaseOrderDAO> poNumberToDAOMao = new HashMap<>();
		for (PurchaseOrderDAO poDAO:previouslyFailedOrders){
			String poNumber = poDAO.getPurchaseOrderNumber();
			if (poNumber != null && !poNumber.isEmpty()){
				poNumberToDAOMao.put(poNumber, poDAO);
			}			
		}
		List<PurchaseOrderPojo> result  = new ArrayList<>(); 
		for (PurchaseOrderPojo poPojo:ordersList){
			if (poNumberToDAOMao.containsKey(poPojo.getPoNumber())){				
				PurchaseOrderDAO poDAo = poNumberToDAOMao.get(poPojo.getPoNumber());
				poPojo.setId(poDAo.getPurchaseOrderId());
				poPojo.setStatus(poDAo.getStatus());
				result.add(poPojo);
			}
		}
		return result;
	}

	private List<PurchaseOrderPojo> extractOrdersToSend(List<PurchaseOrderPojo> ordersInBatch) {
		List<PurchaseOrderPojo> ordersToSend = new ArrayList<>();
		for (PurchaseOrderPojo poPojo:ordersInBatch){
			if (poPojo.getStatus() == PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL && !poPojo.getIsDuplicate()){
				ordersToSend.add(poPojo);
			}
		}
		return ordersToSend;
	}

	private List<PurchaseOrderPojo> extractOrdersPojo(List<MessageBatchTransfer> successfullyProcessedBatches) {
		List<PurchaseOrderPojo> result = new ArrayList<>();
		for (MessageBatchTransfer mb:successfullyProcessedBatches){
			result.addAll(mb.getPurchaseOrders());
		}
		return result;
	}	

	private List<PurchaseOrderPojo> addSalesOrders(List<PurchaseOrderPojo> poPojoListU, Map<String, String> popNumToInternalIdMap) {
		
		List<PurchaseOrderPojo> processedOrdersList = new ArrayList<>();		
		
		for(PurchaseOrderPojo po : poPojoListU){
			if(po.getStatus() == PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL){
				try {
					if (getNetsuiteOperationsManager().addSalesOrder(po, popNumToInternalIdMap)){
						processedOrdersList.add(po);
					}
				} 
				catch (NetsuiteOrderAlreadyExistsException ordrerExistsExc){
					logger.warn(ordrerExistsExc.getMessage());
					po.addException(ordrerExistsExc.getMessage());
					po.setStatus(PurchaseOrderStatus.POPPIN_REJECTED);
					processedOrdersList.add(po);				 
				}
				catch (NetsuiteOrderValidationException orderValidationexception){
					logger.warn(orderValidationexception.getMessage());
					po.addException(orderValidationexception.getMessage());
					po.setStatus(PurchaseOrderStatus.POPPIN_REJECTED);
					processedOrdersList.add(po);
				}
				catch (NetsuiteNullResponseException nsEx/* | NetsuiteOperationException nsEx*/) {
					String errorMessage = "Failed to add sales order with PO # : " + po.getPoNumber() + ". " + nsEx.getMessage(); 
					logger.warn(errorMessage);
					po.addException(errorMessage);
					//ErrorsCollector.addCommonErrorMessage(errorMessage);
					ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
				}
				catch (NetsuiteOperationException nsEx) {
					String errorMessage = "Failed to add sales order with PO # : " + po.getPoNumber() + ". " + nsEx.getMessage(); 
					logger.warn(errorMessage);
					po.addException(errorMessage);
					if(nsEx.getRequestDetails() != null){
						NSRrequestDetails details = nsEx.getRequestDetails();
						if (po.getErrorDetails()==null){
							po.setErrorDetails(new OrderErrorDetails());
						}						
						po.setErrorDetails(new OrderErrorDetails());
						po.getErrorDetails().setRequestFilePath(details.getRequestFilePath());
						po.getErrorDetails().setResponseFilePath(details.getResponseFilePath());
						po.getErrorDetails().setRequestType(details.getRequestType().toString());	
						po.getErrorDetails().setRequestTime(details.getRequestDateTime());
					}
				}
				catch(Exception ex){
					String errorMessage = "Failed to add sales order with PO # : " + po.getPoNumber() + ". " + ex.getMessage(); 
					logger.warn(errorMessage);
					po.addException(errorMessage);					
				}
			}
		}
		return processedOrdersList;
	}

	private void collectErrors(List<PurchaseOrderPojo> generalOrderList){		
		for (PurchaseOrderPojo po :generalOrderList) {
			if(po.getStatus() == PurchaseOrderStatus.POPPIN_REJECTED || po.getStatus() == PurchaseOrderStatus.UNPROCESSIBLE_REJECTED || !po.getExceptionDesc().isEmpty()){
				/*OrderErrorMessage orderErrorMessage = new OrderErrorMessage();
				orderErrorMessage.setPoNumber(po.getPoNumber());
				orderErrorMessage.setMbNumber(po.getOrderMessageBatch());
				orderErrorMessage.setMbFile(po.getOrderBatchPath());
				orderErrorMessage.setPoOrderDate(po.getOrderDate());
				orderErrorMessage.setErrorDetails(po.getExceptionDesc());
				ErrorsCollector.addOrderErrorMessage(orderErrorMessage);*/
				OrderErrorMessage orderError = new OrderErrorMessage();
				orderError.setPoNumber(po.getPoNumber());
				orderError.setMbNumber(po.getOrderMessageBatch());
				orderError.setMbFile(po.getOrderBatchPath());
				
				orderError.setPoOrderDate(po.getOrderDate());
				orderError.setErrorDetails(po.getExceptionDesc());
				orderError.setNsUsername(GlobalPropertiesProvider.getGlobalProperties().getNsUsername());
				if (po.getErrorDetails() != null){
					orderError.setVendorSKU(po.getErrorDetails().getErrorVendorPartNumbers());
					orderError.setRequestFilePath(po.getErrorDetails().getRequestFilePath());
					orderError.setResponseFilePath(po.getErrorDetails().getResponseFilePath());
					orderError.setRequestType(po.getErrorDetails().getRequestType());
					orderError.setRequestDateTime(po.getErrorDetails().getRequestTime());
				}				
				CHIntegrationError error = ErrorMessageWrapper.wrapOrderError(orderError);
				ErrorsCollector.addOrderErrorMessage(error);
			}
		}		
	}
	
	private void updateOrdersStatus(List<PurchaseOrderPojo> poPojoListU) {
		for(PurchaseOrderPojo po : poPojoListU){
			boolean exceptionIsEmpty = po.getExceptionDesc().isEmpty() ? true : false;
			
			if(processingStep == OrderProcessingStep.BEFORE_SENDING){
				if(exceptionIsEmpty){
					po.setStatus(PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL);
				}
				else{
					po.setStatus(PurchaseOrderStatus.UNPROCESSIBLE_REJECTED);
				}
			}
			else if(processingStep == OrderProcessingStep.AFTER_SENDING && po.getStatus() == PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL){
				if(exceptionIsEmpty){
					po.setStatus(PurchaseOrderStatus.POPPIN_PENDING_CONFIRMATION);
				}
				else{
					po.setStatus(PurchaseOrderStatus.POPPIN_REJECTED);
				}
			}
		}
	}
	
	private List<MessageBatchTransfer> processMessages(List<IncomingMessageDAO> newMessagesDAO, Map<String,String> vendorSkuToInventoryDAOMap) {
		List<MessageBatchTransfer> result = new ArrayList<>();		
		for (IncomingMessageDAO messageDAO:newMessagesDAO){
			String path = messageDAO.getMessagePath();
			try {
				BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(path));				
				MessageBatchTransfer mbTransfer = decryptSaveAndParseFile(path,fileStream);
				if (mbTransfer!= null ){
					mbTransfer.setIdIncomingMessage(messageDAO.getIdIncomingMessages());
					for (PurchaseOrderPojo poPojo : mbTransfer.getPurchaseOrders()){
						poPojo.validateInventoryMapping(vendorSkuToInventoryDAOMap);
					}
					result.add(mbTransfer);
				}
			} catch (FileNotFoundException e) {
				logger.warn("Can't find incoming message = "+path+". It will be skiped");
				//ErrorsCollector.addCommonErrorMessage("Couldn't find purchase order file = "+path+".Reason: "+e.getMessage());
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError("Couldn't find purchase order file = "+path+".Reason: "+e.getMessage()));
			} catch (NoSuchProviderException | IOException | PGPException ex){
				String errorMessage = "Failed to decrypt the stream provided for file: "+ path + ". Reason : " + ex.getMessage();
				logger.warn(errorMessage);
				if (logger.isDebugEnabled())
					logger.debug(errorMessage);				
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			} catch (PoParsingException e) {
				logger.warn(e.getMessage());
				if (logger.isDebugEnabled())
						logger.debug(e.getMessage());
				//ErrorsCollector.addCommonErrorMessage(e.getMessage());
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(e.getMessage()));
			}catch (Exception e) {
				logger.warn(e.getMessage());
				if (logger.isDebugEnabled())
						logger.debug(e.getMessage());
				//ErrorsCollector.addCommonErrorMessage(e.getMessage());
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(e.getMessage()));
			}
		}
		return result;
	}
	
	private MessageBatchTransfer decryptSaveAndParseFile(String path ,BufferedInputStream fileStream) throws NoSuchProviderException, IOException, PGPException, PoParsingException{		
		logger.info("Starting to decrypt the file "	+ path);
		String correctedPath = path.replace((CharSequence)(new String("encrypted")), (CharSequence)(new String("decrypted")));
		String decryptedPath = correctedPath.substring(0, correctedPath.length()-3)+"xml";
		byte[] decrypted = null;
		decrypted = encManager.decrypt(fileStream);	
		logger.info("Saving file "+decryptedPath+" to local storage");
		FileOutputStream fsOut = new FileOutputStream(decryptedPath);
		InputStream fsIn = new ByteArrayInputStream(decrypted);
		IOUtils.copy(fsIn,fsOut);
		fsOut.close();
		fsIn.close();
		InputStream streamToParse = new ByteArrayInputStream(decrypted);
		MessageBatchTransfer mbTransfer = null;
		try {
			mbTransfer = XmlParserUtil.convertXmlStringToPurchaseOrderPojo(streamToParse);
			mbTransfer.setDecryptedFilePath(decryptedPath);
			mbTransfer.setEncryptedFilePath(path);
			for (PurchaseOrderPojo poPojo:mbTransfer.getPurchaseOrders()){				
				poPojo.setOrderBatchPath(decryptedPath);
			}
		} catch (Exception e) {
			String errorMessage = "Failed to parse the stream provided for file: " + decryptedPath + ". Reason : " + e.getMessage();			
			throw new PoParsingException(errorMessage);		
		}
		return mbTransfer;
	}

	public INetsuiteOperationsManager getNetsuiteOperationsManager() {
		return netsuiteOperationsManager;
	}

	public void setNetsuiteOperationsManager(INetsuiteOperationsManager netsuiteOperationsManager) {
		this.netsuiteOperationsManager = netsuiteOperationsManager;
	}

}

