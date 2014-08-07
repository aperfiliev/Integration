package com.malkos.poppin.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;

public class IntegrationDetailsProvider {
	
	private static IntegrationDetailsProvider instance;
	
	//private Map<String, String> poppinAssortment;
	//private Map<String, String> retailersShippingAddresses;
	Map<Integer, Map<String, ShippingAddressPojo>> shippingAddressesMappings;
	
	private Map<String, PurchaseOrderType> poTypeCodeToPoTypeTo;
	private List<LineItemIntegrationIdentifierDAO> poppinAssortment;
	private Map<Integer, Map<String, String>> retailerIdToAssortment;
		
	private IntegrationDetailsProvider(){
		//setPoppinAssortment(new HashMap<String, String>());
		//setRetailersShippingAddresses(new HashMap<String, String>());
		shippingAddressesMappings = new HashMap<>();
		IPersistenceManager persistenceManager = new PersistenceManager();
		poppinAssortment =  persistenceManager.getPoppinAssortment();
		shippingAddressesMappings = persistenceManager.getShippingAddressesMappings();
		//setRetailersShippingAddresses(persistenceManager.getShippingAddressLabelToInternalIdMap());
		initializePoTypes();
		retailerIdToAssortment = new HashMap<>();
	}
	private void initializePoTypes(){
		poTypeCodeToPoTypeTo = new HashMap<>();
		getPoTypeCodeToPoType().put("DS", PurchaseOrderType.DROPSHIP);
		getPoTypeCodeToPoType().put("KN", PurchaseOrderType.CROSSDOCK);
		getPoTypeCodeToPoType().put("SA", PurchaseOrderType.BULKIMPORT);
		getPoTypeCodeToPoType().put("PR", PurchaseOrderType.PROMO);
	}
	
	public static IntegrationDetailsProvider getInstance() {		
		if (null == instance){
			synchronized (RetailersManager.class) {
				if (null == instance){
					instance = new IntegrationDetailsProvider();
				}
			}			
		}
		return instance;
	}

	/**
	 * @return the poppinAssortment
	 */
	/*public Map<String, String> getPoppinAssortment() {
		return poppinAssortment;
	}*/

	/**
	 * @param poppinAssortment the poppinAssortment to set
	 */
	/*public void setPoppinAssortment(Map<String, String> poppinAssortment) {
		this.poppinAssortment = poppinAssortment;
	}*/

	/**
	 * @return the staplesRetailerShippingAddresses
	 */
	/*public Map<String, String> getRetailersShippingAddresses() {
		return retailersShippingAddresses;
	}*/

	/**
	 * @param staplesRetailerShippingAddresses the staplesRetailerShippingAddresses to set
	 */
	/*public void setRetailersShippingAddresses(
			Map<String, String> retailersShippingAddresses) {
		this.retailersShippingAddresses = retailersShippingAddresses;
	}*/
	public Map<String, PurchaseOrderType> getPoTypeCodeToPoType() {
		return poTypeCodeToPoTypeTo;
	}
	public void setPoTypeCodeToPoTypeTo(Map<String, PurchaseOrderType> poTypeCodeToPoTypeTo) {
		this.poTypeCodeToPoTypeTo = poTypeCodeToPoTypeTo;
	}
	public List<LineItemIntegrationIdentifierDAO> getPoppinAssortment() {
		return poppinAssortment;
	}
	public Map<String, String> getPoppinAssortmentByRetailer(int retailerId) {
		Map<String, String> retailerAssortment = new HashMap<>();
		if(retailerIdToAssortment.containsKey(retailerId))
			retailerAssortment =  retailerIdToAssortment.get(retailerId);
		return retailerAssortment;
	}
	public void setFreshPoppinAssortment() {
		IPersistenceManager persistenceManager = new PersistenceManager();
		poppinAssortment =  persistenceManager.getPoppinAssortment();
		for(LineItemIntegrationIdentifierDAO item : poppinAssortment){
			Integer retailerId = item.getRetailer().getIdRetailer();
			if(!retailerIdToAssortment.containsKey(retailerId)){
				retailerIdToAssortment.put(retailerId, new HashMap<String, String>());
			}
			retailerIdToAssortment.get(retailerId).put(item.getModelNum(), item.getItemInternalId());
		}
	}
	public Map<String, ShippingAddressPojo> getRetailerShippingAddressesMappings(int retailerId) {
		if(shippingAddressesMappings.containsKey(retailerId))
			return shippingAddressesMappings.get(retailerId);
		else
			return null;
	}
}
