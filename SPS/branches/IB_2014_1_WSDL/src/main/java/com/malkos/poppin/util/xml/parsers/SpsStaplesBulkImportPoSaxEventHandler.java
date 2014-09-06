package com.malkos.poppin.util.xml.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import antlr.ParserSharedInputState;

import com.malkos.poppin.documents.SpsPoDocument;
import com.malkos.poppin.documents.SpsStaplesBulkImportPoDocument;
import com.malkos.poppin.entities.ChargesAllowancesPojo;
import com.malkos.poppin.entities.SPSOrderDatePojo;
import com.malkos.poppin.entities.SpsDocumentItemPojo;
import com.malkos.poppin.entities.SpsDocumentSubLineItemPojo;
import com.malkos.poppin.entities.SpsNotePojo;
import com.malkos.poppin.entities.SpsReferencePojo;
import com.malkos.poppin.integration.retailers.RetailerAbstract;

public class SpsStaplesBulkImportPoSaxEventHandler extends DefaultHandler implements ISpsDocumentProvider {
	protected SpsStaplesBulkImportPoDocument poDocument;
	private RetailerAbstract retailer;
	
	private String tempElementValue;
	
	private SpsDocumentItemPojo tempPoDocumentItemPojo;
	private SpsDocumentSubLineItemPojo tempPoDocumentSubLineItemPojo;
	private SPSOrderDatePojo tempOrderDateNode;
	private ChargesAllowancesPojo tempChargesAllowancesNode;
	private SpsReferencePojo referenceTemp; 
	private SpsNotePojo tempNote;
	
	private boolean parserIsInHeaderNode;
	private boolean parserIsInMetaNode;
	private boolean parserIsInReference;
	private boolean parserIsInReferensIDs;
	private boolean parserIsInAddress;
	private boolean parserIsInShipToAddress;
	private boolean parserIsInBillToAddress;
	private boolean parserIsInVnAddress;	
	private boolean parserIsInContact;
	private boolean parserIsInLineItemsNode;
	private boolean parserIsInSubLinesNode;
	
	public SpsStaplesBulkImportPoSaxEventHandler(RetailerAbstract retailer) {
		this.retailer = retailer;
	}
	
	protected void createNewDocument(){
		poDocument = new SpsStaplesBulkImportPoDocument();
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if(qName.equalsIgnoreCase("PurchaseOrder")){
			createNewDocument();
			poDocument.setTradingPartnerId(retailer.getRetailerName());
		}
		else if(qName.equalsIgnoreCase("Date")){
			tempOrderDateNode = new SPSOrderDatePojo();
		}
		else if (qName.equalsIgnoreCase("ChargesAllowances")){
			tempChargesAllowancesNode = new ChargesAllowancesPojo();
		}
		else if (qName.equalsIgnoreCase("Notes")){
			tempNote = new SpsNotePojo();
		}
		else if(qName.equalsIgnoreCase("Reference")){
			referenceTemp = new SpsReferencePojo();
			parserIsInReference = true;
		}
		else if(qName.equalsIgnoreCase("ReferenceIDs")){
			parserIsInReferensIDs = true;
		}
		else if(qName.equalsIgnoreCase("Header"))
			parserIsInHeaderNode = true;
		else if(qName.equalsIgnoreCase("Meta"))
			parserIsInMetaNode = true;
		else if(qName.equalsIgnoreCase("Address"))
			parserIsInAddress = true;
		else if(qName.equalsIgnoreCase("Contact"))
			parserIsInContact = true;
		else if (qName.equalsIgnoreCase("LineItems"))
			parserIsInLineItemsNode = true;
		else if (qName.equalsIgnoreCase("LineItem")){
			tempPoDocumentItemPojo = new SpsDocumentItemPojo();			
		} else if (qName.equalsIgnoreCase("Sublines")){
			parserIsInSubLinesNode=true;
		}	else if (qName.equalsIgnoreCase("Subline")){
			tempPoDocumentSubLineItemPojo = new SpsDocumentSubLineItemPojo();	
		}
	}
	public void characters(char[] ch, int start, int length) throws SAXException {
		tempElementValue = new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException{
		
		if(qName.equalsIgnoreCase("Header"))
			parserIsInHeaderNode = false;
		else if(qName.equalsIgnoreCase("Meta"))
			parserIsInMetaNode = false;
		else if(qName.equalsIgnoreCase("Contact"))
			parserIsInContact = false;
		else if (qName.equalsIgnoreCase("LineItems"))
			parserIsInLineItemsNode = false;
		else if (qName.equalsIgnoreCase("Sublines"))
			parserIsInSubLinesNode = false;
		//meta
		else if(parserIsInMetaNode)
		{
			if(qName.equalsIgnoreCase("SenderUniqueID"))
				poDocument.setSenderUniqueID(tempElementValue);
			else if(qName.equalsIgnoreCase("SenderCompanyName"))
				poDocument.setSenderCompanyName(tempElementValue);
			else if(qName.equalsIgnoreCase("ReceiverUniqueID"))
				poDocument.setReceiverUniqueID(tempElementValue);
			else if(qName.equalsIgnoreCase("ReceiverCompanyName"))
				poDocument.setReceiverCompanyName(tempElementValue);
			else if(qName.equalsIgnoreCase("InterchangeControlNumber"))
				poDocument.setInterchangeControlNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("GroupControlIdentifier"))
				poDocument.setGroupControlIdentifier(tempElementValue);
			else if(qName.equalsIgnoreCase("GroupControlNumber"))
				poDocument.setGroupControlNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("DocumentControlIdentifier"))
				poDocument.setDocumentControlIdentifier(tempElementValue);
			else if(qName.equalsIgnoreCase("DocumentControlNumber"))
				poDocument.setDocumentControlNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("InterchangeSenderID"))
				poDocument.setInterchangeSenderID(tempElementValue);
			else if(qName.equalsIgnoreCase("InterchangeReceiverID"))
				poDocument.setInterchangeReceiverID(tempElementValue);
			else if(qName.equalsIgnoreCase("GroupSenderID"))
				poDocument.setGroupSenderID(tempElementValue);
			else if(qName.equalsIgnoreCase("GroupReceiverID"))
				poDocument.setGroupReceiverID(tempElementValue);
			else if(qName.equalsIgnoreCase("BatchPart"))
				poDocument.setBatchPart(tempElementValue);
			else if(qName.equalsIgnoreCase("BatchTotal"))
				poDocument.setBatchTotal(tempElementValue);
			else if(qName.equalsIgnoreCase("BatchID"))
				poDocument.setBatchID(tempElementValue);
			else if(qName.equalsIgnoreCase("Validation"))
				poDocument.setValidation(tempElementValue);
			else if(qName.equalsIgnoreCase("OrderManagement"))
				poDocument.setOrderManagement(tempElementValue);
			else if(qName.equalsIgnoreCase("Version"))
				poDocument.setVersion(qName);
		}
		//meta
		
		else if(parserIsInHeaderNode){
			if(qName.equalsIgnoreCase("Vendor"))
				poDocument.setVendor(tempElementValue);
			else if(qName.equalsIgnoreCase("TsetPurposeCode"))
				poDocument.setTsetPurposeCode(tempElementValue);
			else if(qName.equalsIgnoreCase("CarrierEquipmentInitial"))
				poDocument.setCarrierEquipmentInitial(tempElementValue);
			else if(qName.equalsIgnoreCase("CarrierTransMethodCode"))
				poDocument.setCarrierTransMethodCode(tempElementValue);
			else if(qName.equalsIgnoreCase("CarrierRouting"))
				poDocument.setCarrierRouting(tempElementValue);
			else if(qName.equalsIgnoreCase("PromotionDealNumber"))
				poDocument.setPromotionDealNumber(tempElementValue);			
			else if(qName.equalsIgnoreCase("CarrierEquipmentNumber"))
				poDocument.setCarrierEquipmentNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("FOBPayCode"))
				poDocument.setFOBPayCode(tempElementValue);
			else if(qName.equalsIgnoreCase("FOBLocationQualifier"))
				poDocument.setFOBLocationQualifier(tempElementValue);
			else if(qName.equalsIgnoreCase("FOBLocationDescription"))
				poDocument.setFOBLocationDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("CustomerOrderNumber"))
				poDocument.setCustomerOrderNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("Date")){
				poDocument.getSpsOrderDatesPojoList().add(tempOrderDateNode);
			}		
			else if(qName.equalsIgnoreCase("TermsDiscountAmount")){
				poDocument.setTermsDiscountAmount(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("TermsDiscountDate")){
				poDocument.setTermsDiscountDate(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("TermsDiscountPercentage")){
				poDocument.setTermsDiscountPercentage(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("TermsDiscountDueDays")){
				poDocument.setTermsDiscountDueDays(tempElementValue);
			}			
			else if(qName.equalsIgnoreCase("DateTimeQualifier1")){
				tempOrderDateNode.setDateTimeQualifier(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("Date1")){
				tempOrderDateNode.setDate(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("Time1")){
				tempOrderDateNode.setTime(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("TimeCode1")){
				tempOrderDateNode.setTimeCode(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("DateTimeFormQualifier1")){
				tempOrderDateNode.setDateTimeFormQualifier(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("DateTimePeriod")){
				tempOrderDateNode.setDateTimePeriod(tempElementValue);
			}			
			else if(qName.equalsIgnoreCase("Reference")){
				poDocument.getSpsReferencePojoList().add(referenceTemp);
				parserIsInReference = false;
			}
			else if (parserIsInReference == true){
				if (parserIsInReferensIDs == false){
					if(qName.equalsIgnoreCase("ReferenceQual")){
						referenceTemp.setReferenceQual(tempElementValue);
					}
					else if(qName.equalsIgnoreCase("ReferenceID")){
						referenceTemp.setReferenceID(tempElementValue);
					}
					else if(qName.equalsIgnoreCase("Description")){
						referenceTemp.setDescription(tempElementValue);
					}
				}
				else{
					if(qName.equalsIgnoreCase("ReferenceQual")){
						referenceTemp.setInnerReferencePojo(new SpsReferencePojo());
						referenceTemp.getInnerReferencePojo().setReferenceQual(tempElementValue);
					}
					else if(qName.equalsIgnoreCase("ReferenceID")){
						referenceTemp.getInnerReferencePojo().setReferenceID(tempElementValue);
					}
				}
				if(qName.equalsIgnoreCase("ReferenceIDs"))
						parserIsInReferensIDs = false;
			}
			//reference
			
			else if(qName.equalsIgnoreCase("Address")){
				parserIsInAddress = false;
				parserIsInShipToAddress = false;
				parserIsInVnAddress = false;
				parserIsInBillToAddress = false;
			}
			else if(qName.equalsIgnoreCase("AddressTypeCode")){
				if(tempElementValue.equalsIgnoreCase(SpsPoDocument.SHIP_TO_ADDRESS_TYPE_CODE))
					parserIsInShipToAddress = true;
				else if (tempElementValue.equalsIgnoreCase(SpsPoDocument.BILL_TO_ADDRESS_TYPE_CODE))
					parserIsInBillToAddress = true;
				else if (tempElementValue.equalsIgnoreCase(SpsPoDocument.VN_ADDRESS_TYPE_CODE))
					parserIsInVnAddress = true;
			}
			//<header><Contact>...
			else if(parserIsInContact == true && parserIsInAddress == false){
					if(qName.equalsIgnoreCase("ContactTypeCode"))
						poDocument.setHeaderContactTypeCode(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactName"))
						poDocument.setHeaderContactName(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactPhone"))
						poDocument.setHeaderContactPhone(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactFax"))
						poDocument.setHeaderContactFax(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactEmail"))
						poDocument.setHeaderContactEmail(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactReference"))
						poDocument.setHeaderContactReference(tempElementValue);
			}
			//<header><address><addresstypeCode=ST><Contact>...
			else if(parserIsInAddress == true && parserIsInShipToAddress){
					if(qName.equalsIgnoreCase("ContactTypeCode"))
						poDocument.setShipToContactTypeCode(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactName"))
						poDocument.setShipToContactName(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactPhone"))
						poDocument.setShipToContactPhone(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactFax"))
						poDocument.setShipToContactFax(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactEmail"))
						poDocument.setShipToContactEmail(tempElementValue);
					else if(qName.equalsIgnoreCase("ContactReference"))
						poDocument.setShipToContactReference(tempElementValue);
					else if(qName.equalsIgnoreCase("Address2"))
						poDocument.setAddress2(tempElementValue);
					else if(qName.equalsIgnoreCase("AddressAlternateName"))
						poDocument.setAddressAlternateName(tempElementValue);
					else if(qName.equalsIgnoreCase("AddressAlternateName2"))
						poDocument.setAddressAlternateName2(tempElementValue);
					else if(qName.equalsIgnoreCase("Address3"))
						poDocument.setAddress3(tempElementValue);
					else if(qName.equalsIgnoreCase("Address3"))
						poDocument.setAddress3(tempElementValue);
					else if(qName.equalsIgnoreCase("Address4"))
						poDocument.setAddress4(tempElementValue);
					else if(qName.equalsIgnoreCase("LocationID"))
						poDocument.setLocationID(tempElementValue);
					else if(qName.equalsIgnoreCase("CountrySubDivision"))
						poDocument.setCountrySubDivision(tempElementValue);
					else if(qName.equalsIgnoreCase("AddressTaxIdNumber"))
						poDocument.setAddressTaxIdNumber(tempElementValue);
					else if(qName.equalsIgnoreCase("AddressTaxExemptNumber"))
						poDocument.setAddressTaxExemptNumber(tempElementValue);
			}
			else if(parserIsInAddress == true && parserIsInBillToAddress){
				if(qName.equalsIgnoreCase("ContactTypeCode"))
					poDocument.setBillToContactTypeCode(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactName"))
					poDocument.setBillToContactName(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactPhone"))
					poDocument.setBillToContactPhone(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactFax"))
					poDocument.setBillToContactFax(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactEmail"))
					poDocument.setBillToContactEmail(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactReference"))
					poDocument.setBillToContactReference(tempElementValue);
				else if(qName.equalsIgnoreCase("Address2"))
					poDocument.setBillToAddress2(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressAlternateName"))
					poDocument.setBillToAddressAlternateName(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressAlternateName2"))
					poDocument.setBillToAddressAlternateName2(tempElementValue);
				else if(qName.equalsIgnoreCase("Address3"))
					poDocument.setBillToAddress3(tempElementValue);
				else if(qName.equalsIgnoreCase("Address3"))
					poDocument.setBillToAddress3(tempElementValue);
				else if(qName.equalsIgnoreCase("Address4"))
					poDocument.setBillToAddress4(tempElementValue);
				else if(qName.equalsIgnoreCase("LocationID"))
					poDocument.setBillToLocationID(tempElementValue);
				else if(qName.equalsIgnoreCase("CountrySubDivision"))
					poDocument.setBillToCountrySubDivision(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressTaxIdNumber"))
					poDocument.setBillToAddressTaxIdNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressTaxExemptNumber"))
					poDocument.setBillToAddressTaxExemptNumber(tempElementValue);				
				else if(qName.equalsIgnoreCase("AddressLocationNumber"))
					poDocument.setBillToAddressLocationNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressName"))
					poDocument.setBillToName(tempElementValue);
				else if(qName.equalsIgnoreCase("Address1"))
					poDocument.setBillToAddress1(tempElementValue);
				else if(qName.equalsIgnoreCase("City"))
					poDocument.setBillToCity(tempElementValue);
				else if(qName.equalsIgnoreCase("State"))
					poDocument.setBillToState(tempElementValue);
				else if(qName.equalsIgnoreCase("PostalCode"))
					poDocument.setBillToPostalCode(tempElementValue);
				else if(qName.equalsIgnoreCase("Country"))
					poDocument.setBillToCountry(tempElementValue);
		}
		else if(parserIsInAddress == true && parserIsInVnAddress){
				if(qName.equalsIgnoreCase("ContactTypeCode"))
					poDocument.setVnToContactTypeCode(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactName"))
					poDocument.setVnToContactName(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactPhone"))
					poDocument.setVnToContactPhone(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactFax"))
					poDocument.setVnToContactFax(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactEmail"))
					poDocument.setVnToContactEmail(tempElementValue);
				else if(qName.equalsIgnoreCase("ContactReference"))
					poDocument.setVnToContactReference(tempElementValue);
				else if(qName.equalsIgnoreCase("Address2"))
					poDocument.setVnToAddress2(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressAlternateName"))
					poDocument.setVnToAddressAlternateName(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressAlternateName2"))
					poDocument.setVnToAddressAlternateName2(tempElementValue);
				else if(qName.equalsIgnoreCase("Address3"))
					poDocument.setVnToAddress3(tempElementValue);
				else if(qName.equalsIgnoreCase("Address3"))
					poDocument.setVnToAddress3(tempElementValue);
				else if(qName.equalsIgnoreCase("Address4"))
					poDocument.setVnToAddress4(tempElementValue);
				else if(qName.equalsIgnoreCase("LocationID"))
					poDocument.setVnToLocationID(tempElementValue);
				else if(qName.equalsIgnoreCase("CountrySubDivision"))
					poDocument.setVnToCountrySubDivision(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressTaxIdNumber"))
					poDocument.setVnToAddressTaxIdNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressTaxExemptNumber"))
					poDocument.setVnToAddressTaxExemptNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressLocationNumber"))
					poDocument.setVnToAddressLocationNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("AddressName"))
					poDocument.setVnToName(tempElementValue);
				else if(qName.equalsIgnoreCase("Address1"))
					poDocument.setVnToAddress1(tempElementValue);
				else if(qName.equalsIgnoreCase("City"))
					poDocument.setVnToCity(tempElementValue);
				else if(qName.equalsIgnoreCase("State"))
					poDocument.setVnToState(tempElementValue);
				else if(qName.equalsIgnoreCase("PostalCode"))
					poDocument.setVnToPostalCode(tempElementValue);
				else if(qName.equalsIgnoreCase("Country"))
					poDocument.setVnToCountry(tempElementValue);
		}
			else if(qName.equalsIgnoreCase("ReleaseNumber"))
				poDocument.setReleaseNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("Department"))
				poDocument.setDepartment(tempElementValue);
			else if(qName.equalsIgnoreCase("CustomerAccountNumber"))
				poDocument.setCustomerAccountNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("CustomerOrderNumber"))
				poDocument.setCustomerOrderNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("Notes"))
				poDocument.getSpsHeaderNotePojoList().add(tempNote); 
			else if(qName.equalsIgnoreCase("NoteCode"))
				tempNote.setNoteCode(tempElementValue); 
			else if(qName.equalsIgnoreCase("NoteInformationField"))
				tempNote.setNoteInformationField(tempElementValue); 
			else if(qName.equalsIgnoreCase("ChargesAllowances"))
				poDocument.getSpsHeaderChargesAllowancesPojoList().add(tempChargesAllowancesNode);
			else if(qName.equalsIgnoreCase("AllowChrgIndicator"))
				tempChargesAllowancesNode.setAllowChrgIndicator(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgCode"))
				tempChargesAllowancesNode.setAllowChrgCode(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgAmt"))
				tempChargesAllowancesNode.setAllowChrgAmt(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgPercentBasis"))
				tempChargesAllowancesNode.setAllowChrgPercentBasis(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgPercent"))
				tempChargesAllowancesNode.setAllowChrgPercent(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgHandlingCode"))
				tempChargesAllowancesNode.setAllowChrgHandlingCode(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgHandlingDescription"))
				tempChargesAllowancesNode.setAllowChrgHandlingDescription(tempElementValue);			
		} 		
		else if (parserIsInLineItemsNode){
			if (parserIsInSubLinesNode){
				if(qName.equalsIgnoreCase("Subline"))
					tempPoDocumentItemPojo.getSpsDocumentSubLinesList().add(tempPoDocumentSubLineItemPojo);
				else if(qName.equalsIgnoreCase("LineSequenceNumber"))
					tempPoDocumentSubLineItemPojo.setVendorLineNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("VendorPartNumber"))
					tempPoDocumentSubLineItemPojo.setVendorPartNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("BuyerPartNumber"))
					tempPoDocumentSubLineItemPojo.setBuyerPartNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("ConsumerPackageCode"))
					tempPoDocumentSubLineItemPojo.setConsumerPackageCode(tempElementValue);
				else if(qName.equalsIgnoreCase("GTIN"))
					tempPoDocumentSubLineItemPojo.setGTIN(tempElementValue);	
				else if(qName.equalsIgnoreCase("EAN"))
					tempPoDocumentSubLineItemPojo.setEAN(tempElementValue);					
				else if(qName.equalsIgnoreCase("UPCCaseCode"))
					tempPoDocumentSubLineItemPojo.setUPCCaseCode(tempElementValue);
				else if(qName.equalsIgnoreCase("NatlDrugCode"))
					tempPoDocumentSubLineItemPojo.setNatlDrugCode(tempElementValue);
				else if(qName.equalsIgnoreCase("InternationalStandardBookNumber"))
					tempPoDocumentSubLineItemPojo.setInternationalStandardBookNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("PartNumberQual"))
					tempPoDocumentSubLineItemPojo.setPartNumberQual(tempElementValue);
				else if(qName.equalsIgnoreCase("PartNumber"))
					tempPoDocumentSubLineItemPojo.setPartNumber(tempElementValue);
				else if(qName.equalsIgnoreCase("ProductSizeDescription"))
					tempPoDocumentSubLineItemPojo.setProductSizeDescription(tempElementValue);
				else if(qName.equalsIgnoreCase("ProductColorDescription"))
					tempPoDocumentSubLineItemPojo.setProductColorDescription(tempElementValue);	
				else if(qName.equalsIgnoreCase("ProductSizeCode"))
					tempPoDocumentSubLineItemPojo.setProductSizeCode(tempElementValue);
				else if(qName.equalsIgnoreCase("ProductColorCode"))
					tempPoDocumentSubLineItemPojo.setProductColorCode(tempElementValue);
				else if(qName.equalsIgnoreCase("ProductFabricDescription"))
					tempPoDocumentSubLineItemPojo.setProductFabricDescription(tempElementValue);				
				else if(qName.equalsIgnoreCase("QtyPer"))
					tempPoDocumentSubLineItemPojo.setQtyPer(tempElementValue);
				else if(qName.equalsIgnoreCase("QtyPerUOM"))
					tempPoDocumentSubLineItemPojo.setQtyPerUOM(tempElementValue);
				else if(qName.equalsIgnoreCase("UnitPrice"))
					tempPoDocumentSubLineItemPojo.setUnitPrice(tempElementValue);
				else if(qName.equalsIgnoreCase("ItemDescriptionType"))
					tempPoDocumentSubLineItemPojo.setItemDescriptionType(tempElementValue);
				else if(qName.equalsIgnoreCase("ProductDescription"))
					tempPoDocumentSubLineItemPojo.setProductDescription(tempElementValue);					
			}			
			else if(qName.equalsIgnoreCase("LineItem"))
				poDocument.getPoDocumentItemPojoList().add(tempPoDocumentItemPojo);
			else if(qName.equalsIgnoreCase("LineSequenceNumber"))
				tempPoDocumentItemPojo.setVendorLineNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("VendorPartNumber"))
				tempPoDocumentItemPojo.setVendorPartNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("UPCCaseCode"))
				tempPoDocumentItemPojo.setUPCCaseCode(tempElementValue);
			else if(qName.equalsIgnoreCase("ShipQtyUOM"))
				tempPoDocumentItemPojo.setShipQtyUOM(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductSizeDescription"))
				tempPoDocumentItemPojo.setProductSizeDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductFabricDescription"))
				tempPoDocumentItemPojo.setProductFabricDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductDescription"))
				tempPoDocumentItemPojo.setProductDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductColorDescription"))
				tempPoDocumentItemPojo.setProductColorDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductColorCode"))
				tempPoDocumentItemPojo.setProductColorCode(tempElementValue);
			else if(qName.equalsIgnoreCase("ProductSizeCode"))
				tempPoDocumentItemPojo.setProductSizeCode(tempElementValue);			
			else if(qName.equalsIgnoreCase("PartNumberQual"))
				tempPoDocumentItemPojo.setPartNumberQual(tempElementValue);
			else if(qName.equalsIgnoreCase("PartNumber"))
				tempPoDocumentItemPojo.setPartNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("OrderQtyUOM"))
				tempPoDocumentItemPojo.setOrderQtyUOM(tempElementValue);			
			else if(qName.equalsIgnoreCase("NRFSizeCode"))
				tempPoDocumentItemPojo.setNRFSizeCode(tempElementValue);
			else if(qName.equalsIgnoreCase("NRFColorCode"))
				tempPoDocumentItemPojo.setNRFColorCode(tempElementValue);
			else if(qName.equalsIgnoreCase("NatlDrugCode"))
				tempPoDocumentItemPojo.setNatlDrugCode(tempElementValue);			
			else if(qName.equalsIgnoreCase("ItemDescriptionType"))
				tempPoDocumentItemPojo.setItemDescriptionType(tempElementValue);
			else if(qName.equalsIgnoreCase("InternationalStandardBookNumber"))
				tempPoDocumentItemPojo.setInternationalStandardBookNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("GTIN"))
				tempPoDocumentItemPojo.setGTIN(tempElementValue);	
			else if(qName.equalsIgnoreCase("EAN"))
				tempPoDocumentItemPojo.setEAN(tempElementValue);		
			else if(qName.equalsIgnoreCase("BuyerPartNumber"))
				tempPoDocumentItemPojo.setBuyerPartNumber(tempElementValue);
			else if(qName.equalsIgnoreCase("ChargesAllowances"))
				tempPoDocumentItemPojo.getSpsItemChargesAllowancesList().add(tempChargesAllowancesNode);
			else if(qName.equalsIgnoreCase("AllowChrgIndicator"))
				tempChargesAllowancesNode.setAllowChrgIndicator(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgCode"))
				tempChargesAllowancesNode.setAllowChrgCode(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgAmt"))
				tempChargesAllowancesNode.setAllowChrgAmt(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgPercentBasis"))
				tempChargesAllowancesNode.setAllowChrgPercentBasis(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgPercent"))
				tempChargesAllowancesNode.setAllowChrgPercent(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgHandlingCode"))
				tempChargesAllowancesNode.setAllowChrgHandlingCode(tempElementValue);
			else if(qName.equalsIgnoreCase("AllowChrgHandlingDescription"))
				tempChargesAllowancesNode.setAllowChrgHandlingDescription(tempElementValue);
			else if(qName.equalsIgnoreCase("Date")){
				tempPoDocumentItemPojo.getSpsItemDatesList().add(tempOrderDateNode);
			}
			else if(qName.equalsIgnoreCase("DateTimeQualifier1")){
				tempOrderDateNode.setDateTimeQualifier(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("Date1")){
				tempOrderDateNode.setDate(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("Time1")){
				tempOrderDateNode.setTime(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("TimeCode1")){
				tempOrderDateNode.setTimeCode(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("DateTimeFormQualifier1")){
				tempOrderDateNode.setDateTimeFormQualifier(tempElementValue);
			}
			else if(qName.equalsIgnoreCase("DateTimePeriod")){
				tempOrderDateNode.setDateTimePeriod(tempElementValue);
			}			
			else if (parserIsInReference&&!parserIsInAddress){
				if(qName.equalsIgnoreCase("ReferenceQual")){
					tempPoDocumentItemPojo.setItemReferenceQual(tempElementValue);
				}
				else if(qName.equalsIgnoreCase("ReferenceID")){
					tempPoDocumentItemPojo.setItemReferenceId(tempElementValue);
				}
				else if(qName.equalsIgnoreCase("Description")){
					tempPoDocumentItemPojo.setItemReferenceDescription(tempElementValue);
				}
			}
			else if(qName.equalsIgnoreCase("Reference")){			
				parserIsInReference = false;
			}
			else if (qName.equalsIgnoreCase("Address")){
				parserIsInAddress = false;
			}
		}		
	}
	
	@Override
	public SpsPoDocument getSpsPoDocument() {
		return poDocument;
	}

}
