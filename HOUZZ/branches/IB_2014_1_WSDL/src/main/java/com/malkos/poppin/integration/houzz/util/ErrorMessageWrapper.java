package com.malkos.poppin.integration.houzz.util;

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

import com.malkos.poppin.integration.houzz.entities.ErrorMessageWraped;
import com.malkos.poppin.integration.houzz.entities.IntegrationError;
import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;

public class ErrorMessageWrapper {
	private static Map<String, ErrorMessageWraped> commonErrorsMap = new HashMap<>();
	private static Map<String, ErrorMessageWraped> orderErrorsMap = new HashMap<>();
	private static Map<String,String> commonErrorMessageTags = new HashMap<>();		
	private static Logger logger = LoggerFactory.getLogger(ErrorMessageWrapper.class);
	static{		
		commonErrorsMap.put("connection is closed by foreign host", new ErrorMessageWraped("We are unable to connect to the <retailer> FTP. We will try again shortly and upon establishing a connection we will reprocess any items that were not originally processed.\r\n\r\n",false,false));
		commonErrorsMap.put("Connection refused: connect", new ErrorMessageWraped("We are unable to connect to the <retailer> FTP. We will try again shortly and upon establishing a connection we will reprocess any items that were not originally processed.\r\n\r\n",false,false));		
		commonErrorsMap.put("java.net.SocketException: ", new ErrorMessageWraped("The request to NetSuite timed out, we will attempt to reconnect shortly (no action is required)\r\n\r\n",false,false));
		commonErrorsMap.put("SMTP host", new ErrorMessageWraped("Gmail cannot be reached, we will attempt to reconnect shortly and resend any emails that couldn't be sent.",false,false));
		commonErrorsMap.put("Bad envelope tag:  htm", new ErrorMessageWraped("We were able to connect to NetSuite but the NetSuite response was in an invalid format. Contact NetSuite support.\r\n\r\n",false,false));
		commonErrorsMap.put("Reason : (403)Forbidden", new ErrorMessageWraped("NetSuite is not allowing us to establish a connection with reason code <403>. If this error occurs 3 times in a row contact NetSuite with the following details:\r\nUsername: <nsUsername>\r\nTime of request: <requestTime>\r\nType of request: <requestType>\r\n\r\n",false,false));
		commonErrorsMap.put("java.net.SocketTimeoutException: Read timed out", new ErrorMessageWraped("The request made to NetSuite has timed out. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
		commonErrorsMap.put("Only one request may be made against a session at a time",new ErrorMessageWraped("We were able to connect with NetSuite but the previous request is still being processed. We will try to connect again shortly - if you see this message more than two times in a row contact NetSuite and provide the details below:\r\n\r\nUsername: <nsUsername>\r\nTime (EST): <requestTime>\r\nType: <requestType>\r\n\r\n",false,false));
		commonErrorsMap.put("An unexpected error occurred. Error ID:",new ErrorMessageWraped("There was an error when contating NetSuite. Contact NetSuite support and provide them with the following error ID for further investigation:\r\n\r\nNetSuite Error ID: <errorId>\r\nDate: <requestTime>\r\n\r\n",false,false)); 
		commonErrorsMap.put("account you are trying to access is currently unavailable while we undergo our regularly scheduled maintenance", new ErrorMessageWraped("NetSuite is unavailable for scheduled maintenance. We will attempt to reprocess this request shortly.\r\n\r\n",false,false));
					
		commonErrorMessageTags.put("<request>","getRequestFilePath");
		commonErrorMessageTags.put("<response>","getResponseFilePath");
		commonErrorMessageTags.put("<requestType>","getRequestType");
		commonErrorMessageTags.put("<nsUsername>", "getNsUsername");
		commonErrorMessageTags.put("<errorId>", "getErrorId");
		commonErrorMessageTags.put("<requestTime>","getRequestDateTime");	
		commonErrorMessageTags.put("<retailer>","getRetailer");
	}
	
	public static IntegrationError wrapCommonError(String initialError, NSRrequestDetails details){
		String errorID = ExtractErrorId(initialError);
		if (errorID!=null){
			details.setErrorId(errorID);
		}
		IntegrationError errorResult = new IntegrationError();
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
	
	private static String ExtractErrorId(String message){		
		Pattern patern = Pattern.compile("(Error ID: )");
	    Matcher matcher = patern.matcher(message);
	    int endIndex=0;
	    if (matcher.find()){
	    	endIndex = matcher.end();
	    }if (endIndex>0){
	    	return message.substring(endIndex, message.length());
	    }
	    return null;
	}	
}
