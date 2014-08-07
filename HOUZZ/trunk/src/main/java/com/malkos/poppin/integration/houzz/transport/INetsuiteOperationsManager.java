package com.malkos.poppin.integration.houzz.transport;

import java.util.List;
import java.util.Map;

import antlr.collections.impl.Vector;

import com.malkos.poppin.integration.houzz.entities.InventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.OlapicInventoryItemPojo;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;


public interface INetsuiteOperationsManager {	
	Map<String,List> loadHouzzInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList)throws NetsuiteOperationException;
	List<InventoryItemPojo> loadOlapicInventory()throws NetsuiteOperationException;
	LineItemIntegrationIdentifierDAO updateInventoryInternalId(LineItemIntegrationIdentifierDAO lineItemDAO)throws NetsuiteOperationException;
}
