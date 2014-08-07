package com.malkos.poppin.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.entities.ErrorMessageWraped;
import com.malkos.poppin.entities.NSRrequestDetails;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.transport.NetsuiteOperationsManager;

public class ErrorMessageWrapper {
	private static Map<String, ErrorMessageWraped> commonErrorsMap = new HashMap<>();
	private static Map<String, ErrorMessageWraped> orderErrorsMap = new HashMap<>();
	private static Map<String,String> commonErrorMessageTags = new HashMap<>();	
	private static Map<String,String> orderErrorMessageTags = new HashMap<>();	
	private static Logger logger = LoggerFactory.getLogger(ErrorMessageWrapper.class);
	static{		
		commonErrorsMap.put("connection is closed by foreign host", new ErrorMessageWraped("We are unable to connect to the SPS FTP. We will try again shortly and upon establishing a connection we will reprocess any items that were not originally processed.\r\n\r\n",false,false));
		commonErrorsMap.put("Connection refused: connect", new ErrorMessageWraped("We are unable to connect to the SPS FTP. We will try again shortly and upon establishing a connection we will reprocess any items that were not originally processed.\r\n\r\n",false,false));		
		commonErrorsMap.put("java.net.SocketException: ", new ErrorMessageWraped("The request to NetSuite timed out, we will attempt to reconnect shortly (no action is required)\r\n\r\n",false,false));
		commonErrorsMap.put("java.net.ConnectException: Connection timed out: connect", new ErrorMessageWraped("The request to NetSuite timed out, we will attempt to reconnect shortly (no action is required)\r\n\r\n",false,false));
		commonErrorsMap.put("Could not connect to SMTP host", new ErrorMessageWraped("Gmail cannot be reached, we will attempt to reconnect shortly and resend any emails that couldn't be sent.",false,false));
		commonErrorsMap.put("Bad envelope tag:  htm", new ErrorMessageWraped("We were able to connect to NetSuite but the NetSuite response was in an invalid format. Contact NetSuite support and provide them with the attached XML request details.\r\n\r\n",true,false));
		commonErrorsMap.put("Reason : (403)Forbidden", new ErrorMessageWraped("NetSuite is not allowing us to establish a connection with reason code <403>. If this error occurs 3 times in a row contact NetSuite with the following details:\r\nUsername: <nsUsername>\r\nTime of request: <requestTime>\r\nType of request: <requestType>\r\nInclude the attached request XML <request>\r\n\r\n",true,true));
		commonErrorsMap.put("java.net.SocketTimeoutException: Read timed out", new ErrorMessageWraped("The request made to NetSuite has timed out. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Only one request may be made against a session at a time",new ErrorMessageWraped("We were able to connect with NetSuite but the previous request is still being processed. We will try to connect again shortly - if you see this message more than two times in a row contact NetSuite and provide the details below:\r\nUsername: <nsUsername>\r\nTime (EST): <requestTime>\r\nType: <requestType>\r\nAttach the XML request/response details\r\n\r\n",true,true));
		commonErrorsMap.put("An unexpected error occurred. Error ID:",new ErrorMessageWraped("There was an error when contating NetSuite. Contact NetSuite support and provide them with the following error ID for further investigation:\r\nNetSuite Error ID: <errorId>\r\nDate: <requestTime>\r\nInclude the attached XML request <request>\r\n\r\n",true,true));
		commonErrorsMap.put("account you are trying to access is currently unavailable while we undergo our regularly scheduled maintenance", new ErrorMessageWraped("NetSuite is unavailable for scheduled maintenance. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Auth fail", new ErrorMessageWraped("We are unable to connect to the SPS FTP. We will try again shortly and upon establishing a connection we will reprocess any items that were not originally processed.\r\n\r\n",false,false));
		
		orderErrorsMap.put("An unexpected error occurred. Error ID:",new ErrorMessageWraped("There was an error when contating NetSuite. Contact NetSuite support and provide them with the following error ID for further investigation:\r\nNetSuite Error ID: <errorId>\r\nDate: <requestTime>\r\nInclude the attached XML request <request>\r\n\r\n",true,true)); 
		orderErrorsMap.put("Could not find appropriate to AddressLocationNumber", new ErrorMessageWraped("The shipping address for this order is not in NetSuite. To process this you need to login to SPS and add the address in NetSuite.\r\nThe integration will need to be updated with the address label once NetSuite has been updated - send a request to the development team.\r\n\r\n", false, false));
		orderErrorsMap.put("Invalid item reference key", new ErrorMessageWraped("We are unable to create an order in NetSuite due to item <itemId> being either inactive or unavailable in NetSuite. Confirm if the following item is valid in NetSuite:\r\nItem: <itemId>\r\nItem URL: <itemURL>\r\n\r\n", false, false));
		orderErrorsMap.put("inventory item in NetSuite.", new ErrorMessageWraped("The order contains a vendor SKU number that isn't mapped for this retailer. Confirm that vendor part numbers valid and update the SKU mappings and reprocess this order:\r\n<vendorPartNumber>Retail partner: <retailer>\r\nPO#: <poNumber>\r\n\r\n",false,false));
		orderErrorsMap.put("There is no Tracking Number for item", new ErrorMessageWraped("This order was fulfilled in NetSuite with one tracking number for the package (item level tracking details were not populated). We expect to have a tracking number for each line item.\r\nThe order will need to be manually processed or you can update the tracking details in NetSuite so that each line matches the package tracking number and ask to have the order reprocessed.\r\nPO#: <poNumber>\r\nRetailer: <retailer>\r\nFulfillmentURL#: <fulfillmentURL>\r\nOrderURL: <orderURL>\r\nNote: Add the tracking#\r\n\r\n",false,false));
		orderErrorsMap.put("Sales Order contains inconsistent items/illegal items modification.", new ErrorMessageWraped("The order line details don't match the original sales order, either a line item was added or removed manually. This order needed to be manually processed.\r\nPO#: <poNumber>\r\nRetailer: <retailer>\r\nOrderURL: <orderURL>\r\n\r\n",false,false));
		orderErrorsMap.put("already exists in poppin-Netsuite database.", new ErrorMessageWraped("An order already existing in NetSuite with PO number  <poNumber>. The order might have been sent on accident, double check with the retailer is the PO needs to be changed or cancelled and re-entered.\r\n\r\n",false,false));
		orderErrorsMap.put("You do not have permissions to set a value for element", new ErrorMessageWraped("We've encountered a NetSutie error and cannot process this order. Reach out to NetSuite support and provide the details below:\r\nRequest type: <requestType>\r\nUsername: <nsUsername>\r\n\r\n",true,true));	
		orderErrorsMap.put("PurchaseOrderTypeCode", new ErrorMessageWraped("The sale order is in a format that cannot be processed. Submit a request to the development team with the following details.\r\nPartner: <retailer>\r\nPO#: <poNumber>\r\nPO type: <poTypeCode>\r\n\r\nNext steps: Determine if the partner will be submitting these types of orders moving forward and work with the development team to configure the system to accept this type of order.\r\n\r\n",false,false));
		
		commonErrorMessageTags.put("<request>","getRequestFilePath");
		commonErrorMessageTags.put("<response>","getResponseFilePath");
		commonErrorMessageTags.put("<requestType>","getRequestTypeAsString");
		commonErrorMessageTags.put("<nsUsername>", "getNsUsername");
		commonErrorMessageTags.put("<errorId>", "getErrorId");
		commonErrorMessageTags.put("<requestTime>","getRequestDateTime");
		
		orderErrorMessageTags.put("<retailer>","getRetailer");
		orderErrorMessageTags.put("<vendorPartNumber>","getVendorPartNumber");
		orderErrorMessageTags.put("<poNumber>","getPoNumber");
		orderErrorMessageTags.put("<itemId>","getItemId");
		orderErrorMessageTags.put("<itemURL>","getItemURL");
		orderErrorMessageTags.put("<fulfillmentURL>","getFulfillmentURL");
		orderErrorMessageTags.put("<orderURL>","getOrderURL");
		orderErrorMessageTags.put("<requestType>","getRequestTypeAsString");
		orderErrorMessageTags.put("<nsUsername>","getNsUsername");
		orderErrorMessageTags.put("<poTypeCode>","getPoTypeCode");
		orderErrorMessageTags.put("<errorId>","getErrorId");
		orderErrorMessageTags.put("<request>","getRequestFilePath");
		orderErrorMessageTags.put("<requestTime>","getRequestTime");
	}
	
	public static SPSIntegrationError wrapCommonError(String initialError, NSRrequestDetails details){
		String errorID = ExtractErrorId(initialError);
		if (errorID!=null){
			details.setErrorId(errorID);
		}
		boolean matchFound = false;
		SPSIntegrationError errorResult = new SPSIntegrationError();
		for (Entry<String,ErrorMessageWraped> entry : commonErrorsMap.entrySet()){			
			if (initialError.contains(entry.getKey())){
				String resultErrorMessage = entry.getValue().getErrorMessage();
				if (details!=null){
					for (Entry<String,String> tagEntry:commonErrorMessageTags.entrySet()){
						if(resultErrorMessage.contains(tagEntry.getKey())){
							Method method= null;
							try {
							  method = details.getClass().getMethod(tagEntry.getValue());
							} catch (SecurityException | NoSuchMethodException e) {
								logger.error(e.getMessage());					
								e.printStackTrace();
							}					
							String replacement = null;
							if (method!=null){
								try {
									replacement = (String) method.invoke(details);
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
							if 	(replacement!=null){
								resultErrorMessage = resultErrorMessage.replace(tagEntry.getKey(), replacement);
							} else {
								resultErrorMessage = resultErrorMessage.replace(tagEntry.getKey(), "[NULL]");
							}
						}
					}				
				}
				if (errorResult.getErrorMessage()!=null){
					errorResult.setErrorMessage(errorResult.getErrorMessage()+resultErrorMessage);
				} else {
					errorResult.setErrorMessage(resultErrorMessage);
				}				
				List<String> atachments = new ArrayList<>();
				if ((entry.getValue().isAttachRequest())&&(details.getRequestFilePath()!=null)){
					atachments.add(details.getRequestFilePath());
				}
				if ((entry.getValue().isAttachResponse())&&(details.getResponseFilePath()!=null)){
					atachments.add(details.getResponseFilePath());
				}if (!atachments.isEmpty()){
					errorResult.setAttachmentsList(atachments);
				}				
				matchFound = true;
			}
		}
		if (!matchFound){
			errorResult.setErrorMessage(initialError);
		}
		return errorResult;
	}
	
	private static String ExtractErrorId(String message){	
		String result = null;
		Pattern patern = Pattern.compile("(Error ID: )");
	    Matcher matcher = patern.matcher(message);
	    int endIndex=0;
	    if (matcher.find()){
	    	endIndex = matcher.end();
	    }if (endIndex>0){
	    	String substring = message.substring(endIndex, message.length());
	    	int errorEnd = substring.indexOf("\r\n");	    	
	    	if (errorEnd>0){
	    		result = substring.substring(0, errorEnd);
	    	} else {
	    		result = substring.substring(0, substring.length());
	    	}
	    }
	    return result;
	}
	
	public static SPSIntegrationError wrapOrderError(OrderErrorMessage details){
		String initialError=details.getErrorDetails();
		String itemID = ExtractItemId(initialError);
		if (itemID!=null){
			details.setItemId(itemID);
		}	
		String errorID = ExtractErrorId(initialError);
		if (errorID!=null){
			details.setErrorId(errorID);
		}	
		SPSIntegrationError errorResult = new SPSIntegrationError();
		boolean matchFound = false;
		for (Entry<String,ErrorMessageWraped> entry : orderErrorsMap.entrySet()){			
			if (initialError.contains(entry.getKey())){
				String resultErrorMessage = entry.getValue().getErrorMessage();
				if (details!=null){
					for (Entry<String,String> tagEntry:orderErrorMessageTags.entrySet()){
						if(resultErrorMessage.contains(tagEntry.getKey())){
							Method method= null;
							try {
							  method = details.getClass().getMethod(tagEntry.getValue());
							} catch (SecurityException | NoSuchMethodException e) {
								logger.error(e.getMessage());					
								e.printStackTrace();
							}					
							String replacement = null;
							if (method!=null){
								try {
									replacement = (String) method.invoke(details);
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
							if 	(replacement!=null){
								resultErrorMessage = resultErrorMessage.replace(tagEntry.getKey(), replacement);
							} else {
								resultErrorMessage = resultErrorMessage.replace(tagEntry.getKey(), "[NULL]");
							}
						}
					}				
				}
				if (errorResult.getErrorMessage()!=null){
					errorResult.setErrorMessage(errorResult.getErrorMessage()+resultErrorMessage);
				} else {
					String partner = (details.getRetailer()!=null)? "Partner: " + details.getRetailer() + "\r\n" :"";
					errorResult.setErrorMessage("Order day : " + details.getPoOrderDate() + "\r\n" +
							   "PO # : " + details.getPoNumber() + "\r\n" +
							   partner + "PO FILE PATH # : " + details.getPathFile() + "\r\n" +
							   "ERROR MESSAGE : " + resultErrorMessage);
				}				
				List<String> atachments = new ArrayList<>();
				if ((entry.getValue().isAttachRequest())&&(details.getRequestFilePath()!=null)){
					atachments.add(details.getRequestFilePath());
				}
				if ((entry.getValue().isAttachResponse())&&(details.getResponseFilePath()!=null)){
					atachments.add(details.getResponseFilePath());
				}if (!atachments.isEmpty()){
					errorResult.setAttachmentsList(atachments);
				}					
				matchFound = true;
			}
		}
		if (!matchFound){
			String partner = (details.getRetailer()!=null)? "Partner: " + details.getRetailer()+"\r\n" :"";		
			errorResult.setErrorMessage("Order day : " + details.getPoOrderDate() + "\r\n" +
					   "PO # : " + details.getPoNumber() + "\r\n" +
					   partner + "PO FILE PATH # : " + details.getPathFile() + "\r\n" +
					   "ERROR MESSAGE : " + initialError);
		}
		return errorResult;
	}	

	private static String ExtractItemId(String message){		
		Pattern patern = Pattern.compile("(Invalid item reference key )");
	    Matcher matcher = patern.matcher(message);
	    int endIndex=0;
	    if (matcher.find()){
	    	endIndex = matcher.end();
	    }if (endIndex>0){
	    	String substring = message.substring(endIndex, message.length());
	    	endIndex = substring.indexOf(".");	    	
	    	return substring.substring(0, endIndex);
	    }
	    return null;
	}
	
}
