package com.malkos.poppin.documents;

import java.util.Date;
import java.util.List;

import com.malkos.poppin.entities.AddSalesOrderResultPojo;
import com.malkos.poppin.entities.OrderErrorDetails;
import com.malkos.poppin.entities.enums.PurchaseOrderStatus;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.retailers.RetailerAbstract;

public class PoDocument {
	
	private RetailerAbstract retailer;
	private String poNumber;
	private PurchaseOrderType poType/* = PurchaseOrderType.DROPSHIP*/;
	private int retailerId;
	private List<PoDocumentItem> poDocumentItemList;
	private Date poDate;
	private String salesOrderInternalId;
	private String retailerName;
	private String customerName;
	private String customerPhone;
	private String customerEmail;
	private String customerFax;
	//Shipping
	private String shipToName;
	private String shipToAddress1;
	private String shipToAddress2;
	private String shipToCity;
	private String shipToState;
	private String shipToPostalCode;
	private String shipToCountry;
	private String shipLocationNumber;
	
	private OrderErrorDetails errorDetails;
	//billing
	private String billToName;
	private String billToAddress1;
	private String billToAddress2;
	private String billToAddress3;
	private String billToAddress4;
	private String billToCity;
	private String billToState;
	private String billToPostalCode;
	private String billToCountry;
	
	private String incomingMessagePath;
	private String exceptionDescription;
	
	private boolean isAsnGenerated;
	private boolean isInvoiceMessageGenerated;
	private int incomingMessageId;
	
	private PurchaseOrderStatus processingStatus;
	
	private AddSalesOrderResultPojo addSalesOrderResultPojo;
	private String purchaseOrderTypeCode;
	
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public PurchaseOrderType getPoType() {
		return poType;
	}
	public void setPoType(PurchaseOrderType poType) {
		this.poType = poType;
	}
	public int getRetailerId() {
		return retailerId;
	}
	public void setRetailerId(int retailerId) {
		this.retailerId = retailerId;
	}
	public List<PoDocumentItem> getPoDocumentItemList() {
		return poDocumentItemList;
	}
	public void setPoDocumentItemList(List<PoDocumentItem> poDocumentItemList) {
		this.poDocumentItemList = poDocumentItemList;
	}
	public Date getPoDate() {
		return poDate;
	}
	public void setPoDate(Date poDate) {
		this.poDate = poDate;
	}
	public String getRetailerName() {
		return retailerName;
	}
	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerPhone() {
		return customerPhone;
	}
	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getShipToName() {
		return shipToName;
	}
	public void setShipToName(String shipToName) {
		this.shipToName = shipToName;
	}
	public String getShipToAddress1() {
		return shipToAddress1;
	}
	public void setShipToAddress1(String shipToAddress1) {
		this.shipToAddress1 = shipToAddress1;
	}
	public String getShipToAddress2() {
		return shipToAddress2;
	}
	public void setShipToAddress2(String shipToAddress2) {
		this.shipToAddress2 = shipToAddress2;
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
	public String getShipToPostalCode() {
		return shipToPostalCode;
	}
	public void setShipToPostalCode(String shipToPostalCode) {
		this.shipToPostalCode = shipToPostalCode;
	}
	public String getShipToCountry() {
		return shipToCountry;
	}
	public void setShipToCountry(String shipToCountry) {
		this.shipToCountry = shipToCountry;
	}
	public String getCustomerFax() {
		return customerFax;
	}
	public void setCustomerFax(String customerFax) {
		this.customerFax = customerFax;
	}
	public String getShipLocationNumber() {
		return shipLocationNumber;
	}
	public void setShipLocationNumber(String shipLocationNumber) {
		this.shipLocationNumber = shipLocationNumber;
	}
	public void setIncomingMessagePath(String incomingMessagePath) {
		this.incomingMessagePath = incomingMessagePath;
	}
	public String getIncomingMessagePath(){
		return this.incomingMessagePath;
	}
	public void setExceptionDescription(String exceptionDescription) {
		this.exceptionDescription = exceptionDescription;
	}
	public String getExceptionDescription() {
		return this.exceptionDescription;
	}
	/**
	 * @return the isAsnGenerated
	 */
	public boolean isAsnGenerated() {
		return isAsnGenerated;
	}
	/**
	 * @param isAsnGenerated the isAsnGenerated to set
	 */
	public void setAsnGenerated(boolean isAsnGenerated) {
		this.isAsnGenerated = isAsnGenerated;
	}
	/**
	 * @return the isInvoiceMessageGenerated
	 */
	public boolean isInvoiceMessageGenerated() {
		return isInvoiceMessageGenerated;
	}
	/**
	 * @param isInvoiceMessageGenerated the isInvoiceMessageGenerated to set
	 */
	public void setInvoiceMessageGenerated(boolean isInvoiceMessageGenerated) {
		this.isInvoiceMessageGenerated = isInvoiceMessageGenerated;
	}
	public void setIncomingMessageId(int idIncomingMessage) {
		this.incomingMessageId = idIncomingMessage;
	}
	public int getIncomingMessageId(){
		return this.incomingMessageId;
	}
	/**
	 * @return the processingStatus
	 */
	public PurchaseOrderStatus getProcessingStatus() {
		return processingStatus;
	}
	/**
	 * @param processingStatus the processingStatus to set
	 */
	public void setProcessingStatus(PurchaseOrderStatus processingStatus) {
		this.processingStatus = processingStatus;
	}
	public void updateStatus() {
		boolean errorExists = (null != exceptionDescription) ? true : false;
		if(null == processingStatus){
			if(errorExists)
				processingStatus = PurchaseOrderStatus.REJECTED_AS_UNPROCESSIBLE;
			else
				processingStatus = PurchaseOrderStatus.PENDING_POPPIN_PROCESSING;
		}
		else if(processingStatus == PurchaseOrderStatus.PENDING_POPPIN_PROCESSING){
			if(errorExists)
				processingStatus = PurchaseOrderStatus.POPPIN_REJECTED;
			else
				processingStatus = PurchaseOrderStatus.POPPIN_PROCESSED;
		}
	}
	/**
	 * @return the addSalesOrderResultPojo
	 */
	public AddSalesOrderResultPojo getAddSalesOrderResultPojo() {
		return addSalesOrderResultPojo;
	}
	/**
	 * @param addSalesOrderResultPojo the addSalesOrderResultPojo to set
	 */
	public void setAddSalesOrderResultPojo(AddSalesOrderResultPojo addSalesOrderResultPojo) {
		this.addSalesOrderResultPojo = addSalesOrderResultPojo;
	}
	public String getBillToName() {
		return billToName;
	}
	public void setBillToName(String billToName) {
		this.billToName = billToName;
	}
	public String getBillToAddress1() {
		return billToAddress1;
	}
	public void setBillToAddress1(String billToAddress1) {
		this.billToAddress1 = billToAddress1;
	}
	public String getBillToAddress2() {
		return billToAddress2;
	}
	public void setBillToAddress2(String billToAddress2) {
		this.billToAddress2 = billToAddress2;
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
	/**
	 * @return the salesOrderInternalId
	 */
	public String getSalesOrderInternalId() {
		return salesOrderInternalId;
	}
	/**
	 * @param salesOrderInternalId the salesOrderInternalId to set
	 */
	public void setSalesOrderInternalId(String salesOrderInternalId) {
		this.salesOrderInternalId = salesOrderInternalId;
	}
	public void setPurchaseOrderTypeCode(String purchaseOrderTypeCode) {
		this.purchaseOrderTypeCode = purchaseOrderTypeCode;
	}
	public String getPurchaseOrderTypeCode() {
		return this.purchaseOrderTypeCode;
	}
	public RetailerAbstract getRetailer() {
		return retailer;
	}
	public void setRetailer(RetailerAbstract retailer) {
		this.retailer = retailer;
	}
	public OrderErrorDetails getErrorDetails() {
		return errorDetails;
	}
	public void setErrorDetails(OrderErrorDetails errorDetails) {
		this.errorDetails = errorDetails;
	}
}
