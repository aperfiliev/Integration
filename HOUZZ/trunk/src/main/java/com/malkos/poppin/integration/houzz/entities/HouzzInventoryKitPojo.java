package com.malkos.poppin.integration.houzz.entities;

import java.util.Arrays;
import java.util.List;

public class HouzzInventoryKitPojo extends HouzzInventoryPojo{	
	private List<HouzzInventoryKitSubItemPojo> subItemsList;
	private String nsLocationId;	
	public HouzzInventoryKitPojo(){
		propertiesCorrector = new HouzzInventoryItemPropertiesCorrector();
	}	
	public List<HouzzInventoryKitSubItemPojo> getSubItemsList() {
		return subItemsList;
	}
	public void setSubItemsList(List<HouzzInventoryKitSubItemPojo> subItemsList) {
		this.subItemsList = subItemsList;
	}	
	@Override
	public double getQtyAvailable(){
		double[] arrayOfPotentialKits = new double[subItemsList.size()];
		int counter = 0;
		for (HouzzInventoryKitSubItemPojo subItem:subItemsList){
			Double potentialAvailableKitsForSubItem = subItem.getQtyAvailiable()/subItem.getQtyInKit();
			arrayOfPotentialKits[counter] = potentialAvailableKitsForSubItem;
			counter++;
		}
		Arrays.sort(arrayOfPotentialKits);		
		return Math.floor(arrayOfPotentialKits[0]);
	}
	@Override
	public boolean isInactive(){
		if (inactive){
			return true;
		}
		for (HouzzInventoryKitSubItemPojo subItem:subItemsList){
			if (subItem.isInactive()){
				return true;
			}
		}
		return false;
	}
	public String getNsLocationId() {
		return nsLocationId;
	}
	public void setNsLocationId(String nsLocationId) {
		this.nsLocationId = nsLocationId;
	}
}
