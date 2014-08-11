package com.malkos.poppin.entities;

import java.lang.reflect.Method;

public abstract class InventoryPojo {
	private String vendorSKU;	
	private String available;
	private String UPC;
	private String description;
	private String internalId;
	
	public String getVendorSKU() {
		return vendorSKU;
	}
	public void setVendorSKU(String vendorSKU) {
		this.vendorSKU = vendorSKU;
	}	
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	public String getUPC() {
		return UPC;
	}
	public void setUPC(String uPC) {
		UPC = uPC;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public abstract double getQtyAvailable();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			Class c = Class.forName(this.getClass().getName());
			Method m[] = c.getDeclaredMethods();
			Object oo;

			for (int i = 0; i < m.length; i++)
				if (m[i].getName().startsWith("get")) {
					oo = m[i].invoke(this, null);
					sb.append("[" + m[i].getName().substring(3) + "]: "
							+ String.valueOf(oo) + ", \n");
				}
			sb.append("\n\n\n");
		} catch (Throwable e) {
			System.err.println(e);
		}
		return sb.toString();
	}
	public String getInternalId() {
		return internalId;
	}
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

}
