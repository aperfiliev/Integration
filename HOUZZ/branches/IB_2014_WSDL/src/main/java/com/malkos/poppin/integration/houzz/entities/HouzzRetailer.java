package com.malkos.poppin.integration.houzz.entities;

import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.util.NoLogger;
import com.malkos.poppin.integration.houzz.util.OlapicLogger;

public class HouzzRetailer extends RetailerAbstract {
	public HouzzRetailer() {	
		this.identifier=RetailerEnum.HOUZZ;
		this.isNeedAdditionalLogging = false;
		this.isNeedSendFileConfirmation = false;
		this.logger = new NoLogger();
	}
	
	public String getInventoryNotificationEmailsTo() {		
			return this.retailerProperties.getHouzzInventoryNotificationEmailsTo(); 			
	}	
	public String getNotificationEmailServerSmtpUser() {		
		return this.retailerProperties.getHouzzNotificationEmailServerSmtpUser();			
	}	
	public String getNotificationEmailServerStmpPassword() {	
		return this.retailerProperties.getHouzzNotificationEmailServerStmpPassword(); 			
	}	
	public String getNotificationEmailServerPort() {	
		return this.retailerProperties.getHouzzNotificationEmailServerPort();			
	}	
	public String getNotificationEmailServerHost() {
		return this.retailerProperties.getHouzzNotificationEmailServerHost(); 		
	}	
	public String getNotificationEmailFrom() {
		return this.retailerProperties.getHouzzNotificationEmailFrom(); 	
	}	
	public String getNotificationEmailSubject() {
		return this.retailerProperties.getHouzzNotificationEmailSubject(); 
	}	
	public String getIncomingFilesDirName() {
		return this.retailerProperties.getHouzzIncomingFilesDirName(); 
	}	
	public String getNotificationEmailsTo() {
		return this.retailerProperties.getHouzzNotificationEmailsTo(); 
	}	
	public String getFtpConfigPort() {
		return this.retailerProperties.getHouzzFtpConfigPort();
	}	
	public String getFtpConfigPassword() {
		return this.retailerProperties.getHouzzFtpConfigPassword(); 			
	}	
	public String getFtpConfigUse() {
		return this.retailerProperties.getHouzzFtpConfigUse();
	}	
	public String getFtpConfigHost() {
		return this.retailerProperties.getHouzzFtpConfigHost(); 
	}	
	public String getIntegrationInventoryUpdateSchedulingCron() {
		return this.retailerProperties.getIntegrationHouzzInventoryUpdateSchedulingCron(); 
	}
}
