package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.documents.PoDocument;
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
import com.malkos.poppin.validation.BarnesAndNobleCrossDockValidator;
import com.netsuite.webservices.transactions.sales_2013_1.types.SalesOrderOrderStatus;

public class BarnesAndNobleRetailer extends RetailerAbstract {
	
	private static Logger logger = LoggerFactory.getLogger(StaplesRetailer.class);
	
	public BarnesAndNobleRetailer(String retailerName, String companyEmail,String companyInternalId, String departmentInternalId, int retailerId) {
		super(retailerName, companyEmail, companyInternalId, departmentInternalId, retailerId);
		
		this.validators.put(PurchaseOrderType.CROSSDOCK, new BarnesAndNobleCrossDockValidator());
		
		this.retailerDocumentTypes.add(PurchaseOrderType.CROSSDOCK);
		this.shortName = "BarnesAndNoble";
		this.salesOrderStatus = SalesOrderOrderStatus._pendingApproval;
		this.termInternalId = "3";
		this.shippingMethodInternalId="3253";
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
				addCustomerPojo.setBillingAddressInternalId(addressInternalId);
				addSoPojo.setAddCustomerResultsPojo(addCustomerPojo);				
				document.setAddSalesOrderResultPojo(addSoPojo);
				documentsToSend.add(document);
			}
		}
		if(documentsToSend.size() > 0){
			logger.info("BARNES & NOBLE retailer starting to send Purchase orders to Netsuite.");
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
			List<PoDocument> sentDocuments = updateProcessedPoDocuments(documentsToSend);
			
			if(sentDocuments.size() > 0){
				logger.info("BARNES & NOBLE retailer starting to send B&N custom records for future billing to Netsuite.");
				try {
					netsuiteOperationsManager.sendBarnesAndNobleInvoiceCustomRecords(sentDocuments, addressMappings);
				} catch (NetsuiteOperationException e) {
					String errorMessage = "Failed to send custom records , reason: " + e.getMessage();
					logger.warn(errorMessage);
					SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(errorMessage, e.getRequestDetails());
					ErrorsCollector.addCommonErrorMessage(error);
				}
			}
		}
		else
			logger.info("There is nothing to send to Netsuite. Probably all the Purchase Orders were rejected for some reason.");
		return documentsToSend;
	}
}
