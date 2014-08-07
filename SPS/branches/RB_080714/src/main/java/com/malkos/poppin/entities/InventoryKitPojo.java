package com.malkos.poppin.entities;

import java.util.Arrays;
import java.util.List;

public class InventoryKitPojo {
	private String nsInternalId;
	private String popSKU;
	private List<InventoryKitSubItemPojo> subItemsList;
	
	public String getNsInternalId() {
		return nsInternalId;
	}
	public void setNsInternalId(String nsInternalId) {
		this.nsInternalId = nsInternalId;
	}
	public String getPopSKU() {
		return popSKU;
	}
	public void setPopSKU(String popSKU) {
		this.popSKU = popSKU;
	}
	public List<InventoryKitSubItemPojo> getSubItemsList() {
		return subItemsList;
	}
	public void setSubItemsList(List<InventoryKitSubItemPojo> subItemsList) {
		this.subItemsList = subItemsList;
	}	
	
	public double getQtyAvailable(){
		double[] arrayOfPotentialKits = new double[subItemsList.size()];
		int counter = 0;
		for (InventoryKitSubItemPojo subItem:subItemsList){
			Double potentialAvailableKitsForSubItem = subItem.getQtyAvailable()/subItem.getQtyInKit();
			arrayOfPotentialKits[counter] = potentialAvailableKitsForSubItem;
			counter++;
		}
		Arrays.sort(arrayOfPotentialKits);		
		return Math.floor(arrayOfPotentialKits[0]);
	}
}
