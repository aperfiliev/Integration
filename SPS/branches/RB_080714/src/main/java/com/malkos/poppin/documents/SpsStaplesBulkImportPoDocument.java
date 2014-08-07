package com.malkos.poppin.documents;

public class SpsStaplesBulkImportPoDocument extends SpsStaplesCrossDockPoDocument{ 
	private static final String MR_REFERENCE_QUAL ="MR";
	private static final String MR_REFERENCE_ID = "RET";
	private static final String INVOICE_SHIPMENT_DATE_REFERENCE_QUALIFIER = "011";
	
	//headerContact BT
		private String billToContactTypeCode;
		private String billToContactName;
		private String billToContactPhone;
		private String billToContactFax;
		private String billToContactEmail;
		private String billToContactReference;
		//headerContact BT
		
		//header address BT		
		private String billToAddressAlternateName;
		private String billToAddressAlternateName2;
		private String billToAddressLocationNumber;		
		private String billToName;		
		private String billToCity;
		private String billToState;
		private String billToPostalCode;
		private String billToCountry;
		private String billToAddress1;
		private String billToAddress2;
		private String billToAddress3;
		private String billToAddress4;
		private String billToLocationID;
		private String billToCountrySubDivision;
		private String billToAddressTaxIdNumber;
		private String billToAddressTaxExemptNumber;
		
		//header address BT	
		
		//headerContact VN
		private String vnToContactTypeCode;
		private String vnToContactName;
		private String vnToContactPhone;
		private String vnToContactFax;
		private String vnToContactEmail;
		private String vnToContactReference;
		//headerContact VN
		
		//header address VN
		private String vnToAddressAlternateName;
		private String vnToAddressAlternateName2;
		private String vnToAddressLocationNumber;		
		private String vnToName;		
		private String vnToCity;
		private String vnToState;
		private String vnToPostalCode;
		private String vnToCountry;
		private String vnToAddress1;
		private String vnToAddress2;
		private String vnToAddress3;
		private String vnToAddress4;
		private String vnToLocationID;
		private String vnToCountrySubDivision;
		private String vnToAddressTaxIdNumber;
		private String vnToAddressTaxExemptNumber;
		
		//header address VN	
	
	public String getMrReferenceQual() {
		return MR_REFERENCE_QUAL;
	}
	
	public String getMrReferenceId() {
		return MR_REFERENCE_ID;
	}

	public String getInvoiceShipmentDateReferenceQualifier() {
		return INVOICE_SHIPMENT_DATE_REFERENCE_QUALIFIER;
	}
	public String getBillToContactTypeCode() {
		return billToContactTypeCode;
	}
	public void setBillToContactTypeCode(String billToContactTypeCode) {
		this.billToContactTypeCode = billToContactTypeCode;
	}
	public String getBillToContactName() {
		return billToContactName;
	}
	public void setBillToContactName(String billToContactName) {
		this.billToContactName = billToContactName;
	}
	public String getBillToContactPhone() {
		return billToContactPhone;
	}
	public void setBillToContactPhone(String billToContactPhone) {
		this.billToContactPhone = billToContactPhone;
	}
	public String getBillToContactFax() {
		return billToContactFax;
	}
	public void setBillToContactFax(String billToContactFax) {
		this.billToContactFax = billToContactFax;
	}
	public String getBillToContactEmail() {
		return billToContactEmail;
	}
	public void setBillToContactEmail(String billToContactEmail) {
		this.billToContactEmail = billToContactEmail;
	}
	public String getBillToContactReference() {
		return billToContactReference;
	}
	public void setBillToContactReference(String billToContactReference) {
		this.billToContactReference = billToContactReference;
	}
	public String getBillToAddressAlternateName() {
		return billToAddressAlternateName;
	}
	public void setBillToAddressAlternateName(String billToAddressAlternateName) {
		this.billToAddressAlternateName = billToAddressAlternateName;
	}
	public String getBillToAddressAlternateName2() {
		return billToAddressAlternateName2;
	}
	public void setBillToAddressAlternateName2(String billToAddressAlternateName2) {
		this.billToAddressAlternateName2 = billToAddressAlternateName2;
	}
	public String getBillToAddress3() {
		return billToAddress3;
	}
	public void setBillToAddress3(String billToAddress3) {
		this.billToAddress3 = billToAddress3;
	}
	public String getBillToAddress4() {
		return billToAddress4;
	}
	public void setBillToAddress4(String billToAddress4) {
		this.billToAddress4 = billToAddress4;
	}
	public String getBillToLocationID() {
		return billToLocationID;
	}
	public void setBillToLocationID(String billToLocationID) {
		this.billToLocationID = billToLocationID;
	}
	public String getBillToCountrySubDivision() {
		return billToCountrySubDivision;
	}
	public void setBillToCountrySubDivision(String billToCountrySubDivision) {
		this.billToCountrySubDivision = billToCountrySubDivision;
	}
	public String getBillToAddressTaxIdNumber() {
		return billToAddressTaxIdNumber;
	}
	public void setBillToAddressTaxIdNumber(String billToAddressTaxIdNumber) {
		this.billToAddressTaxIdNumber = billToAddressTaxIdNumber;
	}
	public String getBillToAddressTaxExemptNumber() {
		return billToAddressTaxExemptNumber;
	}
	public void setBillToAddressTaxExemptNumber(String billToAddressTaxExemptNumber) {
		this.billToAddressTaxExemptNumber = billToAddressTaxExemptNumber;
	}
	public String getVnToContactTypeCode() {
		return vnToContactTypeCode;
	}
	public void setVnToContactTypeCode(String vnToContactTypeCode) {
		this.vnToContactTypeCode = vnToContactTypeCode;
	}
	public String getVnToContactName() {
		return vnToContactName;
	}
	public void setVnToContactName(String vnToContactName) {
		this.vnToContactName = vnToContactName;
	}
	public String getVnToContactPhone() {
		return vnToContactPhone;
	}
	public void setVnToContactPhone(String vnToContactPhone) {
		this.vnToContactPhone = vnToContactPhone;
	}
	public String getVnToContactFax() {
		return vnToContactFax;
	}
	public void setVnToContactFax(String vnToContactFax) {
		this.vnToContactFax = vnToContactFax;
	}
	public String getVnToContactEmail() {
		return vnToContactEmail;
	}
	public void setVnToContactEmail(String vnToContactEmail) {
		this.vnToContactEmail = vnToContactEmail;
	}
	public String getVnToContactReference() {
		return vnToContactReference;
	}
	public void setVnToContactReference(String vnToContactReference) {
		this.vnToContactReference = vnToContactReference;
	}
	public String getVnToAddressAlternateName() {
		return vnToAddressAlternateName;
	}
	public void setVnToAddressAlternateName(String vnToAddressAlternateName) {
		this.vnToAddressAlternateName = vnToAddressAlternateName;
	}
	public String getVnToAddressAlternateName2() {
		return vnToAddressAlternateName2;
	}
	public void setVnToAddressAlternateName2(String vnToAddressAlternateName2) {
		this.vnToAddressAlternateName2 = vnToAddressAlternateName2;
	}
	public String getVnToAddress3() {
		return vnToAddress3;
	}
	public void setVnToAddress3(String vnToAddress3) {
		this.vnToAddress3 = vnToAddress3;
	}
	public String getVnToAddress4() {
		return vnToAddress4;
	}
	public void setVnToAddress4(String vnToAddress4) {
		this.vnToAddress4 = vnToAddress4;
	}
	public String getVnToLocationID() {
		return vnToLocationID;
	}
	public void setVnToLocationID(String vnToLocationID) {
		this.vnToLocationID = vnToLocationID;
	}
	public String getVnToCountrySubDivision() {
		return vnToCountrySubDivision;
	}
	public void setVnToCountrySubDivision(String vnToCountrySubDivision) {
		this.vnToCountrySubDivision = vnToCountrySubDivision;
	}
	public String getVnToAddressTaxIdNumber() {
		return vnToAddressTaxIdNumber;
	}
	public void setVnToAddressTaxIdNumber(String vnToAddressTaxIdNumber) {
		this.vnToAddressTaxIdNumber = vnToAddressTaxIdNumber;
	}
	public String getVnToAddressTaxExemptNumber() {
		return vnToAddressTaxExemptNumber;
	}
	public void setVnToAddressTaxExemptNumber(String vnToAddressTaxExemptNumber) {
		this.vnToAddressTaxExemptNumber = vnToAddressTaxExemptNumber;
	}
	public String getVnToAddress2() {
		return vnToAddress2;
	}
	public void setVnToAddress2(String vnToAddress2) {
		this.vnToAddress2 = vnToAddress2;
	}
	public String getBillToAddress2() {
		return billToAddress2;
	}
	public void setBillToAddress2(String billToAddress2) {
		this.billToAddress2 = billToAddress2;
	}
	public String getBillToAddressLocationNumber() {
		return billToAddressLocationNumber;
	}

	public void setBillToAddressLocationNumber(String billToAddressLocationNumber) {
		this.billToAddressLocationNumber = billToAddressLocationNumber;
	}

	public String getBillToName() {
		return billToName;
	}

	public void setBillToName(String billToName) {
		this.billToName = billToName;
	}

	public String getBillToCity() {
		return billToCity;
	}

	public void setBillToCity(String billToCity) {
		this.billToCity = billToCity;
	}

	public String getBillToState() {
		return billToState;
	}

	public void setBillToState(String billToState) {
		this.billToState = billToState;
	}

	public String getBillToPostalCode() {
		return billToPostalCode;
	}

	public void setBillToPostalCode(String billToPostalCode) {
		this.billToPostalCode = billToPostalCode;
	}

	public String getBillToCountry() {
		return billToCountry;
	}

	public void setBillToCountry(String billToCountry) {
		this.billToCountry = billToCountry;
	}

	public String getBillToAddress1() {
		return billToAddress1;
	}

	public void setBillToAddress1(String billToAddress1) {
		this.billToAddress1 = billToAddress1;
	}

	public String getVnToAddressLocationNumber() {
		return vnToAddressLocationNumber;
	}

	public void setVnToAddressLocationNumber(String vnToAddressLocationNumber) {
		this.vnToAddressLocationNumber = vnToAddressLocationNumber;
	}

	public String getVnToName() {
		return vnToName;
	}

	public void setVnToName(String vnToName) {
		this.vnToName = vnToName;
	}

	public String getVnToCity() {
		return vnToCity;
	}

	public void setVnToCity(String vnToCity) {
		this.vnToCity = vnToCity;
	}

	public String getVnToState() {
		return vnToState;
	}

	public void setVnToState(String vnToState) {
		this.vnToState = vnToState;
	}

	public String getVnToPostalCode() {
		return vnToPostalCode;
	}

	public void setVnToPostalCode(String vnToPostalCode) {
		this.vnToPostalCode = vnToPostalCode;
	}

	public String getVnToCountry() {
		return vnToCountry;
	}

	public void setVnToCountry(String vnToCountry) {
		this.vnToCountry = vnToCountry;
	}

	public String getVnToAddress1() {
		return vnToAddress1;
	}

	public void setVnToAddress1(String vnToAddress1) {
		this.vnToAddress1 = vnToAddress1;
	}
}
