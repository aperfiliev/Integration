package com.malkos.poppin.integration.houzz.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.entities.IntegrationError;
import com.malkos.poppin.integration.houzz.entities.InventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistenceManager;
import com.malkos.poppin.integration.houzz.services.IInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.transport.INetsuiteOperationsManager;
import com.malkos.poppin.integration.houzz.transport.NetsuiteOperationException;
import com.malkos.poppin.integration.houzz.transport.NetsuiteOperationsManager;
import com.malkos.poppin.integration.houzz.util.ErrorMessageWrapper;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.malkos.poppin.integration.houzz.util.csv.generators.CsvGenerationException;
import com.malkos.poppin.integration.houzz.util.csv.generators.ICsvMessageGenerator;
import com.malkos.poppin.integration.houzz.util.csv.generators.impl.OlapicInventoryUpdateMessageGenerator;

public class OlapicInventoryUpdateFlowService implements IInventoryUpdateFlowService {
	
	private static Logger logger = LoggerFactory.getLogger(OlapicInventoryUpdateFlowService.class);
	private RetailerAbstract olapicRretailer = RetailerManager.get_retailer(RetailerEnum.OLAPIC);
	
	@Override
	public void updateInventory() {		
		logger.debug("OLAPIC Inventory Update Flow Started ...");
		olapicRretailer.getLogger().addMessage("OLAPIC Inventory Update Flow Started ...");
		INetsuiteOperationsManager operationsManager = new NetsuiteOperationsManager();
		ICsvMessageGenerator olapicMessageGenerator = new OlapicInventoryUpdateMessageGenerator();
		IPersistenceManager persistenceManager = new PersistenceManager();
		List<InventoryItemPojo> lstOlapicInventories;
		try {
			logger.debug("OLAPIC Inventory Update retrieving data from NetSuite ...");
			olapicRretailer.getLogger().addMessage("OLAPIC Inventory Update retrieving data from NetSuite ...");
			lstOlapicInventories = operationsManager.loadOlapicInventory();	
		} catch (NetsuiteOperationException e) {
			e.getRequestDetails().setRetailer(RetailerEnum.OLAPIC.toString());
			IntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(), e.getRequestDetails());
			ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);
			logger.debug("OLAPIC Inventory Update couldn't retrieve data from NetSuite. Reason: "+e.getMessage());
			olapicRretailer.getLogger().addError("OLAPIC Inventory Update couldn't retrieve data from NetSuite. Reason: "+e.getMessage());
			return;
		} 	
		String path;
		logger.debug("OLAPIC Inventory Update successfully retrieved data from NetSuite.");
		logger.debug("OLAPIC Inventory Update begin generating csv file.");
		olapicRretailer.getLogger().addMessage("OLAPIC Inventory Update successfully retrieved data from NetSuite.");
		olapicRretailer.getLogger().addMessage("OLAPIC Inventory Update begin generating csv file.");
		try{			
			path =  olapicMessageGenerator.generateMessage(lstOlapicInventories);
		} catch (CsvGenerationException e) {
			NSRrequestDetails details = new NSRrequestDetails();
			details.setRetailer(RetailerEnum.OLAPIC.toString());
			IntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),details);
			ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);			
			logger.debug("OLAPIC Inventory Update couldn't generate csv file/ Reason : "+ e.getMessage());
			olapicRretailer.getLogger().addError("OLAPIC Inventory Update couldn't generate csv file/ Reason : "+ e.getMessage());
			return;
		}
		persistenceManager.persistOutgoingMessage(path,RetailerEnum.OLAPIC);
		logger.debug("OLAPIC Inventory Update successfully generate csv file");			
	}
}
