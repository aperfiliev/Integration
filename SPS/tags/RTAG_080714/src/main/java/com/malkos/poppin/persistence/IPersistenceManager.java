package com.malkos.poppin.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.enums.OutgoingMessageType;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.persistence.dao.FulfillmentLineItemDAO;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.RetailerDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;

public interface IPersistenceManager {
	List<PurchaseOrderDAO> getPoppinPendingProcessingPurchaseOrders();
	void persistPurchaseOrders(List<PurchaseOrderDAO> poDaoList);
	long getLastProcessedPoSalesOrderInternaNumberlId();
	void persistProcessedPurchaseOrders(Map<String, String> processedPurchaseOrders);
	void updateLineItemIntegrationIdentifierDAOlist(Map<String, String> inventoryVendorSKUtoIntenrlIdMap);
	void updateLineItemIntegrationIdentifierDAOlist(List<LineItemIntegrationIdentifierDAO> lineItemDAOList);
	Map<String, String> getPopMapperToItemInternalIdMap();
	void updatePoDaosAfterPoppinProcessing(List<PurchaseOrderDAO> purchaseOrderDAOlist);
	List<PurchaseOrderDAO> getPoppinPendingASNGenerationPurchaseOrders();
	void persistOutgoingMessages(List<String> messagePathList, OutgoingMessageType messagesType);
	List<OutgoingMessageDAO> getOutgoingMessagesToSend();
	void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessages);
	public List<PurchaseOrderDAO> getPoppinPendingInvoiceGenerationPurchaseOrders();	
	void persistIncomingMessages(Set<String> incomingMessagesPaths);
	public List<RetailerDAO> loadRetailers();
	public List<IncomingMessageDAO> loadPendingProcessingDocumentsFilePathList();
	Map<String, String> getShippingAddressLabelToInternalIdMap();	
	List<TransactionDAO> getProccessedTransactions(List<PurchaseOrderDAO> orderDAOList,TransactionType trType);
	List<PackageDAO> persistPackages(List<PurchaseOrderDAO> poList,List<FulfillmentPojo> processedFulfillments);
	void persistTransactions(List<TransactionDAO> newTransactions);
	void updateStatusAfterASNProcessing(List<PurchaseOrderDAO> poList, List<FulfillmentPojo> processedFulfillments);
	void persistOrderLineItemsAfterAsnProcessing(List<PackageDAO> packList, List<FulfillmentPojo> processedFulfillments);
	List<FulfillmentLineItemDAO> getShipedItems(List<PurchaseOrderDAO> poDAOList);
	void updateStatusAfterInvoiceProcessing(List<PurchaseOrderDAO> poList, List<InvoicePojo> processedInvoices);
	List<LineItemIntegrationIdentifierDAO> getPoppinAssortment();
	public List<PurchaseOrderDAO> getPoppinPendingInvoiceGenerationPurchaseOrdersForDataMigration();
	void updateIncomingMessagesStatuses(List<PurchaseOrderDAO> processedDaos);
	void updatePoDAOs(List<PurchaseOrderDAO> poDAOsToUpdate);
	List<PurchaseOrderDAO> getPoppinPendingMessagesGenerationPurchaseOrders();
	Map<Integer, Map<String, ShippingAddressPojo>> getShippingAddressesMappings();
	void persistNotificationEmail(String text, List<String> attachments);
	public List<NotificationEmailDAO> getPendingNotificationEmails();
	public void updateNotificationEmails(List<NotificationEmailDAO> neToUpdate);
}
