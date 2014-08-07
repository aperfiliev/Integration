package com.malkos.poppin.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.integration.retailers.RetailerAbstract;
import com.malkos.poppin.integration.retailers.RetailersManager;
import com.malkos.poppin.persistence.dao.RetailerDAO;

public class ErrorsCollector {
	private static List<SPSIntegrationError> commonErrorMessages = new ArrayList<SPSIntegrationError>();
	private static List<SPSIntegrationError> orderErrorMessages = new ArrayList<SPSIntegrationError>();	
	
	public static List<SPSIntegrationError> getCommonErrorMessages(){
		return commonErrorMessages;
	}
	public static void addCommonErrorMessage(SPSIntegrationError commonErrorMessage){
		commonErrorMessages.add(commonErrorMessage);
	}
	public static Boolean hasCommonErrors(){
		return commonErrorMessages.isEmpty() == false;
	}
	public static Boolean hasOrderErrorMessages(){
		return orderErrorMessages.isEmpty() == false;
	}
	public static void cleanErrors(){
		commonErrorMessages.clear();
		orderErrorMessages.clear();
	}
	public static List<SPSIntegrationError> getOrderErrorMessages(){
		return orderErrorMessages;
	}
	public static void addOrderErrorMessage(SPSIntegrationError orderErrorMessage){
		orderErrorMessages.add(orderErrorMessage);
	}
	public static void searchPossibleOrderErrors(List<PoDocument> documentslist) {
		for(PoDocument document: documentslist){
			if(document.getExceptionDescription() != null){				
				OrderErrorMessage orderError = new OrderErrorMessage();
				RetailerAbstract retailer = document.getRetailer();
				if (retailer!=null){
					orderError.setRetailer(getRetailerName(retailer.getDepartmentInternalId()));
				}
				orderError.setPoNumber(document.getPoNumber());
				orderError.setPathFile(document.getIncomingMessagePath());
				Date correctedDate;
				if (document.getPoDate()!=null){
					correctedDate = Calendar.getInstance().getTime();
				} else {
					correctedDate = Calendar.getInstance().getTime();
				}				
				orderError.setPoOrderDate(new SimpleDateFormat("yyyyMMdd").format(correctedDate.getTime()));
				orderError.setErrorDetails(document.getExceptionDescription());
				orderError.setFulfillmentId(null);
				orderError.setNsUsername(GlobalPropertiesProvider.getGlobalProperties().getNsUsername());
				orderError.setOrderId(null);				
				if (document.getErrorDetails()!=null){
					orderError.setVendorPartNumber(document.getErrorDetails().getErrorVendorPartNumbers());
					orderError.setRequestFilePath(document.getErrorDetails().getRequestFilePath());
					orderError.setRequestType(document.getErrorDetails().getRequestType());
					orderError.setResponseFilePath(document.getErrorDetails().getResponseFilePath());
					orderError.setRequestTime(document.getErrorDetails().getRequestTime());
				}				
				orderError.setPoTypeCode(document.getPurchaseOrderTypeCode());
				SPSIntegrationError error = ErrorMessageWrapper.wrapOrderError(orderError);
				ErrorsCollector.addOrderErrorMessage(error);
			}
		}
	}
	public static void searchPossibleFulfillmentErrors(List<FulfillmentPojo> fulfillmentList) {
		for(FulfillmentPojo fulfilMent : fulfillmentList){
			if (fulfilMent.getExceptionDescription()!= null){
				String nsDepartmentId = fulfilMent.getDepartmentNsInternalId();					
				OrderErrorMessage orderError = new OrderErrorMessage();
				orderError.setRetailer(getRetailerName(nsDepartmentId));
				orderError.setPoNumber(fulfilMent.getPoNumber());
				orderError.setPathFile(fulfilMent.getIncomingMessagePath());
				orderError.setPoOrderDate(new SimpleDateFormat("yyyyMMdd").format(fulfilMent.getPoDate().getTime()));
				orderError.setErrorDetails(fulfilMent.getExceptionDescription());
				orderError.setFulfillmentId(fulfilMent.getNsInternalId());
				orderError.setNsUsername(GlobalPropertiesProvider.getGlobalProperties().getNsUsername());
				orderError.setOrderId(fulfilMent.getSalesorderNsInternalId());			
				SPSIntegrationError error = ErrorMessageWrapper.wrapOrderError(orderError);
				ErrorsCollector.addOrderErrorMessage(error);
			}
		}
	}	
	
	private static String getRetailerName(String nsDepartmentID){
		String retailerName = null;
		if (nsDepartmentID!=null){
			Map<String, RetailerDAO> retailersMap = RetailersManager.getInstance().getRetailersMap();
			if (retailersMap.containsKey(nsDepartmentID)){					
				retailerName = retailersMap.get(nsDepartmentID).getDepartmentAliasName();
			}
		}	
		return retailerName;
	}	
}
