package com.malkos.poppin.entities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;

public class PurchaseOrderPojo {
	private int id;
	private PurchaseOrderStatus status;
	private String exceptionDesc = "";
	/*
	 * not needed now
	 * 
	private String partnerID;
	private String partnerIDRoleType;
	*/
	private String orderBatchPath;
	private String orderMessageBatch;
	private String partnerIDName;
	
	private int transactionID;/*sales order data*/
	private String  participatingParty;/*sales order data*/
	private String  participatingPartyName;/*sales order data*/
	private String  participatingPartyRoleType;/*sales order data*/
	private int  sendersIdForReceiver;/*sales order data*/
	private int orderId;/*sales order data*/
	private int lineCount;/*sales order data*/
	private String poNumber;/*sales order data*/
	private String orderDate;/*sales order data*/
	private String paymentMethod;/*sales order data*/
	private float merchandiseCost;/*sales order data*/
	private float tax;/*sales order data*/
	private String shipToPersonPlaceID;/*needed to recognize shipping address */
	private String customerPersonPlaceID;/*needed to recognize shipping address */
	private String salesDivision;/*sales order data*/
	private String custOrderNumber;/*sales order data*/
	private String packslipMessage;/*sales order data*/
	private String merchandiseTypeCode;/*sales order data*/
	private String merchDivision;/*sales order data*/
		
	private String shipToName1;/*shipping*/
	private String shipToAddress1;/*shipping*/
	private String shipToAddress2;/*shipping*/
	private String shipToAddress3;/*shipping*/
	private String shipToCity;/*shipping*/
	private String shipToState;/*shipping*/
	private String shipToCountry;/*shipping*/
	private String shipToPostalCode;/*shipping*/
	private String shipToEmail;/*shipping*/
	private String shipToDayPhone;/*shipping*/
	private String shipToPartnerPersonPlaceId;/*shipping*/
	private String shipToCompanyName;/*shipping*/
	
	private String customerName1;/*billing*/
	private String customerAddress1;/*billing*/
	private String customerAddress2;/*billing*/
	private String customerAddress3;/*billing*/
	private String customerCity;/*billing*/
	private String customerState;/*billing*/
	private String customerCountry;/*billing*/
	private String customerPostalCode;/*billing*/
	private String customerEmail;/*billing*/
	private String customerDayPhone;/*billing*/
	private String customerPartnerPersonPlaceId;/*billing*/
	private String customerCompanyName;/*billing*/
	
	private List<OrderItemPojo> orderItems;/*list of items*/
	private boolean isDuplicate;
	
	private OrderErrorDetails errorDetails;
	
	public PurchaseOrderPojo() {
		orderItems = new ArrayList<OrderItemPojo>();
	}
	
	
	public String getOrderMessageBatch() {
		return orderMessageBatch;
	}
	public void setOrderMessageBatch(String orderMessageBatch) {
		this.orderMessageBatch = orderMessageBatch;
	}
	public String getPartnerIDName() {
		return partnerIDName;
	}
	public void setPartnerIDName(String partnerIDName) {
		this.partnerIDName = partnerIDName;
	}
	public String getShipToName1() {
		return shipToName1;
	}
	public void setShipToName1(String shipToName1) {
		this.shipToName1 = shipToName1;
	}
	public String getShipToAddress1() {
		return shipToAddress1;
	}
	public void setShipToAddress1(String shipToAddress1) {
		this.shipToAddress1 = shipToAddress1;
	}
	public String getShipToCity() {
		return shipToCity;
	}
	public void setShipToCity(String shipToCity) {
		this.shipToCity = shipToCity;
	}
	public String getShipToState() {
		return shipToState;
	}
	public void setShipToState(String shipToState) {
		this.shipToState = shipToState;
	}
	public String getShipToCountry() {
		return shipToCountry;
	}
	public void setShipToCountry(String shipToCountry) {
		this.shipToCountry = shipToCountry;
	}
	public String getShipToPostalCode() {
		return shipToPostalCode;
	}
	public void setShipToPostalCode(String shipToPostalCode) {
		this.shipToPostalCode = shipToPostalCode;
	}
	public String getShipToEmail() {
		return shipToEmail;
	}
	public void setShipToEmail(String shipToEmail) {
		this.shipToEmail = shipToEmail;
	}
	public String getShipToDayPhone() {
		return shipToDayPhone;
	}
	public void setShipToDayPhone(String shipToDayPhone) {
		this.shipToDayPhone = shipToDayPhone;
	}
	public String getShipToPartnerPersonPlaceId() {
		return shipToPartnerPersonPlaceId;
	}
	public void setShipToPartnerPersonPlaceId(String shipToPartnerPersonPlaceId) {
		this.shipToPartnerPersonPlaceId = shipToPartnerPersonPlaceId;
	}
	public String getShipToCompanyName() {
		return shipToCompanyName;
	}
	public void setShipToCompanyName(String shipToCompanyName) {
		this.shipToCompanyName = shipToCompanyName;
	}
	public String getCustomerName1() {
		return customerName1;
	}
	public void setCustomerName1(String customerName1) {
		this.customerName1 = customerName1;
	}
	public String getCustomerAddress1() {
		return customerAddress1;
	}
	public void setCustomerAddress1(String customerAddress1) {
		this.customerAddress1 = customerAddress1;
	}
	public String getCustomerCity() {
		return customerCity;
	}
	public void setCustomerCity(String customerCity) {
		this.customerCity = customerCity;
	}
	public String getCustomerState() {
		return customerState;
	}
	public void setCustomerState(String customerState) {
		this.customerState = customerState;
	}
	public String getCustomerCountry() {
		return customerCountry;
	}
	public void setCustomerCountry(String customerCountry) {
		this.customerCountry = customerCountry;
	}
	public String getCustomerPostalCode() {
		return customerPostalCode;
	}
	public void setCustomerPostalCode(String customerPostalCode) {
		this.customerPostalCode = customerPostalCode;
	}
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getCustomerDayPhone() {
		return customerDayPhone;
	}
	public void setCustomerDayPhone(String customerDayPhone) {
		this.customerDayPhone = customerDayPhone;
	}
	public String getCustomerPartnerPersonPlaceId() {
		return customerPartnerPersonPlaceId;
	}
	public void setCustomerPartnerPersonPlaceId(String customerPartnerPersonPlaceId) {
		this.customerPartnerPersonPlaceId = customerPartnerPersonPlaceId;
	}
	public String getCustomerCompanyName() {
		return customerCompanyName;
	}
	public void setCustomerCompanyName(String customerCompanyName) {
		this.customerCompanyName = customerCompanyName;
	}
		
	public String getPackslipMessage() {
		return packslipMessage;
	}
	public void setPackslipMessage(String packslipMessage) {
		this.packslipMessage = packslipMessage;
	}
	public String getMerchandiseTypeCode() {
		return merchandiseTypeCode;
	}
	public void setMerchandiseTypeCode(String merchandiseTypeCode) {
		this.merchandiseTypeCode = merchandiseTypeCode;
	}
	public String getMerchDivision() {
		return merchDivision;
	}
	public void setMerchDivision(String merchDivision) {
		this.merchDivision = merchDivision;
	}
	public String getParticipatingParty() {
		return participatingParty;
	}
	public void setParticipatingParty(String participatingParty) {
		this.participatingParty = participatingParty;
	}
	public String getParticipatingPartyName() {
		return participatingPartyName;
	}
	public void setParticipatingPartyName(String participatingPartyName) {
		this.participatingPartyName = participatingPartyName;
	}
	public String getParticipatingPartyRoleType() {
		return participatingPartyRoleType;
	}
	public void setParticipatingPartyRoleType(String participatingPartyRoleType) {
		this.participatingPartyRoleType = participatingPartyRoleType;
	}
	public int getSendersIdForReceiver() {
		return sendersIdForReceiver;
	}
	public void setSendersIdForReceiver(int sendersIdForReceiver) {
		this.sendersIdForReceiver = sendersIdForReceiver;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getLineCount() {
		return lineCount;
	}
	public void setLineCount(int lineCount) {
		this.lineCount = lineCount;
	}
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public float getMerchandiseCost() {
		return merchandiseCost;
	}
	public void setMerchandiseCost(float merchandiseCost) {
		this.merchandiseCost = merchandiseCost;
	}
	public float getTax() {
		return tax;
	}
	public void setTax(float tax) {
		this.tax = tax;
	}
	public String getShipToPersonPlaceID() {
		return shipToPersonPlaceID;
	}
	public void setShipToPersonPlaceID(String shipToPersonPlaceID) {
		this.shipToPersonPlaceID = shipToPersonPlaceID;
	}
	public String getCustomerPersonPlaceID() {
		return customerPersonPlaceID;
	}
	public void setCustomerPersonPlaceID(String customerPersonPlaceID) {
		this.customerPersonPlaceID = customerPersonPlaceID;
	}
	public String getSalesDivision() {
		return salesDivision;
	}
	public void setSalesDivision(String salesDivision) {
		this.salesDivision = salesDivision;
	}
	public String getCustOrderNumber() {
		return custOrderNumber;
	}
	public void setCustOrderNumber(String custOrderNumber) {
		this.custOrderNumber = custOrderNumber;
	}
	
	public int getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}
	public String getShipToAddress2() {
		return shipToAddress2;
	}
	public void setShipToAddress2(String shipToAddress2) {
		this.shipToAddress2 = shipToAddress2;
	}
	public String getShipToAddress3() {
		return shipToAddress3;
	}
	public void setShipToAddress3(String shipToAddress3) {
		this.shipToAddress3 = shipToAddress3;
	}
	public String getCustomerAddress2() {
		return customerAddress2;
	}
	public void setCustomerAddress2(String customerAddress2) {
		this.customerAddress2 = customerAddress2;
	}
	public String getCustomerAddress3() {
		return customerAddress3;
	}
	public void setCustomerAddress3(String customerAddress3) {
		this.customerAddress3 = customerAddress3;
	}
	
	public List<OrderItemPojo> getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(List<OrderItemPojo> orderItems) {
		this.orderItems = orderItems;
	}
	
	public PurchaseOrderStatus getStatus() {
		return status;
	}
	public void setStatus(PurchaseOrderStatus status) {
		this.status = status;
	}
	public String getExceptionDesc() {
		return exceptionDesc;
	}
	public void setExceptionDesc(String exceptionDesc) {
		this.exceptionDesc = exceptionDesc;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			Class c = Class.forName(this.getClass().getName());
			Method m[] = c.getDeclaredMethods();
			Object oo;

			for (int i = 0; i < m.length; i++)
				if (m[i].getName().startsWith("get") && (m[i].getName() != "setOrderItems")) {
					oo = m[i].invoke(this, null);
					sb.append("[" + m[i].getName().substring(3) + "]: "
							+ String.valueOf(oo) + ", \n");
				}
			sb.append("\n\n\n");
		} catch (Throwable e) {
			System.err.println(e);
		}
		return sb.toString();
	}
	public void validateRequiredFields(){
		Set<String> errorPoint = new HashSet<>();
		if(null == poNumber)
			errorPoint.add("poNumber");
		//if(null == customerEmail)
		//	errorPoint = "email";
		if(null == customerCompanyName)
			errorPoint.add("companyName");
		if(null == customerName1)
			errorPoint.add("customer/personPlace/name1");
		if(null == customerDayPhone)
			errorPoint.add("dayPhone");
		if(orderId == 0)
			errorPoint.add("orderId");
		if(orderDate == null)
			errorPoint.add("orderDate");
		//if(null == customerEmail)
		//	errorPoint = "customer/personPlace/email";
		if(null == customerAddress1)
			errorPoint.add("customer/personPlace/address1");
		if(null == customerCity)
			errorPoint.add("customer/personPlace/city");
		if(null == customerPostalCode)
			errorPoint.add("customer/personPlace/postalCode");
		if(null == customerState)
			errorPoint.add("customer/personPlace/state");
		
		if(null == shipToName1)
			errorPoint.add("shipTo/personPlace/name1");
		if(null == shipToDayPhone)
			errorPoint.add("shipTo/personPlace/dayPhone");
		//if(null == shipToEmail)
		//	errorPoint = "shipTo/personPlace/email";
		if(null == shipToAddress1)
			errorPoint.add("shipTo/personPlace/address1");
		if(null == shipToCity)
			errorPoint.add("shipTo/personPlace/city");
		if(null == shipToPostalCode)
			errorPoint.add("shipTo/personPlace/postalCode");
		if(null == shipToState)
			errorPoint.add("shipTo/personPlace/state");
		if(null == shipToPartnerPersonPlaceId)
			errorPoint.add("shipTo/personPlace/partnerPersonPlaceId");
		
		for(OrderItemPojo oip : orderItems){
			if(null == oip.getVendorSKU())
				errorPoint.add("vendorSKU");
			if(0 == oip.getMerchantLineNumber())
				errorPoint.add("merchantLineNumber");
			if(0.0d == oip.getQtyOrdered())
				errorPoint.add("qtyOrdered");
			if(null == oip.getUnitCost())
				errorPoint.add("unitCost");
			if(null == oip.getMerchantSKU())
				errorPoint.add("merchantSKU");
			if(null == oip.getDescription())
				errorPoint.add("description");			
			}
		
		if(!errorPoint.isEmpty()){
			String errorFields = new String();
			for (String field:errorPoint){
				errorFields += field + ", ";
			}			
			addException("Provided orders file didn't contain all the required fields. Please specify value for " + errorFields.substring(0, errorFields.length()-2) + " field(s).");			
			//setStatus(PurchaseOrderStatus.UNPROCESSIBLE_REJECTED);
		}
	}

	public void addException(String exception){
		String parseException = getExceptionDesc();
		if (parseException!=null){
			setExceptionDesc(parseException+exception+"\r\n");
		} else {
			setExceptionDesc(exception+"\r\n");
		}
	}
	
	public void setIsDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}
	public boolean getIsDuplicate() {
		return this.isDuplicate;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getOrderBatchPath() {
		return orderBatchPath;
	}


	public void setOrderBatchPath(String orderBatchPath) {
		this.orderBatchPath = orderBatchPath;
	}


	public void validateInventoryMapping(Map<String,String> vendorSkuToInventoryDAOMap) {
		List<String> popNumsErrored = new ArrayList<>();
		for(OrderItemPojo oip : orderItems){
			String popNum = oip.getVendorSKU();
			if (! vendorSkuToInventoryDAOMap.containsKey(popNum)){
				popNumsErrored.add(popNum);
				addException("Couldn't find appropriate to vendorSKU='"+popNum+"' item in NetSuite");	
			}
		}
		if(popNumsErrored.size() > 0){
			if(errorDetails == null){
				errorDetails = new OrderErrorDetails();
			}
			errorDetails.setErrorVendorPartNumbers(popNumsErrored);
		}
	}


	public OrderErrorDetails getErrorDetails() {
		return errorDetails;
	}


	public void setErrorDetails(OrderErrorDetails errorDetails) {
		this.errorDetails = errorDetails;
	}
}
