package com.malkos.poppin.integration.houzz.entities;

import java.util.ArrayList;
import java.util.List;

public class HouzzUnavailableInventories {
	
private List<HouzzInventoryPojo> inactiveItemList = new ArrayList<>();
private List<HouzzInventoryPojo> lowQuantityItemList = new ArrayList<>();

public List<HouzzInventoryPojo> getInactiveItemList() {
	return inactiveItemList;
}
public void setInactiveItemList(List<HouzzInventoryPojo> inactiveItemList) {
	this.inactiveItemList = inactiveItemList;
}
public List<HouzzInventoryPojo> getLowQuantityItemList() {
	return lowQuantityItemList;
}
public void setLowQuantityItemList(List<HouzzInventoryPojo> lowQuantityItemList) {
	this.lowQuantityItemList = lowQuantityItemList;
}	
}
