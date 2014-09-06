package com.malkos.poppin.documents;

public class SpsStaplesCrossDockPoDocument extends SpsPoDocument {
	private static final String CARRIER_TRANS_METHOD_CODE ="M";
	private static final String CARRIER_ROUTING = "TEST";
	private static final String CARRIER_EQUIPMENT_NUMBER="000";
	private static final String FOB_PAY_CODE = "PP";
	private static final String FOB_LOCATION_QUALIFIER = "DE";
	private static final String FOB_LOCATION_DESCRIPTION = "PREPAID";
	private static final String SHIPMENT_QTY_PACKING_CODE = "CTN";
	private static final String CARRIER_ALPHA_CODE_UPSN = "UPSN";
	private static final String CURRENT_SCHEDULED_DELIVERY_TIME="12:00:00";
	private static final String TERMS_BASIS_DATE_CODE = "3";
	private static final String TERMS_DISCOUNT_PERCENTAGE = "0";
	private static final String TERMS_DISCOUNT_DUE_DATE = "0";
	private static final String TERMS_DISCOUNT_AMOUNT = "0";
	private static final String TOTAL_QUANTITY_INVOICED_UOM ="CA";
	
	private static final String CONTACT_TYPE_CODE ="CR";
	private static final String CONTACT_NAME ="Poppin Work Stylist";
	private static final String CONTACT_PHONE ="8886767746";
	private static final String CONTACT_FAX ="1234567890";
	private static final String CONTACT_EMAIL ="tech@poppin.com";
	
	@Override
	public String getCarrierTransMethodCode() {
		return CARRIER_TRANS_METHOD_CODE;
	}
	
	@Override
	public String getCarrierRouting(){
		return CARRIER_ROUTING;
	}
	
	@Override
	public String getFOBPayCode() {
		return FOB_PAY_CODE;
	}
	
	@Override
	public String getFOBLocationQualifier() {
		return FOB_LOCATION_QUALIFIER;
	}
	
	@Override
	public String getFOBLocationDescription() {
		return FOB_LOCATION_DESCRIPTION;
	}	
	@Override
	public String getShipmentQtyPackingCode(){
		return SHIPMENT_QTY_PACKING_CODE;		
	}

	@Override
	public String getTermsBasisDateCode(){
		return TERMS_BASIS_DATE_CODE;
	}
	@Override
	public String getTermsDiscountPercentage(){
		return TERMS_DISCOUNT_PERCENTAGE;
	}
	@Override
	public String getTermsDiscountDueDays() {
		return TERMS_DISCOUNT_DUE_DATE;
	}
	@Override
	public String getTermsDiscountAmount() {
		return TERMS_DISCOUNT_AMOUNT;
	}
	public String getCarrierAlphaCodeUpsn() {
		return CARRIER_ALPHA_CODE_UPSN;
	}
	@Override
	public String getCurrentSchedulledDeliveryTime() {
		return CURRENT_SCHEDULED_DELIVERY_TIME;
	}
	@Override
	public String getTotalQuantityInvoicedUom() {
		return TOTAL_QUANTITY_INVOICED_UOM;
	}
	@Override
	public String getHeaderContactTypeCode() {
		return CONTACT_TYPE_CODE;
	}
	@Override
	public String getHeaderContactName() {
		return CONTACT_NAME;
	}
	@Override
	public String getHeaderContactPhone() {
		return CONTACT_PHONE;
	}
	@Override
	public String getHeaderContactFax() {
		return CONTACT_FAX;
	}
	@Override
	public String getHeaderContactEmail() {
		return CONTACT_EMAIL;
	}
	@Override
	public String getCarrierEquipmentNumber() {
		return CARRIER_EQUIPMENT_NUMBER;
	}
}
