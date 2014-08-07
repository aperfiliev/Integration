package com.malkos.poppin.integration.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InventoryItemPojo;
import com.malkos.poppin.entities.InventoryKitPojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.enums.OutgoingMessageType;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.integration.services.IInventoryUpdateFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.IntegrationObjectsConverter;

public class InventoryUpdateFlowService implements IInventoryUpdateFlowService {

	@Autowired
	IPersistenceManager persistenceManager;
	
	@Autowired
	INetsuiteOperationsManager netsuiteOperationsManager;
	
	private static Logger logger = LoggerFactory.getLogger(InventoryUpdateFlowService.class);
	
	@Override
	public void updateInventory() {
		logger.info("Updating Inventory.");
		//1. Retrieve inventoryMapping
		List<LineItemIntegrationIdentifierDAO> lineItemDAOs = persistenceManager.getPoppinAssortment();
		//2. Retrieve updated inventory quantities		
		Map<String,List> inventoryTypeClassToInventoryListMap = null;
		try {
			inventoryTypeClassToInventoryListMap = netsuiteOperationsManager.loadInventory(lineItemDAOs);
			mergeLineItemDAOsFieldsToInventoryList(lineItemDAOs,inventoryTypeClassToInventoryListMap);			
		//3. Generating Inventory Update Messages And Sendig to SPS
			//TODO: add implementation here
		} catch (NetsuiteOperationException e) {
			SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),e.getRequestDetails());
			ErrorsCollector.addCommonErrorMessage(error);
		}		
	}
	
	private void mergeLineItemDAOsFieldsToInventoryList(List<LineItemIntegrationIdentifierDAO> lineItemDAOs, Map<String,List> inventoryTypeClassToInventoryListMap){
		List<InventoryItemPojo> invPojoList = inventoryTypeClassToInventoryListMap.get(InventoryItemPojo.class.getName());
		List<InventoryKitPojo> kitPojoList = inventoryTypeClassToInventoryListMap.get(InventoryKitPojo.class.getName());
		
		Map<String, LineItemIntegrationIdentifierDAO> internalIdToLineItemDAOMap = new HashMap<>();
		for (LineItemIntegrationIdentifierDAO lineItemDAO:lineItemDAOs){
			internalIdToLineItemDAOMap.put(lineItemDAO.getItemInternalId(), lineItemDAO);
		}
		
		for (InventoryItemPojo invPojo:invPojoList){
			LineItemIntegrationIdentifierDAO lineItemDAO = internalIdToLineItemDAOMap.get(invPojo.getNsInternalId());
			invPojo.setPopSKU(lineItemDAO.getModelNum());
		}	
		
		for (InventoryKitPojo invKitPojo:kitPojoList){
			LineItemIntegrationIdentifierDAO lineItemDAO = internalIdToLineItemDAOMap.get(invKitPojo.getNsInternalId());
			invKitPojo.setPopSKU(lineItemDAO.getModelNum());
		}
	}

}
