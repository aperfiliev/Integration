package com.malkos.poppin.entities;

import java.util.List;

public class InventoryItemPojo extends InventoryPojo {	
	
	private List<LocationQuantitiesAvailiable> locQtyList;
	
	private String preferedLocationId;
		
	@Override
	public double getQtyAvailable() {
		for (LocationQuantitiesAvailiable locQty:locQtyList){
			if (locQty.getLocationInternalId().equalsIgnoreCase(getPreferedLocationId())){
				return locQty.getLocationQtyAvailiable();
			}
		}
		return 0d;
	}	
	
	public List<LocationQuantitiesAvailiable> getLocQtyList() {
		return locQtyList;
	}
	public void setLocQtyList(List<LocationQuantitiesAvailiable> locQtyList) {
		this.locQtyList = locQtyList;
	}
	public String getPreferedLocationId() {
		return preferedLocationId;
	}
	public void setPreferedLocationId(String preferedLocationId) {
		this.preferedLocationId = preferedLocationId;
	}
}
