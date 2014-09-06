package com.malkos.poppin.util.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentItem;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.IntegrationDetailsProvider;

public class PoDocumentSaxEventHandler extends DefaultHandler {
		
		List<PoDocument> listPo = new ArrayList<PoDocument>();
		List<PoDocumentItem> listItemPo;
		PoDocument tempPo;
		PoDocumentItem tempItemPo;
		String tempElementValue;
		
		String addressTypeCode = "";
		int contactCount=0;
		
		boolean isParserInSubLines;
		
		private Logger logger = LoggerFactory.getLogger(PoDocumentSaxEventHandler.class);
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
			if(qName.equalsIgnoreCase("PurchaseOrder")){
				tempPo = new PoDocument();
				listItemPo = new ArrayList<PoDocumentItem>();				
			}
			
			if(qName.equalsIgnoreCase("LineItem")){
				tempItemPo = new PoDocumentItem();
			}
			
			if(qName.equalsIgnoreCase("Contact")){
				contactCount++;
			}
			
			if (qName.equalsIgnoreCase("Sublines")){
				isParserInSubLines  = true;
			}
		}
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			tempElementValue = new String(ch,start,length);
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException{
			if (qName.equalsIgnoreCase("Sublines")){
				isParserInSubLines  = false;
			}		
			
			if(qName.equalsIgnoreCase("AddressTypeCode")){
				addressTypeCode = tempElementValue;
			}
			
			if(qName.equalsIgnoreCase("PurchaseOrderNumber")){
				tempPo.setPoNumber(tempElementValue);
			}
			
			if (qName.equalsIgnoreCase("PurchaseOrderDate")){
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				//format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
				try {
					tempPo.setPoDate(format.parse(tempElementValue));
				} catch (ParseException e) {
					String error = "Could not parse PO date provided in xml PO file " + tempElementValue + ". Reason : " + e.getMessage();
					logger.warn(error);
					tempPo.setExceptionDescription(error);
				}
			}
			
			if(qName.equalsIgnoreCase("TradingPartnerId")){
				tempPo.setRetailerName(tempElementValue);
				//tempPo.setDepartmentName("Quill");
			}
			
			
			if((qName.equalsIgnoreCase("ContactName")) /*&& (contactCount==1)*/ && (addressTypeCode.equals("ST"))){
				tempPo.setCustomerName(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("ContactPhone"))/* && (contactCount==1) */ && (addressTypeCode.equals("ST"))){
				tempPo.setCustomerPhone(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("ContactEmail")) /*&& (contactCount==1)*/ && (addressTypeCode.equals("ST"))){
				tempPo.setCustomerEmail(tempElementValue);
			}
			if((qName.equalsIgnoreCase("ContactFax")) /*&& (contactCount==1) */&& (addressTypeCode.equals("ST"))){
				tempPo.setCustomerFax(tempElementValue);
			}
			
			//Shipping
			if((qName.equalsIgnoreCase("AddressName")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToName(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address1")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToAddress1(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address2")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToAddress2(tempElementValue);
			}
			
			/*if((qName.equalsIgnoreCase("Address3")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToAddress3(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address4")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToAddress4(tempElementValue);
			}*/
			
			if((qName.equalsIgnoreCase("City")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToCity(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("State")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToState(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("PostalCode")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToPostalCode(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Country")) && (addressTypeCode.equals("ST"))){
				tempPo.setShipToCountry(tempElementValue);
			}
			if ((qName.equalsIgnoreCase("AddressLocationNumber"))&&(addressTypeCode.equals("ST") || addressTypeCode.equals("BS"))){ //BS stands for Barnes and Noble shipping addres type code
				tempPo.setShipLocationNumber(tempElementValue);
			}	
			//Items
			if (!isParserInSubLines){
				if(qName.equalsIgnoreCase("LineSequenceNumber")){
					tempItemPo.setVendorlineNumber(tempElementValue);
				}
				
				if(qName.equalsIgnoreCase("VendorPartNumber")){
					tempItemPo.setPopMapperNum(tempElementValue);
				}
				if(qName.equalsIgnoreCase("BuyerPartNumber")){
					tempItemPo.setMerchantSKU(tempElementValue);
				}
				
				if(qName.equalsIgnoreCase("ConsumerPackageCode")){
					tempItemPo.setUPC(tempElementValue);
				}
					
				/*if(qName.equalsIgnoreCase("PartNumber")){
					tempItemPo.setItemNumber(tempElementValue);
				}*/
					
				if(qName.equalsIgnoreCase("OrderQty")){
					try {
						tempItemPo.setOrderQty(Double.parseDouble(tempElementValue));
					} catch (NumberFormatException e) {
						String error = "Could not parse item quantity provided in xml PO file " + tempElementValue + ". Reason : " + e.getMessage();
						logger.warn(error);
						tempPo.setExceptionDescription(error);
					}
				}

				if(qName.equalsIgnoreCase("UnitPrice")){
					try {
						tempItemPo.setUnitPrice(Double.parseDouble(tempElementValue));
					} catch (Exception e) {
						String error = "Could not parse item unit price provided in xml PO file " + tempElementValue + ". Reason : " + e.getMessage();
						logger.warn(error);
						tempPo.setExceptionDescription(error);
					}
				}
			}
			
			//Billiing
			if((qName.equalsIgnoreCase("AddressName")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToName(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address1")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToAddress1(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address2")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToAddress2(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address3")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToAddress3(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Address4")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToAddress4(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("City")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToCity(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("State")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToState(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("PostalCode")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToPostalCode(tempElementValue);
			}
			
			if((qName.equalsIgnoreCase("Country")) && (addressTypeCode.equals("BT"))){
				tempPo.setBillToCountry(tempElementValue);
			}

			if(qName.equalsIgnoreCase("LineItem")){
				listItemPo.add(tempItemPo);
			}
			
			if(qName.equalsIgnoreCase("LineItems")){
				contactCount=0;
			}
			if(qName.equalsIgnoreCase("PurchaseOrderTypeCode")){
				tempPo.setPurchaseOrderTypeCode(tempElementValue);
				IntegrationDetailsProvider detailsProvider = IntegrationDetailsProvider.getInstance();
				if(detailsProvider.getPoTypeCodeToPoType().containsKey(tempElementValue))
					tempPo.setPoType(detailsProvider.getPoTypeCodeToPoType().get(tempElementValue));
				else
					tempPo.setPoType(PurchaseOrderType.UNKNOWN);
			}
			if(qName.equalsIgnoreCase("PurchaseOrder") || (qName.equalsIgnoreCase("PurchaseOrderChange"))){
				tempPo.setPoDocumentItemList(listItemPo);
				listPo.add(tempPo);
				addressTypeCode="";
			}
			
		}
		
		public List<PoDocument> getPoDocumentList(){
			return listPo;
		}
}
