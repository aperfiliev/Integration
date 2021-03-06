package com.malkos.poppin.util.xml.generators.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.documents.SpsPoDocument;
import com.malkos.poppin.documents.SpsStaplesBulkImportPoDocument;
import com.malkos.poppin.documents.SpsStaplesCrossDockPoDocument;
import com.malkos.poppin.entities.ChargesAllowancesPojo;
import com.malkos.poppin.entities.FulfillmentItemPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoiceItemPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.entities.SPSOrderDatePojo;
import com.malkos.poppin.entities.SpsDocumentItemPojo;
import com.malkos.poppin.entities.SpsDocumentSubLineItemPojo;
import com.malkos.poppin.entities.SpsNotePojo;
import com.malkos.poppin.entities.SpsReferencePojo;
import com.malkos.poppin.integration.retailers.RetailerAbstract;
import com.malkos.poppin.util.xml.XmlParserUtil;
import com.malkos.poppin.util.xml.generators.IXmlMessagesGenerator;
import com.malkos.poppin.util.xml.generators.XmlDocumentGenerator;
import com.malkos.poppin.util.xml.parsers.SpsStaplesBulkImportPoSaxEventHandler;
import com.malkos.poppin.util.xml.parsers.SpsStaplesCrossDockPoSaxEventHandler;

public class StaplesBulkImportMessageGenerator extends XmlDocumentGenerator implements IXmlMessagesGenerator{
	
	public StaplesBulkImportMessageGenerator(RetailerAbstract retailer) {
		super(retailer);
	}

	@Override
	public String generateAsnMessage(FulfillmentPojo fulfillmentPojo) throws XmlMessageGenerationException {
		InputStream incomingMessageStream = null;
		try {
			incomingMessageStream = new FileInputStream(new File(fulfillmentPojo.getIncomingMessagePath()));
		} catch (FileNotFoundException e) {
			throw new XmlMessageGenerationException("Could not find incmoming message file.");
		}
		SpsStaplesBulkImportPoDocument document = null;
		try {
			document = (SpsStaplesBulkImportPoDocument) XmlParserUtil.convertXmlStringToSPSPoDocument(incomingMessageStream, new SpsStaplesBulkImportPoSaxEventHandler(retailer));
		} catch (Exception e) {
			throw new XmlMessageGenerationException("Could not parse incmoming message. Reason :" + e.getMessage());
		}
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new XmlMessageGenerationException("Could not parse incmoming message. Reason :" + e.getMessage());
		}
		Document asnXmlDocument = docBuilder.newDocument();
		
		DecimalFormat triplePrecision = new DecimalFormat("#.###");
		
		SimpleDateFormat basicDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat basicDatePlusTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		SimpleDateFormat basicTimeFormat = new SimpleDateFormat("HHmmss");
		SimpleDateFormat basicTimeFormatDashed = new SimpleDateFormat("HH:mm:ss");
		Date todayNow = new Date();
		
		String todayNowStrDashed = basicDatePlusTimeDashedFormat.format(todayNow);
		String nowStr = basicTimeFormatDashed.format(todayNow);
		String shipNoticeTime = basicTimeFormat.format(todayNow);
		String currentScheduledDeliveryDateString = null;
		String currentScheduledDeliveryTimeString = null;
		
		Element asnRootElement = asnXmlDocument.createElement("AdvanceShipNotices");
		Element advanceShipNotice = asnXmlDocument.createElement("AdvanceShipNotice");
		
		//meta
		Element meta = asnXmlDocument.createElement("Meta");
		
		createElementIfPossibleAndAppendItToparent(document.getSenderUniqueID(), "SenderUniqueID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getSenderCompanyName(), "SenderCompanyName", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getReceiverUniqueID(), "ReceiverUniqueID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getReceiverCompanyName(), "ReceiverCompanyName", asnXmlDocument, meta );
		createElementAndAppendItToparent(Boolean.toString(false), "IsDropShip", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getInterchangeControlNumber(), "InterchangeControlNumber", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getGroupControlIdentifier(), "GroupControlIdentifier", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getGroupControlNumber(), "GroupControlNumber", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getDocumentControlIdentifier(), "DocumentControlIdentifier", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getDocumentControlNumber(), "DocumentControlNumber", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getInterchangeSenderID(), "InterchangeSenderID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getInterchangeReceiverID(), "InterchangeReceiverID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getGroupSenderID(), "GroupSenderID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getGroupReceiverID(), "GroupReceiverID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getBatchPart(), "BatchPart", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getBatchTotal(), "BatchTotal", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getBatchID(), "BatchID", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getComments(), "Comments", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getValidation(), "Validation", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getOrderManagement(), "OrderManagement", asnXmlDocument, meta );
		createElementIfPossibleAndAppendItToparent(document.getVersion(), "Version", asnXmlDocument, meta );
					
		if(meta.hasChildNodes()) advanceShipNotice.appendChild(meta);
		//meta
		
		//header
		Element header = asnXmlDocument.createElement("Header");
		
		//shipment header
		Element shipmentHeader = asnXmlDocument.createElement("ShipmentHeader");
		
		createElementAndAppendItToparent(retailer.getRetailerName(), "TradingPartnerId", asnXmlDocument, shipmentHeader );
		//I am a dummy portion of code. I have appeared because of stupid guys from SPS. Please remove me when you will go to PROD and uncomment the line aboce
		//createElementAndAppendItToparent(GlobalProperties.STAPLES_RETAILER_STUPID_SPS_PSEUDO_NAME, "TradingPartnerId", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(fulfillmentPojo.getPoNumber(), "ShipmentIdentification", asnXmlDocument, shipmentHeader );
		String shipDate = null;
		if(null != fulfillmentPojo.getShipDate()){
			shipDate = basicDateFormat.format(fulfillmentPojo.getShipDate());
			createElementIfPossibleAndAppendItToparent(shipDate, "ShipmentDate", asnXmlDocument, shipmentHeader );
			
			Calendar currentScheduledDeliveryDate = Calendar.getInstance();
			currentScheduledDeliveryDate.setTime(fulfillmentPojo.getShipDate());
			currentScheduledDeliveryDate.add(Calendar.DAY_OF_MONTH, 5);
			
			currentScheduledDeliveryDateString = basicDateFormat.format(currentScheduledDeliveryDate.getTime());
			currentScheduledDeliveryTimeString = basicDatePlusTimeFormat.format(currentScheduledDeliveryDate.getTime());
		}
		boolean carrierEquipmentInitialExists = ( null == document.getCarrierEquipmentInitial() ) ? false : true;
		
		createElementIfPossibleAndAppendItToparent(document.getVendor(), "Vendor", asnXmlDocument, shipmentHeader );
		//if(carrierEquipmentInitialExists)
		//	createElementAndAppendItToparent(document.getEquipmentDescriptionCode(), "EquipmentDescriptionCode", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(document.getTsetPurposeCode(), "TsetPurposeCode", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(shipDate, "ShipNoticeDate", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(shipNoticeTime, "ShipNoticeTime", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getAsnStructureCode(), "ASNStructureCode", asnXmlDocument, shipmentHeader );
/**/	createElementAndAppendItToparent(document.getShipmentQtyPackingCode(), "ShipmentQtyPackingCode", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getShipmentLadingQuantity(), "ShipmentLadingQuantity", asnXmlDocument, shipmentHeader );
/**/	//createElementAndAppendItToparent(document.getGrossWeightQualifier(), "GrossWeightQualifier", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(triplePrecision.format( fulfillmentPojo.getShipmentWeight()), "ShipmentWeight", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getShipmentWeightUm(), "ShipmentWeightUOM", asnXmlDocument, shipmentHeader );
/**/	//createElementAndAppendItToparent(document.getEquipmentDescription(), "EquipmentDescription", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getEquipmentDescriptionCode(), "EquipmentDescriptionCode", asnXmlDocument, shipmentHeader );
/**/	if(carrierEquipmentInitialExists){
			createElementAndAppendItToparent(document.getCarrierEquipmentNumber(), "CarrierEquipmentNumber", asnXmlDocument, shipmentHeader );
/**/ 		createElementIfPossibleAndAppendItToparent(document.getCarrierEquipmentInitial(), "CarrierEquipmentInitial", asnXmlDocument, shipmentHeader );
		}
		createElementAndAppendItToparent(document.getCarrierAlphaCodeUpsn(), "CarrierAlphaCode", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getCarrierTransMethodCode(), "CarrierTransMethodCode", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(document.getCarrierRouting(), "CarrierRouting", asnXmlDocument, shipmentHeader );
/**/	String shipmentOrderStatusCode = (fulfillmentPojo.isProcessingClosed()) ? SpsPoDocument.SHIPMENT_ORDER_STATUS_CODE_COMPLETE : SpsPoDocument.SHIPMENT_ORDER_STATUS_CODE_PARTIAL;
/**/	createElementAndAppendItToparent(shipmentOrderStatusCode, "ShipmentOrderStatusCode", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(fulfillmentPojo.getShipmentTrackingNumbers(), "BillOfLadingNumber", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(fulfillmentPojo.getShipmentTrackingNumbers(), "CarrierProNumber", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(fulfillmentPojo.getShipmentTrackingNumbers(), "SealNumber", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(document.getFOBPayCode(), "FOBPayCode", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(document.getFOBLocationQualifier(), "FOBLocationQualifier", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(document.getFOBLocationDescription(), "FOBLocationDescription", asnXmlDocument, shipmentHeader );
		createElementIfPossibleAndAppendItToparent(document.getAppointmentNumber(), "AppointmentNumber", asnXmlDocument, shipmentHeader );
		//createElementAndAppendItToparent(document.getFobTitlePassageCode(), "FOBTitlePassageCode", asnXmlDocument, shipmentHeader );
		//createElementAndAppendItToparent(document.getFobTitlePassageLocation(), "FOBTitlePassageLocation", asnXmlDocument, shipmentHeader );
		createElementAndAppendItToparent(currentScheduledDeliveryDateString, "CurrentScheduledDeliveryDate", asnXmlDocument, shipmentHeader );
/**/	createElementAndAppendItToparent(document.getCurrentSchedulledDeliveryTime(), "CurrentScheduledDeliveryTime", asnXmlDocument, shipmentHeader );
				
		if(shipmentHeader.hasChildNodes()) header.appendChild(shipmentHeader);
		//shipment header
		
		//header dates
		List<SPSOrderDatePojo> spsOrderDarePojoList = document.getSpsOrderDatesPojoList();
		List<Element> headerDates = new ArrayList<>();
		if(null != spsOrderDarePojoList){
			for(SPSOrderDatePojo date : spsOrderDarePojoList){
				Element headerDate = asnXmlDocument.createElement("Date");
				if (date.getDateTimeQualifier().equalsIgnoreCase(SpsPoDocument.SHIP_DATE_DATETIME_QUALIFIER)){
					createElementIfPossibleAndAppendItToparent(date.getDateTimeQualifier(), "DateTimeQualifier1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(basicDateFormat.format(fulfillmentPojo.getShipDate()), "Date1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(nowStr, "Time1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTimeCode(), "TimeCode1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimeFormQualifier(), "DateTimeFormQualifier1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimePeriod(), "DateTimePeriod", asnXmlDocument, headerDate );
				}
				else{
					createElementIfPossibleAndAppendItToparent(date.getDateTimeQualifier(), "DateTimeQualifier1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDate(), "Date1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTime(), "Time1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTimeCode(), "TimeCode1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimeFormQualifier(), "DateTimeFormQualifier1", asnXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimePeriod(), "DateTimePeriod", asnXmlDocument, headerDate );
				}				
				if(headerDate.hasChildNodes()) {
					headerDates.add(headerDate);
					header.appendChild(headerDate);
				}
			}
		}
		//header dates
		
		//header reference
		List<SpsReferencePojo> spsReferencePojoList = document.getSpsReferencePojoList();
		List<Element> headerReferences = new ArrayList<>();
		if(null != spsReferencePojoList){
			for(SpsReferencePojo reference : spsReferencePojoList){
				Element headerReference = asnXmlDocument.createElement("Reference");
				
				createElementIfPossibleAndAppendItToparent(reference.getReferenceQual(), "ReferenceQual", asnXmlDocument, headerReference );
				createElementIfPossibleAndAppendItToparent(reference.getReferenceID(), "ReferenceID", asnXmlDocument, headerReference );
				createElementIfPossibleAndAppendItToparent(reference.getDescription(), "Description", asnXmlDocument, headerReference );
				
				SpsReferencePojo spsInnerReferensePojo = reference.getInnerReferencePojo();
				if(null != spsInnerReferensePojo){
					Element referenceIDs = asnXmlDocument.createElement("ReferenceIDs");
					createElementIfPossibleAndAppendItToparent(spsInnerReferensePojo.getReferenceQual(), "ReferenceQual", asnXmlDocument, referenceIDs );
					createElementIfPossibleAndAppendItToparent(spsInnerReferensePojo.getReferenceID(), "ReferenceID", asnXmlDocument, referenceIDs );
					if(referenceIDs.hasChildNodes()) headerReference.appendChild(referenceIDs);
				}
				
				if(headerReference.hasChildNodes()) {
					headerReferences.add(headerReference);
					header.appendChild(headerReference);
				}
			}
		}
		//BM reference
/**/	/*Element bmReference = asnXmlDocument.createElement("Reference");
		
		createElementAndAppendItToparent(document.getBmReferenceQual(), "ReferenceQual", asnXmlDocument, bmReference );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipmentTrackingNumbers(), "ReferenceID", asnXmlDocument, bmReference );
		createElementAndAppendItToparent(document.getBmReferenceDescription(), "Description", asnXmlDocument, bmReference );
		header.appendChild(bmReference);*/
		//BM reference
		//header reference
		
		//header contact
		if(null != document.getHeaderContactName()){
			Element headerContact = asnXmlDocument.createElement("Contact");
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactTypeCode(), "ContactTypeCode", asnXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactName(), "ContactName", asnXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactPhone(), "ContactPhone", asnXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactFax(), "ContactFax", asnXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactEmail(), "ContactEmail", asnXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactReference(), "ContactReference", asnXmlDocument, headerContact );
			if(headerContact.hasChildNodes()) header.appendChild(headerContact);
		}
		//header contact
		
		//header address
		Element headerAddress = asnXmlDocument.createElement("Address");
		createElementAndAppendItToparent(SpsPoDocument.SHIP_TO_ADDRESS_TYPE_CODE, "AddressTypeCode", asnXmlDocument, headerAddress );
		createElementAndAppendItToparent(document.getLocationQualifier(), "LocationCodeQualifier", asnXmlDocument, headerAddress );
		createElementAndAppendItToparent(fulfillmentPojo.getShipToPostalCode(), "AddressLocationNumber", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToName(), "AddressName", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressAlternateName(), "AddressAlternateName", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressAlternateName2(), "AddressAlternateName2", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToAddress1(), "Address1", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress2(), "Address2", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress3(), "Address3", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress4(), "Address4", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToCity(), "City", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToState(), "State", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToPostalCode(), "PostalCode", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getShipToCountry(), "Country", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getLocationID(), "LocationID", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getCountrySubDivision(), "CountrySubDivision", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressTaxIdNumber(), "AddressTaxIdNumber", asnXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressTaxExemptNumber(), "AddressTaxExemptNumber", asnXmlDocument, headerAddress );

		//header address contact
		Element headerAddressContact = asnXmlDocument.createElement("Contact");
		createElementIfPossibleAndAppendItToparent(document.getShipToContactTypeCode(), "ContactTypeCode", asnXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactName(), "ContactName", asnXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactPhone(), "ContactPhone", asnXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactFax(), "ContactFax", asnXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactEmail(), "ContactEmail", asnXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactReference(), "ContactReference", asnXmlDocument, headerAddressContact );
		if(headerAddressContact.hasChildNodes()) headerAddress.appendChild(headerAddressContact);
		//header address contact
		
		//header address
		if(headerAddress.hasChildNodes()) header.appendChild(headerAddress);
		//header address
		//Ship From address
/**/	Element shipFromAddress = asnXmlDocument.createElement("Address");
		createElementAndAppendItToparent(SpsPoDocument.SHIP_FROM_ADDRESS_TYPE_CODE, "AddressTypeCode", asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromAddressName(),"AddressName" ,asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromAddress1(),"Address1", asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromCity(),"City", asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromState(),"State", asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromPostalCode(),"PostalCode", asnXmlDocument, shipFromAddress);
		createElementAndAppendItToparent(document.getShipFromCountry(),"Country", asnXmlDocument, shipFromAddress);
		
		header.appendChild(shipFromAddress);
		//Ship From address
		advanceShipNotice.appendChild(header);
		//header
		//OrderLevel
		Element orderLevel = asnXmlDocument.createElement("OrderLevel");
		
		//order level order header
		Element orderLevelOrderHeader = asnXmlDocument.createElement("OrderHeader");
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getSoTransactionId(), "OrderNumber", asnXmlDocument, orderLevelOrderHeader );
		if(null != fulfillmentPojo.getPoDate()){
			String orderDate = basicDateFormat.format(fulfillmentPojo.getPoDate());
			createElementIfPossibleAndAppendItToparent(orderDate, "OrderDate", asnXmlDocument, orderLevelOrderHeader );			
		}
		createElementIfPossibleAndAppendItToparent(document.getInvoiceNumber(), "InvoiceNumber", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(fulfillmentPojo.getPoDate()), "InvoiceDate", asnXmlDocument, orderLevelOrderHeader );		
		createElementIfPossibleAndAppendItToparent(fulfillmentPojo.getPoNumber(), "PurchaseOrderNumber", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(document.getReleaseNumber(), "ReleaseNumber", asnXmlDocument, orderLevelOrderHeader );		
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(fulfillmentPojo.getPoDate()), "PurchaseOrderDate", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(document.getShipmentQtyPackingCode(), "OrderQtyPackingCode", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(String.valueOf(fulfillmentPojo.getPackageItems().size()), "OrderLadingQuantity", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(triplePrecision.format(fulfillmentPojo.getShipmentWeight()), "OrderWeight", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(document.getOrderWeightUom(), "OrderWeightUOM", asnXmlDocument, orderLevelOrderHeader);		
		createElementIfPossibleAndAppendItToparent(document.getDepartment(), "Department", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(document.getVendor(), "Vendor", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(shipmentOrderStatusCode, "OrderStatusCode", asnXmlDocument, orderLevelOrderHeader );
		/*createElementIfPossibleAndAppendItToparent(document.getCustomerAccountNumber(), "CustomerAccountNumber", asnXmlDocument, orderLevelOrderHeader );
		createElementIfPossibleAndAppendItToparent(document.getCustomerOrderNumber(), "CustomerOrderNumber", asnXmlDocument, orderLevelOrderHeader );*/
		
		if(orderLevelOrderHeader.hasChildNodes()) orderLevel.appendChild(orderLevelOrderHeader);
		//order level order header
		
		//reference
		
		if(null != spsReferencePojoList){
			for(SpsReferencePojo reference : spsReferencePojoList){
				Element orderLevelReference = asnXmlDocument.createElement("Reference");
				
				createElementIfPossibleAndAppendItToparent(reference.getReferenceQual(), "ReferenceQual", asnXmlDocument, orderLevelReference );
				createElementIfPossibleAndAppendItToparent(reference.getReferenceID(), "ReferenceID", asnXmlDocument, orderLevelReference );
				createElementIfPossibleAndAppendItToparent(reference.getDescription(), "Description", asnXmlDocument, orderLevelReference );
				
				if(orderLevelReference.hasChildNodes()) orderLevel.appendChild(orderLevelReference);
			}
		}
		//reference
		
		//order address
		//if(headerAddress.hasChildNodes()) orderLevel.appendChild(headerAddress.cloneNode(true));
		//order address
		
		//OrderLevel pack level
		Element packLevel = asnXmlDocument.createElement("PackLevel");
		for (PackagePojo packPojo:fulfillmentPojo.getPackageItems()){
			Element pack = asnXmlDocument.createElement("Pack");
			createElementAndAppendItToparent(document.getPackLevelType(), "PackLevelType", asnXmlDocument, pack);
			createElementAndAppendItToparent(Integer.toString(packPojo.getItemQty()), "OuterPack", asnXmlDocument, pack);
			createElementAndAppendItToparent(document.getInnerPackQty(), "InnerPack", asnXmlDocument, pack);			
			createElementAndAppendItToparent(document.getMarksAndNumbersQualifier1(), "MarksAndNumbersQualifier1", asnXmlDocument, pack);
			createElementAndAppendItToparent(document.getMarksAndNumbers1(), "MarksAndNumbers1", asnXmlDocument, pack);
			createElementAndAppendItToparent(document.getMarksAndNumbersQualifier2(), "MarksAndNumbersQualifier2", asnXmlDocument, pack);
			createElementAndAppendItToparent(packPojo.getTrackingNumber(), "MarksAndNumbers2", asnXmlDocument, pack);
			if(pack.hasChildNodes()) packLevel.appendChild(pack);
		}
		//Date
		for (Element date : headerDates){
			packLevel.appendChild(date.cloneNode(true));
		}
		//Date
		//Reference
		for (Element reference:headerReferences){
			packLevel.appendChild(reference.cloneNode(true));
		}
		//Reference
		
		//orderLevel ItemLevel for each item level
		List<SpsDocumentItemPojo> spsOrderItemsList = document.getPoDocumentItemPojoList();
		Map<String, SpsDocumentItemPojo> vendorLineNumberSpsOiPojo = new HashMap<String, SpsDocumentItemPojo>();
		for(SpsDocumentItemPojo spsOiPojo : spsOrderItemsList){
			vendorLineNumberSpsOiPojo.put(spsOiPojo.getVendorLineNumber(), spsOiPojo); 
		}
		
		int totalItems = 0;
		double totalQuantity = 0;
		for(FulfillmentItemPojo oiPojo : fulfillmentPojo.getOrderItems()){
			SpsDocumentItemPojo spsDocItemPojo = vendorLineNumberSpsOiPojo.get(oiPojo.getVendorlineNumber());
			//if there is no such item in PO file - do not process this fulfillment item
			if(null == spsDocItemPojo)
				continue;
			//item level
			Element itemLevel = asnXmlDocument.createElement("ItemLevel");
			//shipment line
			Element shipmentLine = asnXmlDocument.createElement("ShipmentLine");
			
			createElementAndAppendItToparent(oiPojo.getVendorlineNumber(), "LineSequenceNumber", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getBuyerPartNumber(), "BuyerPartNumber", asnXmlDocument, shipmentLine );			
			
			createElementAndAppendItToparent(spsDocItemPojo.getVendorPartNumber(), "VendorPartNumber", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(oiPojo.getUPC(), "ConsumerPackageCode", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getEAN(), "EAN", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getGTIN(), "GTIN", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getUPCCaseCode(), "UPCCaseCode", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getNatlDrugCode(), "NatlDrugCode", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getInternationalStandardBookNumber(), "InternationalStandardBookNumber", asnXmlDocument, shipmentLine );
			
			//item level shipmentLine product ID
			Element productId = asnXmlDocument.createElement("ProductID");
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getPartNumberQual(), "PartNumberQual", asnXmlDocument, productId );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getPartNumber(), "PartNumber", asnXmlDocument, productId );
			if(productId.hasChildNodes()) shipmentLine.appendChild(productId);
			//item level shipmentLine product ID
			createElementAndAppendItToparent(Double.toString(oiPojo.getOrderQty()), "OrderQty", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getOrderQtyUOM(), "OrderQtyUOM", asnXmlDocument, shipmentLine );
			createElementAndAppendItToparent(Double.toString(oiPojo.getUnitPrice()), "UnitPrice", asnXmlDocument, shipmentLine );
			//createElementAndAppendItToparent(document.getPackLevelType(), "PackLevelType", asnXmlDocument, shipmentLine);
			createElementAndAppendItToparent(Double.toString(oiPojo.getShipQty()), "OuterPack", asnXmlDocument, shipmentLine);
			createElementAndAppendItToparent(document.getInnerPackQty(), "InnerPack", asnXmlDocument, shipmentLine);	
			createElementAndAppendItToparent(Double.toString(oiPojo.getShipQty()), "ShipQty", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getOrderQtyUOM(), "ShipQtyUOM", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductSizeCode(), "ProductSizeCode", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductSizeDescription(), "ProductSizeDescription", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductColorCode(), "ProductColorCode", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductColorDescription(), "ProductColorDescription", asnXmlDocument, shipmentLine );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductFabricDescription(), "ProductFabricDescription", asnXmlDocument, shipmentLine );
			//createElementAndAppendItToparent(document.getItemStatusCode(), "ItemStatusCode", asnXmlDocument, shipmentLine );
			//NRFStandardColorAndSize
			Element nRFStandardColorAndSize = asnXmlDocument.createElement("NRFStandardColorAndSize");
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getNRFColorCode(), "NRFColorCode", asnXmlDocument, nRFStandardColorAndSize );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getNRFSizeCode(), "NRFSizeCode", asnXmlDocument, nRFStandardColorAndSize );
			if(nRFStandardColorAndSize.hasChildNodes()) shipmentLine.appendChild(nRFStandardColorAndSize);
			//NRFStandardColorAndSize
			//shipment line
			
			itemLevel.appendChild(shipmentLine);
			
			//ProductOrItemDescription
			Element productOrItemDescription = asnXmlDocument.createElement("ProductOrItemDescription");
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getItemDescriptionType(), "ItemDescriptionType", asnXmlDocument, productOrItemDescription );
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getProductDescription(), "ProductDescription", asnXmlDocument, productOrItemDescription );			
			if(productOrItemDescription.hasChildNodes()) itemLevel.appendChild(productOrItemDescription);
			//ProductOrItemDescription			
			//Date			
			for(SPSOrderDatePojo date : spsDocItemPojo.getSpsItemDatesList()){
					Element itemDate = asnXmlDocument.createElement("Date");
					createElementIfPossibleAndAppendItToparent(date.getDateTimeQualifier(), "DateTimeQualifier1", asnXmlDocument, itemDate );
					createElementIfPossibleAndAppendItToparent(date.getDate(), "Date1", asnXmlDocument, itemDate );
					createElementIfPossibleAndAppendItToparent(date.getTime(), "Time1", asnXmlDocument, itemDate );
					createElementIfPossibleAndAppendItToparent(date.getTimeCode(), "TimeCode1", asnXmlDocument, itemDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimeFormQualifier(), "DateTimeFormQualifier1", asnXmlDocument, itemDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimePeriod(), "DateTimePeriod", asnXmlDocument, itemDate );
					if(itemDate.hasChildNodes()) itemLevel.appendChild(itemDate);					
			}						
			//Date
			//Reference
			Element reference = asnXmlDocument.createElement("Reference");
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getItemReferenceQual(), "ReferenceQual", asnXmlDocument, reference);
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getItemReferenceId(),"ReferenceID", asnXmlDocument, reference);
			createElementIfPossibleAndAppendItToparent(spsDocItemPojo.getItemDescription(),"Description", asnXmlDocument, reference);
			if(reference.hasChildNodes()) itemLevel.appendChild(reference);
			//Reference
			packLevel.appendChild(itemLevel);			
			totalItems ++;
			totalQuantity += oiPojo.getShipQty();
			//item level
			
			//sublines
			List<SpsDocumentSubLineItemPojo> sublineItems = spsDocItemPojo.getSpsDocumentSubLinesList();
			if(sublineItems.size() > 0){
				Element sublines = asnXmlDocument.createElement("Sublines");
				for(SpsDocumentSubLineItemPojo sublineItem : sublineItems){
					Element subline = asnXmlDocument.createElement("Subline");
					//sublineItemDetails
					Element sublineItemDetail = asnXmlDocument.createElement("SublineItemDetail");					
					createElementAndAppendItToparent(sublineItem.getVendorLineNumber(), "LineSequenceNumber", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getBuyerPartNumber(), "BuyerPartNumber", asnXmlDocument, sublineItemDetail );						
					createElementAndAppendItToparent(sublineItem.getVendorPartNumber(), "VendorPartNumber", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getConsumerPackageCode(), "ConsumerPackageCode", asnXmlDocument, sublineItemDetail );					
					createElementIfPossibleAndAppendItToparent(sublineItem.getEAN(), "EAN", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getGTIN(), "GTIN", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getUPCCaseCode(), "UPCCaseCode", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getNatlDrugCode(), "NatlDrugCode", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getInternationalStandardBookNumber(), "InternationalStandardBookNumber", asnXmlDocument, sublineItemDetail );					
					//item level shipmentLine product ID
					Element subLineProductId = asnXmlDocument.createElement("ProductID");
					createElementIfPossibleAndAppendItToparent(sublineItem.getPartNumberQual(), "PartNumberQual", asnXmlDocument, subLineProductId );
					createElementIfPossibleAndAppendItToparent(sublineItem.getPartNumber(), "PartNumber", asnXmlDocument, subLineProductId );
					if(subLineProductId.hasChildNodes()) sublineItemDetail.appendChild(subLineProductId);
					//item level shipmentLine product ID					
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductSizeCode(), "ProductSizeCode", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductSizeDescription(), "ProductSizeDescription", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductColorCode(), "ProductColorCode", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductColorDescription(), "ProductColorDescription", asnXmlDocument, sublineItemDetail );					
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductFabricDescription(), "ProductFabricDescription", asnXmlDocument, sublineItemDetail );					
					createElementIfPossibleAndAppendItToparent(sublineItem.getQtyPer(), "QtyPer", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getQtyPerUOM(), "QtyPerUOM", asnXmlDocument, sublineItemDetail );
					createElementIfPossibleAndAppendItToparent(sublineItem.getUnitPrice(), "UnitPrice", asnXmlDocument, sublineItemDetail );					
					subline.appendChild(sublineItemDetail);
					//sublineItemDetails
					//ProductOrItemDescription
					Element subLineProductOrItemDescription = asnXmlDocument.createElement("ProductOrItemDescription");
					createElementIfPossibleAndAppendItToparent(sublineItem.getItemDescriptionType(), "ItemDescriptionType", asnXmlDocument, subLineProductOrItemDescription );
					createElementIfPossibleAndAppendItToparent(sublineItem.getProductDescription(), "ProductDescription", asnXmlDocument, subLineProductOrItemDescription );			
					if(subLineProductOrItemDescription.hasChildNodes()) subline.appendChild(subLineProductOrItemDescription);
					//ProductOrItemDescription	
					sublines.appendChild(subline);
				}
				if(sublines.hasChildNodes()) itemLevel.appendChild(sublines);
			}
			//sublines
		}
		//orderLevel ItemLevel
		if(packLevel.hasChildNodes()) orderLevel.appendChild(packLevel);
		
		advanceShipNotice.appendChild(orderLevel);
		//OrderLevel
		
		//summary
		Element summary = asnXmlDocument.createElement("Summary");
		createElementAndAppendItToparent(Integer.toString(totalItems), "TotalLineItems", asnXmlDocument, summary );
		createElementAndAppendItToparent(Double.toString(totalQuantity), "TotalQuantity", asnXmlDocument, summary );
		advanceShipNotice.appendChild(summary);
		//summary
		
		asnRootElement.appendChild(advanceShipNotice);
		asnXmlDocument.appendChild(asnRootElement);
		
		String filePath = GlobalPropertiesProvider.getGlobalProperties().getCurrentOutgoingMessagesDirectory() + File.separator + "ASN" + "PO_"+ fulfillmentPojo.getPoNumber()+ "_"+ todayNowStrDashed + ".xml";
		
		try {
			saveXmlDocumentToFileSystem(asnXmlDocument, filePath);
		} catch (TransformerException e) {
			throw new XmlMessageGenerationException(e.getMessage());
		}
		return filePath;		
	}

	@Override
	public String generateInvoiceMessage(InvoicePojo invoicePojo) throws XmlMessageGenerationException {
		InputStream incomingMessageStream = null;
		try {
			incomingMessageStream = new FileInputStream(new File(invoicePojo.getIncomingMessagePath()));
		} catch (FileNotFoundException e) {
			throw new XmlMessageGenerationException("Could not find incmoming message file.");
		}
		SpsStaplesBulkImportPoDocument document = null;
		try {
			document = (SpsStaplesBulkImportPoDocument) XmlParserUtil.convertXmlStringToSPSPoDocument(incomingMessageStream, new SpsStaplesBulkImportPoSaxEventHandler(retailer));
		} catch (Exception e) {
			throw new XmlMessageGenerationException("Could not parse incmoming message. Reason :" + e.getMessage());
		}
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new XmlMessageGenerationException("Could not parse incmoming message. Reason :" + e.getMessage());
		}
		Document invoiceXmlDocument = docBuilder.newDocument();
		
		Element invoiceRootElement = invoiceXmlDocument.createElement("Invoices");
		Element invoice = invoiceXmlDocument.createElement("Invoice");
		
		DecimalFormat triplePrecision = new DecimalFormat("#.###");
		DecimalFormat doublePrecision = new DecimalFormat("#.##");
		Date todayNow = new Date();
		
		SimpleDateFormat basicDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat basicDatePlusTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		SimpleDateFormat basicTimeFormat = new SimpleDateFormat("HHmmss");
		SimpleDateFormat basicTimeFormatDashed = new SimpleDateFormat("HH:mm:ss");
		
		String nowStr = basicTimeFormatDashed.format(todayNow);
		
		//Meta
		Element meta = invoiceXmlDocument.createElement("Meta");
		createElementIfPossibleAndAppendItToparent(document.getVersion(), "Version", invoiceXmlDocument, meta);
		
		if(meta.hasChildNodes())
			invoice.appendChild(meta);
		//Meta
		
		//header
		Element header = invoiceXmlDocument.createElement("Header");
		//InvoiceHeader
		Element invoiceHeader = invoiceXmlDocument.createElement("InvoiceHeader");
		createElementAndAppendItToparent(retailer.getRetailerName(), "TradingPartnerId", invoiceXmlDocument, invoiceHeader );
		//I am a dummy portion of code. I have appeared because of stupid guys from SPS. Please remove me when you will go to PROD and uncomment the line aboce
		//createElementAndAppendItToparent(GlobalProperties.STAPLES_RETAILER_STUPID_SPS_PSEUDO_NAME, "TradingPartnerId", invoiceXmlDocument, invoiceHeader );
		if(null != invoicePojo.getShipDate()){
			String shipDate = basicDateFormat.format(invoicePojo.getShipDate());
			createElementIfPossibleAndAppendItToparent(shipDate, "ShipmentDate", invoiceXmlDocument, invoiceHeader );
		}
		createElementIfPossibleAndAppendItToparent(invoicePojo.getInvoiceNumber(), "InvoiceNumber", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(invoicePojo.getInvoiceDate()), "InvoiceDate", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(invoicePojo.getPoDate()), "PurchaseOrderDate", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(invoicePojo.getPoNumber(), "PurchaseOrderNumber", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getReleaseNumber(), "ReleaseNumber", invoiceXmlDocument, invoiceHeader);
		createElementAndAppendItToparent(document.getInvoiceTypeCode(), "InvoiceTypeCode", invoiceXmlDocument, invoiceHeader);
		createElementAndAppendItToparent(document.getBuyersCurrency(), "BuyersCurrency", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getVendor(), "Vendor", invoiceXmlDocument, invoiceHeader);
		//createElementIfPossibleAndAppendItToparent(document.getCustomerAccountNumber(), "CustomerAccountNumber", invoiceXmlDocument, invoiceHeader);
		//createElementIfPossibleAndAppendItToparent(document.getCustomerOrderNumber(), "CustomerOrderNumber", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getPromotionDealNumber(), "PromotionDealNumber", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getFOBPayCode(), "FOBPayCode", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getFOBLocationQualifier(), "FOBLocationQualifier", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getFOBLocationDescription(), "FOBLocationDescription", invoiceXmlDocument, invoiceHeader);
		/*createElementAndAppendItToparent(document.getFobTitlePassageCode(), "FOBTitlePassageCode", invoiceXmlDocument, invoiceHeader );
		createElementAndAppendItToparent(document.getFobTitlePassageLocation(), "FOBTitlePassageLocation", invoiceXmlDocument, invoiceHeader );*/
		createElementIfPossibleAndAppendItToparent(document.getCarrierTransMethodCode(), "CarrierTransMethodCode", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getCarrierEquipmentNumber(), "CarrierEquipmentNumber", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getCarrierAlphaCodeUpsn(), "CarrierAlphaCode", invoiceXmlDocument, invoiceHeader);
		createElementIfPossibleAndAppendItToparent(document.getCarrierRouting(), "CarrierRouting", invoiceXmlDocument, invoiceHeader);
		
		if(invoiceHeader.hasChildNodes()) header.appendChild(invoiceHeader);
		//InvoiceHeader
		
		//PaymentTerms
		String termsNetDueDateString = null;
		
		if(null != invoicePojo.getInvoiceDate()){			
			Calendar termsNetDueDate = Calendar.getInstance();
			termsNetDueDate.setTime(invoicePojo.getInvoiceDate());
			termsNetDueDate.add(Calendar.DAY_OF_MONTH, 90);						
			termsNetDueDateString = basicDateFormat.format(termsNetDueDate.getTime());
		}
					
		Element paymentTerms = invoiceXmlDocument.createElement("PaymentTerms");
		createElementAndAppendItToparent(document.getTermsType(), "TermsType", invoiceXmlDocument, paymentTerms);
		createElementAndAppendItToparent(document.getTermsBasisDateCode(), "TermsBasisDateCode", invoiceXmlDocument, paymentTerms);
		createElementIfPossibleAndAppendItToparent(document.getTermsDiscountPercentage(), "TermsDiscountPercentage", invoiceXmlDocument, paymentTerms);
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(todayNow), "TermsDiscountDate", invoiceXmlDocument, paymentTerms);
		createElementIfPossibleAndAppendItToparent(document.getTermsDiscountDueDays(), "TermsDiscountDueDays", invoiceXmlDocument, paymentTerms);
		createElementAndAppendItToparent(termsNetDueDateString, "TermsNetDueDate", invoiceXmlDocument, paymentTerms);
		createElementIfPossibleAndAppendItToparent(document.getTermsNetDueDays(), "TermsNetDueDays", invoiceXmlDocument, paymentTerms);
		createElementIfPossibleAndAppendItToparent(document.getTermsDiscountAmount(), "TermsDiscountAmount", invoiceXmlDocument, paymentTerms);
		createElementAndAppendItToparent(document.getTermsDescription(), "TermsDescription", invoiceXmlDocument, paymentTerms);
					
		if(paymentTerms.hasChildNodes()) header.appendChild(paymentTerms);
		//PaymentTerms
		
		//header dates
		List<SPSOrderDatePojo> spsOrderDarePojoList = document.getSpsOrderDatesPojoList();
		List<Element> headerDates = new ArrayList<>();
		if(null != spsOrderDarePojoList){
			for(SPSOrderDatePojo date : spsOrderDarePojoList){
				Element headerDate = invoiceXmlDocument.createElement("Date");
				if (date.getDateTimeQualifier().equalsIgnoreCase(SpsPoDocument.SHIP_DATE_DATETIME_QUALIFIER)){
					createElementIfPossibleAndAppendItToparent(date.getDateTimeQualifier(), "DateTimeQualifier1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(basicDateFormat.format(invoicePojo.getShipDate()), "Date1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(nowStr, "Time1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTimeCode(), "TimeCode1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimeFormQualifier(), "DateTimeFormQualifier1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimePeriod(), "DateTimePeriod", invoiceXmlDocument, headerDate );
				}
				else{
					createElementIfPossibleAndAppendItToparent(date.getDateTimeQualifier(), "DateTimeQualifier1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDate(), "Date1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTime(), "Time1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getTimeCode(), "TimeCode1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimeFormQualifier(), "DateTimeFormQualifier1", invoiceXmlDocument, headerDate );
					createElementIfPossibleAndAppendItToparent(date.getDateTimePeriod(), "DateTimePeriod", invoiceXmlDocument, headerDate );
				}				
				if(headerDate.hasChildNodes()) {
					headerDates.add(headerDate);
					header.appendChild(headerDate);
				}
			}
		}
		
		//header shipment date
		Element headerShipmentDate = invoiceXmlDocument.createElement("Date");
		createElementIfPossibleAndAppendItToparent(document.getInvoiceShipmentDateReferenceQualifier(), "DateTimeQualifier1", invoiceXmlDocument, headerShipmentDate );
		createElementIfPossibleAndAppendItToparent(basicDateFormat.format(invoicePojo.getShipDate()), "Date1", invoiceXmlDocument, headerShipmentDate );
		if(headerShipmentDate.hasChildNodes()) header.appendChild(headerShipmentDate);
		//header dates
		
		//header contact
		if(null != document.getHeaderContactName()){
			Element headerContact = invoiceXmlDocument.createElement("Contact");
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactTypeCode(), "ContactTypeCode", invoiceXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactName(), "ContactName", invoiceXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactPhone(), "ContactPhone", invoiceXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactFax(), "ContactFax", invoiceXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactEmail(), "ContactEmail", invoiceXmlDocument, headerContact );
			createElementIfPossibleAndAppendItToparent(document.getHeaderContactReference(), "ContactReference", invoiceXmlDocument, headerContact );
			if(headerContact.hasChildNodes()) header.appendChild(headerContact);
		}
		//header contact
		//header address
		Element headerAddress = invoiceXmlDocument.createElement("Address");
		createElementAndAppendItToparent(SpsPoDocument.SHIP_TO_ADDRESS_TYPE_CODE, "AddressTypeCode", invoiceXmlDocument, headerAddress );
		createElementAndAppendItToparent(document.getLocationQualifier(), "LocationCodeQualifier", invoiceXmlDocument, headerAddress );
		createElementAndAppendItToparent(invoicePojo.getShipToPostalCode(), "AddressLocationNumber", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToName(), "AddressName", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressAlternateName(), "AddressAlternateName", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressAlternateName2(), "AddressAlternateName2", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToAddress1(), "Address1", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress2(), "Address2", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress3(), "Address3", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddress4(), "Address4", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToCity(), "City", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToState(), "State", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToPostalCode(), "PostalCode", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(invoicePojo.getShipToCountry(), "Country", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getLocationID(), "LocationID", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getCountrySubDivision(), "CountrySubDivision", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressTaxIdNumber(), "AddressTaxIdNumber", invoiceXmlDocument, headerAddress );
		createElementIfPossibleAndAppendItToparent(document.getAddressTaxExemptNumber(), "AddressTaxExemptNumber", invoiceXmlDocument, headerAddress );
		
		
		if(headerAddress.hasChildNodes()) header.appendChild(headerAddress);
		//header address contact
		Element headerAddressContact = invoiceXmlDocument.createElement("Contact");
		createElementIfPossibleAndAppendItToparent(document.getShipToContactTypeCode(), "ContactTypeCode", invoiceXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactName(), "ContactName", invoiceXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactPhone(), "ContactPhone", invoiceXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactFax(), "ContactFax", invoiceXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactEmail(), "ContactEmail", invoiceXmlDocument, headerAddressContact );
		createElementIfPossibleAndAppendItToparent(document.getShipToContactReference(), "ContactReference", invoiceXmlDocument, headerAddressContact );
		if(headerAddressContact.hasChildNodes()) headerAddress.appendChild(headerAddressContact);
		//header address contact
		
		//header reference
		List<SpsReferencePojo> spsReferencePojoList = document.getSpsReferencePojoList();
		List<Element> headerReferences = new ArrayList<>();
		if(null != spsReferencePojoList){
			for(SpsReferencePojo reference : spsReferencePojoList){
				Element headerReference = invoiceXmlDocument.createElement("Reference");
				
				createElementIfPossibleAndAppendItToparent(reference.getReferenceQual(), "ReferenceQual", invoiceXmlDocument, headerReference );
				createElementIfPossibleAndAppendItToparent(reference.getReferenceID(), "ReferenceID", invoiceXmlDocument, headerReference );
				createElementIfPossibleAndAppendItToparent(reference.getDescription(), "Description", invoiceXmlDocument, headerReference );
				
				SpsReferencePojo spsInnerReferensePojo = reference.getInnerReferencePojo();
				if(null != spsInnerReferensePojo){
					Element referenceIDs = invoiceXmlDocument.createElement("ReferenceIDs");
					createElementIfPossibleAndAppendItToparent(spsInnerReferensePojo.getReferenceQual(), "ReferenceQual", invoiceXmlDocument, referenceIDs );
					createElementIfPossibleAndAppendItToparent(spsInnerReferensePojo.getReferenceID(), "ReferenceID", invoiceXmlDocument, referenceIDs );
					if(referenceIDs.hasChildNodes()) headerReference.appendChild(referenceIDs);
				}
				
				if(headerReference.hasChildNodes()) {
					headerReferences.add(headerReference);
					header.appendChild(headerReference);
				}
			}
		}
		Element mrReference = invoiceXmlDocument.createElement("Reference");
		createElementAndAppendItToparent(document.getMrReferenceQual(), "ReferenceQual", invoiceXmlDocument, mrReference );
		createElementAndAppendItToparent(document.getMrReferenceId(), "ReferenceID", invoiceXmlDocument, mrReference );
		header.appendChild(mrReference);
		
		Element bmReference = invoiceXmlDocument.createElement("Reference");
		createElementAndAppendItToparent(document.getBmReferenceQual(), "ReferenceQual", invoiceXmlDocument, bmReference );
		createElementAndAppendItToparent(invoicePojo.getShipmentTrackingNumbers(), "ReferenceID", invoiceXmlDocument, bmReference );
		header.appendChild(bmReference);
		
		//header notes
		List<SpsNotePojo> headerNotePojoList = document.getSpsHeaderNotePojoList();
		for (SpsNotePojo notePojo:headerNotePojoList){
			Element headerNote = invoiceXmlDocument.createElement("Notes");
			createElementIfPossibleAndAppendItToparent(notePojo.getNoteCode(), "NoteCode", invoiceXmlDocument, headerNote);
			createElementIfPossibleAndAppendItToparent(notePojo.getNoteInformationField(), "NoteInformationField", invoiceXmlDocument, headerNote);
			if (headerNote.hasChildNodes()) header.appendChild(headerNote);
		}		
		//header notes
		
		//Charges Allowances
		List<ChargesAllowancesPojo> headerSpsChargesAllowancesPojos = document.getSpsHeaderChargesAllowancesPojoList();
		for (ChargesAllowancesPojo chargesAllowances:headerSpsChargesAllowancesPojos){
			Element headerChargesAllowances = invoiceXmlDocument.createElement("ChargesAllowances");
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgIndicator(), "AllowChrgIndicator", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgCode(), "AllowChrgCode", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgAmt(), "AllowChrgAmt", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgPercentBasis(), "AllowChrgPercentBasis", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgPercent(), "AllowChrgPercent", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgHandlingCode(), "AllowChrgHandlingCode", invoiceXmlDocument, headerChargesAllowances);
			createElementIfPossibleAndAppendItToparent(chargesAllowances.getAllowChrgHandlingDescription(), "AllowChrgHandlingDescription", invoiceXmlDocument, headerChargesAllowances);
			if (headerChargesAllowances.hasChildNodes()) header.appendChild(headerChargesAllowances);
		}		
		//Charges Allowances
		
		invoice.appendChild(header);
		List<SpsDocumentItemPojo> spsOrderItems = document.getPoDocumentItemPojoList();
		Map<String, SpsDocumentItemPojo> vendorLineNumberSpsOiPojo = new HashMap<String, SpsDocumentItemPojo>();
		for(SpsDocumentItemPojo spsOiPojo : spsOrderItems){
			vendorLineNumberSpsOiPojo.put(spsOiPojo.getVendorLineNumber(), spsOiPojo);
		}
		
		int totalItems = 0;
		double totalAmount = 0;
		double totalQuantity = 0;
		double totalWeight = 0;
		double taxAmount=0;
		
		Element lineItems = invoiceXmlDocument.createElement("LineItems");
		for(InvoiceItemPojo oiPojo : invoicePojo.getItemList()){
			SpsDocumentItemPojo spsOiPojo = vendorLineNumberSpsOiPojo.get(oiPojo.getVendorlineNumber());
			//if there is no such item in PO file - do not process this fulfillment item
			if(null == spsOiPojo)
				continue;
			//lineItem
			Element lineItem = invoiceXmlDocument.createElement("LineItem");
			//InvoiceLine
			Element invoiceLine = invoiceXmlDocument.createElement("InvoiceLine");
			createElementAndAppendItToparent(oiPojo.getVendorlineNumber(), "LineSequenceNumber", invoiceXmlDocument, invoiceLine);
			//createElementIfPossibleAndAppendItToparent(oiPojo.getMerchantSKU(), "BuyerPartNumber", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getBuyerPartNumber(), "BuyerPartNumber", invoiceXmlDocument, invoiceLine);			
			createElementAndAppendItToparent(spsOiPojo.getVendorPartNumber(), "VendorPartNumber", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(oiPojo.getUPC(), "ConsumerPackageCode", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getEAN(), "EAN", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getGTIN(), "GTIN", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getUPCCaseCode(), "UPCCaseCode", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getNatlDrugCode(),"NatlDrugCode", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getInternationalStandardBookNumber(),"InternationalStandardBookNumber", invoiceXmlDocument, invoiceLine);
			//ProductID
			Element productID = invoiceXmlDocument.createElement("ProductID");
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getPartNumberQual(),"PartNumberQual", invoiceXmlDocument, productID);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getPartNumber(),"PartNumber", invoiceXmlDocument, productID);
			if(productID.hasChildNodes()) invoiceLine.appendChild(productID);
			//ProductID
			createElementAndAppendItToparent(Double.toString(oiPojo.getUnitPrice()), "UnitPrice", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(Double.toString(oiPojo.getInvoiceQty()),"ShipQty", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getOrderQtyUOM(), "ShipQtyUOM", invoiceXmlDocument, invoiceLine );
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getProductSizeDescription(),"ProductSizeDescription", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getProductColorDescription(),"ProductColorDescription", invoiceXmlDocument, invoiceLine);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getProductFabricDescription(),"ProductFabricDescription", invoiceXmlDocument, invoiceLine);
			//NRFStandardColorAndSize
			Element NRFStandardColorAndSize = invoiceXmlDocument.createElement("NRFStandardColorAndSize");
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getNRFColorCode(),"NRFColorCode", invoiceXmlDocument, NRFStandardColorAndSize);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getNRFSizeCode(),"NRFSizeCode", invoiceXmlDocument, NRFStandardColorAndSize);
			if(NRFStandardColorAndSize.hasChildNodes()) invoiceLine.appendChild(NRFStandardColorAndSize);
			//NRFStandardColorAndSize
			//InvoiceLine
			
			//ProductOrItemDescription
			Element productOrItemDescription = invoiceXmlDocument.createElement("ProductOrItemDescription");
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getItemDescriptionType(),"ItemDescriptionType", invoiceXmlDocument, productOrItemDescription);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getProductDescription(),"ProductDescription", invoiceXmlDocument, productOrItemDescription);
			if(productOrItemDescription.hasChildNodes()) invoiceLine.appendChild(productOrItemDescription);
			//ProductOrItemDescription
			
			//Reference
			Element reference = invoiceXmlDocument.createElement("Reference");
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getItemReferenceQual(), "ReferenceQual", invoiceXmlDocument, reference);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getItemReferenceId(),"ReferenceID", invoiceXmlDocument, reference);
			createElementIfPossibleAndAppendItToparent(spsOiPojo.getItemDescription(),"Description", invoiceXmlDocument, reference);
			//Reference
			lineItem.appendChild(invoiceLine);
			if(productOrItemDescription.hasChildNodes()) lineItem.appendChild(productOrItemDescription);
			if(reference.hasChildNodes()) lineItem.appendChild(reference);
			Element tax = invoiceXmlDocument.createElement("Tax");
			//Tax
			//if (invoicePojo.getTaxTotal()>0){				
			createElementAndAppendItToparent(document.getTaxTypeCode(), "TaxTypeCode", invoiceXmlDocument, tax);
			createElementAndAppendItToparent(doublePrecision.format(oiPojo.getTaxAmount()), "TaxAmount", invoiceXmlDocument, tax);
			double taxPercent = oiPojo.getTaxAmount()/(oiPojo.getUnitPrice()*oiPojo.getInvoiceQty())*100;				
			createElementAndAppendItToparent(doublePrecision.format(taxPercent), "TaxPercent", invoiceXmlDocument, tax);
			createElementAndAppendItToparent(document.getJurisdictionQual(), "JurisdictionQual", invoiceXmlDocument, tax);
			createElementAndAppendItToparent(document.getJurisdictionCode(), "JurisdictionCode", invoiceXmlDocument, tax);
			//createElementAndAppendItToparent(document.getTaxExemptCode(), "TaxExemptCode", invoiceXmlDocument, tax);
			createElementAndAppendItToparent(document.getTaxId(), "TaxID", invoiceXmlDocument, tax);
			//}			
			//Tax
			if(tax.hasChildNodes()) lineItem.appendChild(tax);
			//Charges Allowances
			List<ChargesAllowancesPojo> itemChargesAllowancesPojos = spsOiPojo.getSpsItemChargesAllowancesList();
			for (ChargesAllowancesPojo chargeAllowancesPojo:itemChargesAllowancesPojos){
				Element itemChargesAllowances = invoiceXmlDocument.createElement("ChargesAllowances");
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgIndicator(), "AllowChrgIndicator", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgCode(), "AllowChrgCode", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgAmt(), "AllowChrgAmt", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgPercentBasis(), "AllowChrgPercentBasis", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgPercent(), "AllowChrgPercent", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgHandlingCode(), "AllowChrgHandlingCode", invoiceXmlDocument, itemChargesAllowances);
				createElementIfPossibleAndAppendItToparent(chargeAllowancesPojo.getAllowChrgHandlingDescription(), "AllowChrgHandlingDescription", invoiceXmlDocument, itemChargesAllowances);
				if (itemChargesAllowances.hasChildNodes()) lineItem.appendChild(itemChargesAllowances);
			}			
			//Charges Allowances			
			lineItems.appendChild(lineItem);	
			taxAmount+=oiPojo.getTaxAmount();
			totalItems ++;
			totalAmount += oiPojo.getUnitPrice() * oiPojo.getInvoiceQty()+oiPojo.getTaxAmount();
			totalQuantity += oiPojo.getInvoiceQty();
		}
		invoice.appendChild(lineItems);		
		
		//Summary
		Element summary = invoiceXmlDocument.createElement("Summary");
		
		Element tax = invoiceXmlDocument.createElement("Tax");
		//Tax
		createElementAndAppendItToparent(document.getTaxTypeCode(), "TaxTypeCode", invoiceXmlDocument, tax);
		createElementAndAppendItToparent(doublePrecision.format(taxAmount), "TaxAmount", invoiceXmlDocument, tax);
		double taxPercent = taxAmount/(totalAmount-taxAmount)*100;				
		createElementAndAppendItToparent(doublePrecision.format(taxPercent), "TaxPercent", invoiceXmlDocument, tax);
		createElementAndAppendItToparent(document.getJurisdictionQual(), "JurisdictionQual", invoiceXmlDocument, tax);
		createElementAndAppendItToparent(document.getJurisdictionCode(), "JurisdictionCode", invoiceXmlDocument, tax);
		//createElementAndAppendItToparent(document.getTaxExemptCode(), "TaxExemptCode", invoiceXmlDocument, tax);
		createElementAndAppendItToparent(document.getTaxId(), "TaxID", invoiceXmlDocument, tax);					
		//Tax	
		summary.appendChild(tax);
		//Totals
		Element totals = invoiceXmlDocument.createElement("Totals");		
		String totalAmountFormated = doublePrecision.format(totalAmount);
		createElementAndAppendItToparent(totalAmountFormated, "TotalAmount", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(doublePrecision.format(invoicePojo.getTotalSalesOrderAmount()), "TotalNetSalesAmount", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(document.getTotalTermsDiscountAmount(), "TotalTermsDiscountAmount", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(Double.toString(totalQuantity), "TotalQtyInvoiced", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(triplePrecision.format(invoicePojo.getShipmentWeight()), "TotalWeight", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(Integer.toString(totalItems), "TotalLineItemNumber", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(totalAmountFormated, "InvoiceAmtDueByTermsDate", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(document.getTotalQuantityInvoicedUom(), "TotalQtyInvoicedUOM", invoiceXmlDocument, totals);
		createElementAndAppendItToparent(document.getShipmentWeightUm(), "TotalWeightUOM", invoiceXmlDocument, totals);
		//totals
		summary.appendChild(totals);
		//summary
		invoice.appendChild(summary);
		
		invoiceRootElement.appendChild(invoice);
		invoiceXmlDocument.appendChild(invoiceRootElement);
		
		String todayNowInFileName = new SimpleDateFormat("MMddyyyy_HHmm-ss-SSS").format(new Date());
		String filePath = GlobalPropertiesProvider.getGlobalProperties().getCurrentOutgoingMessagesDirectory() + File.separator + "INVOICE_PO_"+ invoicePojo.getPoNumber()+ "_"+ todayNowInFileName + ".xml";
		try {
			saveXmlDocumentToFileSystem(invoiceXmlDocument, filePath);
		} catch (TransformerException e) {
			throw new XmlMessageGenerationException(e.getMessage());
		}
		return filePath;
	}

}
