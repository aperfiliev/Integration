package com.malkos.poppin.integration.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.entities.FulfillmentItemPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.InvoiceItemPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.enums.OutgoingMessageType;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.integration.services.IInvoiceMessageFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.FulfillmentLineItemDAO;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.IntegrationObjectsConverter;

public class InvoiceMessageFlowService implements IInvoiceMessageFlowService{

	@Autowired
	IPersistenceManager persistenceManager;
	
	@Autowired
	INetsuiteOperationsManager netsuiteOperationsManager;
	
	private static Logger logger = LoggerFactory.getLogger(InvoiceMessageFlowService.class);
	
	@Override
	public void retrieveReadyForInvoiceOrders() {
		//1. Retrieve orders to process Invoice messages generation 
				logger.info("Getting pending Invoice message generation orders from DB.");
				List<PurchaseOrderDAO> purchaseOrderDAOs = persistenceManager.getPoppinPendingInvoiceGenerationPurchaseOrders();
				
				//2. Retrieve already processed Invoices transactions for that orders
				logger.info("Retrieve already processed invoice transactions for that orders");
				List<TransactionDAO> transactionDAOs = persistenceManager.getProccessedTransactions(purchaseOrderDAOs,TransactionType.INVOICE);
				
				Map<String, PurchaseOrderDAO> soInternalIdToPoDAO = prepareSoNumberToPoDaoMap(purchaseOrderDAOs);
				
				//3. Retrieve new Invoices
				logger.info("Retrieve new Invoices that requires to generate messages for.");		
				List<InvoicePojo> invoicePojoList = retrieveNewInvoices(purchaseOrderDAOs, transactionDAOs , soInternalIdToPoDAO);
				
				//4. Generate xml Invoice message  + store file in file system+ update transaction status
				if (invoicePojoList.size() > 0){
					RetailersManager retailerManager = RetailersManager.getInstance();
					logger.info("Processing retrieved invoices.");
					//Check for shiped items in retived Invoices
					List<FulfillmentLineItemDAO> shipedItems = persistenceManager.getShipedItems(purchaseOrderDAOs);			
					//Filter  Invoice and applying packages and tracking numbers to invoice
					List<InvoicePojo> filteredAndMappedInvoiceList = mapInvoiceItemsToShipItemsAndFilterInvoiceList(shipedItems, invoicePojoList);
					setInvoicePojosPurhcaseOrderTypes(filteredAndMappedInvoiceList, soInternalIdToPoDAO);					
					retailerManager.assignInvoices(filteredAndMappedInvoiceList);					
					List<TransactionDAO> newTransactionDAOs = IntegrationObjectsConverter.convertInvoicePojosToTransactionDAOs(filteredAndMappedInvoiceList);
					List<String> invoiceMessagePathList = retailerManager.askRetailersForInvoiceMessages();
					//5. Insert Invoice message details into DB
					logger.info("Updating DB.");
					persistenceManager.persistOutgoingMessages(invoiceMessagePathList, OutgoingMessageType.INVOICE);
					//6. Insert new processed invoices in DB
					persistenceManager.persistTransactions(newTransactionDAOs);	
					//7. Update PO status after invoice processing
					persistenceManager.updateStatusAfterInvoiceProcessing(purchaseOrderDAOs, filteredAndMappedInvoiceList);
					
					retailerManager.askRetailersToClearDocuments();
				} else {
					logger.info("There no new invoices in Poppin Netsuite");
				}
	}
	private List<InvoicePojo> retrieveNewInvoices(List<PurchaseOrderDAO> purchaseOrderDAOs, List<TransactionDAO> processedInvoices, Map<String, PurchaseOrderDAO> soInternalIdToPoDAO) {
		
		List<InvoicePojo> lstRes = new ArrayList<InvoicePojo>();
		
		Set<String> processedInvoicesInternalIds = new HashSet<String>();
		for (TransactionDAO trDAO : processedInvoices){
			processedInvoicesInternalIds.add(trDAO.getTransactionInternalId());
		}		
		
		if(soInternalIdToPoDAO.keySet().size() > 0){
			try {				
				lstRes = netsuiteOperationsManager.getReadyForMessagingInvoices(soInternalIdToPoDAO.keySet(), processedInvoicesInternalIds);
				if (lstRes.size() > 0){					
					setPODAOsFieldsToInvoicePojos(soInternalIdToPoDAO, lstRes);					
				}			
			} catch (NetsuiteOperationException e) {
				SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),e.getRequestDetails());
				ErrorsCollector.addCommonErrorMessage(error);
			}
		}
		else
			logger.info("There are no PO in DB that requires to generate Invoice messages for.");
		return lstRes;
	}
	private void setPODAOsFieldsToInvoicePojos(Map<String, PurchaseOrderDAO> mapPODaos, List<InvoicePojo> invPojoList){
		for (String internalId : mapPODaos.keySet()){
			for (InvoicePojo invPojo:invPojoList){
				if (internalId.equalsIgnoreCase(invPojo.getSalesorderNsInternalId())){
					invPojo.setIncomingMessagePath(mapPODaos.get(internalId).getIncomingMessageDao().getMessagePath());
					invPojo.setPurchaseOrderId(mapPODaos.get(internalId).getIdPurchaseOrder());
				}
			}
		}
	}
	private List<InvoicePojo> mapInvoiceItemsToShipItemsAndFilterInvoiceList(List<FulfillmentLineItemDAO> shipedItemList, List<InvoicePojo> invoicePojoList) {
		Map<String,List<FulfillmentLineItemDAO>> poNumberToPoShipedItemsMap = new HashMap<>();
		for (FulfillmentLineItemDAO fulItemDAO:shipedItemList){
			String poNumber = fulItemDAO.getPackageDAO().getPurchaseOrder().getPoNumber();
			if (poNumberToPoShipedItemsMap.containsKey(poNumber)){
				List<FulfillmentLineItemDAO> fulfillmentItemDAOList = poNumberToPoShipedItemsMap.get(poNumber);
				fulfillmentItemDAOList.add(fulItemDAO);
			}else{
				List<FulfillmentLineItemDAO> fulfillmentItemDAOList = new ArrayList<>();
				fulfillmentItemDAOList.add(fulItemDAO);
				poNumberToPoShipedItemsMap.put(poNumber, fulfillmentItemDAOList);
			}
		}
		List<InvoicePojo> filteredInvoicePojo = new ArrayList<>();
		for (InvoicePojo invPojo:invoicePojoList){
			String poNumber = invPojo.getPoNumber();			
			if (poNumberToPoShipedItemsMap.containsKey(poNumber)){
				Set<PackageDAO> invoicePackagesSet = new HashSet<>();//Check if there are fulfillments for each po
				boolean isInvoiceShiped = true;
				List<FulfillmentLineItemDAO> fulfillmentItemDAOList = poNumberToPoShipedItemsMap.get(poNumber);
				for (InvoiceItemPojo invItemPojo:invPojo.getItemList()){
					boolean isItemShiped=false;
					for (FulfillmentLineItemDAO fulItemDAO:fulfillmentItemDAOList){
						if  (invItemPojo.getVendorlineNumber().equalsIgnoreCase(fulItemDAO.getVendorLineNumber())){
							isItemShiped=true;
							invItemPojo.setTrackingNumber(fulItemDAO.getTrackingNumber());
							invoicePackagesSet.add(fulItemDAO.getPackageDAO());
							break;
						}						
					}
					isInvoiceShiped&=isItemShiped;
				}
				if (isInvoiceShiped){
					List<PackagePojo> packPojoList =IntegrationObjectsConverter.convertPackageDAOsToPackagePojos(invoicePackagesSet);
					invPojo.setPackageList(packPojoList);
					filteredInvoicePojo.add(invPojo);
				}
			}			
		}
		return filteredInvoicePojo;
	}
	private void setInvoicePojosPurhcaseOrderTypes(List<InvoicePojo> invoicePojos, Map<String, PurchaseOrderDAO> soInternalIdToPoDAO){
		for (InvoicePojo invoicePojo : invoicePojos){
			PurchaseOrderDAO poDao = soInternalIdToPoDAO.get(invoicePojo.getSalesorderNsInternalId());
			invoicePojo.setPurchaseOrderType(poDao.getPoType());
		}
	}
	private Map<String, PurchaseOrderDAO> prepareSoNumberToPoDaoMap(List<PurchaseOrderDAO> poDAOs){
		Map<String, PurchaseOrderDAO> soInternalIdToPoDAO = new HashMap<String, PurchaseOrderDAO>();		
		for(PurchaseOrderDAO poDAO : poDAOs){
			soInternalIdToPoDAO.put(poDAO.getSalesOrderNsInternald(), poDAO);			
		}
		return soInternalIdToPoDAO;
	}	
	
}
