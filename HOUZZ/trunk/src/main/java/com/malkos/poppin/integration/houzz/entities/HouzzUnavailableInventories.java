package com.malkos.poppin.integration.houzz.entities;

import java.util.ArrayList;
import java.util.List;

public class HouzzUnavailableInventories {
	
private List<HouzzInventoryItemPojo> inactiveItemList = new ArrayList<>();
private List<HouzzInventoryItemPojo> lowQuantityItemList = new ArrayList<>();

public List<HouzzInventoryItemPojo> getInactiveItemList() {
	return inactiveItemList;
}
public void setInactiveItemList(List<HouzzInventoryItemPojo> inactiveItemList) {
	this.inactiveItemList = inactiveItemList;
}
public List<HouzzInventoryItemPojo> getLowQuantityItemList() {
	return lowQuantityItemList;
}
public void setLowQuantityItemList(List<HouzzInventoryItemPojo> lowQuantityItemList) {
	this.lowQuantityItemList = lowQuantityItemList;
}	
}
