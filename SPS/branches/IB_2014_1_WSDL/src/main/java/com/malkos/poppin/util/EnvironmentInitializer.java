package com.malkos.poppin.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.retailers.RetailersManager;

public class EnvironmentInitializer {
	
	private final static String ENCRYPRED = "encrypted";
	private final static String DECRYPRED = "decrypted";
	
	private static GlobalProperties properties;
	static{
		properties = GlobalPropertiesProvider.getGlobalProperties();
		initializeRetailers();
	}
	
	private static void initializeRetailers(){
		RetailersManager.getInstance();
	}
	
	public static void initializeDirectoriesEnvironment(){
		String messagesDirectoryRootPath = properties.getMessagesDirectoryRoot();
		String incomingMessagesDirectoryPath = messagesDirectoryRootPath + File.separator + properties.getIncomingMessagesDirectory();
		String outgoingMessagesDirectoryPath = messagesDirectoryRootPath + File.separator + properties.getOutgoingMessagesDirectory();
		String requestsResponseDirectoryPath = messagesDirectoryRootPath + File.separator + properties.getRequestsResoinsesDirectoryName();
		
		File messagesRootDirectory = new File(messagesDirectoryRootPath);
		if(!messagesRootDirectory.exists()){
			messagesRootDirectory.mkdirs();
			new File(incomingMessagesDirectoryPath).mkdir();
			new File(outgoingMessagesDirectoryPath).mkdir();
			new File(requestsResponseDirectoryPath).mkdir();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String currentYear = Integer.toString(calendar.get(Calendar.YEAR));
		String currentMonth = new SimpleDateFormat("M").format(calendar.getTime());
		String currentDay = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		
		File incomingMessagesCurrentYearDirectory = new File(incomingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		File outgoingMessagesCurrentYearDirectory = new File(outgoingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		File requestsResponseCurrentYearDirectory = new File(requestsResponseDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		
		if(!incomingMessagesCurrentYearDirectory.exists()){
			incomingMessagesCurrentYearDirectory.mkdirs();
			outgoingMessagesCurrentYearDirectory.mkdirs();
			requestsResponseCurrentYearDirectory.mkdirs();
		}
		properties.setCurrentIncomingMessagesDirectory(incomingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		properties.setCurrentOutgoingMessagesDirectory(outgoingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		properties.setCurrentRequestRespnonseDirectory(requestsResponseDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
	}
}
