package com.malkos.poppin.integration.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.engine.jdbc.spi.ExtractedDatabaseMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.entities.FulfillmentItemPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.OrderItemPojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.entities.PurchaseOrderPreloadedFieldsPojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.enums.OutgoingMessageType;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.integration.services.IAsnMessageFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.IntegrationObjectsConverter;

public class AsnMessageFlowService implements IAsnMessageFlowService{
	
	@Autowired
	IPersistenceManager persistenceManager;
	
	@Autowired
	INetsuiteOperationsManager netsuiteOperationsManager;
	
	private static Logger logger = LoggerFactory.getLogger(AsnMessageFlowService.class);
	
	@Override
	public void retrieveReadyForShippingOrders() {			
		logger.info("Getting pending ASN message generation orders from DB.");
		//1. Retrieve orders to process ASN generation 
		List<PurchaseOrderDAO> purchaseOrderDAOs = persistenceManager.getPoppinPendingASNGenerationPurchaseOrders();
		//2. Retrieve already processed fulfillment transactions for that orders
		List<TransactionDAO> transactionDAOs = persistenceManager.getProccessedTransactions(purchaseOrderDAOs,TransactionType.FULFILLMENT);
		//3 Retrieve new Fulfillments that requires to generate ASN messages for and Generate new TransactionDAOs for them
		logger.info("Retrieve new Fulfillments that requires to generate ASN messages for.");
		Map<String, PurchaseOrderDAO> soInternalIdToPoDAO = prepareSoNumberToPoDaoMap(purchaseOrderDAOs);
		List<FulfillmentPojo> fulPojo = retrieveNewFullFillments(transactionDAOs, soInternalIdToPoDAO);
		List<FulfillmentPojo> readyForAsnGenerationFulfilmentPojos = exctractReadyForAsnGenerationFulfilmentPojos(fulPojo);
		//4. Generate xml ASN message  + store file in file system
		if (fulPojo.size() > 0){
			setFulfilmentPurhcaseOrderTypes(readyForAsnGenerationFulfilmentPojos, soInternalIdToPoDAO);
			setItemQtyToPackages(readyForAsnGenerationFulfilmentPojos);
			RetailersManager retailerManager = RetailersManager.getInstance();
			retailerManager.assignFulfillments(readyForAsnGenerationFulfilmentPojos);
			List<TransactionDAO> newTransactionDAOs = IntegrationObjectsConverter.convertFulfillmentPojosToTransactionDAOs(readyForAsnGenerationFulfilmentPojos);
			logger.info("Processing retrieved fulfillments.");					
			List<String> asnMessagePathList = retailerManager.askRetailersForAsnMessages();
			//5. Insert ASN message details into DB
			logger.info("Updating DB.");
			persistenceManager.persistOutgoingMessages(asnMessagePathList, OutgoingMessageType.ASN);
			//6. Insert new processed fulfillments in DB
			persistenceManager.persistTransactions(newTransactionDAOs);	
			//7. Update PO status after ASN processing
			persistenceManager.updateStatusAfterASNProcessing(purchaseOrderDAOs, fulPojo);
			//8. Insert new Packages
			List<PackageDAO> packageList = persistenceManager.persistPackages(purchaseOrderDAOs, readyForAsnGenerationFulfilmentPojos);
			//9. Persist fulfillmentlineitems			
			persistenceManager.persistOrderLineItemsAfterAsnProcessing(packageList, readyForAsnGenerationFulfilmentPojos);
			
			retailerManager.askRetailersToClearDocuments();
			ErrorsCollector.searchPossibleFulfillmentErrors(fulPojo);
		} else {
			logger.info("There no new fulfillments in Poppin Netsuite");
		}
	}
	private List<FulfillmentPojo> exctractReadyForAsnGenerationFulfilmentPojos(List<FulfillmentPojo> fulfillmentPojos){
		List<FulfillmentPojo> readyForAsnGenerationFulfilmentPojos = new ArrayList();
		for (FulfillmentPojo fulPojo : fulfillmentPojos){
			if(fulPojo.getExceptionDescription() == null)
				readyForAsnGenerationFulfilmentPojos.add(fulPojo);
		}
		return readyForAsnGenerationFulfilmentPojos;
	}
	private List<FulfillmentPojo> retrieveNewFullFillments(List<TransactionDAO> processedFulfillments, Map<String, PurchaseOrderDAO> soInternalIdToPoDAO) {
		
		Set<String> processedFulfillmentInternalIds = new HashSet<String>();
		for (TransactionDAO trDAO:processedFulfillments){
			processedFulfillmentInternalIds.add(trDAO.getTransactionInternalId());
		}
		
		List<FulfillmentPojo> fulfillmentPojoList = new ArrayList<FulfillmentPojo>();
		if(soInternalIdToPoDAO.keySet().size() > 0){
			try {				
				fulfillmentPojoList=netsuiteOperationsManager.getReadyForASNItemFulfillments(soInternalIdToPoDAO.keySet(), processedFulfillmentInternalIds);
				Set<String> ordersIdForPreloadFields = new HashSet<String>();
				List<PurchaseOrderPreloadedFieldsPojo> preloadedFieldsPurchaseOrderPojo = new ArrayList<PurchaseOrderPreloadedFieldsPojo>();
				if (fulfillmentPojoList.size()>0){
					for (FulfillmentPojo fulPojo:fulfillmentPojoList){
						ordersIdForPreloadFields.add(fulPojo.getSalesorderNsInternalId());
					}								
					preloadedFieldsPurchaseOrderPojo = netsuiteOperationsManager.preloadSOfields(ordersIdForPreloadFields);	
					setPODAOsFieldsToFulfillmentPojos(soInternalIdToPoDAO, fulfillmentPojoList);
					mergeFulfillmentPojosAndPurchaseOrderPojoPreloadedFields(fulfillmentPojoList, preloadedFieldsPurchaseOrderPojo);
				}			
			} catch (NetsuiteOperationException e) {
				SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),e.getRequestDetails());
				ErrorsCollector.addCommonErrorMessage(error);
				fulfillmentPojoList = new ArrayList<FulfillmentPojo>();
			}
		}
		else
			logger.info("There are no PO in DB that requires to generate ASN messages for.");
		return fulfillmentPojoList;
	}
	private void mergeFulfillmentPojosAndPurchaseOrderPojoPreloadedFields(List<FulfillmentPojo> fulfillmentsToMerge, List<PurchaseOrderPreloadedFieldsPojo> pOsPreloadedFields){
		for(PurchaseOrderPreloadedFieldsPojo poPojo : pOsPreloadedFields){
			for (FulfillmentPojo fulPojo : fulfillmentsToMerge){
				if (fulPojo.getSalesorderNsInternalId().equalsIgnoreCase(poPojo.getSalesOrderNsInternalId())){
					fulPojo.setPoNumber(poPojo.getPoNumber());
					fulPojo.setPoDate(poPojo.getPoDate());
					fulPojo.setDepartmentNsInternalId(poPojo.getDepartmentNsInternalId());
					fulPojo.setShipDate(poPojo.getShipDate());
					fulPojo.setSoTransactionId(poPojo.getSalesOrderTransactionId());
					fulPojo.setProcessingClosed(poPojo.isASNGenerated());
					for (FulfillmentItemPojo itemFulfillment:fulPojo.getOrderItems()){
						for (OrderItemPojo itemOrder:poPojo.getOrderItems()){							
							if (itemFulfillment.getItemNumber().equalsIgnoreCase(itemOrder.getItemNumber())){								
								itemFulfillment.setUPC(itemOrder.getUPC());
								if (itemOrder.getVendorlineNumber()==null){
									fulPojo.setExceptionDescription("Sales Order contains inconsistent items/illegal items modification. Please process it manually.");
								}
								itemFulfillment.setVendorlineNumber(itemOrder.getVendorlineNumber());
								itemFulfillment.setUnitPrice(itemOrder.getUnitPrice());
								itemFulfillment.setOrderQty(itemOrder.getOrderQty());
							}
						}
					}								
				}
			}					
		}		
	}
	private void setPODAOsFieldsToFulfillmentPojos(Map<String, PurchaseOrderDAO> mapPODaos, List<FulfillmentPojo> fulPojos){
		for (String internalId:mapPODaos.keySet()){
			for (FulfillmentPojo fulPojo:fulPojos){
				if (internalId.equalsIgnoreCase(fulPojo.getSalesorderNsInternalId())){
					fulPojo.setIncomingMessagePath(mapPODaos.get(internalId).getIncomingMessageDao().getMessagePath());
					fulPojo.setPurchaseOrderId(mapPODaos.get(internalId).getIdPurchaseOrder());
				}
			}
		}
	}
	private void setFulfilmentPurhcaseOrderTypes(List<FulfillmentPojo> fulfillmentPojos, Map<String, PurchaseOrderDAO> soInternalIdToPoDAO){
		for (FulfillmentPojo fulPojo : fulfillmentPojos){
			PurchaseOrderDAO poDao = soInternalIdToPoDAO.get(fulPojo.getSalesorderNsInternalId());
			fulPojo.setPurchaseOrderType(poDao.getPoType());
		}
	}
	private Map<String, PurchaseOrderDAO> prepareSoNumberToPoDaoMap(List<PurchaseOrderDAO> poDAOs){
		Map<String, PurchaseOrderDAO> soInternalIdToPoDAO = new HashMap<String, PurchaseOrderDAO>();		
		for(PurchaseOrderDAO poDAO : poDAOs){
			soInternalIdToPoDAO.put(poDAO.getSalesOrderNsInternald(), poDAO);			
		}
		return soInternalIdToPoDAO;
	}
	
	private void setItemQtyToPackages(List<FulfillmentPojo> fulfillmentPojoList){
		Map<String,PackagePojo> trackNumberToPackageMap = new HashMap<>();
		for (FulfillmentPojo fulPojo:fulfillmentPojoList){
			for (PackagePojo packPojo:fulPojo.getPackageItems()){
				trackNumberToPackageMap.put(packPojo.getTrackingNumber(), packPojo);
			}			
		}
		
		for (FulfillmentPojo fulPojo:fulfillmentPojoList){
			for (FulfillmentItemPojo fulItemPojo:fulPojo.getOrderItems()){
				if (trackNumberToPackageMap.containsKey(fulItemPojo.getTrackingNumber())){
					PackagePojo packagePojo=trackNumberToPackageMap.get(fulItemPojo.getTrackingNumber());
					int qty=packagePojo.getItemQty();
					packagePojo.setItemQty(qty+(int)fulItemPojo.getShipQty());
				}
			}
		}
	}
}
