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
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.entities.FulfillmentItemPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.enums.EmailMessageStatus;
import com.malkos.poppin.entities.enums.OutgoingMessageType;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.entities.enums.IncomingMessageStatus;
import com.malkos.poppin.entities.enums.OutgoingMessageStatus;
import com.malkos.poppin.persistence.dao.FulfillmentLineItemDAO;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.ProcessedPurchaseOrdersNumbersDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.RetailerDAO;
import com.malkos.poppin.persistence.dao.ShippingAddressDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;

public class PersistenceManager implements IPersistenceManager{

	@Override
	public List<PurchaseOrderDAO> getPoppinPendingProcessingPurchaseOrders() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<PurchaseOrderDAO> poppinPendingOrdersDaos = session.createQuery("from PurchaseOrderDAO where processingStatus='" + PurchaseOrderStatus.PENDING_POPPIN_PROCESSING+"'").list();
		session.close();
		return poppinPendingOrdersDaos;
	}
	@Override
	public void persistPurchaseOrders(List<PurchaseOrderDAO> poDAOlist){
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(PurchaseOrderDAO poDao : poDAOlist){
			//session.save(poDao.getIncomingMessageDao());
			if(null != poDao.getRetailer())
				session.save(poDao);
		}
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public void persistProcessedPurchaseOrders(Map<String, String> processedPoNumberToSoInteranIdMap) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(Entry<String, String> poNumberToSoInternalId : processedPoNumberToSoInteranIdMap.entrySet()){
			ProcessedPurchaseOrdersNumbersDAO poDAO = new ProcessedPurchaseOrdersNumbersDAO();
			poDAO.setProcessedPurchaseOrderNumber(poNumberToSoInternalId.getKey());
			poDAO.setSalesOrderInternalIdNumber(Long.parseLong(poNumberToSoInternalId.getValue()));
			session.save(poDAO);
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
	public long getLastProcessedPoSalesOrderInternaNumberlId() {
		long lastProcessedPoNumber = 0;
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		Object result = session.createQuery("select max( salesOrderInternalIdNumber ) from ProcessedPurchaseOrdersNumbersDAO").uniqueResult();
		if(null != result)
			lastProcessedPoNumber = Long.parseLong(result.toString());
		session.close();
		return lastProcessedPoNumber;
	}
	@Override
	public void updateLineItemIntegrationIdentifierDAOlist(Map<String, String> inventoryVendorSKUtoIntenrlIdMap) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<LineItemIntegrationIdentifierDAO> lineItemIntegrationIdentifierDAOsToUpdate = new ArrayList<LineItemIntegrationIdentifierDAO>();
		List<LineItemIntegrationIdentifierDAO> lineItemIntegrationIdentifierDAOs = session.createQuery("from LineItemIntegrationIdentifierDAO").list();
		for(LineItemIntegrationIdentifierDAO liiDao : lineItemIntegrationIdentifierDAOs){
			if(inventoryVendorSKUtoIntenrlIdMap.containsKey(liiDao.getVendorSKU())){
				liiDao.setItemInternalId(inventoryVendorSKUtoIntenrlIdMap.get(liiDao.getVendorSKU()));
				lineItemIntegrationIdentifierDAOsToUpdate.add(liiDao);
			}
		}
		session.beginTransaction();
		for(LineItemIntegrationIdentifierDAO liiDao : lineItemIntegrationIdentifierDAOsToUpdate)
			session.update(liiDao);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public Map<String, String> getPopMapperToItemInternalIdMap(){
		Map<String, String> vendorSKUtoItemInternalId = new HashMap<String, String>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<LineItemIntegrationIdentifierDAO> lineItemIntegrationIdentifierDAOs = session.createQuery("from LineItemIntegrationIdentifierDAO").list();
		for(LineItemIntegrationIdentifierDAO liiDao : lineItemIntegrationIdentifierDAOs){
			if(liiDao.getItemInternalId() != null){
				vendorSKUtoItemInternalId.put(liiDao.getModelNum(), liiDao.getItemInternalId());
			}
		}
		session.close();
		return vendorSKUtoItemInternalId;
	}

	@Override
	public void updatePoDaosAfterPoppinProcessing(List<PurchaseOrderDAO> purchaseOrderDAOlist) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(PurchaseOrderDAO poDao : purchaseOrderDAOlist){
			session.update(poDao);
			if(poDao.getSalesOrderNsInternald() != null){
				ProcessedPurchaseOrdersNumbersDAO processedPoNumDAO = new ProcessedPurchaseOrdersNumbersDAO();
				processedPoNumDAO.setProcessedPurchaseOrderNumber(poDao.getPoNumber());
				processedPoNumDAO.setSalesOrderInternalIdNumber(Long.parseLong(poDao.getSalesOrderNsInternald()));
				session.save(processedPoNumDAO);
			}
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
	public void persistOutgoingMessages(List<String> messagePathList, OutgoingMessageType messagesType) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(String messagePath : messagePathList){
			OutgoingMessageDAO omDAO = new OutgoingMessageDAO();
			omDAO.setCreatedDate(new Date());
			omDAO.setOutgoingMessagePath(messagePath);
			omDAO.setOutgoingMessageType(messagesType);
			omDAO.setOutgoingMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
			session.save(omDAO);
		}
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public List<OutgoingMessageDAO> getOutgoingMessagesToSend() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<OutgoingMessageDAO> outgoingMessagesToSend = session.createQuery("from OutgoingMessageDAO where outgoingMessageStatus='" + OutgoingMessageStatus.PENDING_FOR_SENDING+"'").list();
		session.close();
		return outgoingMessagesToSend;
	}
	@Override
	public void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessages) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(OutgoingMessageDAO oiD : sentMessages){
			oiD.setOutgoingMessageStatus(OutgoingMessageStatus.SENT);
			session.update(oiD);
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
	public List<PurchaseOrderDAO> getPoppinPendingInvoiceGenerationPurchaseOrders() {		
		List<PurchaseOrderDAO> purchaseOrderDAOs = new ArrayList<PurchaseOrderDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		purchaseOrderDAOs = session.createQuery("from PurchaseOrderDAO where processingStatus='" + PurchaseOrderStatus.POPPIN_PROCESSED+"' and isInvoiceMessagesGenerated=false").list();
		session.close();
		return purchaseOrderDAOs;		
	}
	@Override
	public void persistIncomingMessages(Set<String> incomingMessagesPaths) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(String messagePath : incomingMessagesPaths){
			IncomingMessageDAO imDAO = new IncomingMessageDAO();
			imDAO.setRecievedDate(new Date());
			imDAO.setIncomingMessageStatus(IncomingMessageStatus.PENDING_PROCESSING);
			imDAO.setMessagePath(messagePath);
			session.save(imDAO);
		}
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public List<RetailerDAO> loadRetailers() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<RetailerDAO> retailerDAOlist = new ArrayList<RetailerDAO>();
		retailerDAOlist = session.createQuery("from RetailerDAO").list();
		session.close();
		return retailerDAOlist;
	}
	@Override
	public List<IncomingMessageDAO> loadPendingProcessingDocumentsFilePathList() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<IncomingMessageDAO> incomingMessagesDAOList = new ArrayList<IncomingMessageDAO>();
		incomingMessagesDAOList = session.createQuery("from IncomingMessageDAO where incomingMessageStatus='" + IncomingMessageStatus.PENDING_PROCESSING+"'").list();
		session.close();
		//List<String> result = new ArrayList<String>();
		//for (IncomingMessageDAO incMessDAO :incomingMessagesDAOList){
		//	result.add(incMessDAO.getMessagePath());
		//}
		//return result;
		return incomingMessagesDAOList;
	}
	@Override
	public Map<String,String> getShippingAddressLabelToInternalIdMap(){
		Map<String,String> result = new HashMap<String,String>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<ShippingAddressDAO> shippingAddressList = session.createQuery("from ShippingAddressDAO").list();
		for(ShippingAddressDAO shipAdrDao : shippingAddressList){
			result.put(shipAdrDao.getShippingAddressLabel(), shipAdrDao.getShippingAddressInternalId());
		}
		session.close();		
		return result;
	}
	@Override
	public Map<Integer, Map<String, ShippingAddressPojo>> getShippingAddressesMappings(){
		Map<Integer, Map<String, ShippingAddressPojo>> result = new HashMap<Integer, Map<String,ShippingAddressPojo>>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<ShippingAddressDAO> shippingAddressList = session.createQuery("from ShippingAddressDAO").list();
		
		for(ShippingAddressDAO shipAdrDao : shippingAddressList){
			if(!result.containsKey(shipAdrDao.getShippingAddressRetailerId())){
				result.put(shipAdrDao.getShippingAddressRetailerId(), new HashMap<String,ShippingAddressPojo>());
			}
			Map<String,ShippingAddressPojo> mappings = result.get(shipAdrDao.getShippingAddressRetailerId());
			mappings.put(shipAdrDao.getShippingAddressLabel(), new ShippingAddressPojo(shipAdrDao.getShippingAddressInternalId(), shipAdrDao.getShippingAddressemail()));
		}
		
		session.close();
		return result;
	}
	public boolean checkIfSalesOrderExistsForGivenPoNumber(PoDocument document) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		
		Query query = session.createQuery("from ProcessedPurchaseOrdersNumbersDAO where purchaseOrderNumber='" + document.getPoNumber() + "'");
		query.setMaxResults(1);
		ProcessedPurchaseOrdersNumbersDAO result = (ProcessedPurchaseOrdersNumbersDAO) query.uniqueResult();
		if(null != result)
			return true;
		
		session.close();
		return false;
	}
	/*public void updateIncomingMessagesStatuses(List<PoDocument> documentslist) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(PoDocument document : documentslist){
			IncomingMessageDAO imDAO = (IncomingMessageDAO) session.get(IncomingMessageDAO.class,document.getIncomingMessageId());
			imDAO.setIncomingMessageStatus(IncomingMessageStatus.PROCESSED);
			session.update(imDAO);
		}
		session.getTransaction().commit();
		session.close();
	}*/
	@Override
	public List<PurchaseOrderDAO> getPoppinPendingASNGenerationPurchaseOrders() {
		List<PurchaseOrderDAO> purchaseOrderDAOs = new ArrayList<PurchaseOrderDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		purchaseOrderDAOs = session.createQuery("from PurchaseOrderDAO where processingStatus='" + PurchaseOrderStatus.POPPIN_PROCESSED+"' and isAsnGenerated=false").list();
		session.close();
		return purchaseOrderDAOs;
	}
	@Override
	public List<TransactionDAO> getProccessedTransactions(List<PurchaseOrderDAO> orderDAOList,TransactionType trType) {
		String poDaoIdSetSubQuery = new String("");
		if (orderDAOList.size()>0){
			poDaoIdSetSubQuery+=" and purchaseOrder.idPurchaseOrder in (";
		}
		int counter = 0;
		for (PurchaseOrderDAO poDao:orderDAOList){
			if (counter==orderDAOList.size()-1){
				poDaoIdSetSubQuery+=poDao.getIdPurchaseOrder()+")";
			}else{
				poDaoIdSetSubQuery+=poDao.getIdPurchaseOrder()+",";
			}
			counter++;
		}
		
		List<TransactionDAO> fulfillmentsDAOs = new ArrayList<TransactionDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();		
		Session session = factory.openSession();
		fulfillmentsDAOs = session.createQuery("from TransactionDAO where transactionType='" + trType+"'"+poDaoIdSetSubQuery).list();
		session.close();
		return fulfillmentsDAOs;
	}
	@Override
	public List<PackageDAO> persistPackages(List<PurchaseOrderDAO> poList,List<FulfillmentPojo> processedFulfillments) {
		
		Map<String,PurchaseOrderDAO> pOnumberToPurchaseOrderDAOMap = new HashMap<String,PurchaseOrderDAO>();
		List<PackageDAO> packageDaoList = new ArrayList<PackageDAO>();
		for (PurchaseOrderDAO poDao:poList){
			pOnumberToPurchaseOrderDAOMap.put(poDao.getPoNumber(), poDao);
		}		
		for (FulfillmentPojo fulPojo:processedFulfillments){
			if (pOnumberToPurchaseOrderDAOMap.containsKey(fulPojo.getPoNumber())){
				PurchaseOrderDAO poDao = pOnumberToPurchaseOrderDAOMap.get(fulPojo.getPoNumber());
				for (PackagePojo packPojo:fulPojo.getPackageItems()){				
					PackageDAO packageDao = new PackageDAO();
					packageDao.setPackageDescription(packPojo.getPackageDescription());
					packageDao.setPackageTrackingNumber(packPojo.getTrackingNumber());
					packageDao.setPackageWeight(packPojo.getPackageWeight());
					packageDao.setPurchaseOrder(poDao);
					packageDaoList.add(packageDao);
				}
			}			
		}		
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(PackageDAO packageDAO : packageDaoList){
			session.save(packageDAO);
			if ( counter % 20 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
			counter++;
		}
		session.getTransaction().commit();
		session.close();	
		return packageDaoList;
	}
	@Override
	public void persistTransactions(List<TransactionDAO> newTransactions) {
	SessionFactory factory = HibernateSessionProvider.getSessionFactory();
	Session session = factory.openSession();
	session.beginTransaction();
	for(TransactionDAO trDao : newTransactions){
		//session.save(trDao.);
		session.save(trDao);
	}
	session.getTransaction().commit();
	session.close();
	}
	@Override
	public void updateStatusAfterASNProcessing(List<PurchaseOrderDAO> poList, List<FulfillmentPojo> processedFulfillments) {

		List<PurchaseOrderDAO> updatedDAOs = new ArrayList<PurchaseOrderDAO>();		
		Map<Integer, FulfillmentPojo> processedPOMap = new HashMap<>();
		for(FulfillmentPojo fulfillment : processedFulfillments){
			processedPOMap.put(fulfillment.getPurchaseOrderId(), fulfillment);
		}	
		
		for(PurchaseOrderDAO poDAO : poList){
			if (processedPOMap.containsKey(poDAO.getIdPurchaseOrder())){
				FulfillmentPojo fulPojo = processedPOMap.get(poDAO.getIdPurchaseOrder());

				if(fulPojo.getExceptionDescription() != null){
					poDAO.setProcessingStatus(PurchaseOrderStatus.SYSTEM_REJECTED);
					poDAO.setExceptionDescription(fulPojo.getExceptionDescription());
					updatedDAOs.add(poDAO);
				}
				else if (fulPojo.isProcessingClosed() ){
					poDAO.setAsnGenerated(true);
					updatedDAOs.add(poDAO);
				}					
			}						
		}
		updatePoDAOs(updatedDAOs);	
	}
	@Override
	public void persistOrderLineItemsAfterAsnProcessing(List<PackageDAO> packList, List<FulfillmentPojo> processedFulfillments) {
		List<FulfillmentLineItemDAO> lstFulfillmentItemDAO = prepareFulfillmentsDAOtoPersist(packList, processedFulfillments);
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(FulfillmentLineItemDAO fliDAO : lstFulfillmentItemDAO){
			session.save(fliDAO);
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
	
	private List<FulfillmentLineItemDAO> prepareFulfillmentsDAOtoPersist(List<PackageDAO> packList, List<FulfillmentPojo> processedFulfillments){
		Map<String,List<PackageDAO>> poNumberToPackageListMap = new HashMap<>();
		for (PackageDAO pDAO:packList){
			String poNumber = pDAO.getPurchaseOrder().getPoNumber();
			if (poNumberToPackageListMap.containsKey(poNumber)){
				poNumberToPackageListMap.get(poNumber).add(pDAO);
			}else{
				List<PackageDAO> listPackDAO= new ArrayList<>();
				listPackDAO.add(pDAO);
				poNumberToPackageListMap.put(poNumber, listPackDAO);
			}
		}
		List<FulfillmentLineItemDAO> itemsToPersist = new ArrayList<>();
		for (FulfillmentPojo fulPojo:processedFulfillments){
			String poNumber = fulPojo.getPoNumber();
			if (poNumberToPackageListMap.containsKey(poNumber)){
				List<PackageDAO> lstPackDAO= poNumberToPackageListMap.get(poNumber);
				for (FulfillmentItemPojo fulItemPojo:fulPojo.getOrderItems()){
					for (PackageDAO pDAO:lstPackDAO){
						if (fulItemPojo.getTrackingNumber().equalsIgnoreCase(pDAO.getPackageTrackingNumber())){
							FulfillmentLineItemDAO itemDAO = new FulfillmentLineItemDAO();
							itemDAO.setItemInternalId(fulItemPojo.getItemInternalId());
							itemDAO.setItemNumber(fulItemPojo.getItemNumber());
							itemDAO.setOrderQuantity(fulItemPojo.getOrderQty());
							itemDAO.setShipQuantity(fulItemPojo.getShipQty());
							itemDAO.setUnitPrice(fulItemPojo.getUnitPrice());
							itemDAO.setUPC(fulItemPojo.getUPC());
							itemDAO.setVendorLineNumber(fulItemPojo.getVendorlineNumber());
							itemDAO.setTrackingNumber(fulItemPojo.getTrackingNumber());
							itemDAO.setPackageDAO(pDAO);
							itemsToPersist.add(itemDAO);
							break;
						}
					}
				}
			}
		}
		return itemsToPersist;
	}
	@Override
	public List<FulfillmentLineItemDAO> getShipedItems(List<PurchaseOrderDAO> poDAOList) {
		List<FulfillmentLineItemDAO> result = new ArrayList<>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();		
		String querry = "from FulfillmentLineItemDAO where packageDAO.purchaseOrder.idPurchaseOrder in (";
		for (PurchaseOrderDAO poDAO:poDAOList){
			querry+="'"+poDAO.getIdPurchaseOrder()+"',";
		}
		querry=querry.substring(0,querry.length()-1);
		querry+=")";
		result  = session.createQuery(querry).list();		
		session.close();		
		return result;		
	}
	@Override
	public void updateStatusAfterInvoiceProcessing(List<PurchaseOrderDAO> poList, List<InvoicePojo> processedInvoices) {//				
		
		List<PurchaseOrderDAO> updatedDAOs = new ArrayList<PurchaseOrderDAO>();		
		Map<Integer, Boolean> processedPOMap = new HashMap<Integer, Boolean>();
		for(InvoicePojo invoice:processedInvoices){
			processedPOMap.put(invoice.getPurchaseOrderId(), invoice.isProcessingClosed());
		}	
		
		for(PurchaseOrderDAO poDAO:poList){
			if (processedPOMap.containsKey(poDAO.getIdPurchaseOrder())){
				if (processedPOMap.get(poDAO.getIdPurchaseOrder())){
					poDAO.setInvoiceMessageGenerated(true);
					updatedDAOs.add(poDAO);
				}					
			}						
		}	
		updatePoDAOs(updatedDAOs);
	}
	@Override
	public List<LineItemIntegrationIdentifierDAO> getPoppinAssortment() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<LineItemIntegrationIdentifierDAO> lineItemIntegrationIdentifierDAOs = session.createQuery("from LineItemIntegrationIdentifierDAO").list();
		session.close();
		return lineItemIntegrationIdentifierDAOs;
	}
	@Override
	public void updateLineItemIntegrationIdentifierDAOlist(	List<LineItemIntegrationIdentifierDAO> lineItemDAOList) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();			
		session.beginTransaction();
		int counter = 1;
		for(LineItemIntegrationIdentifierDAO liiDao : lineItemDAOList){
			session.update(liiDao);
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
	public List<PurchaseOrderDAO> getPoppinPendingInvoiceGenerationPurchaseOrdersForDataMigration() {
		List<PurchaseOrderDAO> purchaseOrderDAOs = new ArrayList<PurchaseOrderDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		purchaseOrderDAOs = session.createQuery("from PurchaseOrderDAO where processingStatus='" + PurchaseOrderStatus.POPPIN_PROCESSED+"' and isAsnGenerated=true and isInvoiceMessagesGenerated=false").list();
		session.close();
		return purchaseOrderDAOs;
	}
	@Override
	public void updateIncomingMessagesStatuses(List<PurchaseOrderDAO> processedDaos) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(PurchaseOrderDAO document : processedDaos){
			IncomingMessageDAO imDAO = (IncomingMessageDAO) session.get(IncomingMessageDAO.class, document.getIncomingMessageDao().getIdIncomingMessage());
			imDAO.setIncomingMessageStatus(IncomingMessageStatus.PROCESSED);
			session.update(imDAO);
		}
		session.getTransaction().commit();
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
		purchaseOrderDAOs = session.createQuery("from PurchaseOrderDAO where processingStatus='" + PurchaseOrderStatus.POPPIN_PROCESSED+"' and (isInvoiceMessagesGenerated=false or isASNGenerated=false)").list();
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
