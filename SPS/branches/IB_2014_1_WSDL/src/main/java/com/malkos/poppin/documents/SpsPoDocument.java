package com.malkos.poppin.documents;

import java.util.ArrayList;
import java.util.List;

import com.malkos.poppin.entities.ChargesAllowancesPojo;
import com.malkos.poppin.entities.SPSOrderDatePojo;
import com.malkos.poppin.entities.SpsDocumentItemPojo;
import com.malkos.poppin.entities.SpsNotePojo;
import com.malkos.poppin.entities.SpsReferencePojo;


public class SpsPoDocument {
	
	public SpsPoDocument(){
		poDocumentItemPojoList = new ArrayList<>();
		spsOrderDatesPojoList = new ArrayList<>();
		spsReferencePojoList = new ArrayList<>();
		spsHeaderChargesAllowancesPojoList = new ArrayList<>();
		spsHeaderNotePojoList = new ArrayList<>();
	}
	
	private List<SpsDocumentItemPojo> poDocumentItemPojoList;
	private List<SPSOrderDatePojo> spsOrderDatesPojoList;
	private List<SpsReferencePojo> spsReferencePojoList;
	private List<ChargesAllowancesPojo> spsHeaderChargesAllowancesPojoList;
	private List<SpsNotePojo> spsHeaderNotePojoList;
	
	private static final String EQUIPMENT_DESCRIPTION_CODE = "TL";
	private static final String ASN_STRUCTURE_CODE = "0001";
	private static final String SHIPMENT_QTY_PACKING_CODE = "CTN25";
	private static final String SHIPMENT_LADING_QUANTITY = "1";
	private static final String GROSS_WEIGHT_QUALIFIER = "POUNDS";
	private static final String SHIPMENT_WEIGHT_UM = "LB";
	private static final String EQUIPMENT_DESCRIPTION = "000";	
	private static final String SHIPMENT_ORDER_STATUS_CODE = "pendingBilling";
	private static final String CURRENT_SCHEDULLED_DELIVERY_TIME = "04:00:00";
	private static final String CARRIER_ALPHA_CODE = "UPSG";
	private static final String FOB_TITLE_PASSAGE_CODE = "OR";
	private static final String FOB_TITLE_PASSAGE_LOCATION = "Poppin Warehouse (NJ)";
	private static final String BM_REFERENCE_QUAL = "BM";
	private static final String BM_REFERENCE_DESCRIPTION = "Tracking number";
	private static final String LOCATION_QUALIFIER = "16";
	private static final String SHIP_FROM_ADDRESS_NAME = "Poppin";
	private static final String SHIP_FROM_ADDRESS_1 = "c/o Dotcom Distribution 300 Nixon Ln";
	private static final String SHIP_FROM_CITY = "Edison";
	private static final String SHIP_FROM_STATE = "NJ";
	private static final String SHIP_FROM_POSTAL_CODE = "08837";
	private static final String SHIP_FROM_COUNTRY = "USA";
	private static final String PACK_LEVEL_TYPE = "P";
	private static final String MARKS_AND_NUMBERS_QUALIFIER_1 = "GM";
	private static final String MARKS_AND_NUMBERS_1 = "1234567890";
	private static final String INVOICE_TYPE_CODE = "DR";
	private static final String BUYERS_CURRENCY = "USD";
	private static final String TERMS_TYPE="14";
	private static final String TERMS_BASIS_DATE_CODE="2";
	private static final String TERMS_NET_DUE_DAYS ="90";
	private static final String TERMS_DESCRIPTION ="NET 90";
	private static final String TOTAL_QUANTITY_INVOICED_UOM ="CA";
	private static final String TOTAL_TERMS_DISCOUNT_AMOUNT ="0.00";
	private static final String ASN_CARRIER_EQUIPMENT_NUMBER = "000";
	private static final String INVOICE_NUMBER = "123";
	private static final String ORDER_WEIGHT_UOM = "LB";
	private static final String INNER_PACK_QTY = "1";
	private static final String MARKS_AND_NUMBERS_QUALIFIER_2 = "CP";
	private static final String ITEM_STATUS_CODE = "SHIPPED";
	private static final String INVOICE_DATE_TIME_QUALIFIER_1 = "011";
	private static final String TAX_TYPE_CODE = "LS";
	private static final String JURISDICTION_QUAL = "SP";
	//private static final String JURISDICTION_CODE = "testJurisdictionCode";
	private static final String JURISDICTION_CODE = " ";
	private static final String TAX_EXEMPT_CODE ="TAX";
	private static final String TAX_ID ="testTaxID";
	private static final String APPOINTMENT_NUMBER ="000";
	
	public static final String SHIP_TO_ADDRESS_TYPE_CODE = "ST";
	public static final String SHIP_FROM_ADDRESS_TYPE_CODE = "SF";
	public static final String BILL_TO_ADDRESS_TYPE_CODE = "BT";
	public static final String VN_ADDRESS_TYPE_CODE = "VN";
	public static final String SHIPMENT_ORDER_STATUS_CODE_PARTIAL ="PR";
	public static final String SHIPMENT_ORDER_STATUS_CODE_COMPLETE = "CL";
	public static final String SHIP_DATE_DATETIME_QUALIFIER = "370";
	//Meta
	private String senderUniqueID;
	private String senderCompanyName;
	private String receiverUniqueID;
	private String receiverCompanyName;
	private String isDropShip;
	private String interchangeControlNumber;
	private String groupControlIdentifier;
	private String groupControlNumber;
	private String documentControlIdentifier;
	private String documentControlNumber;
	private String interchangeSenderID;
	private String interchangeReceiverID;
	private String groupSenderID;
	private String groupReceiverID;
	private String batchPart;
	private String batchTotal;
	private String batchID;
	private String comments;
	private String validation;
	private String orderManagement;
	private String version;
	
	//OrderHeader
	private String tradingPartnerId;
	private String poNumber;
	private String tsetPurposeCode;
	private String vendor;
	private String carrierEquipmentInitial;
	private String carrierEquipmentNumber;
	private String CarrierTransMethodCode;
	private String CarrierRouting;
	private String FOBPayCode;
	private String FOBLocationQualifier;
	private String FOBLocationDescription;
	private String promotionDealNumber;	
	private String termsDiscountPercentage;
	private String termsDiscountDate;
	private String termsDiscountDueDays;
	private String termsDiscountAmount;
	private String releaseNumber;	
	//headerContact
	private String headerContactTypeCode;
	private String headerContactName;
	private String headerContactPhone;
	private String headerContactFax;
	private String headerContactEmail;
	private String headerContactReference;
	//headerContact
	
	//headerContact ST
	private String shipToContactTypeCode;
	private String shipToContactName;
	private String shipToContactPhone;
	private String shipToContactFax;
	private String shipToContactEmail;
	private String shipToContactReference;
	//headerContact ST
	
	//header address ST
	private String addressAlternateName;
	private String addressAlternateName2;
	private String address2;
	private String address3;
	private String address4;
	private String locationID;
	private String CountrySubDivision;
	private String AddressTaxIdNumber;
	private String AddressTaxExemptNumber;
	
	//header address ST	
	
	
	
	private String department;
	private String customerAccountNumber;
	private String customerOrderNumber;	
	
	/**
	 * @return the senderCompanyName
	 */
	public String getSenderCompanyName() {
		return this.senderCompanyName;
	}
	/**
	 * @param senderCompanyName the senderCompanyName to set
	 */
	public void setSenderCompanyName(String senderCompanyName) {
		this.senderCompanyName = senderCompanyName;
	}
	/**
	 * @return the senderUniqueID
	 */
	public String getSenderUniqueID() {
		return senderUniqueID;
	}
	/**
	 * @param senderUniqueID the senderUniqueID to set
	 */
	public void setSenderUniqueID(String senderUniqueID) {
		this.senderUniqueID = senderUniqueID;
	}
	public String getReceiverUniqueID() {
		return receiverUniqueID;
	}
	public void setReceiverUniqueID(String receiverUniqueID) {
		this.receiverUniqueID = receiverUniqueID;
	}
	public String getReceiverCompanyName() {
		return receiverCompanyName;
	}
	public void setReceiverCompanyName(String receiverCompanyName) {
		this.receiverCompanyName = receiverCompanyName;
	}
	public String getIsDropShip() {
		return isDropShip;
	}
	public void setIsDropShip(String isDropShip) {
		this.isDropShip = isDropShip;
	}
	public String getInterchangeControlNumber() {
		return interchangeControlNumber;
	}
	public void setInterchangeControlNumber(String interchangeControlNumber) {
		this.interchangeControlNumber = interchangeControlNumber;
	}
	public String getGroupControlIdentifier() {
		return groupControlIdentifier;
	}
	public void setGroupControlIdentifier(String groupControlIdentifier) {
		this.groupControlIdentifier = groupControlIdentifier;
	}
	public String getGroupControlNumber() {
		return groupControlNumber;
	}
	public void setGroupControlNumber(String groupControlNumber) {
		this.groupControlNumber = groupControlNumber;
	}
	public String getDocumentControlIdentifier() {
		return documentControlIdentifier;
	}
	public void setDocumentControlIdentifier(String documentControlIdentifier) {
		this.documentControlIdentifier = documentControlIdentifier;
	}
	public String getDocumentControlNumber() {
		return documentControlNumber;
	}
	public void setDocumentControlNumber(String documentControlNumber) {
		this.documentControlNumber = documentControlNumber;
	}
	public String getInterchangeSenderID() {
		return interchangeSenderID;
	}
	public void setInterchangeSenderID(String interchangeSenderID) {
		this.interchangeSenderID = interchangeSenderID;
	}
	public String getInterchangeReceiverID() {
		return interchangeReceiverID;
	}
	public void setInterchangeReceiverID(String interchangeReceiverID) {
		this.interchangeReceiverID = interchangeReceiverID;
	}
	public String getGroupSenderID() {
		return groupSenderID;
	}
	public void setGroupSenderID(String groupSenderID) {
		this.groupSenderID = groupSenderID;
	}
	public String getGroupReceiverID() {
		return groupReceiverID;
	}
	public void setGroupReceiverID(String groupReceiverID) {
		this.groupReceiverID = groupReceiverID;
	}
	public String getBatchPart() {
		return batchPart;
	}
	public void setBatchPart(String batchPart) {
		this.batchPart = batchPart;
	}
	public String getBatchTotal() {
		return batchTotal;
	}
	public void setBatchTotal(String batchTotal) {
		this.batchTotal = batchTotal;
	}
	public String getBatchID() {
		return batchID;
	}
	public void setBatchID(String batchID) {
		this.batchID = batchID;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getValidation() {
		return validation;
	}
	public void setValidation(String validation) {
		this.validation = validation;
	}
	public String getOrderManagement() {
		return orderManagement;
	}
	public void setOrderManagement(String orderManagement) {
		this.orderManagement = orderManagement;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the tsetPurposeCode
	 */
	public String getTsetPurposeCode() {
		return tsetPurposeCode;
	}
	/**
	 * @param tsetPurposeCode the tsetPurposeCode to set
	 */
	public void setTsetPurposeCode(String tsetPurposeCode) {
		this.tsetPurposeCode = tsetPurposeCode;
	}
	/**
	 * @return the tradingPartnerId
	 */
	public String getTradingPartnerId() {
		return tradingPartnerId;
	}
	/**
	 * @param tradingPartnerId the tradingPartnerId to set
	 */
	public void setTradingPartnerId(String tradingPartnerId) {
		this.tradingPartnerId = tradingPartnerId;
	}
	/**
	 * @return the poNumber
	 */
	public String getPoNumber() {
		return poNumber;
	}
	/**
	 * @param poNumber the poNumber to set
	 */
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	/**
	 * @return the poDocumentItemPojoList
	 */
	public List<SpsDocumentItemPojo> getPoDocumentItemPojoList() {
		return poDocumentItemPojoList;
	}
	/**
	 * @param poDocumentItemPojoList the poDocumentItemPojoList to set
	 */
	public void setPoDocumentItemPojoList(List<SpsDocumentItemPojo> poDocumentItemPojoList) {
		this.poDocumentItemPojoList = poDocumentItemPojoList;
	}
	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}
	/**
	 * @param vendor the vendor to set
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	/**
	 * @return the carrierEquipmentInitial
	 */
	public String getCarrierEquipmentInitial() {
		return carrierEquipmentInitial;
	}
	/**
	 * @param carrierEquipmentInitial the carrierEquipmentInitial to set
	 */
	public void setCarrierEquipmentInitial(String carrierEquipmentInitial) {
		this.carrierEquipmentInitial = carrierEquipmentInitial;
	}
	/**
	 * @return the carrierEquipmentNumber
	 */
	public String getCarrierEquipmentNumber() {
		return carrierEquipmentNumber;
	}
	/**
	 * @param carrierEquipmentNumber the carrierEquipmentNumber to set
	 */
	public void setCarrierEquipmentNumber(String carrierEquipmentNumber) {
		this.carrierEquipmentNumber = carrierEquipmentNumber;
	}
	/**
	 * @return the carrierTransMethodCode
	 */
	public String getCarrierTransMethodCode() {
		return CarrierTransMethodCode;
	}
	/**
	 * @param carrierTransMethodCode the carrierTransMethodCode to set
	 */
	public void setCarrierTransMethodCode(String carrierTransMethodCode) {
		CarrierTransMethodCode = carrierTransMethodCode;
	}
	/**
	 * @return the carrierRouting
	 */
	public String getCarrierRouting() {
		return CarrierRouting;
	}
	/**
	 * @param carrierRouting the carrierRouting to set
	 */
	public void setCarrierRouting(String carrierRouting) {
		CarrierRouting = carrierRouting;
	}
	/**
	 * @return the fOBPayCode
	 */
	public String getFOBPayCode() {
		return FOBPayCode;
	}
	/**
	 * @param fOBPayCode the fOBPayCode to set
	 */
	public void setFOBPayCode(String fOBPayCode) {
		FOBPayCode = fOBPayCode;
	}
	/**
	 * @return the fOBLocationQualifier
	 */
	public String getFOBLocationQualifier() {
		return FOBLocationQualifier;
	}
	/**
	 * @param fOBLocationQualifier the fOBLocationQualifier to set
	 */
	public void setFOBLocationQualifier(String fOBLocationQualifier) {
		FOBLocationQualifier = fOBLocationQualifier;
	}
	/**
	 * @return the fOBLocationDescription
	 */
	public String getFOBLocationDescription() {
		return FOBLocationDescription;
	}
	/**
	 * @param fOBLocationDescription the fOBLocationDescription to set
	 */
	public void setFOBLocationDescription(String fOBLocationDescription) {
		FOBLocationDescription = fOBLocationDescription;
	}
	/**
	 * @return the equipmentDescriptionCode
	 */
	public String getEquipmentDescriptionCode() {
		return EQUIPMENT_DESCRIPTION_CODE;
	}
	/**
	 * @return the asnStructureCode
	 */
	public String getAsnStructureCode() {
		return ASN_STRUCTURE_CODE;
	}
	/**
	 * @return the shipmentQtyPackingCode
	 */
	public String getShipmentQtyPackingCode() {
		return SHIPMENT_QTY_PACKING_CODE;
	}
	/**
	 * @return the shipmentLadingQuantity
	 */
	public String getShipmentLadingQuantity() {
		return SHIPMENT_LADING_QUANTITY;
	}
	/**
	 * @return the grossWeightQualifier
	 */
	public String getGrossWeightQualifier() {
		return GROSS_WEIGHT_QUALIFIER;
	}
	/**
	 * @return the shipmentWeightUm
	 */
	public String getShipmentWeightUm() {
		return SHIPMENT_WEIGHT_UM;
	}
	/**
	 * @return the equipmentDescription
	 */
	public String getEquipmentDescription() {
		return EQUIPMENT_DESCRIPTION;
	}
	/**
	 * @return the shipmentStatusCode
	 */
	public String getShipmentOrderStatusCode() {
		return SHIPMENT_ORDER_STATUS_CODE;
	}
	/**
	 * @return the currentSchedulledDeliveryTime
	 */
	public String getCurrentSchedulledDeliveryTime() {
		return CURRENT_SCHEDULLED_DELIVERY_TIME;
	}
	/**
	 * @return the fobTitlePassageCode
	 */
	public String getFobTitlePassageCode() {
		return FOB_TITLE_PASSAGE_CODE;
	}
	/**
	 * @return the fobTitlePassageLocation
	 */
	public String getFobTitlePassageLocation() {
		return FOB_TITLE_PASSAGE_LOCATION;
	}
	/**
	 * @return the carrierAlphaCode
	 */
	public String getCarrierAlphaCode() {
		return CARRIER_ALPHA_CODE;
	}
	/**
	 * @return the spsOrderDatesPojoList
	 */
	public List<SPSOrderDatePojo> getSpsOrderDatesPojoList() {
		return spsOrderDatesPojoList;
	}
	/**
	 * @param spsOrderDatesPojoList the spsOrderDatesPojoList to set
	 */
	public void setSpsOrderDatesPojoList(List<SPSOrderDatePojo> spsOrderDatesPojoList) {
		this.spsOrderDatesPojoList = spsOrderDatesPojoList;
	}
	/**
	 * @return the spsReferencePojoList
	 */
	public List<SpsReferencePojo> getSpsReferencePojoList() {
		return spsReferencePojoList;
	}
	/**
	 * @param spsReferencePojoList the spsReferencePojoList to set
	 */
	public void setSpsReferencePojoList(List<SpsReferencePojo> spsReferencePojoList) {
		this.spsReferencePojoList = spsReferencePojoList;
	}
	/**
	 * @return the bmReferenceQual
	 */
	public String getBmReferenceQual() {
		return BM_REFERENCE_QUAL;
	}
	public String getBmReferenceDescription() {
		return BM_REFERENCE_DESCRIPTION;
	}
	public String getHeaderContactTypeCode() {
		return headerContactTypeCode;
	}
	public void setHeaderContactTypeCode(String headerContactTypeCode) {
		this.headerContactTypeCode = headerContactTypeCode;
	}
	public String getHeaderContactName() {
		return headerContactName;
	}
	public void setHeaderContactName(String headerContactName) {
		this.headerContactName = headerContactName;
	}
	public String getHeaderContactPhone() {
		return headerContactPhone;
	}
	public void setHeaderContactPhone(String headerContactPhone) {
		this.headerContactPhone = headerContactPhone;
	}
	public String getHeaderContactFax() {
		return headerContactFax;
	}
	public void setHeaderContactFax(String headerContactFax) {
		this.headerContactFax = headerContactFax;
	}
	public String getHeaderContactEmail() {
		return headerContactEmail;
	}
	public void setHeaderContactEmail(String headerContactEmail) {
		this.headerContactEmail = headerContactEmail;
	}
	public String getHeaderContactReference() {
		return headerContactReference;
	}
	public void setHeaderContactReference(String headerContactReference) {
		this.headerContactReference = headerContactReference;
	}
	public String getShipToContactTypeCode() {
		return shipToContactTypeCode;
	}
	public void setShipToContactTypeCode(String shipToContactTypeCode) {
		this.shipToContactTypeCode = shipToContactTypeCode;
	}
	public String getShipToContactName() {
		return shipToContactName;
	}
	public void setShipToContactName(String shipToContactName) {
		this.shipToContactName = shipToContactName;
	}
	public String getShipToContactPhone() {
		return shipToContactPhone;
	}
	public void setShipToContactPhone(String shipToContactPhone) {
		this.shipToContactPhone = shipToContactPhone;
	}
	public String getShipToContactFax() {
		return shipToContactFax;
	}
	public void setShipToContactFax(String shipToContactFax) {
		this.shipToContactFax = shipToContactFax;
	}
	public String getShipToContactEmail() {
		return shipToContactEmail;
	}
	public void setShipToContactEmail(String shipToContactEmail) {
		this.shipToContactEmail = shipToContactEmail;
	}
	public String getShipToContactReference() {
		return shipToContactReference;
	}
	public void setShipToContactReference(String shipToContactReference) {
		this.shipToContactReference = shipToContactReference;
	}
	public String getAddressAlternateName() {
		return addressAlternateName;
	}
	public void setAddressAlternateName(String addressAlternateName) {
		this.addressAlternateName = addressAlternateName;
	}
	public String getAddressAlternateName2() {
		return addressAlternateName2;
	}
	public void setAddressAlternateName2(String addressAlternateName2) {
		this.addressAlternateName2 = addressAlternateName2;
	}
	public String getAddress3() {
		return address3;
	}
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	public String getAddress4() {
		return address4;
	}
	public void setAddress4(String address4) {
		this.address4 = address4;
	}
	public String getLocationID() {
		return locationID;
	}
	public void setLocationID(String locationID) {
		this.locationID = locationID;
	}
	public String getCountrySubDivision() {
		return CountrySubDivision;
	}
	public void setCountrySubDivision(String countrySubDivision) {
		CountrySubDivision = countrySubDivision;
	}
	public String getAddressTaxIdNumber() {
		return AddressTaxIdNumber;
	}
	public void setAddressTaxIdNumber(String addressTaxIdNumber) {
		AddressTaxIdNumber = addressTaxIdNumber;
	}
	public String getAddressTaxExemptNumber() {
		return AddressTaxExemptNumber;
	}
	public void setAddressTaxExemptNumber(String addressTaxExemptNumber) {
		AddressTaxExemptNumber = addressTaxExemptNumber;
	}
	public String getLocationQualifier() {
		return LOCATION_QUALIFIER;
	}
	public String getShipFromAddressName() {
		return SHIP_FROM_ADDRESS_NAME;
	}
	public String getShipFromAddress1() {
		return SHIP_FROM_ADDRESS_1;
	}
	public String getShipFromCity() {
		return SHIP_FROM_CITY;
	}
	public String getShipFromState() {
		return SHIP_FROM_STATE;
	}
	public String getShipFromPostalCode() {
		return SHIP_FROM_POSTAL_CODE;
	}
	public String getShipFromCountry() {
		return SHIP_FROM_COUNTRY;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getCustomerAccountNumber() {
		return customerAccountNumber;
	}
	public void setCustomerAccountNumber(String customerAccountNumber) {
		this.customerAccountNumber = customerAccountNumber;
	}
	public String getCustomerOrderNumber() {
		return customerOrderNumber;
	}
	public void setCustomerOrderNumber(String customerOrderNumber) {
		this.customerOrderNumber = customerOrderNumber;
	}
	public String getPackLevelType() {
		return PACK_LEVEL_TYPE;
	}
	public String getMarksAndNumbersQualifier1() {
		return MARKS_AND_NUMBERS_QUALIFIER_1;
	}
	public String getInvoiceTypeCode() {
		return INVOICE_TYPE_CODE;
	}
	public String getBuyersCurrency() {
		return BUYERS_CURRENCY;
	}
	public String getPromotionDealNumber() {
		return promotionDealNumber;
	}
	public void setPromotionDealNumber(String promotionDealNumber) {
		this.promotionDealNumber = promotionDealNumber;
	}
	public String getTermsType() {
		return TERMS_TYPE;
	}
	public String getTermsBasisDateCode() {
		return TERMS_BASIS_DATE_CODE;
	}
	public String getTermsNetDueDays() {
		return TERMS_NET_DUE_DAYS;
	}
	public String getTermsDescription() {
		return TERMS_DESCRIPTION;
	}
	public String getTermsDiscountPercentage() {
		return termsDiscountPercentage;
	}
	public void setTermsDiscountPercentage(String termsDiscountPercentage) {
		this.termsDiscountPercentage = termsDiscountPercentage;
	}
	public String getTermsDiscountDate() {
		return termsDiscountDate;
	}
	public void setTermsDiscountDate(String termsDiscountDate) {
		this.termsDiscountDate = termsDiscountDate;
	}
	public String getTermsDiscountDueDays() {
		return termsDiscountDueDays;
	}
	public void setTermsDiscountDueDays(String termsDiscoutntDueDays) {
		this.termsDiscountDueDays = termsDiscoutntDueDays;
	}
	public String getTermsDiscountAmount() {
		return termsDiscountAmount;
	}
	public void setTermsDiscountAmount(String termsDiscountAmount) {
		this.termsDiscountAmount = termsDiscountAmount;
	}
	public String getTotalQuantityInvoicedUom() {
		return TOTAL_QUANTITY_INVOICED_UOM;
	}
	public String getTotalTermsDiscountAmount() {
		return TOTAL_TERMS_DISCOUNT_AMOUNT;
	}
	public String getAsnCarrierEquipmentNumber() {
		return ASN_CARRIER_EQUIPMENT_NUMBER;
	}
	public String getInvoiceNumber() {
		return INVOICE_NUMBER;
	}
	public String getReleaseNumber() {
		return releaseNumber;
	}
	public void setReleaseNumber(String releaseNumber) {
		this.releaseNumber = releaseNumber;
	}
	public String getOrderWeightUom() {
		return ORDER_WEIGHT_UOM;
	}
	public String getInnerPackQty() {
		return INNER_PACK_QTY;
	}
	public String getMarksAndNumbersQualifier2() {
		return MARKS_AND_NUMBERS_QUALIFIER_2;
	}
	public String getMarksAndNumbers1() {
		return MARKS_AND_NUMBERS_1;
	}
	public String getItemStatusCode() {
		return ITEM_STATUS_CODE;
	}
	public String getInvoiceDateTimeQualifier1() {
		return INVOICE_DATE_TIME_QUALIFIER_1;
	}	
	public String getTaxTypeCode() {
		return TAX_TYPE_CODE;
	}
	public String getJurisdictionQual() {
		return JURISDICTION_QUAL;
	}
	public String getJurisdictionCode() {
		return JURISDICTION_CODE;
	}
	public String getTaxExemptCode() {
		return TAX_EXEMPT_CODE;
	}
	public String getTaxId() {
		return TAX_ID;
	}
	public String getAppointmentNumber() {
		return APPOINTMENT_NUMBER;
	}
	public List<ChargesAllowancesPojo> getSpsHeaderChargesAllowancesPojoList() {
		return spsHeaderChargesAllowancesPojoList;
	}
	public void setSpsHeaderChargesAllowancesPojoList(
			List<ChargesAllowancesPojo> spsHeaderChargesAllowancesPojoList) {
		this.spsHeaderChargesAllowancesPojoList = spsHeaderChargesAllowancesPojoList;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getAddress2() {
		return this.address2;
	}
	public List<SpsNotePojo> getSpsHeaderNotePojoList() {
		return spsHeaderNotePojoList;
	}
	public void setSpsHeaderNotePojoList(List<SpsNotePojo> spsHeaderNotePojoList) {
		this.spsHeaderNotePojoList = spsHeaderNotePojoList;
	}		
}
