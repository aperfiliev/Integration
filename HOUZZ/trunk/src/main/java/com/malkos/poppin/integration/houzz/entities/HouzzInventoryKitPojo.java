package com.malkos.poppin.integration.houzz.entities;

import java.util.Arrays;
import java.util.List;

public class HouzzInventoryKitPojo {
	private String nsInternalId;
	private String sku;
	private double price; 
	private boolean inactive;
	private boolean wrongConfigured = false;
	private List<HouzzInventoryKitSubItemPojo> subItemsList;
	private String nsLocationId;
	
	public String getNsInternalId() {
		return nsInternalId;
	}
	public void setNsInternalId(String nsInternalId) {
		this.nsInternalId = nsInternalId;
	}
	public String getSKU() {
		return this.sku;
	}
	public void setSKU(String sku) {
		this.sku = sku;
	}
	public List<HouzzInventoryKitSubItemPojo> getSubItemsList() {
		return subItemsList;
	}
	public void setSubItemsList(List<HouzzInventoryKitSubItemPojo> subItemsList) {
		this.subItemsList = subItemsList;
	}	
	
	public double getQtyAvailable(){
		double[] arrayOfPotentialKits = new double[subItemsList.size()];
		int counter = 0;
		for (HouzzInventoryKitSubItemPojo subItem:subItemsList){
			Double potentialAvailableKitsForSubItem = subItem.getQtyAvailable()/subItem.getQtyInKit();
			arrayOfPotentialKits[counter] = potentialAvailableKitsForSubItem;
			counter++;
		}
		Arrays.sort(arrayOfPotentialKits);		
		return Math.floor(arrayOfPotentialKits[0]);
	}	
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public boolean isInactive() {
		return inactive;
	}
	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}
	public String getNsLocationId() {
		return nsLocationId;
	}
	public void setNsLocationId(String nsLocationId) {
		this.nsLocationId = nsLocationId;
	}
	public boolean isWrongConfigured() {
		return wrongConfigured;
	}
	public void setWrongConfigured(boolean wrongConfigured) {
		this.wrongConfigured = wrongConfigured;
	}
}
