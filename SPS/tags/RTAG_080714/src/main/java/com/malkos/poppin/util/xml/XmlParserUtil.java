package com.malkos.poppin.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.SpsPoDocument;
import com.malkos.poppin.util.xml.parsers.ISpsDocumentProvider;


public class XmlParserUtil {

	public static List<PoDocument> convertXmlStringToPurchaseOrderDocument(InputStream xml) throws Exception{
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		PoDocumentSaxEventHandler handler = new PoDocumentSaxEventHandler();
		
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(xml, handler);
		}catch(SAXException | IOException | ParserConfigurationException ex){
			throw ex;
		}
		finally{
			xml.close();
		}		
		return handler.getPoDocumentList();
	}
	
	public static SpsPoDocument convertXmlStringToSPSPoDocument(InputStream xml, DefaultHandler handler) throws Exception{
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(xml, handler);
		}catch(SAXException | IOException | ParserConfigurationException ex){
			throw ex;
		}
		finally{
			xml.close();
		}
		ISpsDocumentProvider documentProvider = (ISpsDocumentProvider) handler;
		return documentProvider.getSpsPoDocument();
	}
}
