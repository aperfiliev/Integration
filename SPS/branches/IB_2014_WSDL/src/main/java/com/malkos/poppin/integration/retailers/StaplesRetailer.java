package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.malkos.poppin.util.xml.generators.impl.QuillDropShipXmlMessageGenerator;
import com.malkos.poppin.util.xml.generators.impl.StaplesBulkImportMessageGenerator;
import com.malkos.poppin.util.xml.generators.impl.StaplesCrossDockMessageGenerator;
import com.malkos.poppin.util.xml.generators.impl.StaplesDropShipMessageXmlGenerator;
import com.malkos.poppin.validation.IValidator;
import com.malkos.poppin.validation.QuillDropShipPoValidator;
import com.malkos.poppin.validation.StaplesBulkImportValidator;
import com.malkos.poppin.validation.StaplesDropShipValidator;
import com.malkos.poppin.validation.StaplesPromoValidator;
import com.netsuite.webservices.transactions.sales_2014_2.types.SalesOrderOrderStatus;

public class StaplesRetailer extends RetailerAbstract {
	
	private static Logger logger = LoggerFactory.getLogger(StaplesRetailer.class);
	
	public StaplesRetailer(String retailerName, String companyEmail,String companyInternalId, String departmentInternalId, int retailerId) {
		super(retailerName, companyEmail, companyInternalId, departmentInternalId, retailerId);
		
		//this.validators.put(PurchaseOrderType.DROPSHIP, new StaplesDropShipValidator());
		//this.validators.put(PurchaseOrderType.CROSSDOCK, new StaplesCrossDockValidator());
		this.validators.put(PurchaseOrderType.BULKIMPORT, new StaplesBulkImportValidator());
		this.validators.put(PurchaseOrderType.PROMO, new StaplesPromoValidator());
		
		//this.messageGenerators.put(PurchaseOrderType.DROPSHIP, new StaplesDropShipMessageXmlGenerator(this));
		//this.messageGenerators.put(PurchaseOrderType.CROSSDOCK, new StaplesCrossDockMessageGenerator(this));
		this.messageGenerators.put(PurchaseOrderType.BULKIMPORT, new StaplesBulkImportMessageGenerator(this));
		this.messageGenerators.put(PurchaseOrderType.PROMO, new StaplesBulkImportMessageGenerator(this));
		
		this.retailerDocumentTypes.add(PurchaseOrderType.BULKIMPORT);
		this.retailerDocumentTypes.add(PurchaseOrderType.PROMO);
		this.shortName = "Staples";
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
			logger.info("STAPLES retailer starting to send Purchase orders to Netsuite.");
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
}
