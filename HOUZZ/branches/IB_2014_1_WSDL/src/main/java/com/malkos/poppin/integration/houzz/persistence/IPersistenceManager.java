package com.malkos.poppin.integration.houzz.persistence;

import java.util.List;

import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.integration.houzz.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;

public interface IPersistenceManager {	
	List<LineItemIntegrationIdentifierDAO> getHouzzAssortment();
	void updateHouzzAssortment(List<LineItemIntegrationIdentifierDAO> lineItemDAOList);	
	List<OutgoingMessageDAO> getOutgoingMessagesToSend();
	void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessagesList);
	void persistOutgoingMessage(String messagePath, RetailerEnum retailer);
	void persistNotificationEmail(String text,String subject,String recepients, List<String> attachments, RetailerEnum retailer);
	public List<NotificationEmailDAO> getPendingNotificationEmails();
	public void updateNotificationEmails(List<NotificationEmailDAO> neToUpdate);	
}
