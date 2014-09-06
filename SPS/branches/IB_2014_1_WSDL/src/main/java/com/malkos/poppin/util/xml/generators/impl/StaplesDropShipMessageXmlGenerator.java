package com.malkos.poppin.util.xml.generators.impl;

import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.integration.retailers.RetailerAbstract;
import com.malkos.poppin.util.xml.generators.IXmlMessagesGenerator;
import com.malkos.poppin.util.xml.generators.XmlDocumentGenerator;

public class StaplesDropShipMessageXmlGenerator extends XmlDocumentGenerator implements IXmlMessagesGenerator{

	public StaplesDropShipMessageXmlGenerator(RetailerAbstract retailer) {
		super(retailer);
	}
	
	@Override
	public String generateAsnMessage(FulfillmentPojo fulfillmentPojo) {
		return null;
		
	}

	@Override
	public String generateInvoiceMessage(InvoicePojo invoicePojo) {
		return null;
		
	}

}
