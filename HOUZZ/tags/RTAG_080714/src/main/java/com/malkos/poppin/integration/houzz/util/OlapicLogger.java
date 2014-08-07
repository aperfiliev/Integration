package com.malkos.poppin.integration.houzz.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.services.impl.OlapicInventoryUpdateFlowService;

public class OlapicLogger extends RetailerLogger {

	public OlapicLogger(){
		this.logger = LoggerFactory.getLogger(OlapicInventoryUpdateFlowService.class);
	}
	
	@Override
	public void addMessage(String message) {
		try {			
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Olapic.log", true)));	
		    SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("MM-dd-yyyy_HH:mm");		
			Date todayNow = new Date();
		    out.println(basicDatePlusTimeDashedFormat.format(todayNow)+" -[DEBUG]- "+message);
		    out.close();
		} catch (IOException e) {
		   logger.debug(e.getMessage());
		}
	}

	@Override
	public void addError(String message) {
		try {			
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Olapic.log", true)));	
		    SimpleDateFormat basicDatePlusTimeDashedFormat = new SimpleDateFormat("MM-dd-yyyy_HH:mm");		
			Date todayNow = new Date();
		    out.println(basicDatePlusTimeDashedFormat.format(todayNow)+" -[ERROR]- "+message);
		    out.close();
		} catch (IOException e) {
		   logger.debug(e.getMessage());
		}
	}

}
