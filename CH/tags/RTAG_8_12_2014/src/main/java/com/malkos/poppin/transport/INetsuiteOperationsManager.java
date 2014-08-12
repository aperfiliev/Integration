package com.malkos.poppin.transport;

import java.util.List;
import java.util.Map;

import com.malkos.poppin.entities.CustomerShppingAddressPojo;
import com.malkos.poppin.entities.InventoryPojo;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.entities.UnprocessibleOrdersType;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerAddressbook;
import com.netsuite.webservices.platform.core_2012_1.Record;
import com.netsuite.webservices.transactions.sales_2012_1.SalesOrder;
import com.netsuite.webservices.lists.accounting_2012_1.InventoryItem;

public interface INetsuiteOperationsManager {
	//public String addCustomer(PurchaseOrderPojo po) throws NetsuiteAddRowException;
	//public String getCustomerIdByEmail(String email);
	public boolean addSalesOrder(PurchaseOrderPojo po, Map<String, String> popNumToInternalIdMap) throws NetsuiteNullResponseException, NetsuiteOperationException;
	//String getSalesOrderIdByPO(String po);
	public List<String> createFAMessage(List<PurchaseOrderPojo> poList);
	public List<SalesOrder> getPendingBilledOrBilledSalesOrdersFromPoppin();
	public List<SalesOrder> getPendingBilledOrBilledSalesOrdersFromPoppin(List<String> poIds) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException;
	public List<SalesOrder> getBilledSalesOrdersFromPoppin(List<String> poIds) throws NetsuiteNullResponseException, NetsuiteOperationException;
	public List<SalesOrder> getBilledSalesOrdersFromPoppinAdvanced(List<String> poIds) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException;
	public Map<String, String> getItemInternalIdToItemNumberMap(List<SalesOrder> records) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException;
	public Map<String, CustomerShppingAddressPojo> getsalesOrderIdToShippingAddressMap(List<SalesOrder> records) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException;
	public List<InventoryItem> getInventoryFromPoppin();
	public List<InventoryPojo> getInventoryFromPoppinAdvanced(Map<String, String> vendorSkuToModelNumMap) throws NetsuiteNullResponseException, NetsuiteOperationException;
	//public void retrieveInventoryInternalIdFromPoppin(List<VendorSkuToModelNumMapDAO> inventoryDAOs);
	public List<InventoryPojo> getInventoryFromPoppinUpdated(List<VendorSkuToModelNumMapDAO> inventoryDAOs) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException;
	Map<String, UnprocessibleOrdersType> retrieveCancelledClosedOrders(List<PurchaseOrderDAO> purchaseOrderDAOs);
}
