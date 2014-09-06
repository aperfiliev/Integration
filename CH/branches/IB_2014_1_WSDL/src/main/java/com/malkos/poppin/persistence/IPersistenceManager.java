package com.malkos.poppin.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.entities.MessageBatchTransfer;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;

public interface IPersistenceManager {	
	void updatePurchaseOrders(List<PurchaseOrderPojo> poPojoList);
	List<String> getPoIdsWithPendingConfirmationStatus();
	List<String> getPoIdsWithConfirmationAcceptedStatus();
	//String getVendorSkuByModelNum(String modelNum);
	void updatePoWithPendingConfirmationStatus(List<String> poNumbers);
	List<PurchaseOrderDAO> getPoppinPendingOrders();
	void updatePurchaseOrder(PurchaseOrderPojo poPojo);
	void updatePoWithConfirmationAcceptedStatus(List<String> poNumbersToUpdate);
	//Map<String, String> getVendorSkuToModelNumMap();
	void updatePurchaseOrder(List<PurchaseOrderPojo> poPojoListU);
	List<VendorSkuToModelNumMapDAO> getInventoryDAO();
	void updateInventoryDAO(List<VendorSkuToModelNumMapDAO> inventoryDAOs);
	List<OutgoingMessageDAO> getOutgoingMessagesToSend();
	void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessages);
	void persistIncomingMessages(Set<String> incomingMessagesPaths);
	List<IncomingMessageDAO> retrieveNewIncomingMessages();
	void updateIncomingMessagesStatus(List<IncomingMessageDAO> failedToProcessMessages);
	void persistMessageBatch(List<MessageBatchTransfer> successfullyProcessedBatches);
	List<PurchaseOrderDAO> getPreviouslyNSprocessingFailedOrders(List<IncomingMessageDAO> sourceMessageDAOList);
	void persistOutgoingMessages(List<OutgoingMessageDAO> faMessagesDAOList);
	void updatePoDAOs(List<PurchaseOrderDAO> poDAOsToUpdate);
	List<PurchaseOrderDAO> getPoppinPendingMessagesGenerationPurchaseOrders();
	void persistNotificationEmail(String text, List<String> attachments);
	List<NotificationEmailDAO> getPendingNotificationEmails();
	void updateNotificationEmails(List<NotificationEmailDAO> neToUpdate);
}
