package com.malkos.poppin.integration.retailers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentException;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.enums.PurchaseOrderType;
import com.malkos.poppin.integration.IntegrationDetailsProvider;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.PersistenceManager;
import com.malkos.poppin.persistence.dao.RetailerDAO;

public class RetailersManager {
	
	private Map<String,RetailerAbstract> retailers;
	
	private static RetailersManager instance;
	private List<RetailerDAO> retailerDAOList;
	
	private static Logger logger = LoggerFactory.getLogger(RetailersManager.class);
	
	private RetailersManager(){
		retailers = new HashMap<>();
		PersistenceManager persistencaManager = new PersistenceManager();
		setRetailerDAOList(persistencaManager.loadRetailers());
		initializeRetailers(getRetailerDAOList());
	}
	
	private void initializeRetailers(List<RetailerDAO> retailerDAOList) {
		for (RetailerDAO retailerDAO : retailerDAOList){
			String retailerName = retailerDAO.getRetailerName();			
			switch (retailerName){
				case GlobalProperties.QUILL_RETAILER_NAME:
					retailers.put(retailerName, new QuillRetailer(retailerDAO.getRetailerName(), retailerDAO.getCompanyEmail(), retailerDAO.getCompanyNsInternalId(), retailerDAO.getDepartmentNsInternalId(), retailerDAO.getIdRetailer()));
					break;
				case GlobalProperties.STAPLES_RETAILER_NAME: 
					retailers.put(retailerName, new StaplesRetailer(retailerDAO.getRetailerName(), retailerDAO.getCompanyEmail(), retailerDAO.getCompanyNsInternalId(), retailerDAO.getDepartmentNsInternalId(), retailerDAO.getIdRetailer()));
					break;
				case GlobalProperties.INDIGO_RETAILER_NAME: 
					retailers.put(retailerName, new IndigoRetailer(retailerDAO.getRetailerName(), retailerDAO.getCompanyEmail(), retailerDAO.getCompanyNsInternalId(), retailerDAO.getDepartmentNsInternalId(), retailerDAO.getIdRetailer()));
					break;
				case GlobalProperties.BARNES_N_NOBLE_RETAILER_NAME: 
					retailers.put(retailerName, new BarnesAndNobleRetailer(retailerDAO.getRetailerName(), retailerDAO.getCompanyEmail(), retailerDAO.getCompanyNsInternalId(), retailerDAO.getDepartmentNsInternalId(), retailerDAO.getIdRetailer()));
					break;
				default :
					logger.warn("Unknown retailer name/id : " + retailerName);
			}
		}		
	}

	public void assignPoDocumnets(List<PoDocument> poDocumentsList){
		for(PoDocument document : poDocumentsList){
			/*if(document.getPoType() == PurchaseOrderType.UNKNOWN){
				document.setExceptionDescription("Unknown PurhcaseOrderType code " + document.getPurchaseOrderTypeCode() + " has been provided for this PO.");
				continue;
			}*/
			if(retailers.containsKey(document.getRetailerName())){
				RetailerAbstract retailer = retailers.get(document.getRetailerName());
				document.setRetailer(retailer);
				if(retailer.getRetailerDocumentTypes().contains(document.getPoType()))
					retailer.addPoDocument(document);
				else{
					document.setExceptionDescription("Retailer " +retailer.getRetailerName()+ " doesn't support \"" + document.getPurchaseOrderTypeCode() + "\" PurchaseOrderTypeCode");
					continue;
				}
			}
			else{
				/*//I am a dummy portion of code. I have appeared because of stupid guys from SPS. Please remove me when you will go to PROD
				if(document.getRetailerName().equalsIgnoreCase(GlobalProperties.STAPLES_RETAILER_STUPID_SPS_PSEUDO_NAME)){
					if(retailers.containsKey(GlobalProperties.STAPLES_RETAILER_NAME)){
						RetailerAbstract retailer = retailers.get(GlobalProperties.STAPLES_RETAILER_NAME);
						retailer.addPoDocument(document);
						continue;
					}
				}*/
				document.setExceptionDescription( "Unknown PO document retailer '" + document.getRetailerName() +"' was provided for order with PO number #" + document.getPoNumber());
			}
		}
	}
	
	public void assignFulfillments(List<FulfillmentPojo> fulfillMentPojos){
		Map<String, RetailerDAO> departmentIdToRetailerDAOmap = new HashMap<>();
		for(RetailerDAO retDAO : retailerDAOList){
			departmentIdToRetailerDAOmap.put(retDAO.getDepartmentNsInternalId(), retDAO);
		}
		for(FulfillmentPojo fPojo : fulfillMentPojos){
			String retailerName = departmentIdToRetailerDAOmap.get(fPojo.getDepartmentNsInternalId()).getRetailerName();
			RetailerAbstract retailer = retailers.get(retailerName);
			retailer.addFulfillMent(fPojo);
		}
	}
	public void assignInvoices(List<InvoicePojo> invoicesPojos){
		Map<String, RetailerDAO> departmentIdToRetailerDAOmap = new HashMap<>();
		for(RetailerDAO retDAO : retailerDAOList){
			departmentIdToRetailerDAOmap.put(retDAO.getDepartmentNsInternalId(), retDAO);
		}
		for(InvoicePojo iPojo : invoicesPojos){
			String retailerName = departmentIdToRetailerDAOmap.get(iPojo.getDepartmentNsInternalId()).getRetailerName();
			RetailerAbstract retailer = retailers.get(retailerName);
			retailer.addInvoice(iPojo);
		}
	}
	public List<String> askRetailersForAsnMessages(){
		List<String> asnMessagesPaths = new ArrayList<>();
		for(RetailerAbstract retailer : retailers.values())
			asnMessagesPaths.addAll(retailer.generateAsnMessages());
		return asnMessagesPaths;
	}
	public List<String> askRetailersForInvoiceMessages(){
		List<String> invoiceMessagesPaths = new ArrayList<>();
		for(RetailerAbstract retailer : retailers.values())
			invoiceMessagesPaths.addAll(retailer.generateInvoiceMessages());
		return invoiceMessagesPaths;
	}

	public static RetailersManager getInstance() {		
		if (null == instance){
			synchronized (RetailersManager.class) {
				if (null == instance){
					instance = new RetailersManager();
				}
			}			
		}
		return instance;
	}

	public void askRetailersToValidate() {
		for(RetailerAbstract retailer : retailers.values())
			retailer.validate();
	}
	public void checkDocumentsPoNumbersIfTheyAlreadyAdded(List<PoDocument> documents){
		PersistenceManager persistencaManager = new PersistenceManager();
		for(PoDocument document : documents){
			boolean poWasAddedPreviously = persistencaManager.checkIfSalesOrderExistsForGivenPoNumber(document);
			if(poWasAddedPreviously)
				document.setExceptionDescription("Sales Order for the given PO # "+ document.getPoNumber() + " already exists in poppin-Netsuite database.");
		}
	}
	public void updatePoDocumentStatuses(List<PoDocument> documents){
		for(PoDocument document : documents){
			document.updateStatus();
		}
	}

	/**
	 * @return the retailerDAOList
	 */
	public List<RetailerDAO> getRetailerDAOList() {
		return retailerDAOList;
	}

	/**
	 * @param retailerDAOList the retailerDAOList to set
	 */
	public void setRetailerDAOList(List<RetailerDAO> retailerDAOList) {
		this.retailerDAOList = retailerDAOList;
	}

	public List<PoDocument> sendPurchaseOrders() {
		List<PoDocument> sendedDocumentsList = new ArrayList<>();
		logger.info("Asking each of retailers to send their Purchase Orders.");
		for(RetailerAbstract retailer : retailers.values()){
			if(retailer.getPoDocumentList().size() > 0)
				sendedDocumentsList.addAll(retailer.sendPurchaseOrders());
			else
				logger.info(retailer.shortName + " doesn't have new Purchase order to send. Skipping...");
		}
		return sendedDocumentsList;
	}

	public void askRetailersToClearDocuments() {
		logger.info("Asking each of retailers clear the processed documents.");
		for(RetailerAbstract retailer : retailers.values()){
			retailer.clearDocuments();
		}
	}

	public void askRetailersToRefreshAssortment() {
		logger.info("Asking each of retailers to refresh their Poppin assortment.");
		for(RetailerAbstract retailer : retailers.values()){
			retailer.updatePoppinAssortment();
		}
	}
	
	public Map<String, RetailerDAO> getRetailersMap(){
		Map<String, RetailerDAO> result = new HashMap<String, RetailerDAO>();
		List<RetailerDAO> retailersList = getRetailerDAOList();
		for (RetailerDAO retailer : retailersList){
			result.put(retailer.getDepartmentNsInternalId(), retailer);
		}
		return result;
	}
}
