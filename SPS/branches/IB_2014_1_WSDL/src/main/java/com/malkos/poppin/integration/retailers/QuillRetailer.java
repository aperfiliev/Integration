package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.entities.AddCustomerResultsPojo;
import com.malkos.poppin.entities.AddSalesOrderResultPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.xml.generators.IXmlMessagesGenerator;
import com.malkos.poppin.util.xml.generators.impl.QuillDropShipXmlMessageGenerator;
import com.malkos.poppin.validation.QuillDropShipPoValidator;

public class QuillRetailer extends RetailerAbstract {
	
	private static Logger logger = LoggerFactory.getLogger(QuillRetailer.class);
	
	public QuillRetailer(String retailerName, String companyEmail,String companyInternalId, String departmentInternalId,int retailerId) {
		super(retailerName, companyEmail, companyInternalId, departmentInternalId,retailerId);
		this.validators.put(PurchaseOrderType.DROPSHIP, new QuillDropShipPoValidator());
		this.messageGenerators.put(PurchaseOrderType.DROPSHIP, new QuillDropShipXmlMessageGenerator(this));
		this.retailerDocumentTypes.add(PurchaseOrderType.DROPSHIP);		
		this.shortName = "Quill";
		this.shippingMethodInternalId="3253";
	}

	@Override
	List<PoDocument> sendPurchaseOrders() {
		List<PoDocument> documentsToSend = new ArrayList<>();
		for(PoDocument document : getPoDocumentList()){
			if(document.getProcessingStatus() == PurchaseOrderStatus.PENDING_POPPIN_PROCESSING)
				documentsToSend.add(document);
		}
		if(documentsToSend.size() > 0){
			logger.info("QUILL retailer starting to send Purchase orders to Netsuite.");
			logger.info("Adding Customers to Poppin-Netsuite for each of Purchase Orders to add.");
			List<AddCustomerResultsPojo> addCustomerResultsPojoList = null;
			try {
				addCustomerResultsPojoList = netsuiteOperationsManager.addCustomers(documentsToSend, this);
			} catch (NetsuiteOperationException e) {
				String errorMessage = "Failed to add customers , reason: " + e.getMessage();
				logger.warn(errorMessage);				
				SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage, e.getRequestDetails());				
				ErrorsCollector.addCommonErrorMessage(error);
				documentsToSend.clear();
			}
			logger.info("Filtering Purchase Orders list. Pushing out Purchase Order POJOs for which customers was not added succesifully.");
			List<PoDocument> filteredAfterCustomerAddPurchaseOrderPojoList = null;
			if(null != addCustomerResultsPojoList){
				filteredAfterCustomerAddPurchaseOrderPojoList = filterPoDocumentsAfterCustomerAdd(addCustomerResultsPojoList, documentsToSend);
				logger.info("Adding Sales Orders to Poppin-Netsuite.");
				try {
					netsuiteOperationsManager.addSalesOrders(filteredAfterCustomerAddPurchaseOrderPojoList, this);
				} catch (NetsuiteOperationException e) {
					String errorMessage = "Failed to add sales orders , reason: " + e.getMessage();
					logger.warn(errorMessage);
					SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage, e.getRequestDetails());
					ErrorsCollector.addCommonErrorMessage(error);
					documentsToSend.clear();
				}
			}
			else{
				logger.info("Not adding sales orders. Something unexpected happend while adding customers.");
			}
			updateProcessedPoDocuments(documentsToSend);
		}
		else
			logger.info("There is nothing to send to Netsuite. Probably all the Purchase Orders were rejected for some reason.");
		return documentsToSend;
	}
	private List<PoDocument> filterPoDocumentsAfterCustomerAdd(List<AddCustomerResultsPojo> addCustomerResultsPojoList, List<PoDocument> documentsToSend){
		List<PoDocument> filteredAfterCustomerAddPoDocumentsList = new ArrayList<PoDocument>();
		int poPojoLoopCounter = 0;
		for(PoDocument poPojo : documentsToSend){
			AddCustomerResultsPojo addCustomerResultsPojo = addCustomerResultsPojoList.get(poPojoLoopCounter);
			AddSalesOrderResultPojo addSalesOrderResultPojo = new AddSalesOrderResultPojo();
			addSalesOrderResultPojo.setAddCustomerResultsPojo(addCustomerResultsPojo);
			poPojo.setAddSalesOrderResultPojo(addSalesOrderResultPojo);
			if(addCustomerResultsPojo.isReadyForMapping()){
				filteredAfterCustomerAddPoDocumentsList.add(poPojo);
			}
			else{
				addSalesOrderResultPojo.setAddedSuccessifully(false);
				addSalesOrderResultPojo.setProblemDescription(addCustomerResultsPojo.getProblemDescription());
			}
			poPojoLoopCounter++;
		}
		return filteredAfterCustomerAddPoDocumentsList;
	}

	
}
