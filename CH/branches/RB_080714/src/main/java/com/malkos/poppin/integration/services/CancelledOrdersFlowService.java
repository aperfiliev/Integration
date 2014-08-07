package com.malkos.poppin.integration.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.malkos.poppin.entities.PurchaseOrderStatus;
import com.malkos.poppin.entities.UnprocessibleOrdersType;
import com.malkos.poppin.integration.services.ICanceledOrdersFlowService;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;

public class CancelledOrdersFlowService implements ICanceledOrdersFlowService {

	@Autowired
	IPersistenceManager persistenceManager;
	
	private INetsuiteOperationsManager netsuiteOperationsManager;
	
	private static Logger logger = LoggerFactory.getLogger(CancelledOrdersFlowService.class);
	
	@Override
	public void processCancelledOrders() {		
		//1. Retrieve orders that could be cancelled/closed
		logger.info("Getting orders that could be cancelled/closed");
		List<PurchaseOrderDAO> purchaseOrderDAOs = persistenceManager.getPoppinPendingMessagesGenerationPurchaseOrders();
		if (purchaseOrderDAOs.size()>0){
			Map<String, PurchaseOrderDAO> poNumberToPoDAOMap = new HashMap <String, PurchaseOrderDAO>();
			for (PurchaseOrderDAO poDAo:purchaseOrderDAOs){
				poNumberToPoDAOMap.put(poDAo.getPurchaseOrderNumber(), poDAo);
			}			
			//2. Retrieve orders statuses from NetSuite
			logger.info("Retrieve orders statuses from NetSuite Poppin");
			Map<String, UnprocessibleOrdersType> poNumberToUnprocessibleOrderTypeMap = getNetsuiteOperationsManager().retrieveCancelledClosedOrders(purchaseOrderDAOs);	
			if (!poNumberToUnprocessibleOrderTypeMap.isEmpty()){
				List<PurchaseOrderDAO> poDAOsToUpdate = extractPoDAOsToUpdate(poNumberToPoDAOMap, poNumberToUnprocessibleOrderTypeMap);
				persistenceManager.updatePoDAOs(poDAOsToUpdate);
			}
		}		
	}

	private List<PurchaseOrderDAO> extractPoDAOsToUpdate(Map<String, PurchaseOrderDAO> poNumberToPoDAOMap, Map<String, UnprocessibleOrdersType> poNumberToUnprocessibleOrderTypeMap) {
		List<PurchaseOrderDAO> poDAOsToUpdate = new ArrayList<>();
		for (String poNumber : poNumberToUnprocessibleOrderTypeMap.keySet()){
			PurchaseOrderDAO poDao = poNumberToPoDAOMap.get(poNumber);
			String orderExceptionDescription = "Order ";
			if (poNumberToUnprocessibleOrderTypeMap.get(poNumber)==UnprocessibleOrdersType.CANCELLED){
				orderExceptionDescription += "cancelled";
			} else {
				orderExceptionDescription += "closed";
			}
			poDao.setDetails(orderExceptionDescription);			
			poDao.setStatus(PurchaseOrderStatus.POPPIN_REJECTED);
			poDAOsToUpdate.add(poDao);
		}
		return poDAOsToUpdate;
	}

	public INetsuiteOperationsManager getNetsuiteOperationsManager() {
		return netsuiteOperationsManager;
	}

	public void setNetsuiteOperationsManager(INetsuiteOperationsManager netsuiteOperationsManager) {
		this.netsuiteOperationsManager = netsuiteOperationsManager;
	} 
}
