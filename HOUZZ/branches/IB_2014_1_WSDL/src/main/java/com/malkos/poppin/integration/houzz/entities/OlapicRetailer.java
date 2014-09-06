package com.malkos.poppin.integration.houzz.entities;

import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.util.OlapicLogger;

public class OlapicRetailer extends RetailerAbstract {

	public OlapicRetailer() {	
		this.identifier=RetailerEnum.OLAPIC;
		this.isNeedAdditionalLogging = true;
		this.isNeedSendFileConfirmation = true;
		this.InventoryConfirmationEmailSubject= this.retailerProperties.getEnvironmentName()+" - Olapic product feed posted successfully";
		this.InventoryConfirmationEmailContent = "The Olapic product feed has been posted successfully. Attached is the file that was sent to Olapic.";
		this.logger = new OlapicLogger();
	}

	public String getInventoryNotificationEmailsTo() {		
			return this.retailerProperties.getOlapicInventoryNotificationEmailsTo(); 			
	}	
	public String getNotificationEmailServerSmtpUser() {		
		return this.retailerProperties.getOlapicNotificationEmailServerSmtpUser();			
	}	
	public String getNotificationEmailServerStmpPassword() {	
		return this.retailerProperties.getOlapicNotificationEmailServerStmpPassword(); 			
	}	
	public String getNotificationEmailServerPort() {	
		return this.retailerProperties.getOlapicNotificationEmailServerPort();			
	}	
	public String getNotificationEmailServerHost() {
		return this.retailerProperties.getOlapicNotificationEmailServerHost(); 		
	}	
	public String getNotificationEmailFrom() {
		return this.retailerProperties.getOlapicNotificationEmailFrom(); 	
	}	
	public String getNotificationEmailSubject() {
		return this.retailerProperties.getOlapicNotificationEmailSubject(); 
	}	
	public String getIncomingFilesDirName() {
		return this.retailerProperties.getOlapicIncomingFilesDirName(); 
	}	
	public String getNotificationEmailsTo() {
		return this.retailerProperties.getOlapicNotificationEmailsTo(); 
	}	
	public String getFtpConfigPort() {
		return this.retailerProperties.getOlapicFtpConfigPort();
	}	
	public String getFtpConfigPassword() {
		return this.retailerProperties.getOlapicFtpConfigPassword(); 			
	}	
	public String getFtpConfigUse() {
		return this.retailerProperties.getOlapicFtpConfigUse();
	}	
	public String getFtpConfigHost() {
		return this.retailerProperties.getOlapicFtpConfigHost(); 
	}	
	public String getIntegrationInventoryUpdateSchedulingCron() {
		return this.retailerProperties.getIntegrationOlapicInventoryUpdateSchedulingCron(); 
	}		
}
