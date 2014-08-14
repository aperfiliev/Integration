package com.malkos.poppin.entities;

import java.util.ArrayList;
import java.util.List;

public class SpsDocumentItemPojo {
	private String vendorLineNumber;
	private String buyerPartNumber;
	private String vendorPartNumber;
	private String EAN;
	private String GTIN;
	private String UPCCaseCode;
	private String natlDrugCode;
	private String internationalStandardBookNumber;
	private String partNumberQual;
	private String partNumber;
	private String orderQtyUOM;
	private String shipQtyUOM;
	private String productSizeCode;
	private String productColorCode;
	private String productSizeDescription;
	private String productColorDescription;
	private String productFabricDescription;
	private String NRFColorCode;
	private String NRFSizeCode;
	private String itemDescriptionType;
	private String productDescription;
	
	private String itemReferenceQual;
	private String itemReferenceId;
	private String itemReferenceDescription;
	
	private List<ChargesAllowancesPojo> spsItemChargesAllowancesList;
	private List<SPSOrderDatePojo> spsItemDatesList;	
	private List<SpsDocumentSubLineItemPojo> spsDocumentSubLinesList;
	
	public SpsDocumentItemPojo(){
		spsItemDatesList = new ArrayList<>();
		spsItemChargesAllowancesList = new ArrayList<>();
		spsDocumentSubLinesList= new ArrayList<>();
	}
	
	public String getBuyerPartNumber() {
		return buyerPartNumber;
	}

	public void setBuyerPartNumber(String buyerPartNumber) {
		this.buyerPartNumber = buyerPartNumber;
	}

	public String getVendorPartNumber() {
		return vendorPartNumber;
	}

	public void setVendorPartNumber(String vendorPartNumber) {
		this.vendorPartNumber = vendorPartNumber;
	}

	public String getEAN() {
		return EAN;
	}

	public void setEAN(String eAN) {
		EAN = eAN;
	}

	public String getGTIN() {
		return GTIN;
	}

	public void setGTIN(String gTIN) {
		GTIN = gTIN;
	}

	public String getUPCCaseCode() {
		return UPCCaseCode;
	}

	public void setUPCCaseCode(String uPCCaseCode) {
		UPCCaseCode = uPCCaseCode;
	}

	public String getNatlDrugCode() {
		return natlDrugCode;
	}

	public void setNatlDrugCode(String natlDrugCode) {
		this.natlDrugCode = natlDrugCode;
	}

	public String getInternationalStandardBookNumber() {
		return internationalStandardBookNumber;
	}

	public void setInternationalStandardBookNumber(
			String internationalStandardBookNumber) {
		this.internationalStandardBookNumber = internationalStandardBookNumber;
	}

	public String getPartNumberQual() {
		return partNumberQual;
	}

	public void setPartNumberQual(String partNumberQual) {
		this.partNumberQual = partNumberQual;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getOrderQtyUOM() {
		return orderQtyUOM;
	}

	public void setOrderQtyUOM(String orderQtyUOM) {
		this.orderQtyUOM = orderQtyUOM;
	}

	public String getShipQtyUOM() {
		return shipQtyUOM;
	}

	public void setShipQtyUOM(String shipQtyUOM) {
		this.shipQtyUOM = shipQtyUOM;
	}

	public String getProductSizeDescription() {
		return productSizeDescription;
	}

	public void setProductSizeDescription(String productSizeDescription) {
		this.productSizeDescription = productSizeDescription;
	}

	public String getProductColorDescription() {
		return productColorDescription;
	}

	public void setProductColorDescription(String productColorDescription) {
		this.productColorDescription = productColorDescription;
	}

	public String getProductFabricDescription() {
		return productFabricDescription;
	}

	public void setProductFabricDescription(String productFabricDescription) {
		this.productFabricDescription = productFabricDescription;
	}

	public String getNRFColorCode() {
		return NRFColorCode;
	}

	public void setNRFColorCode(String nRFColorCode) {
		NRFColorCode = nRFColorCode;
	}

	public String getNRFSizeCode() {
		return NRFSizeCode;
	}

	public void setNRFSizeCode(String nRFSizeCode) {
		NRFSizeCode = nRFSizeCode;
	}

	public String getItemDescriptionType() {
		return itemDescriptionType;
	}

	public void setItemDescriptionType(String itemDescriptionType) {
		this.itemDescriptionType = itemDescriptionType;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}	

	public String getVendorLineNumber() {
		return vendorLineNumber;
	}

	public void setVendorLineNumber(String vendorLineNumber) {
		this.vendorLineNumber = vendorLineNumber;
	}

	public String getItemReferenceQual() {
		return itemReferenceQual;
	}

	public void setItemReferenceQual(String itemReferenceQual) {
		this.itemReferenceQual = itemReferenceQual;
	}

	public String getItemReferenceId() {
		return itemReferenceId;
	}

	public void setItemReferenceId(String itemReferenceId) {
		this.itemReferenceId = itemReferenceId;
	}

	public String getItemDescription() {
		return itemReferenceDescription;
	}

	public void setItemReferenceDescription(String itemDescription) {
		this.itemReferenceDescription = itemDescription;
	}	

	public List<SPSOrderDatePojo> getSpsItemDatesList() {
		return spsItemDatesList;
	}

	public void setSpsItemDatesList(List<SPSOrderDatePojo> spsItemDatesList) {
		this.spsItemDatesList = spsItemDatesList;
	}

	public List<ChargesAllowancesPojo> getSpsItemChargesAllowancesList() {
		return spsItemChargesAllowancesList;
	}

	public void setSpsItemChargesAllowancesList(
			List<ChargesAllowancesPojo> spsItemChargesAllowancesList) {
		this.spsItemChargesAllowancesList = spsItemChargesAllowancesList;
	}

	public List<SpsDocumentSubLineItemPojo> getSpsDocumentSubLinesList() {
		return spsDocumentSubLinesList;
	}

	public void setSpsDocumentSubLinesList(List<SpsDocumentSubLineItemPojo> spsDocumentSubLinesList) {
		this.spsDocumentSubLinesList = spsDocumentSubLinesList;
	}

	public String getProductColorCode() {
		return productColorCode;
	}

	public void setProductColorCode(String productColorCode) {
		this.productColorCode = productColorCode;
	}

	public String getProductSizeCode() {
		return productSizeCode;
	}

	public void setProductSizeCode(String productSizeCode) {
		this.productSizeCode = productSizeCode;
	}
}
