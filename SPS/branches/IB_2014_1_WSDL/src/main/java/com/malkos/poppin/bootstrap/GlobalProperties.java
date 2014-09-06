package com.malkos.poppin.bootstrap;

import java.util.HashMap;
import java.util.Map;

import com.netsuite.webservices.platform.common_2014_1.types.Country;

public class GlobalProperties {
	
	private String spsOutgoingFilesDirectoryName;
	private String spsIncomingFilesDirectoryName;
	private String messagesDirectoryRoot;
	private String incomingMessagesDirectory;
	private String outgoingMessagesDirectory;
	
	private String currentIncomingMessagesDirectory;
	private String currentOutgoingMessagesDirectory;
	private String currentRequestRespnonseDirectory;
	
	private String environment;
	
	private String bnnRetailerNsMappingCustomRecordId;
	
	private String notificationEmailTo;
	private String notificationEmailFrom;
	private String notificationEmailSubject;
	
	private String nsUsername;
	
	public static final String ENVIRONMENT_SANDBOX = "sandbox";
	public static final String ENVIRONMENT_PRODUCTION = "production";
	public static final String DEFAULT_SHIPPING_COUNTRY = "USA";
	public static final String QUILL_RETAILER_NAME = "77HALLPOPPIN000";
	public static final String STAPLES_RETAILER_NAME = "549ALLPOPPIN000";
	public static final String INDIGO_RETAILER_NAME = "BNDALLPOPPIN000";
	//I am a dummy portion of code. I have appeared because of stupid guys from SPS. Please remove me when you will go to PROD
	public static final String STAPLES_RETAILER_STUPID_SPS_PSEUDO_NAME = "SPS720POPPIN";
	public static final String WS_REQUEST_RESPONSE_DIRETORY = "requestsandresponses";
	public static final String BARNES_N_NOBLE_RETAILER_NAME = "582ALLPOPPIN000";
	
	public static Map<String, String> countryMappings = new HashMap<String,String>();
	
	static{
		countryMappings.put(Country.__unitedStates, "USA");
		countryMappings.put(Country.__canada, "Canada");
	}
	
	public String getMessagesDirectoryRoot() {
		return messagesDirectoryRoot;
	}
	public void setMessagesDirectoryRoot(String messagesDirectoryRoot) {
		this.messagesDirectoryRoot = messagesDirectoryRoot;
	}
	public String getIncomingMessagesDirectory() {
		return incomingMessagesDirectory;
	}
	public void setIncomingMessagesDirectory(String incomingMessagesDirectory) {
		this.incomingMessagesDirectory = incomingMessagesDirectory;
	}
	public String getOutgoingMessagesDirectory() {
		return outgoingMessagesDirectory;
	}
	public void setOutgoingMessagesDirectory(String outgoingMessagesDirectory) {
		this.outgoingMessagesDirectory = outgoingMessagesDirectory;
	}
	
	public String getSpsOutgoingFilesDirectoryName() {
		return spsOutgoingFilesDirectoryName;
	}
	public void setSpsOutgoingFilesDirectoryName(
			String spsOutgoingFilesDirectoryName) {
		this.spsOutgoingFilesDirectoryName = spsOutgoingFilesDirectoryName;
	}
	public String getSpsIncomingFilesDirectoryName() {
		return spsIncomingFilesDirectoryName;
	}
	public void setSpsIncomingFilesDirectoryName(
			String spsIncomingFilesDirectoryName) {
		this.spsIncomingFilesDirectoryName = spsIncomingFilesDirectoryName;
	}
	public String getCurrentIncomingMessagesDirectory() {
		return currentIncomingMessagesDirectory;
	}
	public void setCurrentIncomingMessagesDirectory(
			String currentIncomingMessagesDirectory) {
		this.currentIncomingMessagesDirectory = currentIncomingMessagesDirectory;
	}
	public String getCurrentOutgoingMessagesDirectory() {
		return currentOutgoingMessagesDirectory;
	}
	public void setCurrentOutgoingMessagesDirectory(
			String currentOutgoingMessagesDirectory) {
		this.currentOutgoingMessagesDirectory = currentOutgoingMessagesDirectory;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String  getRequestsResoinsesDirectoryName(){
		return WS_REQUEST_RESPONSE_DIRETORY;
	}
	public String getCurrentRequestRespnonseDirectory() {
		return currentRequestRespnonseDirectory;
	}
	public void setCurrentRequestRespnonseDirectory(String currentRequestRespnonseDirectory) {
		this.currentRequestRespnonseDirectory = currentRequestRespnonseDirectory;
	}
	public String getBnnRetailerNsMappingCustomRecordId() {
		return bnnRetailerNsMappingCustomRecordId;
	}
	public void setBnnRetailerNsMappingCustomRecordId(
			String bnnRetailerNsMappingCustomRecordId) {
		this.bnnRetailerNsMappingCustomRecordId = bnnRetailerNsMappingCustomRecordId;
	}
	public String getNotificationEmailTo() {
		return notificationEmailTo;
	}
	public void setNotificationEmailTo(String notificationEmailTo) {
		this.notificationEmailTo = notificationEmailTo;
	}
	public String getNotificationEmailFrom() {
		return notificationEmailFrom;
	}
	public void setNotificationEmailFrom(String notificationEmailFrom) {
		this.notificationEmailFrom = notificationEmailFrom;
	}
	public String getNotificationEmailSubject() {
		return notificationEmailSubject;
	}
	public void setNotificationEmailSubject(String notificationEmailSubject) {
		this.notificationEmailSubject = notificationEmailSubject;
	}
	public String getNsUsername() {
		return nsUsername;
	}
	public void setNsUsername(String nsUsername) {
		this.nsUsername = nsUsername;
	}
}
