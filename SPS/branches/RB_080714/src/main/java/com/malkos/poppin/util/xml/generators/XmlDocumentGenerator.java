package com.malkos.poppin.util.xml.generators;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.malkos.poppin.integration.retailers.RetailerAbstract;

public class XmlDocumentGenerator {
	
	protected RetailerAbstract retailer;
	
	public XmlDocumentGenerator(RetailerAbstract retailer){
		this.retailer = retailer;
	}
	
	protected void saveXmlDocumentToFileSystem(Document doc, String path) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// create file
		DOMSource source = new DOMSource(doc);
		
		String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
		File f = new File(path);
		StreamResult result = new StreamResult(new File(path));
		
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);
	}
	protected void createElementIfPossibleAndAppendItToparent(Object elementValue, String elementName, Document doc, Element parentElement){
		if(null != elementValue){
			Element element = doc.createElement(elementName);
			element.appendChild(doc.createTextNode((String) elementValue));
			parentElement.appendChild(element);
		}
	}
	protected void createElementAndAppendItToparent(Object elementValue, String elementName, Document doc, Element parentElement){
			Element element = doc.createElement(elementName);
			element.appendChild(doc.createTextNode((String) elementValue));
			parentElement.appendChild(element);
	}
}
