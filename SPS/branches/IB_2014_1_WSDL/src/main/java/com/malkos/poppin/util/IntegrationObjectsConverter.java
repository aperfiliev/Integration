package com.malkos.poppin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.TransactionType;
import com.malkos.poppin.persistence.dao.IncomingMessageDAO;
import com.malkos.poppin.persistence.dao.PackageDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.RetailerDAO;
import com.malkos.poppin.persistence.dao.TransactionDAO;

public class IntegrationObjectsConverter {
	public static List<PurchaseOrderDAO> convertPoDocumentsToPoDaos(List<PoDocument> documentslist, List<RetailerDAO> retailerDAOList) {
		Map<Integer,RetailerDAO> retailerIdToRetailerDAOmap = new HashMap<>();
		for(RetailerDAO  rDao : retailerDAOList)
			retailerIdToRetailerDAOmap.put(rDao.getIdRetailer(), rDao);
		List<PurchaseOrderDAO> poDaoList = new ArrayList<>();
		for(PoDocument document : documentslist){
			if(document.getProcessingStatus() != PurchaseOrderStatus.PENDING_POPPIN_PROCESSING){
				PurchaseOrderDAO poDao = new PurchaseOrderDAO();
				poDao.setPoNumber(document.getPoNumber());
				IncomingMessageDAO imDao = new IncomingMessageDAO();
				imDao.setIdIncomingMessage(document.getIncomingMessageId());
				poDao.setPoType(document.getPoType());
				poDao.setIncomingMessageDao(imDao);
				poDao.setProcessingStatus(document.getProcessingStatus());
				poDao.setAsnGenerated(document.isAsnGenerated());
				poDao.setInvoiceMessageGenerated(document.isInvoiceMessageGenerated());
				poDao.setRetailer(retailerIdToRetailerDAOmap.get(document.getRetailerId()));
				poDao.setExceptionDescription(document.getExceptionDescription());
				
				poDaoList.add(poDao);
			}
		}
		return poDaoList;
	}
	public static List<TransactionDAO> convertFulfillmentPojosToTransactionDAOs(List<FulfillmentPojo> fulfillmentPojos){
		List<TransactionDAO> newTransactions = new ArrayList<TransactionDAO>();
		for (FulfillmentPojo fulPojo:fulfillmentPojos){
			TransactionDAO trDao = new TransactionDAO();
			trDao.setTransactionType(TransactionType.FULFILLMENT);
			trDao.setTransactionInternalId(fulPojo.getNsInternalId());
			PurchaseOrderDAO poDao = new PurchaseOrderDAO();
			poDao.setIdPurchaseOrder(fulPojo.getPurchaseOrderId());
			trDao.setPurchaseOrder(poDao);
			newTransactions.add(trDao);
		}
		return newTransactions;
	}
	public static List<TransactionDAO> convertInvoicePojosToTransactionDAOs(List<InvoicePojo> invoicePojos){
		List<TransactionDAO> newTransactions = new ArrayList<TransactionDAO>();
		for (InvoicePojo invPojo:invoicePojos){
			TransactionDAO trDao = new TransactionDAO();
			trDao.setTransactionType(TransactionType.INVOICE);
			trDao.setTransactionInternalId(invPojo.getNsInternalId());
			PurchaseOrderDAO poDao = new PurchaseOrderDAO();
			poDao.setIdPurchaseOrder(invPojo.getPurchaseOrderId());
			trDao.setPurchaseOrder(poDao);
			newTransactions.add(trDao);
		}
		return newTransactions;
	}
	public static List<PackagePojo> convertPackageDAOsToPackagePojos (Set<PackageDAO> packDAOSet){
		List<PackagePojo> result = new ArrayList<>();
		for (PackageDAO packDAO:packDAOSet){
			PackagePojo packagePojo = new PackagePojo();
			packagePojo.setPackageDescription(packDAO.getPackageDescription());
			packagePojo.setPackageWeight(packDAO.getPackageWeight());
			packagePojo.setTrackingNumber(packDAO.getPackageTrackingNumber());
			result.add(packagePojo);
		}
		return result;
	}
}
