package com.malkos.poppin.persistence.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="packages")
public class PackageDAO {
	@Id
	@GeneratedValue
    private int idPackage;	
    private String packageDescription;    
    private String packageTrackingNumber;
    private double packageWeight;    
    @ManyToOne
	@JoinColumn(name="purchaseOrderId")
	private PurchaseOrderDAO purchaseOrder; 

	public PurchaseOrderDAO getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(PurchaseOrderDAO purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}

	public String getPackageDescription() {
		return packageDescription;
	}

	public void setPackageDescription(String packageDescription) {
		this.packageDescription = packageDescription;
	}

	public String getPackageTrackingNumber() {
		return packageTrackingNumber;
	}

	public void setPackageTrackingNumber(String packageTrackingNumber) {
		this.packageTrackingNumber = packageTrackingNumber;
	}

	public double getPackageWeight() {
		return packageWeight;
	}

	public void setPackageWeight(double packageWeight) {
		this.packageWeight = packageWeight;
	}

	public int getIdPackage() {
		return idPackage;
	}

	public void setIdPackage(int idPackage) {
		this.idPackage = idPackage;
	}    
}
