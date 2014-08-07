package com.malkos.poppin.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentItem;
import com.malkos.poppin.entities.OrderErrorDetails;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.integration.IntegrationDetailsProvider;

public class StaplesBulkImportValidator implements IValidator{

	@Override
	public void validate(PoDocument document) {
		IntegrationDetailsProvider idProvider = IntegrationDetailsProvider.getInstance();
		List<String> requiredFieldsMissedList = new ArrayList<String>();
		String commonError = "";
		if(document.getPoNumber() == null)
			requiredFieldsMissedList.add("poNumber");
		if (document.getPoDate() == null)
			requiredFieldsMissedList.add("poDate");
		
		if (document.getShipLocationNumber() == null)
			requiredFieldsMissedList.add("AddressLocationNumber");
				
		/*if(document.getShipToName() == null)
			requiredFieldsMissedList.add("shipToName");*/
		List<String> wrongVendorPartNumbersList = new ArrayList<>();
		for(PoDocumentItem pdi : document.getPoDocumentItemList()){
			if(pdi.getPopMapperNum() == null)
				requiredFieldsMissedList.add("VendorPartNumber");
			if(pdi.getOrderQty() == 0.0)
				requiredFieldsMissedList.add("orderQty");
			if(pdi.getUnitPrice() == 0.0)
				requiredFieldsMissedList.add("unitPrice");
			if(pdi.getVendorlineNumber() == null)
				requiredFieldsMissedList.add("LineSequenceNumber");
			if(!document.getRetailer().getPoppinAssortment().containsKey(pdi.getPopMapperNum())){
				commonError += "Could not find appropriate to " + pdi.getPopMapperNum() + " inventory item in NetSuite.\r\n";
				wrongVendorPartNumbersList.add(pdi.getPopMapperNum());
			}
		}
		
		if (!wrongVendorPartNumbersList.isEmpty()){
			if (document.getErrorDetails()==null){
				document.setErrorDetails(new OrderErrorDetails());
			}
			document.getErrorDetails().setErrorVendorPartNumbers(wrongVendorPartNumbersList);
		}
		
		if(document.getShipLocationNumber() != null){
			//if(!idProvider.getRetailersShippingAddresses().containsKey(document.getShipLocationNumber()))
			Map<String, ShippingAddressPojo> addressMappings = idProvider.getRetailerShippingAddressesMappings(document.getRetailerId());
			if(!addressMappings.containsKey(document.getShipLocationNumber()))
				commonError += "Could not find appropriate to AddressLocationNumber = "+ document.getShipLocationNumber() + " shipping address in NetSuite.\r\n";
		}
		if(!requiredFieldsMissedList.isEmpty()){
			StringBuilder sb = new StringBuilder();
			sb.append("Provided orders file didn't contain all the required fields. Please specify value for next fields:\r\n");
			for(String str: requiredFieldsMissedList)
				sb.append(str+"\r\n");
			document.setExceptionDescription(sb.toString());
		}					
		if(! commonError.isEmpty()){
			if(null == document.getExceptionDescription())
				document.setExceptionDescription(commonError);
			else
				document.setExceptionDescription(document.getExceptionDescription() + "\r\nAlso :\r\n" + commonError);
		}
		document.setAsnGenerated(true);
		document.setInvoiceMessageGenerated(true);
	}

}
