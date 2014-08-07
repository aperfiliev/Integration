package com.malkos.poppin.integration.houzz.bootstrap;

import com.malkos.poppin.integration.houzz.entities.HouzzRetailer;
import com.malkos.poppin.integration.houzz.entities.OlapicRetailer;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;

public class GlobalProperties {	
	
	//Common
	private String resendEmailSchedulingCron;
	private String integrationPushFilesSchedulingCron;
	private String netsuiteConfigAccount;
	private String netsuiteConfigEmail;	
	private String netsuiteConfigPassword;
	private String netsuiteConfigRole;
	private String netsuiteConfigUrl;
	private String messagesDirectoryRoot;
	private String outgoingMessagesDirectory;
	private String environmentName;
	private String currentOutgoingMessagesDirectory;
	private int inventoryUpdateAutoretryAttempts;
	private int inventoryUpdateAutoretryIntervalMinutes;
	private boolean isFirstRun;
	
	//HOUZZ	
	private String integrationHouzzInventoryUpdateSchedulingCron;
	private String houzzFtpConfigHost;
	private String houzzFtpConfigUse;
	private String houzzFtpConfigPassword;
	private String houzzFtpConfigPort;
	private String houzzIncomingFilesDirName;
	private String houzzNotificationEmailsTo;
	private String houzzNotificationEmailFrom;
	private String houzzNotificationEmailSubject;
    private String houzzNotificationEmailServerHost;
	private String houzzNotificationEmailServerPort;
	private String houzzNotificationEmailServerSmtpUser;
	private String houzzNotificationEmailServerStmpPassword;
	private String houzzInventoryNotificationEmailsTo;
	
	
	//OLAPIC
	private String integrationOlapicInventoryUpdateSchedulingCron;	
	private String olapicFtpConfigHost;
	private String olapicFtpConfigUse;
	private String olapicFtpConfigPassword;
	private String olapicFtpConfigPort;
	private String olapicIncomingFilesDirName;
	private String olapicNotificationEmailsTo;
	private String olapicNotificationEmailFrom;
	private String olapicNotificationEmailSubject;
    private String olapicNotificationEmailServerHost;
	private String olapicNotificationEmailServerPort;
	private String olapicNotificationEmailServerSmtpUser;
	private String olapicNotificationEmailServerStmpPassword;
	private String olapicInventoryNotificationEmailsTo;	
	
	
	public String getIntegrationPushFilesSchedulingCron() {
		return integrationPushFilesSchedulingCron;
	}
	public void setIntegrationPushFilesSchedulingCron(String integrationPushFilesSchedulingCron) {
		this.integrationPushFilesSchedulingCron = integrationPushFilesSchedulingCron;
	}
	public String getNetsuiteConfigAccount() {
		return netsuiteConfigAccount;
	}
	public void setNetsuiteConfigAccount(String netsuiteConfigAccount) {
		this.netsuiteConfigAccount = netsuiteConfigAccount;
	}
	public String getNetsuiteConfigEmail() {
		return netsuiteConfigEmail;
	}
	public void setNetsuiteConfigEmail(String netsuiteConfigEmail) {
		this.netsuiteConfigEmail = netsuiteConfigEmail;
	}
	public String getNetsuiteConfigPassword() {
		return netsuiteConfigPassword;
	}
	public void setNetsuiteConfigPassword(String netsuiteConfigPassword) {
		this.netsuiteConfigPassword = netsuiteConfigPassword;
	}
	public String getNetsuiteConfigRole() {
		return netsuiteConfigRole;
	}
	public void setNetsuiteConfigRole(String netsuiteConfigRole) {
		this.netsuiteConfigRole = netsuiteConfigRole;
	}
	public String getNetsuiteConfigUrl() {
		return netsuiteConfigUrl;
	}
	public void setNetsuiteConfigUrl(String netsuiteConfigUrl) {
		this.netsuiteConfigUrl = netsuiteConfigUrl;
	}	
	public String getHouzzIncomingFilesDirName() {
		return houzzIncomingFilesDirName;
	}
	public void setHouzzIncomingFilesDirName(String houzzIncomingFilesDirName) {
		this.houzzIncomingFilesDirName = houzzIncomingFilesDirName;
	}
	public String getMessagesDirectoryRoot() {
		return messagesDirectoryRoot;
	}
	public void setMessagesDirectoryRoot(String messagesDirectoryRoot) {
		this.messagesDirectoryRoot = messagesDirectoryRoot;
	}
	public String getOutgoingMessagesDirectory() {
		return outgoingMessagesDirectory;
	}
	public void setOutgoingMessagesDirectory(String outgoingMessagesDirectory) {
		this.outgoingMessagesDirectory = outgoingMessagesDirectory;
	}
	public String getCurrentOutgoingMessagesDirectory() {
		return currentOutgoingMessagesDirectory;
	}
	public void setCurrentOutgoingMessagesDirectory(
			String currentOutgoingMessagesDirectory) {
		this.currentOutgoingMessagesDirectory = currentOutgoingMessagesDirectory;
	}
	public boolean isFirstRun() {
		return isFirstRun;
	}
	public void setFirstRun(boolean isFirstRun) {
		this.isFirstRun = isFirstRun;
	}
	public int getInventoryUpdateAutoretryAttempts() {
		return inventoryUpdateAutoretryAttempts;
	}
	public void setInventoryUpdateAutoretryAttempts(
			int inventoryUpdateAutoretryAttempts) {
		this.inventoryUpdateAutoretryAttempts = inventoryUpdateAutoretryAttempts;
	}
	public int getInventoryUpdateAutoretryIntervalMinutes() {
		return inventoryUpdateAutoretryIntervalMinutes;
	}
	public void setInventoryUpdateAutoretryIntervalMinutes(
			int inventoryUpdateAutoretryIntervalMinutes) {
		this.inventoryUpdateAutoretryIntervalMinutes = inventoryUpdateAutoretryIntervalMinutes;
	}	
	public String getEnvironmentName() {
		return environmentName;
	}
	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}
	public String getIntegrationHouzzInventoryUpdateSchedulingCron() {
		return integrationHouzzInventoryUpdateSchedulingCron;
	}
	public void setIntegrationHouzzInventoryUpdateSchedulingCron(
			String integrationHouzzInventoryUpdateSchedulingCron) {
		this.integrationHouzzInventoryUpdateSchedulingCron = integrationHouzzInventoryUpdateSchedulingCron;
	}
	public String getHouzzFtpConfigHost() {
		return houzzFtpConfigHost;
	}
	public void setHouzzFtpConfigHost(String houzzFtpConfigHost) {
		this.houzzFtpConfigHost = houzzFtpConfigHost;
	}
	public String getHouzzFtpConfigUse() {
		return houzzFtpConfigUse;
	}
	public void setHouzzFtpConfigUse(String houzzFtpConfigUse) {
		this.houzzFtpConfigUse = houzzFtpConfigUse;
	}
	public String getHouzzFtpConfigPassword() {
		return houzzFtpConfigPassword;
	}
	public void setHouzzFtpConfigPassword(String houzzFtpConfigPassword) {
		this.houzzFtpConfigPassword = houzzFtpConfigPassword;
	}
	public String getHouzzFtpConfigPort() {
		return houzzFtpConfigPort;
	}
	public void setHouzzFtpConfigPort(String houzzFtpConfigPort) {
		this.houzzFtpConfigPort = houzzFtpConfigPort;
	}
	public String getHouzzNotificationEmailsTo() {
		return houzzNotificationEmailsTo;
	}
	public void setHouzzNotificationEmailsTo(String houzzNotificationEmailsTo) {
		this.houzzNotificationEmailsTo = houzzNotificationEmailsTo;
	}
	public String getHouzzNotificationEmailFrom() {
		return houzzNotificationEmailFrom;
	}
	public void setHouzzNotificationEmailFrom(String houzzNotificationEmailFrom) {
		this.houzzNotificationEmailFrom = houzzNotificationEmailFrom;
	}
	public String getHouzzInventoryNotificationEmailsTo() {
		return houzzInventoryNotificationEmailsTo;
	}
	public void setHouzzInventoryNotificationEmailsTo(
			String houzzInventoryNotificationEmailsTo) {
		this.houzzInventoryNotificationEmailsTo = houzzInventoryNotificationEmailsTo;
	}
	public String getHouzzNotificationEmailServerStmpPassword() {
		return houzzNotificationEmailServerStmpPassword;
	}
	public void setHouzzNotificationEmailServerStmpPassword(
			String houzzNotificationEmailServerStmpPassword) {
		this.houzzNotificationEmailServerStmpPassword = houzzNotificationEmailServerStmpPassword;
	}
	public String getHouzzNotificationEmailServerSmtpUser() {
		return houzzNotificationEmailServerSmtpUser;
	}
	public void setHouzzNotificationEmailServerSmtpUser(
			String houzzNotificationEmailServerSmtpUser) {
		this.houzzNotificationEmailServerSmtpUser = houzzNotificationEmailServerSmtpUser;
	}
	public String getHouzzNotificationEmailServerPort() {
		return houzzNotificationEmailServerPort;
	}
	public void setHouzzNotificationEmailServerPort(
			String houzzNotificationEmailServerPort) {
		this.houzzNotificationEmailServerPort = houzzNotificationEmailServerPort;
	}
	public String getHouzzNotificationEmailServerHost() {
		return houzzNotificationEmailServerHost;
	}
	public void setHouzzNotificationEmailServerHost(
			String houzzNotificationEmailServerHost) {
		this.houzzNotificationEmailServerHost = houzzNotificationEmailServerHost;
	}
	public String getHouzzNotificationEmailSubject() {
		return houzzNotificationEmailSubject;
	}
	public void setHouzzNotificationEmailSubject(
			String houzzNotificationEmailSubject) {
		this.houzzNotificationEmailSubject = houzzNotificationEmailSubject;
	}
	public String getOlapicInventoryNotificationEmailsTo() {
		return olapicInventoryNotificationEmailsTo;
	}
	public void setOlapicInventoryNotificationEmailsTo(
			String olapicInventoryNotificationEmailsTo) {
		this.olapicInventoryNotificationEmailsTo = olapicInventoryNotificationEmailsTo;
	}
	public String getOlapicNotificationEmailServerSmtpUser() {
		return olapicNotificationEmailServerSmtpUser;
	}
	public void setOlapicNotificationEmailServerSmtpUser(
			String olapicNotificationEmailServerSmtpUser) {
		this.olapicNotificationEmailServerSmtpUser = olapicNotificationEmailServerSmtpUser;
	}
	public String getOlapicNotificationEmailServerStmpPassword() {
		return olapicNotificationEmailServerStmpPassword;
	}
	public void setOlapicNotificationEmailServerStmpPassword(
			String olapicNotificationEmailServerStmpPassword) {
		this.olapicNotificationEmailServerStmpPassword = olapicNotificationEmailServerStmpPassword;
	}
	public String getOlapicNotificationEmailServerPort() {
		return olapicNotificationEmailServerPort;
	}
	public void setOlapicNotificationEmailServerPort(
			String olapicNotificationEmailServerPort) {
		this.olapicNotificationEmailServerPort = olapicNotificationEmailServerPort;
	}
	public String getOlapicNotificationEmailServerHost() {
		return olapicNotificationEmailServerHost;
	}
	public void setOlapicNotificationEmailServerHost(
			String olapicNotificationEmailServerHost) {
		this.olapicNotificationEmailServerHost = olapicNotificationEmailServerHost;
	}
	public String getOlapicNotificationEmailFrom() {
		return olapicNotificationEmailFrom;
	}
	public void setOlapicNotificationEmailFrom(
			String olapicNotificationEmailFrom) {
		this.olapicNotificationEmailFrom = olapicNotificationEmailFrom;
	}
	public String getOlapicNotificationEmailSubject() {
		return olapicNotificationEmailSubject;
	}
	public void setOlapicNotificationEmailSubject(
			String olapicNotificationEmailSubject) {
		this.olapicNotificationEmailSubject = olapicNotificationEmailSubject;
	}
	public String getOlapicIncomingFilesDirName() {
		return olapicIncomingFilesDirName;
	}
	public void setOlapicIncomingFilesDirName(String olapicIncomingFilesDirName) {
		this.olapicIncomingFilesDirName = olapicIncomingFilesDirName;
	}
	public String getOlapicNotificationEmailsTo() {
		return olapicNotificationEmailsTo;
	}
	public void setOlapicNotificationEmailsTo(String olapicNotificationEmailsTo) {
		this.olapicNotificationEmailsTo = olapicNotificationEmailsTo;
	}
	public String getOlapicFtpConfigPort() {
		return olapicFtpConfigPort;
	}
	public void setOlapicFtpConfigPort(String olapicFtpConfigPort) {
		this.olapicFtpConfigPort = olapicFtpConfigPort;
	}
	public String getOlapicFtpConfigPassword() {
		return olapicFtpConfigPassword;
	}
	public void setOlapicFtpConfigPassword(String olapicFtpConfigPassword) {
		this.olapicFtpConfigPassword = olapicFtpConfigPassword;
	}
	public String getOlapicFtpConfigUse() {
		return olapicFtpConfigUse;
	}
	public void setOlapicFtpConfigUse(String olapicFtpConfigUse) {
		this.olapicFtpConfigUse = olapicFtpConfigUse;
	}
	public String getOlapicFtpConfigHost() {
		return olapicFtpConfigHost;
	}
	public void setOlapicFtpConfigHost(String olapicFtpConfigHost) {
		this.olapicFtpConfigHost = olapicFtpConfigHost;
	}
	public String getIntegrationOlapicInventoryUpdateSchedulingCron() {
		return integrationOlapicInventoryUpdateSchedulingCron;
	}
	public void setIntegrationOlapicInventoryUpdateSchedulingCron(
			String integrationOlapicInventoryUpdateSchedulingCron) {
		this.integrationOlapicInventoryUpdateSchedulingCron = integrationOlapicInventoryUpdateSchedulingCron;
	}
	public String getResendEmailSchedulingCron() {
		return resendEmailSchedulingCron;
	}
	public void setResendEmailSchedulingCron(String resendEmailSchedulingCron) {
		this.resendEmailSchedulingCron = resendEmailSchedulingCron;
	}		
}