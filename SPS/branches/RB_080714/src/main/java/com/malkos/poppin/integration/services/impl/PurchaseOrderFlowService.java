package com.malkos.poppin.integration.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentsManager;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.integration.IntegrationDetailsProvider;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.integration.services.IPurchaseOrderFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.IntegrationObjectsConverter;

public class PurchaseOrderFlowService implements IPurchaseOrderFlowService {

	@Override
	public void processNewPurchaseOrders() {
		IntegrationDetailsProvider.getInstance().setFreshPoppinAssortment();
		RetailersManager retailersManager = RetailersManager.getInstance();
		IPersistenceManager persistenceManager = new PersistenceManager();
		PoDocumentsManager poDocManager = new PoDocumentsManager();
		List<PoDocument> documentslist = poDocManager.loadPendingProcessingDocuments();
		
		retailersManager.askRetailersToRefreshAssortment();
		retailersManager.assignPoDocumnets(documentslist);
		retailersManager.askRetailersToValidate();
		retailersManager.checkDocumentsPoNumbersIfTheyAlreadyAdded(documentslist);
		retailersManager.updatePoDocumentStatuses(documentslist);
		
		List<PoDocument> sendedDocumentsList = retailersManager.sendPurchaseOrders();
		
		retailersManager.updatePoDocumentStatuses(sendedDocumentsList);
		List<PurchaseOrderDAO> poDaoList = IntegrationObjectsConverter.convertPoDocumentsToPoDaos(documentslist, retailersManager.getRetailerDAOList());
		persistenceManager.persistPurchaseOrders(poDaoList);
		
		persistenceManager.updatePoDaosAfterPoppinProcessing(getPaoDaoListFromSendedPoDocuments(sendedDocumentsList, poDaoList));
		persistenceManager.updateIncomingMessagesStatuses(poDaoList);
		
		ErrorsCollector.searchPossibleOrderErrors(documentslist);
		retailersManager.askRetailersToClearDocuments();
	}
	private List<PurchaseOrderDAO> getPaoDaoListFromSendedPoDocuments(List<PoDocument> documents, List<PurchaseOrderDAO> initialPoDaos){
		List<PurchaseOrderDAO> poDaoListFromSendedDocuments = new ArrayList<>();
		Map<String, PurchaseOrderDAO> poNumberToPoDAOmap = new HashMap<>();
		for(PurchaseOrderDAO poDao : initialPoDaos)
			poNumberToPoDAOmap.put(poDao.getPoNumber(), poDao);
		for(PoDocument document : documents){
			PurchaseOrderDAO poDao = poNumberToPoDAOmap.get(document.getPoNumber());
			poDao.setProcessingStatus(document.getProcessingStatus());
			poDao.setSalesOrderNsInternald(document.getSalesOrderInternalId());
			poDaoListFromSendedDocuments.add(poDao);
		}
		return poDaoListFromSendedDocuments;
	}
}
