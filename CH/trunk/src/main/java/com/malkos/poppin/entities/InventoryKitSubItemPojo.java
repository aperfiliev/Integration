package com.malkos.poppin.entities;

public class InventoryKitSubItemPojo extends InventoryPojo {
	private double qtyInKit;
	private double qtyAvailiable;
	private boolean isInactive;
	
	public Double getQtyInKit() {
		return qtyInKit;
	}

	public void setQtyInKit(Double qtyInKit) {
		this.qtyInKit = qtyInKit;
	}	

	public void setQtyAvailiable(double qtyAvailiable) {
		this.qtyAvailiable = qtyAvailiable;
	}

	public boolean isInactive() {
		return isInactive;
	}

	public void setInactive(boolean isInactive) {
		this.isInactive = isInactive;
	}

	@Override
	public double getQtyAvailable() {
		return qtyAvailiable;
	}	
}
