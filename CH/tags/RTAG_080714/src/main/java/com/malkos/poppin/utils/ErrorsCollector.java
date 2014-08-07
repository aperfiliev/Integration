package com.malkos.poppin.utils;

import java.util.ArrayList;
import java.util.List;

import com.malkos.poppin.entities.CHIntegrationError;

public class ErrorsCollector {
	
		//private static List<String> commonErrorMessages = new ArrayList<String>();
		private static List<CHIntegrationError> commonErrorMessages = new ArrayList<>();
		//private static List<OrderErrorMessage> orderErrorMessages = new ArrayList<OrderErrorMessage>();
		private static List<CHIntegrationError> orderErrorMessages = new ArrayList<>();
		//private static List<String> remoteFileOperatorErrorMessage = new ArrayList<>();
		private static List<CHIntegrationError> remoteFileOperatorErrorMessage = new ArrayList<>();
		
		/*public static List<String> getCommonErrorMessages(){
			return commonErrorMessages;
		}*/
		public static List<CHIntegrationError> getCommonErrorMessages(){
			return commonErrorMessages;
		}
		/*public static List<OrderErrorMessage> getOrderErrorMessages(){
			return orderErrorMessages;
		}*/		
		/*public static List<String> getRemoteFileOperatorErrorMessages(){
			return remoteFileOperatorErrorMessage;
		}*/
		public static List<CHIntegrationError> getRemoteFileOperatorErrorMessages(){
			return remoteFileOperatorErrorMessage;
		}
		public static void addOrderErrorMessage(CHIntegrationError orderErrorMessage){
			orderErrorMessages.add(orderErrorMessage);
		}
		/*public static void addOrderErrorMessage(OrderErrorMessage orderErrorMessage){
			orderErrorMessages.add(orderErrorMessage);
		}*/
		public static void addCommonRemoteFileOperatorErrorMessage(CHIntegrationError errorMessage) {
			remoteFileOperatorErrorMessage.add(errorMessage);			
		}
		/*public static void addCommonRemoteFileOperatorErrorMessage(	String errorMessage) {
			remoteFileOperatorErrorMessage.add(errorMessage);			
		}*/
		/*public static void addCommonErrorMessage(String commonErrorMessage){
			commonErrorMessages.add(commonErrorMessage);
		}*/
		public static void addCommonErrorMessage(CHIntegrationError commonErrorMessage){
			commonErrorMessages.add(commonErrorMessage);
		}
		
		
		
		public static Boolean hasCommonErrors(){
			return commonErrorMessages.isEmpty() == false;
		}
		public static Boolean hasOrderErrorMessages(){
			return getOrderErrorMessages().isEmpty() == false;
		}
		public static Boolean hasRemoteFileManagerErrorMessages(){
			return remoteFileOperatorErrorMessage.isEmpty() == false;
		}
		
		
		
		public static void cleanCommonErrors(){
			commonErrorMessages.clear();
		}
		public static void cleanOrderErrors(){
			getOrderErrorMessages().clear();
		}
		public static void cleanRemoteFileOperatorErrors(){
			remoteFileOperatorErrorMessage.clear();
		}
		public static List<CHIntegrationError> getOrderErrorMessages() {
			return orderErrorMessages;
		}
		public static void setOrderErrorMessages(List<CHIntegrationError> orderErrorMessages) {
			ErrorsCollector.orderErrorMessages = orderErrorMessages;
		}		
		
	
}
