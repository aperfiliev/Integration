package com.malkos.poppin.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;

public class EnvironmentInitializer {
	
	private final static String ENCRYPRED = "encrypted";
	private final static String DECRYPRED = "decrypted";
	
	private static GlobalProperties properties;
	static{
		properties = GlobalPropertiesProvider.getGlobalProperties();
	}
	
	public static void initializeDirectoriesEnvironment(){
		String today = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		String todayMessagesDirectoryPath = properties.getLocalMessagesArchiveRootPath() + File.separator + today;
		String poStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getPurchaseOrderAbbr(); 
		String faStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getFunctionalAckAbbr();
		String confStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getConfirmationMessageAbbr();
		String inventoryStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getInventoryMessageAbbr();
		String invoiceStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getInvoiceMessageAbbr();
		String requestResponseStorageDirectoryPath = todayMessagesDirectoryPath + File.separator + properties.getRequestResponseDirectory();
		File todayMessagesDirectory = new File(todayMessagesDirectoryPath);
		if(!todayMessagesDirectory.exists()){
			todayMessagesDirectory.mkdirs();
			//PO messages folder	
			File poStorageDirectory = new File(poStorageDirectoryPath);
			poStorageDirectory.mkdir();
			File poEncrypted =  new File(poStorageDirectoryPath + File.separator + ENCRYPRED);
			poEncrypted.mkdir();
			File poDecrypted = new File(poStorageDirectoryPath + File.separator + DECRYPRED);
			poDecrypted.mkdir();
						
			//FA messages folder
			File faStorageDirectory = new File(faStorageDirectoryPath);
			faStorageDirectory.mkdir();
			File faEncrypted =  new File(faStorageDirectoryPath + File.separator + ENCRYPRED);
			faEncrypted.mkdir();
			File faDecrypted = new File(faStorageDirectoryPath + File.separator + DECRYPRED);
			faDecrypted.mkdir();
			
			//Confirmation messages folder
			
			File confStorageDirectory = new File(confStorageDirectoryPath);
			confStorageDirectory.mkdir();
			File confEncrypted =  new File(confStorageDirectoryPath + File.separator + ENCRYPRED);
			confEncrypted.mkdir();
			File confDecrypted = new File(confStorageDirectoryPath + File.separator + DECRYPRED);
			confDecrypted.mkdir();
			
			//Inventory
			File inventoryStorageDirectory = new File(inventoryStorageDirectoryPath);
			inventoryStorageDirectory.mkdir();
			File inventoryEncrypted =  new File(inventoryStorageDirectoryPath + File.separator + ENCRYPRED);
			inventoryEncrypted.mkdir();
			File inventoryDecrypted = new File(inventoryStorageDirectoryPath + File.separator + DECRYPRED);
			inventoryDecrypted.mkdir();
			
			File invoiceStorageDirectory = new File(invoiceStorageDirectoryPath);
			invoiceStorageDirectory.mkdir();
			File invoiceEncrypted =  new File(invoiceStorageDirectoryPath + File.separator + ENCRYPRED);
			invoiceEncrypted.mkdir();
			File invoiceDecrypted = new File(invoiceStorageDirectoryPath + File.separator + DECRYPRED);
			invoiceDecrypted.mkdir();
			
			File requestResponseStorageDirectory = new File(requestResponseStorageDirectoryPath);
			requestResponseStorageDirectory.mkdir();
			
		}
		properties.setPurchaseOrderEncryptedPath(poStorageDirectoryPath + File.separator + ENCRYPRED + File.separator);
		properties.setPurchaseOrderDecryptedPath(poStorageDirectoryPath + File.separator + DECRYPRED + File.separator);
		
		properties.setFuncAckEncryptedPath(faStorageDirectoryPath + File.separator + ENCRYPRED + File.separator);
		properties.setFuncAckDecryptedPath(faStorageDirectoryPath + File.separator + DECRYPRED + File.separator);
		
		properties.setConfirmationEncryptedPath(confStorageDirectoryPath + File.separator + ENCRYPRED + File.separator);
		properties.setConfirmationDecryptedPath(confStorageDirectoryPath + File.separator + DECRYPRED + File.separator);
		
		properties.setInventoryEncryptedPath(inventoryStorageDirectoryPath + File.separator + ENCRYPRED + File.separator);
		properties.setInventoryDecryptedPath(inventoryStorageDirectoryPath + File.separator + DECRYPRED + File.separator);
		
		properties.setInvoiceEncryptedPath(invoiceStorageDirectoryPath + File.separator + ENCRYPRED + File.separator);
		properties.setInvoiceDecryptedPath(invoiceStorageDirectoryPath + File.separator + DECRYPRED + File.separator);
		
		properties.setRequestResponseCurrentPath(requestResponseStorageDirectoryPath+File.separator);
		
	}
}
