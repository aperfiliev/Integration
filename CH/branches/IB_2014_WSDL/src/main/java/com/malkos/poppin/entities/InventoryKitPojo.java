package com.malkos.poppin.entities;

import java.util.Arrays;
import java.util.List;

public class InventoryKitPojo extends InventoryPojo{	
	private List<InventoryKitSubItemPojo> subItemsList;
	private String nsLocationId;	
	
	public List<InventoryKitSubItemPojo> getSubItemsList() {
		return subItemsList;
	}
	public void setSubItemsList(List<InventoryKitSubItemPojo> subItemsList) {
		this.subItemsList = subItemsList;
	}	
	@Override
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
	
	public String getNsLocationId() {
		return nsLocationId;
	}
	public void setNsLocationId(String nsLocationId) {
		this.nsLocationId = nsLocationId;
	}
}
