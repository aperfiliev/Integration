package com.malkos.poppin.integration.houzz.util;

import java.util.ArrayList;
import java.util.List;

import com.malkos.poppin.integration.houzz.entities.IntegrationError;

public class ErrorsCollector {
	private static List<IntegrationError> commonInventoryUpdateErrorMessages = new ArrayList<IntegrationError>();	
	private static List<String> commonPushFilesErrorMessages = new ArrayList<String>();
	private static List<String> nsInventoryConfigurationError = new ArrayList<String>();
	
	public static List<String> getCommonPushFilesErrorMessages(){
		return commonPushFilesErrorMessages;
	}
	public static void addCommonPushFilesErrorMessage(String commonPushFilesErrorMessage){
		commonPushFilesErrorMessages.add(commonPushFilesErrorMessage);
	}
	public static Boolean hasCommonPushFilesErrors(){
		return commonPushFilesErrorMessages.isEmpty() == false;
	}	
	public static void cleanPushFilesErrors(){
		commonPushFilesErrorMessages.clear();		
	}	
	
	public static List<IntegrationError> getCommonInventoryUpdateErrorMessages(){
		return commonInventoryUpdateErrorMessages;
	}
	public static void addCommonInventoryUpdateErrorMessage(IntegrationError commonInventoryUpdateErrorMessage){
		commonInventoryUpdateErrorMessages.add(commonInventoryUpdateErrorMessage);
	}
	public static Boolean hasCommonInventoryUpdateErrors(){
		return commonInventoryUpdateErrorMessages.isEmpty() == false;
	}	
	public static void cleanInventoryUpdateErrors(){
		commonInventoryUpdateErrorMessages.clear();		
	}
	public static void cleanNsInventoryConfigurationErrors(){
		nsInventoryConfigurationError.clear();
	}
	public static List<String> getNsInventoryConfigurationError() {
		return nsInventoryConfigurationError;
	}
	public static void setNsInventoryConfigurationError(List<String> nsInventoryConfigurationError) {
		ErrorsCollector.nsInventoryConfigurationError = nsInventoryConfigurationError;
	}	
	public static Boolean hasNsInventoryConfigurationErrors(){
		return nsInventoryConfigurationError.isEmpty() == false;
	}	
}
