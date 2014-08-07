package com.malkos.poppin.integration.houzz.entities;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalProperties;
import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.util.RetailerLogger;

public abstract class RetailerAbstract {
	protected RetailerEnum identifier;
	protected boolean isNeedSendFileConfirmation;
	protected boolean isNeedAdditionalLogging;
	protected RetailerLogger logger;
	
	protected String InventoryConfirmationEmailSubject;
	protected String InventoryConfirmationEmailContent;
	
	public GlobalProperties retailerProperties = GlobalPropertiesProvider.getGlobalProperties();
	
	public RetailerAbstract(){		
		this.isNeedSendFileConfirmation = false;
		this.isNeedAdditionalLogging = false;
	}	
	public RetailerEnum getIdentifier() {
		return identifier;
	}	
	public void setIdentifier(RetailerEnum identifier) {
		this.identifier = identifier;
	}
	public boolean isNeedSendFileConfirmation() {
		return isNeedSendFileConfirmation;
	}
	public void setNeedSendFileConfirmation(boolean isNeedSendFileConfirmation) {
		this.isNeedSendFileConfirmation = isNeedSendFileConfirmation;
	}
	public boolean isNeedAdditionalLogging() {
		return isNeedAdditionalLogging;
	}
	public void setNeedAdditionalLogging(boolean isNeedAdditionalLogging) {
		this.isNeedAdditionalLogging = isNeedAdditionalLogging;
	}
	
	public String getInventoryNotificationEmailsTo() {		
		return this.retailerProperties.getOlapicInventoryNotificationEmailsTo(); 
	}	
	public abstract String getNotificationEmailServerSmtpUser();
	public abstract String getNotificationEmailServerStmpPassword() ;
	public abstract String getNotificationEmailServerPort();
	public abstract String getNotificationEmailServerHost();
	public abstract String getNotificationEmailFrom();
	public abstract String getNotificationEmailSubject();
	public abstract String getIncomingFilesDirName();	
	public abstract String getNotificationEmailsTo();
	public abstract String getFtpConfigPort();
	public abstract String getFtpConfigPassword();
	public abstract String getFtpConfigUse();
	public abstract String getFtpConfigHost();
	public abstract String getIntegrationInventoryUpdateSchedulingCron();
	public RetailerLogger getLogger() {
		return logger;
	}
	public void setLogger(RetailerLogger logger) {
		this.logger = logger;
	}
	public String getInventoryConfirmationEmailSubject() {
		return InventoryConfirmationEmailSubject;
	}
	public String getInventoryConfirmationEmailContent() {
		return InventoryConfirmationEmailContent;
	}
}
