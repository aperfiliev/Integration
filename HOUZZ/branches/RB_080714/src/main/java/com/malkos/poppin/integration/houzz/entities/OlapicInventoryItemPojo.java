package com.malkos.poppin.integration.houzz.entities;

public class OlapicInventoryItemPojo extends InventoryItemPojo{
	private String displayName;
	private String itemUrl;	
	private String itemDisplayImage;
	
	public String getItemDisplayImage() {
		return itemDisplayImage;
	}
	public void setItemDisplayImage(String itemDisplayImage) {
		this.itemDisplayImage = itemDisplayImage;
	}	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getItemUrl() {
		return itemUrl;
	}
	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}
}
