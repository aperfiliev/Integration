package com.malkos.poppin.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.entities.EmailMessageStatus;
import com.malkos.poppin.entities.IncomingMessageStatus;
import com.malkos.poppin.entities.MessageBatchTransfer;
import com.malkos.poppin.entities.OutgoingMessageStatus;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.entities.PurchaseOrderStatus;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.MessageBatchDAO;
import com.malkos.poppin.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;

public class PersistenceManager implements IPersistenceManager{
	
	Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

	@Override
	public void persistMessageBatch(List<MessageBatchTransfer> mbTransferList) {		
	/*	SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();		
		for (MessageBatchTransfer mbTransfer:mbTransferList){
			MessageBatchDAO mbDao = new MessageBatchDAO();
			mbDao.setMessageBatchId(mbTransfer.getMessageBatchId());
			mbDao.setDecryptedFilePath(mbTransfer.getDecryptedFilePath());
			mbDao.setEncryptedFilePath(mbTransfer.getEncryptedFilePath());
			mbDao.setDateRecieved(new Date());
			IncomingMessageDAO messageDAO = new IncomingMessageDAO();
			messageDAO.setIdIncomingMessages(mbTransfer.getIdIncomingMessage());
			mbDao.setIncomingMessage(messageDAO);
			session.beginTransaction();
			session.save(mbDao);
			session.getTransaction().commit();
			session.flush();
			mbTransfer.setMessageBatchId((int) mbDao.getMessageBatchId());
			session.clear();
			List<PurchaseOrderDAO> poDaoList = new ArrayList<PurchaseOrderDAO>();
			for (PurchaseOrderPojo poPojo:mbTransfer.getPurchaseOrders()){
				PurchaseOrderDAO poDao = new PurchaseOrderDAO();				
				poDao.setPurchaseOrderNumber(poPojo.getPoNumber());
				poDao.setMbDao(mbDao);
				poDao.setDetails(poPojo.getExceptionDesc());
				poDao.setStatus(poPojo.getStatus());
				poDaoList.add(poDao);
				session.beginTransaction();
				session.save(poDao);
				session.getTransaction().commit();
				session.flush();
				poPojo.setId(poDao.getPurchaseOrderId());
				session.clear();				
			}
		}
		session.close();	*/		
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		for (MessageBatchTransfer mbTransfer:mbTransferList){
			MessageBatchDAO mbDao = (MessageBatchDAO) session.get(MessageBatchDAO.class, (long)mbTransfer.getMessageBatchId());
			boolean messageBatchSaved = false;
			
			if(null == mbDao)
			{
				mbDao = new MessageBatchDAO();
				mbDao.setMessageBatchId(mbTransfer.getMessageBatchId());
				mbDao.setDecryptedFilePath(mbTransfer.getDecryptedFilePath());
				mbDao.setEncryptedFilePath(mbTransfer.getEncryptedFilePath());
				mbDao.setDateRecieved(new Date());
				IncomingMessageDAO imDAO = new IncomingMessageDAO();
				imDAO.setIdIncomingMessages(mbTransfer.getIdIncomingMessage());
				mbDao.setIncomingMessage(imDAO);
				List<PurchaseOrderDAO> poDaoList = new ArrayList<PurchaseOrderDAO>();
				Map<String, PurchaseOrderPojo> poNumberToPoPojoMap = new HashMap<String, PurchaseOrderPojo>();
				for(PurchaseOrderPojo poPojo : mbTransfer.getPurchaseOrders()){
					PurchaseOrderDAO poDao = new PurchaseOrderDAO();
					poNumberToPoPojoMap.put(poPojo.getPoNumber(), poPojo);
					poDao.setPurchaseOrderNumber(poPojo.getPoNumber());
					poDao.setMbDao(mbDao);
					poDao.setDetails(poPojo.getExceptionDesc());
					poDao.setStatus(poPojo.getStatus());
					poDaoList.add(poDao);
				}
				
				try {
					session.beginTransaction();
					session.save(mbDao);
					session.getTransaction().commit();
					messageBatchSaved = true;
				} catch (Exception e) {
					logger.warn("Failed to save message batch # "+ mbTransfer.getMessageBatchId() +" . Reason :" + e.getMessage());
				}
				if(messageBatchSaved){
					for(PurchaseOrderDAO poDao : poDaoList){
						PurchaseOrderDAO poDaoGet = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where purchaseordernumber='" + poDao.getPurchaseOrderNumber() + "'").uniqueResult();
						if (poDaoGet == null){
							try {
								session.beginTransaction();
								session.save(poDao);
								session.getTransaction().commit();
								session.flush();
								session.clear();
							} catch (Exception e) {
								PurchaseOrderPojo poPojo = poNumberToPoPojoMap.get(poDao.getPurchaseOrderNumber());
								poPojo.addException("Could not insert purchase order into db. Po # : " + poPojo.getPoNumber() + ". Reason: " + e.getMessage());
							}
						}
						else{
							PurchaseOrderPojo poPojo = poNumberToPoPojoMap.get(poDao.getPurchaseOrderNumber());
							poPojo.addException("Purchase Order with PO # " + poPojo.getPoNumber() +" was processed by application previously. Skipping this order.");
						}
					}
				}
			}
			else{
				logger.warn("Message batch with # "+ mbTransfer.getMessageBatchId() +"  was already processed before. Skipping orders from this message batch.");
				for(PurchaseOrderPojo poPojo : mbTransfer.getPurchaseOrders()){
					//poPojo.setStatus(PurchaseOrderStatus.UNPROCESSIBLE_REJECTED);
					poPojo.addException("Purchase Order with PO # " + poPojo.getPoNumber() +" was processed by application previously. Skipping this order.");
					poPojo.setIsDuplicate(true);
				}
			}			
		}
		session.close();
	}
	
	@Override
	public void updatePurchaseOrders(List<PurchaseOrderPojo> poPojoList) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();		
		List<PurchaseOrderDAO> poDaoList = new ArrayList<PurchaseOrderDAO>();		
		for(PurchaseOrderPojo poPojo : poPojoList){
			if (!poPojo.getIsDuplicate()){
				PurchaseOrderDAO poDAO = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where purchaseOrderNumber='"+poPojo.getPoNumber()+"'").uniqueResult();
				poDAO.setStatus(poPojo.getStatus());	
				poDaoList.add(poDAO);
			}					
		}
		session.beginTransaction();
		for(PurchaseOrderDAO poDao : poDaoList)
			session.update(poDao);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public void updatePurchaseOrder(PurchaseOrderPojo poPojo) {
		if(poPojo.getIsDuplicate() == false){
			SessionFactory factory = HibernateSessionProvider.getSessionFactory();
			Session session = factory.openSession();
			
			//if(poPojo.getStatus() != PurchaseOrderStatus.UNPROCESSIBLE_REJECTED){
				//PurchaseOrderDAO poDao = (PurchaseOrderDAO)session.get(PurchaseOrderDAO.class, Long.parseLong(poPojo.getPoNumber()));
				PurchaseOrderDAO poDao = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where purchaseordernumber='" + poPojo.getPoNumber() + "'").uniqueResult();
				if(null != poDao){
					poDao.setDetails(poPojo.getExceptionDesc());
					poDao.setStatus(poPojo.getStatus());
					
					session.beginTransaction();
					session.update(poDao);
					session.getTransaction().commit();
				}
			//}
			session.close();
		}
	}

	@Override
	public List<String> getPoIdsWithPendingConfirmationStatus() {
		List<String> poIds = new ArrayList<String>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		List<PurchaseOrderDAO> poList = session.createQuery("from PurchaseOrderDAO where status='" + PurchaseOrderStatus.POPPIN_PENDING_CONFIRMATION+"'").list();
		for(PurchaseOrderDAO poDao : poList)
			//poIds.add(Long.toString(poDao.getPurchaseOrderId()));
			poIds.add(poDao.getPurchaseOrderNumber());
		session.close();
		return poIds;
	}
	@Override
	public void updatePoWithPendingConfirmationStatus(List<String> poNumbers){
		
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		
		for(String poNumber : poNumbers){
			PurchaseOrderDAO dao = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where PurchaseOrderNumber='" + poNumber + "'").uniqueResult();
			if(dao != null){
				dao.setStatus(PurchaseOrderStatus.STAPLES_CONFIRMATION_ACCEPTED);
				session.update(dao);
			}
		}
		session.getTransaction().commit();
		session.close();
	}

	/*@Override
	public String getVendorSkuByModelNum(String modelNum) {
		String vendorSKU = null;
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		VendorSkuToModelNumMapDAO vendorSkuToModelNumMapDAO = (VendorSkuToModelNumMapDAO) session.createQuery("from VendorSkuToModelNumMapDAO where modelNum='" + modelNum + "'").uniqueResult();
		if(null != vendorSkuToModelNumMapDAO)
			vendorSKU = Long.toString(vendorSkuToModelNumMapDAO.getVendorSku());
		session.close();
		return vendorSKU;
	}
	@Override
	public Map<String,String> getVendorSkuToModelNumMap() {
		Map<String,String> VendorSkuToModelNumMap = new HashMap<String,String>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		List<VendorSkuToModelNumMapDAO> vendorSkuToModelNumMapDAO = session.createQuery("from VendorSkuToModelNumMapDAO").list();
		if(null != vendorSkuToModelNumMapDAO){
			for(VendorSkuToModelNumMapDAO dao : vendorSkuToModelNumMapDAO)
				VendorSkuToModelNumMap.put(Long.toString(dao.getVendorSku()), dao.getModelNum());
		}
		session.close();
		return VendorSkuToModelNumMap;
	}*/

	@Override
	public List<PurchaseOrderDAO> getPoppinPendingOrders() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<PurchaseOrderDAO> poppinPendingOrdersDaos = session.createQuery("from PurchaseOrderDAO where status='" + PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL+"'").list();
		session.close();
		return poppinPendingOrdersDaos;
	}

	@Override
	public List<String> getPoIdsWithConfirmationAcceptedStatus() {
		List<String> poIds = new ArrayList<String>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		List<PurchaseOrderDAO> poList = session.createQuery("from PurchaseOrderDAO where status='" + PurchaseOrderStatus.STAPLES_CONFIRMATION_ACCEPTED+"'").list();
		for(PurchaseOrderDAO poDao : poList)
			//poIds.add(Long.toString(poDao.getPurchaseOrderId()));
			poIds.add(poDao.getPurchaseOrderNumber());
		session.close();
		return poIds;
	}

	public List<VendorSkuToModelNumMapDAO> getInventoryDAO(){
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<VendorSkuToModelNumMapDAO> inventoryDAOs = session.createQuery("from VendorSkuToModelNumMapDAO").list();
		session.close();
		return inventoryDAOs;
	}
	
	public void updateInventoryDAO(List<VendorSkuToModelNumMapDAO> inventoryDAOs){		
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();		
		for(VendorSkuToModelNumMapDAO inventoryDAO : inventoryDAOs){			
			session.update(inventoryDAO);
		}		
		session.getTransaction().commit();
		session.close();
	}
	
	public String getModelNumByItemInternalId(String internalId) {
		String modelNum = null;
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		VendorSkuToModelNumMapDAO vendorSkuToModelNumMapDAO = (VendorSkuToModelNumMapDAO) session.createQuery("from VendorSkuToModelNumMapDAO where vendorSku='" + internalId + "'").uniqueResult();
		if(null != vendorSkuToModelNumMapDAO)
			modelNum = vendorSkuToModelNumMapDAO.getModelNum();
		
		session.close();
		
		return modelNum;
	}

	@Override
	public void updatePoWithConfirmationAcceptedStatus(List<String> poNumbersToUpdate) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		
		for(String poNumber : poNumbersToUpdate){
			PurchaseOrderDAO dao = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where PurchaseOrderNumber='" + poNumber + "'").uniqueResult();
			if(dao != null){
				dao.setStatus(PurchaseOrderStatus.STAPLES_INVOICE_ACCEPTED);
				session.update(dao);
			}
		}
		session.getTransaction().commit();
		session.close();
		
	}

	@Override
	public void updatePurchaseOrder(List<PurchaseOrderPojo> poPojoListU) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		for(PurchaseOrderPojo poPojo : poPojoListU){
			if(poPojo.getIsDuplicate() == false){
				PurchaseOrderDAO poDao = (PurchaseOrderDAO) session.createQuery("from PurchaseOrderDAO where purchaseordernumber='" + poPojo.getPoNumber() + "'").uniqueResult();
				if(null != poDao){
					poDao.setDetails(poPojo.getExceptionDesc());
					poDao.setStatus(poPojo.getStatus());
					
					session.beginTransaction();
					session.update(poDao);
					session.getTransaction().commit();
				}
			}
		}
		session.close();
	}

	@Override
	public List<OutgoingMessageDAO> getOutgoingMessagesToSend() {	
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();		
		List<OutgoingMessageDAO> resultList = session.createQuery("from OutgoingMessageDAO where messageStatus='" + OutgoingMessageStatus.PENDING_FOR_SENDING + "'").list();		
		session.close();		
		return resultList;
	}

	@Override
	public void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessages) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		Transaction transaction = session.beginTransaction();		
		for(OutgoingMessageDAO omDAO : sentMessages){
			omDAO.setMessageStatus(OutgoingMessageStatus.SENT);
			session.update(omDAO);
		}		
		transaction.commit();		
		session.close();
	}

	@Override
	public void persistIncomingMessages(Set<String> incomingMessagesPaths) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		Transaction transaction = session.beginTransaction();		
		for(String path: incomingMessagesPaths){
			IncomingMessageDAO imDAO = new IncomingMessageDAO();
			imDAO.setMessagePath(path);
			imDAO.setMessageStatus(IncomingMessageStatus.PENDING_PROCESSING);			
			session.save(imDAO);
		}		
		transaction.commit();
		session.close();
	}

	@Override
	public List<IncomingMessageDAO> retrieveNewIncomingMessages() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();		
		List<IncomingMessageDAO> resultList = session.createQuery("from IncomingMessageDAO where messageStatus='" + IncomingMessageStatus.PENDING_PROCESSING + "'").list();		
		session.close();		
		return resultList;
	}

	@Override
	public void updateIncomingMessagesStatus(List<IncomingMessageDAO> failedToProcessMessages) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		Transaction transaction = session.beginTransaction();		
		for(IncomingMessageDAO imDAO : failedToProcessMessages){
			imDAO.setMessageStatus(IncomingMessageStatus.PROCESSED);
			session.update(imDAO);
		}		
		transaction.commit();		
		session.close();
	}

	@Override
	public List<PurchaseOrderDAO> getPreviouslyNSprocessingFailedOrders(List<IncomingMessageDAO> sourceMessageDAOList) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<PurchaseOrderDAO> resultList = session.createQuery("from PurchaseOrderDAO where status='" + PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL + "'").list();		
		sourceMessageDAOList.addAll(session.createQuery("select messages from PurchaseOrderDAO as orders inner join orders.mbDao as batch inner join batch.incomingMessage as messages where orders.status='" + PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL + "'").list());		
		session.close();
		return resultList;
	}

	@Override
	public void persistOutgoingMessages(List<OutgoingMessageDAO> faMessagesDAOList) {		
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		Transaction transaction = session.beginTransaction();		
		for(OutgoingMessageDAO messageDAO: faMessagesDAOList){				
			session.save(messageDAO);
		}		
		transaction.commit();
		session.close();
	}	
	
	@Override
	public void updatePoDAOs(List<PurchaseOrderDAO> poDAOsToUpdate) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(PurchaseOrderDAO poDAO : poDAOsToUpdate){
			session.update(poDAO);
			if ( counter % 20 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
			counter++;
		}
		session.getTransaction().commit();
		session.close();	
	}
	@Override
	public List<PurchaseOrderDAO> getPoppinPendingMessagesGenerationPurchaseOrders() {		
		List<PurchaseOrderDAO> purchaseOrderDAOs = new ArrayList<PurchaseOrderDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		purchaseOrderDAOs = session.createQuery("from PurchaseOrderDAO where status='" + PurchaseOrderStatus.POPPIN_PENDING_CONFIRMATION+"' or status='"+PurchaseOrderStatus.STAPLES_CONFIRMATION_ACCEPTED+"'").list();
		session.close();
		return purchaseOrderDAOs;		
	}
	@Override
	public void persistNotificationEmail(String text, List<String> attachments) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		NotificationEmailDAO notDAO = new NotificationEmailDAO();
		notDAO.setMessage(text);
		notDAO.setStatus(EmailMessageStatus.PENDING_FOR_SENDING);
		if ((attachments!=null)&&(!attachments.isEmpty())){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();		    
		    Blob blob = null;
			try {
				ObjectOutputStream oos = new ObjectOutputStream(bos);			
			    oos.writeObject(attachments);
			    byte[] bytes = bos.toByteArray();
				blob = new SerialBlob(bytes);
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			notDAO.setAttachments(blob);		
		}		
		session.save(notDAO);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public List<NotificationEmailDAO> getPendingNotificationEmails() {
		List<NotificationEmailDAO> neDAOs = new ArrayList<NotificationEmailDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		neDAOs = session.createQuery("from NotificationEmailDAO where status='" + EmailMessageStatus.PENDING_FOR_SENDING + "'").list();
		session.close();
		return neDAOs;
	}
	
	@Override
	public void updateNotificationEmails(List<NotificationEmailDAO> neToUpdate) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(NotificationEmailDAO neDAO : neToUpdate){			
			session.update(neDAO);
			if ( counter % 20 == 0 ) { 		       
		        session.flush();
		        session.clear();
		    }
			counter++;
		}
		session.getTransaction().commit();
		session.close();
	}
}
