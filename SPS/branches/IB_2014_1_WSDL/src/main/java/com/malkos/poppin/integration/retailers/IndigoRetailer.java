package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentItem;
import com.malkos.poppin.entities.AddCustomerResultsPojo;
import com.malkos.poppin.entities.AddSalesOrderResultPojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.IntegrationDetailsProvider;
import com.malkos.poppin.transport.NetsuiteOperationException;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.xml.generators.impl.IndigoBulkImportMessageGenerator;
import com.malkos.poppin.validation.IndigoBulkImportValidator;
import com.netsuite.webservices.transactions.sales_2014_2.types.SalesOrderOrderStatus;

public class IndigoRetailer extends RetailerAbstract {

	private static Logger logger = LoggerFactory.getLogger(IndigoRetailer.class);
	
	public IndigoRetailer(String retailerName, String companyEmail, String companyInternalId, String departmentInternalId, int retailerId) {
		super(retailerName, companyEmail, companyInternalId, departmentInternalId,retailerId);	
		
		this.validators.put(PurchaseOrderType.BULKIMPORT, new IndigoBulkImportValidator());
		this.messageGenerators.put(PurchaseOrderType.BULKIMPORT, new IndigoBulkImportMessageGenerator(this));		
		this.retailerDocumentTypes.add(PurchaseOrderType.BULKIMPORT);
		this.shortName = "Indigo";
		this.termInternalId = "12";
		this.salesOrderStatus = SalesOrderOrderStatus._pendingApproval;
	}

	@Override
	List<PoDocument> sendPurchaseOrders() {
		List<PoDocument> documentsToSend = new ArrayList<>();

		IntegrationDetailsProvider idProvider = IntegrationDetailsProvider.getInstance();
		//Map<String, String> customerShippingAddresess = idProvider.getRetailersShippingAddresses();
		Map<String, ShippingAddressPojo> addressMappings = idProvider.getRetailerShippingAddressesMappings(getRetailerId());
		
		for(PoDocument document : getPoDocumentList()){
			if(document.getProcessingStatus() == PurchaseOrderStatus.PENDING_POPPIN_PROCESSING) {
				AddSalesOrderResultPojo addSoPojo = new AddSalesOrderResultPojo();				
				addSoPojo.setAddedSuccessifully(false);
				AddCustomerResultsPojo addCustomerPojo = new AddCustomerResultsPojo();				
				addCustomerPojo.setInternalId(this.getCompanyInternalId());
				String addressInternalId = addressMappings.get(document.getShipLocationNumber()).getInternalId();
				addCustomerPojo.setShippingAddressInternalId(addressInternalId);			
				addSoPojo.setAddCustomerResultsPojo(addCustomerPojo);				
				document.setAddSalesOrderResultPojo(addSoPojo);
				documentsToSend.add(document);
			}
		}
		if(documentsToSend.size() > 0){
			logger.info("INDIGO retailer starting to send Purchase orders to Netsuite.");
			logger.info("Adding Sales Orders to Poppin-Netsuite.");
			try {
				netsuiteOperationsManager.addSalesOrders(documentsToSend, this);
			} catch (NetsuiteOperationException e) {
				String errorMessage = "Failed to add sales orders , reason: " + e.getMessage();
				logger.warn(errorMessage);
				SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage, e.getRequestDetails());
				ErrorsCollector.addCommonErrorMessage(error);
				documentsToSend.clear();
			}
			updateProcessedPoDocuments(documentsToSend);
			}
		else
			logger.info("There is nothing to send to Netsuite. Probably all the Purchase Orders were rejected for some reason.");
		return documentsToSend;
	}
	
	@Override
	protected void fillInDocumentData(PoDocument document, Map<String, String> poppinAssortment){
		document.setRetailerId(this.getRetailerId());
		for(PoDocumentItem pdItem : document.getPoDocumentItemList()){
			if(poppinAssortment.containsKey(pdItem.getUPC()))
				pdItem.setItemInternalId(poppinAssortment.get(pdItem.getUPC()));
		}
	}
	
	/*@Override void validate(){
		super.validate();
		Map<String,String> skuToInternalIdMap = null;
		List<PoDocument> documentsToValidate = getPoDocumentList();
		Set<String> upcSet = new HashSet();
		for (PoDocument doc : documentsToValidate){
			//if (doc.getExceptionDescription() == null){
				for (PoDocumentItem docItem :  doc.getPoDocumentItemList()){
					upcSet.add(docItem.getUPC());
				}
			//}			
		}
		try {
			skuToInternalIdMap=netsuiteOperationsManager.getInventoryInternalIdsByUpcCodes(upcSet,this);
		} catch (NetsuiteOperationException e) {	
			clearDocuments();
			String errorMessage = "Skip Indigo add Sales Order operation.Failed reason: netsuite exception on order validation. Message: " + e.getMessage();
			logger.warn("Skip Indigo add Sales Order operation. ",errorMessage);
			ErrorsCollector.addCommonErrorMessage(errorMessage);
		}		
		for (PoDocument doc : documentsToValidate){
			//if (doc.getExceptionDescription() == null){
				String exceptionDescription = new String();
				for (PoDocumentItem docItem :  doc.getPoDocumentItemList()){
					String internalId=skuToInternalIdMap.get(docItem.getUPC());
					if (internalId!=null){
						docItem.setItemInternalId(internalId);									
					} else {
						exceptionDescription+="Couldn't find apropriate to UPC='"+docItem.getUPC()+"' inventory item in Poppin Netsuite database.\r\n";
					}
				}
				String generalExceptionDescription = doc.getExceptionDescription();
				if (!exceptionDescription.isEmpty()){
					if (generalExceptionDescription == null){
						doc.setExceptionDescription(exceptionDescription);
					}else{
						doc.setExceptionDescription(generalExceptionDescription+exceptionDescription);
					}
				}
			//}			
		}
	}*/
}
