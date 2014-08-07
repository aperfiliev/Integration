package com.malkos.poppin.validation;

import com.malkos.poppin.documents.PoDocument;

public class StaplesPromoValidator extends StaplesBulkImportValidator {
	@Override
	public void validate(PoDocument document) {
		super.validate(document);
		//document.setAsnGenerated(true);
		//document.setInvoiceMessageGenerated(true);
	}
	
}
