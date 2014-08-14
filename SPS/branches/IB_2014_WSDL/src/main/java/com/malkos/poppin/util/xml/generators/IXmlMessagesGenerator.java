package com.malkos.poppin.util.xml.generators;

import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.util.xml.generators.impl.XmlMessageGenerationException;

public interface IXmlMessagesGenerator {
	String generateAsnMessage(FulfillmentPojo fulfillmentPojo) throws XmlMessageGenerationException;
	String generateInvoiceMessage(InvoicePojo invoicePojo) throws XmlMessageGenerationException;
}
