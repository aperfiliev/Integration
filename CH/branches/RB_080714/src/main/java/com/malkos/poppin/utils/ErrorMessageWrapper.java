package com.malkos.poppin.utils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.entities.ErrorMessageWraped;
import com.malkos.poppin.entities.NSRrequestDetails;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.utils.OrderErrorMessage;

public class ErrorMessageWrapper {
	private static Map<String, ErrorMessageWraped> commonErrorsMap = new HashMap<>();
	private static Map<String,String> errorMessageTags = new HashMap<>();
	private static Logger logger = LoggerFactory.getLogger(ErrorMessageWrapper.class);
	private static Map<String,String> orderErrorMessageTags = new HashMap<>();
	private static Map<String, ErrorMessageWraped> orderErrorsMap = new HashMap<>();
	
	static{		
		commonErrorsMap.put("Could not send outgoing messages to CH. Reason :connection is closed by foreign host", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("java.net.SocketException: Connection reset", new ErrorMessageWraped("The request to NetSuite timed out, we will attempt to reconnect shortly (no action is required)\r\n\r\n",false,false));
		commonErrorsMap.put("Could not send outgoing messages to CH. Reason :Connection refused: connect", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("java.net.SocketException: ", new ErrorMessageWraped("The request to NetSuite timed out, we will attempt to reconnect shortly (no action is required)\r\n\r\n",false,false));
		commonErrorsMap.put("Could not connect to SMTP host", new ErrorMessageWraped("Gmail cannot be reached, we will attempt to reconnect shortly and resend any emails that couldn't be sent.\r\n\r\n",false,false));
		commonErrorsMap.put("Bad envelope tag:  htm", new ErrorMessageWraped("We were able to connect to NetSuite but the NetSuite response was in an invalid format. Contact NetSuite support and provide them with the attached XML request details.\r\n\r\n",true,false));
		commonErrorsMap.put("Reason : (403)Forbidden", new ErrorMessageWraped("NetSuite is not allowing us to establish a connection with reason code <403>. If this error occurs 3 times in a row contact NetSuite with the following details:\r\nUsername: <nsUsername>\r\nTime of request: <requestTime>\r\nType of request: <requestType>\r\nInclude the attached request XML <request>\r\n\r\n",true,true));
		commonErrorsMap.put("java.net.SocketTimeoutException: Read timed out", new ErrorMessageWraped("The request made to NetSuite has timed out. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Only one request may be made against a session at a time",new ErrorMessageWraped("We were able to connect with NetSuite but the previous request is still being processed. We will try to connect again shortly - if you see this message more than two times in a row contact NetSuite and provide the details below:\r\nUsername: <nsUsername>\r\nTime (EST): <requestTime>\r\nType: <requestType>\r\nAttach the XML request/response details\r\n\r\n",true,true));
		commonErrorsMap.put("An unexpected error occurred. Error ID:",new ErrorMessageWraped("There was an error when contating NetSuite. Contact NetSuite support and provide them with the following error ID for further investigation:\r\nNetSuite Error ID: <errorId>\r\nDate: <requestTime>\r\nInclude the attached XML request <request>\r\n\r\n",true,true)); 
		commonErrorsMap.put("account you are trying to access is currently unavailable while we undergo our regularly scheduled maintenance", new ErrorMessageWraped("NetSuite is unavailable for scheduled maintenance. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Reason: ftp1.commercehub.com", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Connection timed out: connect", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Reply String : 530 Login or password incorrect!", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		//commonErrorsMap.put("Reason: ftp1.commercehub.com", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));
		//commonErrorsMap.put("Auth fail", new ErrorMessageWraped("The integration is not able to establish a connection with the EDI Vendor's FTP. We will attempt to connect again shortly.\r\n\r\n",false,false));		
		
		orderErrorsMap.put("An unexpected error occurred. Error ID:",new ErrorMessageWraped("There was an error when contating NetSuite. Contact NetSuite support and provide them with the following error ID for further investigation:\r\nNetSuite Error ID: <errorId>\r\nDate: <requestTime>\r\nInclude the attached XML request <request>\r\n\r\n",true,true)); 
		orderErrorsMap.put("Invalid item reference key", new ErrorMessageWraped("We are unable to create an order in NetSuite due to item <itemId> being either inactive or unavailable in NetSuite. Confirm if the following item is valid in NetSuite:\r\nItem: <itemId>\r\nItem URL: <itemURL>\r\n\r\n", false, false));
		orderErrorsMap.put("item in NetSuite", new ErrorMessageWraped("The order contains a vendor SKU number that isn't mapped for the Staples retailer. Confirm that vendor SKU is valid, update the SKU mappings and reprocess this order:\r\n<vendorSKU>\r\n\r\n",false,false));
		orderErrorsMap.put("was processed by application previously", new ErrorMessageWraped("An order already existing in NetSuite with PO number  <poNumber>. The order might have been sent on accident, double check with the retailer is the PO needs to be changed or cancelled and re-entered.\r\n\r\n",false,false));
		orderErrorsMap.put("You do not have permissions to set a value for element", new ErrorMessageWraped("We've encountered a NetSutie error and cannot process this order. Reach out to NetSuite support and provide the details below:\r\nRequest type: <requestType>\r\nUsername: <nsUsername>\r\n\r\n",true,true));	
				
		errorMessageTags.put("<request>","getRequestFilePath");
		errorMessageTags.put("<response>","getResponseFilePath");
		errorMessageTags.put("<requestType>","getRequestType");
		errorMessageTags.put("<nsUsername>", "getNsUsername");
		errorMessageTags.put("<errorId>", "getErrorId");
		errorMessageTags.put("<requestTime>","getRequestDateTime");
		
		orderErrorMessageTags.put("<errorId>", "getErrorId");
		orderErrorMessageTags.put("<vendorSKU>","getVendorSKU");
		orderErrorMessageTags.put("<poNumber>","getPoNumber");
		orderErrorMessageTags.put("<itemId>","getItemId");
		orderErrorMessageTags.put("<itemURL>","getItemURL");
		orderErrorMessageTags.put("<orderURL>","getOrderURL");
		orderErrorMessageTags.put("<requestType>","getRequestType");
		orderErrorMessageTags.put("<nsUsername>","getNsUsername");
		orderErrorMessageTags.put("<poTypeCode>","getPoTypeCode");
		orderErrorMessageTags.put("<request>","getRequestFilePath");
		orderErrorMessageTags.put("<requestTime>","getRequestDateTime");
		
	}
	public static CHIntegrationError wrapCommonError(String initialError){
		return wrapCommonError(initialError, null);
	}
	public static CHIntegrationError wrapCommonError(String initialError, NSRrequestDetails details){
		String errorID = ExtractErrorId(initialError);
		if (errorID!=null){
			details.setErrorId(errorID);
		}
		CHIntegrationError errorResult = new CHIntegrationError();
		for (Entry<String,ErrorMessageWraped> entry : commonErrorsMap.entrySet()){			
			if (initialError.contains(entry.getKey())){
				String resultErrorMessage = entry.getValue().getErrorMessage();
				if (details!=null){
					for (Entry<String,String> tagEntry:errorMessageTags.entrySet()){
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
								resultErrorMessage.replace(tagEntry.getKey(), replacement);
							} else {
								resultErrorMessage.replace(tagEntry.getKey(), "[NULL]");
							}
						}
					}				
				}				
				errorResult.setErrorMessage(resultErrorMessage);
				List<String> atachments = new ArrayList<>();
				if ((entry.getValue().isAttachRequest())&&(details.getRequestFilePath()!=null)){
					atachments.add(details.getRequestFilePath());
				}
				if ((entry.getValue().isAttachResponse())&&(details.getResponseFilePath()!=null)){
					atachments.add(details.getResponseFilePath());
				}if (!atachments.isEmpty()){
					errorResult.setAttachmentsList(atachments);
				}				
				return errorResult;
			}
		}
		errorResult.setErrorMessage(initialError);
		return errorResult;
	}
	public static CHIntegrationError wrapOrderError(OrderErrorMessage details){
		String initialError=details.getErrorDetails();
		String itemID = ExtractItemId(initialError);
		if (itemID != null){
			details.setItemId(itemID);
		}	
		String errorID = ExtractErrorId(initialError);
		if (errorID!=null){
			details.setErrorId(errorID);
		}
		CHIntegrationError errorResult = new CHIntegrationError();
		for (Entry<String,ErrorMessageWraped> entry : orderErrorsMap.entrySet()){			
			if (initialError.contains(entry.getKey())){
				String resultErrorMessage = entry.getValue().getErrorMessage();
				if (details!=null){
					for (Entry<String,String> tagEntry : orderErrorMessageTags.entrySet()){
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
				errorResult.setErrorMessage(resultErrorMessage);
				List<String> atachments = new ArrayList<>();
				if ((entry.getValue().isAttachRequest())&&(details.getRequestFilePath()!=null)){
					atachments.add(details.getRequestFilePath());
				}
				if ((entry.getValue().isAttachResponse())&&(details.getResponseFilePath()!=null)){
					atachments.add(details.getResponseFilePath());
				}if (!atachments.isEmpty()){
					errorResult.setAttachmentsList(atachments);
				}	
				errorResult.setErrorMessage("Order day : " + details.getPoOrderDate() + "\r\n" +
						   "PO # : " + details.getPoNumber() + "\r\n" +
						   "BATCH # : " + details.getMbNumber() + "\r\n" +
						   "PO FILE PATH # : " + details.getMbFile() + "\r\n" +
						   "ERROR MESSAGE : " + errorResult.getErrorMessage());
				return errorResult;
			}
		}
		errorResult.setErrorMessage(initialError);
				errorResult.setErrorMessage("Order day : " + details.getPoOrderDate() + "\r\n" +
				   "PO # : " + details.getPoNumber() + "\r\n" +
				   "PO FILE PATH # : " + details.getMbFile() + "\r\n" +
				   "ERROR MESSAGE : " + errorResult.getErrorMessage());
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
