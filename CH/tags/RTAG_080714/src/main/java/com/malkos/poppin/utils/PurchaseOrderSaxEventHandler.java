package com.malkos.poppin.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.malkos.poppin.entities.MessageBatchTransfer;
import com.malkos.poppin.entities.OrderItemPojo;
import com.malkos.poppin.entities.PurchaseOrderStatus;
import com.malkos.poppin.entities.PurchaseOrderPojo;

public class PurchaseOrderSaxEventHandler extends DefaultHandler{
	
	private static Logger logger = LoggerFactory.getLogger(PurchaseOrderSaxEventHandler.class);
	
	List<PurchaseOrderPojo> listPo = new ArrayList<PurchaseOrderPojo>();
	List<OrderItemPojo> orderItemsPo = new ArrayList<OrderItemPojo>();
	PurchaseOrderPojo tempPo;
	OrderItemPojo tempItemPo;
	String tempElementValue;
	String tempPersonPlaceID = "";
	String tempBatchNumber;
	String tempPartnerIDName = "";
	
	MessageBatchTransfer mbTransfer;
	

	
	//SAX parser event hadnelrs
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equalsIgnoreCase("OrderMessageBatch")){
			tempBatchNumber = attributes.getValue("batchNumber");
			mbTransfer = new MessageBatchTransfer(tempBatchNumber);
	    }
		
		if(qName.equalsIgnoreCase("hubOrder")){
			tempPo = new PurchaseOrderPojo();
			tempPo.setOrderMessageBatch(tempBatchNumber);
			tempPo.setPartnerIDName(tempPartnerIDName);
	    }
		if(qName.equalsIgnoreCase("lineItem")){
			tempItemPo = new OrderItemPojo();
		}
				
		else if(qName.equalsIgnoreCase("partnerID")){
			tempPartnerIDName = attributes.getValue("name");
			//tempPo.setPartnerIDRoleType(attributes.getValue("roleType"));
		}
		else if(qName.equalsIgnoreCase("hubOrder")){
			try {
				tempPo.setTransactionID(Integer.parseInt(attributes.getValue("transactionID")));
			} catch (NumberFormatException e) {
				//logger.warn("Failed to parse transactionID from String to Integer ." + attributes.getValue("transactionID") + " is unparsible.");
				String rejectReason = "Failed to parse transactionID from String to Integer ." + attributes.getValue("transactionID") + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("participatingParty")){
			tempPo.setParticipatingPartyName(attributes.getValue("name"));
			tempPo.setParticipatingPartyRoleType(attributes.getValue("roleType"));
		}
		else if(qName.equalsIgnoreCase("shipTo")){
			tempPo.setShipToPersonPlaceID(attributes.getValue("personPlaceID"));
		}
		else if(qName.equalsIgnoreCase("customer")){
			tempPo.setCustomerPersonPlaceID(attributes.getValue("personPlaceID"));
		}
		else if(qName.equalsIgnoreCase("personPlace")){
			tempPersonPlaceID = attributes.getValue("personPlaceID");
		}
	}
	public void characters(char[] ch, int start, int length) throws SAXException {
		tempElementValue = new String(ch,start,length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("lineItem")){
			orderItemsPo.add(tempItemPo);
		}
		
		if(qName.equalsIgnoreCase("hubOrder")){
			tempPo.setOrderItems(orderItemsPo);
			orderItemsPo = new ArrayList<OrderItemPojo>();
			//if(!tempPo.getExceptionDesc().isEmpty())
			//	tempPo.setStatus(PurchaseOrderStatus.UNPROCESSIBLE_REJECTED);
			//else
			//	tempPo.setStatus(PurchaseOrderStatus.PENDING_POPPIN_PROCESSUAL);
			//listPo.add(tempPo);
			tempPo.validateRequiredFields();			
			mbTransfer.getPurchaseOrders().add(tempPo);
		}
		
		/*else if(qName.equalsIgnoreCase("partnerID")){
			tempPo.setPartnerID(tempElementValue);
		}*/
		else if(qName.equalsIgnoreCase("participatingParty")){
			tempPo.setParticipatingParty(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("sendersIdForReceiver")){
			try {
				tempPo.setSendersIdForReceiver(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse SendersIdForReceiver from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("orderId")){
			try {
				tempPo.setOrderId(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse OrderId from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
			
		}
		else if(qName.equalsIgnoreCase("lineCount")){
			try {
				tempPo.setLineCount(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse LineCount from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("poNumber")){
			tempPo.setPoNumber(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("orderDate")){
			try {
				tempPo.setOrderDate(tempElementValue);
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse OrderDate from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
			
		}
		else if(qName.equalsIgnoreCase("paymentMethod")){
			tempPo.setPaymentMethod(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("merchandiseCost")){
			tempPo.setMerchandiseCost(Float.parseFloat(tempElementValue));
		}
		else if(qName.equalsIgnoreCase("tax")){
			try {
				tempPo.setTax(Float.parseFloat(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse Tax from String to Float ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("salesDivision")){
			tempPo.setSalesDivision(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("custOrderNumber")){
			tempPo.setCustOrderNumber(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("packslipMessage")){
			tempPo.setPackslipMessage(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("merchandiseTypeCode")){
			tempPo.setMerchandiseTypeCode(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("merchDivision")){
			tempPo.setMerchDivision(tempElementValue);
		}

		else if(qName.equalsIgnoreCase("lineItemId")){
			try {
				tempItemPo.setLineItemId(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse LineItemId from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		/*else if(qName.equalsIgnoreCase("orderLineNumber")){
			tempItemPo.setOrderLineNumber(Integer.parseInt(tempElementValue));
		}*/
		else if(qName.equalsIgnoreCase("merchantLineNumber")){
			try {
				tempItemPo.setMerchantLineNumber(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse MerchantLineNumber from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("qtyOrdered")){
			try {
				tempItemPo.setQtyOrdered(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse qtyOrdered from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("unitOfMeasure")){
			tempItemPo.setUnitOfMeasure(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("UPC")){
			tempItemPo.setUPC(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("description")){
			tempItemPo.setDescription(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("merchantSKU")){
			tempItemPo.setMerchantSKU(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("vendorSKU")){
			tempItemPo.setVendorSKU(tempElementValue);
			tempItemPo.setModelNum(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("unitPrice")){
			try {
				tempItemPo.setUnitPrice(Double.parseDouble(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse UnitPrice from String to Float ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("unitCost")){
			try {
				tempItemPo.setUnitCost(Double.parseDouble(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse UnitCost from String to Float ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("lineMerchandise")){
			try {
				tempItemPo.setLineMerchandise(Float.parseFloat(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse LineMerchandise from String to Float ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("shippingCode")){
			tempItemPo.setShippingCode(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("vendorDescription")){
			tempItemPo.setVendorDescription(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("lineReqDelvDate")){
			try {
				tempItemPo.setLineReqDelvDate(Integer.parseInt(tempElementValue));
			} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse lineReqDelvDate from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
			}
		}
		else if(qName.equalsIgnoreCase("vendorMessage")){
			tempItemPo.setVendorMessage(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("nameValuePair")){
			tempItemPo.setNameValuePair(tempElementValue);
		}
		/*
		 else if(qName.equalsIgnoreCase("factoryOrderNumber")){
			tempItemPo.setFactoryOrderNumber(tempElementValue);
		}*/
		else if(qName.equalsIgnoreCase("subUnitQty")){
			tempItemPo.setSubUnitQty(Integer.parseInt(tempElementValue));
		}
		else if(qName.equalsIgnoreCase("name1") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerName1(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("name1") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToName1(tempElementValue);
		}
		
		else if(qName.equalsIgnoreCase("address1") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerAddress1(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("address1") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToAddress1(tempElementValue);
		}
		
		else if(qName.equalsIgnoreCase("address2") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerAddress2(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("address2") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToAddress2(tempElementValue);
		}
		
		else if(qName.equalsIgnoreCase("address3") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerAddress3(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("address3") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToAddress3(tempElementValue);
		}
		
		else if(qName.equalsIgnoreCase("city") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerCity(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("city") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToCity(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("state") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerState(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("state") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToState(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("country") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerCountry(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("country") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToCountry(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("postalCode") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerPostalCode(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("postalCode") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToPostalCode(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("email") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerEmail(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("email") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToEmail(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("dayPhone") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerDayPhone(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("dayPhone") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToDayPhone(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("companyName") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			tempPo.setCustomerCompanyName(tempElementValue);
		}
		else if(qName.equalsIgnoreCase("companyName") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			tempPo.setShipToCompanyName(tempElementValue);
		}

		else if(qName.equalsIgnoreCase("partnerPersonPlaceId") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getCustomerPersonPlaceID())){
			//try {
				//tempPo.setCustomerPartnerPersonPlaceId(Integer.parseInt(tempElementValue));
				tempPo.setCustomerPartnerPersonPlaceId(tempElementValue);
			/*} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse CustomerPartnerPersonPlaceId from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
				ErrorsCollector.addErrorMessage(rejectReason);
			}*/
		}
		else if(qName.equalsIgnoreCase("partnerPersonPlaceId") && 
				tempPersonPlaceID.equalsIgnoreCase(tempPo.getShipToPersonPlaceID())){
			//try {
				//tempPo.setShipToPartnerPersonPlaceId(Integer.parseInt(tempElementValue));
				tempPo.setShipToPartnerPersonPlaceId(tempElementValue);
			/*} catch (NumberFormatException e) {
				String rejectReason = "Failed to parse ShipToPartnerPersonPlaceId from String to Integer ." + tempElementValue + " is unparsible.";
				logger.warn(rejectReason);
				addException(rejectReason);
				ErrorsCollector.addErrorMessage(rejectReason);
			}*/
		}
	
	}
	public MessageBatchTransfer getMessageBatchTransfer(){
		return mbTransfer;
	}
	
	private void addException(String exception){
		String previousExceptions = tempPo.getExceptionDesc();
		if (previousExceptions!=null){
			tempPo.setExceptionDesc(previousExceptions+exception+"\r\n");
		} else {
			tempPo.setExceptionDesc(exception+"\r\n");
		}		
	}
}
