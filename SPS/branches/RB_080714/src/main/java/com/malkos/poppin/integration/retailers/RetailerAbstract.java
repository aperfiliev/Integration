package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.bootstrap.ApplicationContextProvider;
import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentItem;
import com.malkos.poppin.entities.AddSalesOrderResultPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.IntegrationDetailsProvider;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.util.ErrorsCollector;
import com.malkos.poppin.util.xml.generators.IXmlMessagesGenerator;
import com.malkos.poppin.util.xml.generators.impl.XmlMessageGenerationException;
import com.malkos.poppin.validation.IValidator;
import com.netsuite.webservices.transactions.sales_2013_1.types.SalesOrderOrderStatus;

public abstract class RetailerAbstract {
	
	private List<PoDocument> poDocumentList;
	private List<FulfillmentPojo> fulFilmentPojoList;
	private List<InvoicePojo> invoicesPojoList;
	
	private String retailerName;
	private String companyEmail;
	private String companyInternalId;
	private String departmentInternalId;
	protected String shortName;
	protected SalesOrderOrderStatus salesOrderStatus;
	protected String shippingMethodInternalId;	
	
	private int retailerId;
	
	protected String termInternalId;
	protected Set<PurchaseOrderType> retailerDocumentTypes;
	
	protected Map<PurchaseOrderType, IValidator> validators = new HashMap<>();
	protected Map<PurchaseOrderType, IXmlMessagesGenerator> messageGenerators = new HashMap<>();
	protected Map<String, String> poppinAssortment = new HashMap<>();
	
	protected INetsuiteOperationsManager netsuiteOperationsManager;
	
	public RetailerAbstract(String retailerName, String companyEmail, String companyInternalId, String departmentInternalId, int retailerId){
		poDocumentList = new ArrayList<>();
		fulFilmentPojoList = new ArrayList<>();
		invoicesPojoList = new ArrayList<>();
		
		this.retailerName = retailerName;
		this.companyEmail = companyEmail;
		this.companyInternalId = companyInternalId;
		this.departmentInternalId = departmentInternalId;
		this.retailerId = retailerId;
		this.termInternalId = "8";
		this.shippingMethodInternalId="3250";
		this.salesOrderStatus = SalesOrderOrderStatus._pendingFulfillment;
				
		netsuiteOperationsManager = (INetsuiteOperationsManager) ApplicationContextProvider.getApplicationContext().getBean("netsuiteOperationsManager");
		retailerDocumentTypes = new HashSet<>();
	}
	public void addPoDocument(PoDocument document){
		IntegrationDetailsProvider idProvider = IntegrationDetailsProvider.getInstance();
		Map<String, String> poppinAssortment = idProvider.getPoppinAssortmentByRetailer(retailerId);
		fillInDocumentData(document, poppinAssortment);
		//document.setRetailer(this);
		getPoDocumentList().add(document);
	}
	public void addFulfillMent(FulfillmentPojo fPojo) {
		fulFilmentPojoList.add(fPojo);
	}
	protected void fillInDocumentData(PoDocument document, Map<String, String> poppinAssortment){
		document.setRetailerId(retailerId);
		for(PoDocumentItem pdItem : document.getPoDocumentItemList()){
			if(poppinAssortment.containsKey(pdItem.getPopMapperNum()))
				pdItem.setItemInternalId(poppinAssortment.get(pdItem.getPopMapperNum()));
		}
	}
	
	void validate(){
		for(PoDocument document : getPoDocumentList()){
			IValidator validator = this.validators.get(document.getPoType());
			validator.validate(document);
		}
	}
	List<String> generateAsnMessages() {
		List<String> asnMessagesPaths = new ArrayList<>();
		for(FulfillmentPojo fPojo : getFulFilmentPojoList()){
			if(null == fPojo.getExceptionDescription()){
				IXmlMessagesGenerator messageGenerator = messageGenerators.get(fPojo.getPurchaseOrderType());
				String messagePath;
				try {
					messagePath = messageGenerator.generateAsnMessage(fPojo);
					asnMessagesPaths.add(messagePath);
				} catch (XmlMessageGenerationException e) {
					SPSIntegrationError error = new SPSIntegrationError();
					error.setErrorMessage("Failed to create ASN message for " + fPojo.getPoNumber() + ". Reason : " + e.getMessage());
					ErrorsCollector.addCommonErrorMessage(error);					
				}
			}
		}
		return asnMessagesPaths;
	}

	List<String> generateInvoiceMessages() {
		List<String> invoiceMessagesPaths = new ArrayList<>();
		for(InvoicePojo iPojo : getInvoicesPojoList()){
			IXmlMessagesGenerator messageGenerator = messageGenerators.get(iPojo.getPurchaseOrderType());
			String messagePath;
			try {
				messagePath = messageGenerator.generateInvoiceMessage(iPojo);
				invoiceMessagesPaths.add(messagePath);
			} catch (XmlMessageGenerationException e) {
				SPSIntegrationError error = new SPSIntegrationError();
				error.setErrorMessage("Failed to create Invoice message for " + iPojo.getPoNumber() + ". Reason : " + e.getMessage());
				ErrorsCollector.addCommonErrorMessage(error);				
			}			
		}
		return invoiceMessagesPaths;
	}
	
	abstract List<PoDocument> sendPurchaseOrders();
	
	protected List<PoDocument> updateProcessedPoDocuments(List<PoDocument> poDocumentToSend) {
		List<PoDocument> sentDocuments = new ArrayList<>();
		for(PoDocument document : poDocumentToSend){
			AddSalesOrderResultPojo addSalesOrderResultPojo = document.getAddSalesOrderResultPojo();
			if(null != addSalesOrderResultPojo){
				if(addSalesOrderResultPojo.isAddedSuccessifully()){
					document.setSalesOrderInternalId(addSalesOrderResultPojo.getSoInternalId());
					sentDocuments.add(document);
				}
				else{
					document.setExceptionDescription(addSalesOrderResultPojo.getProblemDescription());
				}
			}
		}
		return sentDocuments;
	}
	
	/**
	 * @return the companyInternalId
	 */
	public String getCompanyInternalId() {
		return companyInternalId;
	}
	/**
	 * @param companyInternalId the companyInternalId to set
	 */
	public void setCompanyInternalId(String companyInternalId) {
		this.companyInternalId = companyInternalId;
	}
	/**
	 * @return the departmentInternalId
	 */
	public String getDepartmentInternalId() {
		return departmentInternalId;
	}
	/**
	 * @param departmentInternalId the departmentInternalId to set
	 */
	public void setDepartmentInternalId(String departmentInternalId) {
		this.departmentInternalId = departmentInternalId;
	}
	/**
	 * @return the companyEmail
	 */
	public String getCompanyEmail() {
		return companyEmail;
	}
	/**
	 * @param companyEmail the companyEmail to set
	 */
	public void setCompanyEmail(String companyEmail) {
		this.companyEmail = companyEmail;
	}
	/**
	 * @return the retailerId
	 */
	public int getRetailerId() {
		return retailerId;
	}
	/**
	 * @param retailerId the retailerId to set
	 */
	public void setRetailerId(int retailerId) {
		this.retailerId = retailerId;
	}
	/**
	 * @return the retailerName
	 */
	public String getRetailerName() {
		return retailerName;
	}
	/**
	 * @param retailerName the retailerName to set
	 */
	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}
	/**
	 * @return the poDocumentList
	 */
	public List<PoDocument> getPoDocumentList() {
		return poDocumentList;
	}
	/**
	 * @param poDocumentList the poDocumentList to set
	 */
	public void setPoDocumentList(List<PoDocument> poDocumentList) {
		this.poDocumentList = poDocumentList;
	}
	/**
	 * @return the fulFilmentPojoList
	 */
	public List<FulfillmentPojo> getFulFilmentPojoList() {
		return fulFilmentPojoList;
	}
	/**
	 * @param fulFilmentPojoList the fulFilmentPojoList to set
	 */
	public void setFulFilmentPojoList(List<FulfillmentPojo> fulFilmentPojoList) {
		this.fulFilmentPojoList = fulFilmentPojoList;
	}
	public void addInvoice(InvoicePojo iPojo) {
		getInvoicesPojoList().add(iPojo);
	}
	/**
	 * @return the invoicesPojoList
	 */
	public List<InvoicePojo> getInvoicesPojoList() {
		return invoicesPojoList;
	}
	/**
	 * @param invoicesPojoList the invoicesPojoList to set
	 */
	public void setInvoicesPojoList(List<InvoicePojo> invoicesPojoList) {
		this.invoicesPojoList = invoicesPojoList;
	}
	public void clearDocuments() {
		if(poDocumentList.size() > 0)
			poDocumentList.clear();
		if(fulFilmentPojoList.size() > 0)
			fulFilmentPojoList.clear();
		if(invoicesPojoList.size() > 0)
			invoicesPojoList.clear();
	}
	public void updatePoppinAssortment() {
		poppinAssortment = IntegrationDetailsProvider.getInstance().getPoppinAssortmentByRetailer(retailerId);
	}
	public Map<String, String> getPoppinAssortment() {
		return poppinAssortment;
	}
	public Set<PurchaseOrderType> getRetailerDocumentTypes() {
		return retailerDocumentTypes;
	}
	public String getTermInternalId() {
		return termInternalId;
	}
	public SalesOrderOrderStatus getSalesOrderStatus() {
		return salesOrderStatus;
	}
	public String getShippingMethodInternalId() {
		return shippingMethodInternalId;
	}
	public String getShortName() {
		return shortName;
	}	
}
