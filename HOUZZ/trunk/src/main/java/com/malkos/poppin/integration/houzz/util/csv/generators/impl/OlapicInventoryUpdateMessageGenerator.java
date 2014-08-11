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
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;
import com.malkos.poppin.integration.houzz.entities.OlapicInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.util.csv.generators.CsvGenerationException;
import com.malkos.poppin.integration.houzz.util.csv.generators.ICsvMessageGenerator;

public class OlapicInventoryUpdateMessageGenerator implements ICsvMessageGenerator {
	private RetailerAbstract retailer = RetailerManager.get_retailer(RetailerEnum.OLAPIC);

	@SuppressWarnings("unused")
	@Override
	public String generateMessage(Collection<InventoryPojo> prepearedInventoryList) throws CsvGenerationException {
		 PrintWriter printWriter = null;
		 String filePath = null;
		 SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("MMddyyyyHHmm");		
		 Date todayNow = new Date();
	     try {
	    	 	String fileName = "Poppin_product_feed" + basicDatePlusTimeDashedFormat.format(todayNow) + ".csv";
	    	 	filePath = GlobalPropertiesProvider.getGlobalProperties().getCurrentOutgoingMessagesDirectory() + File.separator + fileName;
	            printWriter = new PrintWriter(filePath,"UTF-8");
	            if (printWriter == null){
	            	 throw new CsvGenerationException("Couldn't generate file "+fileName);
	            }
	            printWriter.println("Internal ID,Display Name,Item URL,Internal ID,Item Display Image");
	            for (InventoryPojo invItemPojoBasic : prepearedInventoryList){	
	            	if (invItemPojoBasic instanceof OlapicInventoryItemPojo){
	            		OlapicInventoryItemPojo invItemPojo = (OlapicInventoryItemPojo)invItemPojoBasic;
	            		printWriter.println(invItemPojo.getInternalId()+","+invItemPojo.getDisplayName()+","+invItemPojo.getItemUrl()+","+invItemPojo.getInternalId()+","+invItemPojo.getItemDisplayImage());
	            	}	          		            	            	
	            }
	            retailer.getLogger().addMessage("Olapic inventory update file "+fileName+" was successfully generated");
	     } catch (FileNotFoundException fileNotFoundException) {
	    	 throw new CsvGenerationException(fileNotFoundException.getMessage());
	     } catch (UnsupportedEncodingException e) {
	    	 throw new CsvGenerationException(e.getMessage());
		 } finally {
	            printWriter.close();
	     }
	     return filePath;
	}	
}
