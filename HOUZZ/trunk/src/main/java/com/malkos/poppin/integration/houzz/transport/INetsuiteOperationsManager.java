package com.malkos.poppin.integration.houzz.transport;

import java.util.List;
import java.util.Map;

import antlr.collections.impl.Vector;

import com.malkos.poppin.integration.houzz.entities.HouzzInventoryPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;
import com.malkos.poppin.integration.houzz.entities.OlapicInventoryItemPojo;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;


public interface INetsuiteOperationsManager {	
	Map<String,InventoryPojo> loadHouzzInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList)throws NetsuiteOperationException;
	List<InventoryPojo> loadOlapicInventory()throws NetsuiteOperationException;
	LineItemIntegrationIdentifierDAO updateInventoryInternalId(LineItemIntegrationIdentifierDAO lineItemDAO)throws NetsuiteOperationException;
}
