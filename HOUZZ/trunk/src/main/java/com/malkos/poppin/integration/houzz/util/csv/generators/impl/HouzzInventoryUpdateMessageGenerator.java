package com.malkos.poppin.integration.houzz.util.csv.generators.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.malkos.poppin.integration.houzz.util.csv.generators.CsvGenerationException;
import com.malkos.poppin.integration.houzz.util.csv.generators.ICsvMessageGenerator;

public class HouzzInventoryUpdateMessageGenerator implements ICsvMessageGenerator {

	@Override
	public String generateMessage(Collection<InventoryPojo> prepearedInventoryList) throws CsvGenerationException {
		 PrintWriter printWriter = null;
		 String filePath = null;
		 SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("MMddyyyyHHmm");
		 DecimalFormat doublePrecision = new DecimalFormat("#.00"); 
		 DecimalFormat decimalWholeNumberFormat = new DecimalFormat("#"); 
		 Date todayNow = new Date();
	     try {
	    	 	filePath = GlobalPropertiesProvider.getGlobalProperties().getCurrentOutgoingMessagesDirectory() + File.separator + "inventory_poppin" + basicDatePlusTimeDashedFormat.format(todayNow) + ".csv";;
	            printWriter = new PrintWriter(filePath, "UTF-8");
	            printWriter.println("SKU,Price,Quantity,Status,Keywords,Manufacturer,MSRP");
	            for (InventoryPojo invItemBasicPojo : prepearedInventoryList){	            	
            		HouzzInventoryPojo invItemPojo = (HouzzInventoryPojo)invItemBasicPojo;
            		if (!invItemPojo.isWrongConfigured()){
	            		String status = invItemPojo.getCorrectedProperties().isInactive() ? "Inactive":"Active";
		            	printWriter.println(invItemPojo.getSku()+","+doublePrecision.format(invItemPojo.getPrice())+","+decimalWholeNumberFormat.format(invItemPojo.getCorrectedProperties().getQuantity())+","+status+",,"+"Poppin"+","+doublePrecision.format(invItemPojo.getPrice()));
	            	}	            		            		            	
	            }
	     } catch (FileNotFoundException fileNotFoundException) {
	    	 throw new CsvGenerationException(fileNotFoundException.getMessage());
	     } catch (UnsupportedEncodingException unsupportedEncodingException) {
	    	 throw new CsvGenerationException(unsupportedEncodingException.getMessage());
	     } finally {
	            printWriter.close();
	     }
	     return filePath;
	}
}
