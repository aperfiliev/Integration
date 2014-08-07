package com.malkos.poppin.util.xml.parsers;

import com.malkos.poppin.documents.SpsIndigoBulkImportPoDocument;
import com.malkos.poppin.integration.retailers.RetailerAbstract;

public class SpsIndigoBulkImportPoSaxEventHandler extends SpsStaplesBulkImportPoSaxEventHandler{

	public SpsIndigoBulkImportPoSaxEventHandler(RetailerAbstract retailer) {
		super(retailer);
	}
	
	@Override 
	protected void createNewDocument(){
		this.poDocument = new SpsIndigoBulkImportPoDocument();
	}
}
