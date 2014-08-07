package com.malkos.poppin.documents;

public class SpsIndigoBulkImportPoDocument extends SpsStaplesBulkImportPoDocument{
	
	private static final String TERMS_NET_DUE_DAYS ="45";
	private static final String TERMS_DESCRIPTION ="NET 45";
	private static final String LOCATION_QUALIFIER="15";
	private static final String VN_LOCATION_CODE_QUALIFIER = "1";
	private static final String SHIP_FROM_LOCATION_CODE_QUALIFIER = "1";
	private static final String SHIP_FROM_LOCATION_NUMBER = "053722230";
	
	@Override
	public String getTermsNetDueDays() {
		return TERMS_NET_DUE_DAYS;
	}
	@Override
	public String getTermsDescription() {
		return TERMS_DESCRIPTION;
	}
	@Override
	public String getLocationQualifier() {
		return LOCATION_QUALIFIER;
	}	
	public String getVnLocationQualifier() {
		return VN_LOCATION_CODE_QUALIFIER;
	}
	public String getShipFromLocationCodeQualifier() {
		return SHIP_FROM_LOCATION_CODE_QUALIFIER;
	}
	public String getShipFromAdrressLocationNumber() {
		return SHIP_FROM_LOCATION_NUMBER;
	}
}
