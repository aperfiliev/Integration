package com.malkos.poppin.transport;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.entities.AddCustomerResultsPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.PurchaseOrderPreloadedFieldsPojo;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.enums.UnprocessibleOrdersType;
import com.malkos.poppin.integration.retailers.IndigoRetailer;
import com.malkos.poppin.integration.retailers.RetailerAbstract;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;

public interface INetsuiteOperationsManager {
	Map<String, String> loadProcessedPurchaseOrderNumbers(long lastProcessedPoNumber) throws NetsuiteOperationException;
	Map<String, String> loadInventoryMapping() throws NetsuiteOperationException;
	List<AddCustomerResultsPojo> addCustomers(List<PoDocument> poDocumentList, RetailerAbstract retailer) throws NetsuiteOperationException;
	void addSalesOrders(List<PoDocument> filteredAfterCustomerAddPurchaseOrderPojoList, RetailerAbstract retailer) throws NetsuiteOperationException;
	List<FulfillmentPojo> getReadyForASNItemFulfillments(Set<String> orderInternalIds, Set<String> proccessedFulfillmentsId)	throws NetsuiteOperationException;
	List<PurchaseOrderPreloadedFieldsPojo> preloadSOfields(Set<String> orderIdsToPreload) throws NetsuiteOperationException;
	List<InvoicePojo> getReadyForMessagingInvoices(Set<String> orderInternalId, Set<String> processedInvoices) throws NetsuiteOperationException;
	Map<String,List> loadInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList)throws NetsuiteOperationException;
	Map<String, String> getInventoryInternalIdsByUpcCodes(Set<String> upcCodes, RetailerAbstract retailer) throws NetsuiteOperationException;
	public void updateRetailersInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList)throws NetsuiteOperationException;
	Map<String, UnprocessibleOrdersType> retrieveCancelledClosedOrders(List<PurchaseOrderDAO> purchaseOrderDAOs);
	void sendBarnesAndNobleInvoiceCustomRecords(List<PoDocument> documents, Map<String, ShippingAddressPojo> addressMappings) throws NetsuiteOperationException;
}
