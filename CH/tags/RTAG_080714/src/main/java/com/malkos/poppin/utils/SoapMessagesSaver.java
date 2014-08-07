package com.malkos.poppin.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.SoapSaverResponse;

public class SoapMessagesSaver {	
	public SoapSaverResponse saveSoapMessage(Document request, Document response, SoapMessageType messageType) throws TransformerException{
		return saveSoapMessage(request, response, messageType, null);
	}
	
	public SoapSaverResponse saveSoapMessage(Document request, Document response, SoapMessageType messageType, String messageDetails) throws TransformerException{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH-mm-ss-SSS");
		
		//save response
		Date now = new Date();
		
		messageDetails = messageDetails!=null ? messageDetails+"_":"";
		
		String requestFilePath = GlobalPropertiesProvider.getGlobalProperties().getRequestResponseCurrentPath() + messageDetails+"request_" + messageType.toString().toLowerCase() + "_" + dateFormat.format(now) + ".xml";
		String responseFilePath = GlobalPropertiesProvider.getGlobalProperties().getRequestResponseCurrentPath() + messageDetails+ "response_" + messageType.toString().toLowerCase() + "_" + dateFormat.format(now) + ".xml";
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
	    transformer = transformerFactory.newTransformer();
	    DOMSource requestSource = new DOMSource(request);
	    DOMSource responseSource = new DOMSource(response);
	    
	    StreamResult requestStreamresult = new StreamResult(new File(requestFilePath));
	    StreamResult responseStreamresult = new StreamResult(new File(responseFilePath));
		
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(requestSource, requestStreamresult);
		transformer.transform(responseSource, responseStreamresult);
		SoapSaverResponse soapSaverResponse = new SoapSaverResponse();
		soapSaverResponse.setRequestFilePath(requestFilePath);
		soapSaverResponse.setResponseFilePath(responseFilePath);
		return soapSaverResponse;
	}
}
