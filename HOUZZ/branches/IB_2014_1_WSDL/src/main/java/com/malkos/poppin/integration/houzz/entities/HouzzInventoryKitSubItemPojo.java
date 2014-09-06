package com.malkos.poppin.integration.houzz.entities;

public class HouzzInventoryKitSubItemPojo extends InventoryPojo {
	private Double qtyInKit;
	private Double qtyAvailiable;
	private boolean isInactive;
	
	public Double getQtyInKit() {
		return qtyInKit;
	}

	public void setQtyInKit(Double qtyInKit) {
		this.qtyInKit = qtyInKit;
	}

	public Double getQtyAvailiable() {
		return qtyAvailiable;
	}

	public void setQtyAvailiable(Double qtyAvailiable) {
		this.qtyAvailiable = qtyAvailiable;
	}

	public boolean isInactive() {
		return isInactive;
	}

	public void setInactive(boolean isInactive) {
		this.isInactive = isInactive;
	}

}
