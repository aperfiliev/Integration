package com.malkos.poppin.bootstrap;

import java.io.File;

public class GlobalProperties {
	
	private String localMessagesArchiveRootPath;
	private String purchaseOrderAbbr;
	private String functionalAckAbbr;
	private String confirmationMessageAbbr;
	private String invoiceMessageAbbr;
	private String inventoryMessageAbbr;


	private String purchaseOrderEncryptedPath;
	private String purchaseOrderDecryptedPath;
	private String funcAckPath;
	private String funcAckEncryptedPath;
	
	private String funcAckDecryptedPath;
	private String confirmationPath;
	private String confirmationEncryptedPath;
	private String confirmationDecryptedPath;
	private String invoicePath;
	private String invoiceEncryptedPath;
	private String invoiceDecryptedPath;
	private String requestResponseCurrentPath;
	
	private String inventoryPath;
	private String inventoryEncryptedPath;
	private String inventoryDecryptedPath;	
	
	private String ftpStoragePurchaseOrdersPath;
	private String ftpStorageFaPath;
	private String ftpStorageComfirmationMessagePath;
	private String ftpStorageInventoryMessagePath;


	private String ftpStorageInvoiceMessagePath;
	private String poppinPrivateKeyPath;
	private String staplesPublicKeyPath;
	private String secretPassword;
	private String companyId;
	private String companyInternalId;
	
	public static final String STAPLES_PO_MESSAGE_PREFIX =  "STAPLES_ECOM_PO_";
	public static final String STAPLES_FA_MESSAGE_PREFIX =  "STAPLES_ECOM_FA_";
	public static final String STAPLES_INVENTORY_MESSAGE_PREFIX =  "STAPLES_";
	public static final String STAPLES_CONFIRM_MESSAGE_PREFIX =  "STAPLES_ECOM_CONFIRM_";
	public static final String STAPLES_INVOICE_MESSAGE_PREFIX =  " STAPLES_ECOM_INVOICE_";
	
	public static final String SPECIAL_FILE_NAME_DATE_FORMAT = "MMddyyyy_HHmm";
	public static final String TIME_ZONE = "America/New_York";
	
	public static final String WS_REQUEST_RESPONSE_DIRETORY = "requestsandresponses";
	
	private String multiplierToInvetoryAmount;  
	private String multiplierToInvetoryAmountMoreThen100;
	private String inventoryThreshold;
	
	private String inventoryUpdateAutoretryAttempts;
	private String inventoryUpdateAutoretryInterval;
	
	private String nsUsername;
	
	private String notificationEmailTo;
	private String notificationEmailFrom;
	private String notificationEmailSubject;
	
	private String environment;
	
	public String getMultiplierToInvetoryAmountMoreThen100() {
		return multiplierToInvetoryAmountMoreThen100;
	}

	public void setMultiplierToInvetoryAmountMoreThen100(
			String multiplierToInvetoryAmountMoreThen100) {
		this.multiplierToInvetoryAmountMoreThen100 = multiplierToInvetoryAmountMoreThen100;
	}

	public String getMultiplierToInvetoryAmount() {
		return multiplierToInvetoryAmount;
	}

	public void setMultiplierToInvetoryAmount(String multiplierToInvetoryAmount) {
		this.multiplierToInvetoryAmount = multiplierToInvetoryAmount;
	}

	public String getInventoryThreshold() {
		return inventoryThreshold;
	}

	public void setInventoryThreshold(String inventoryThreshold) {
		this.inventoryThreshold = inventoryThreshold;
	}

	public String getPurchaseOrderEncryptedPath() {
		return purchaseOrderEncryptedPath;
	}

	public void setPurchaseOrderEncryptedPath(String purchaseOrderEncryptedPath) {
		this.purchaseOrderEncryptedPath = purchaseOrderEncryptedPath;
	}

	public String getPurchaseOrderDecryptedPath() {
		return purchaseOrderDecryptedPath;
	}

	public void setPurchaseOrderDecryptedPath(String purchaseOrderDecryptedPath) {
		this.purchaseOrderDecryptedPath = purchaseOrderDecryptedPath;
	}

	public String getFuncAckPath() {
		return funcAckPath;
	}

	public void setFuncAckPath(String funcAckPath) {
		this.funcAckPath = funcAckPath;
	}

	public String getFuncAckEncryptedPath() {
		return funcAckEncryptedPath;
	}

	public void setFuncAckEncryptedPath(String funcAckEncryptedPath) {
		this.funcAckEncryptedPath = funcAckEncryptedPath;
	}

	public String getFuncAckDecryptedPath() {
		return funcAckDecryptedPath;
	}

	public void setFuncAckDecryptedPath(String funcAckDecryptedPath) {
		this.funcAckDecryptedPath = funcAckDecryptedPath;
	}

	public String getConfirmationPath() {
		return confirmationPath;
	}

	public void setConfirmationPath(String confirmationPath) {
		this.confirmationPath = confirmationPath;
	}

	public String getConfirmationEncryptedPath() {
		return confirmationEncryptedPath;
	}

	public String getInventoryPath() {
		return inventoryPath;
	}

	public void setInventoryPath(String inventoryPath) {
		this.inventoryPath = inventoryPath;
	}

	public String getInventoryEncryptedPath() {
		return inventoryEncryptedPath;
	}

	public void setInventoryEncryptedPath(String inventoryEncryptedPath) {
		this.inventoryEncryptedPath = inventoryEncryptedPath;
	}

	public String getInventoryDecryptedPath() {
		return inventoryDecryptedPath;
	}

	public void setInventoryDecryptedPath(String inventoryDecryptedPath) {
		this.inventoryDecryptedPath = inventoryDecryptedPath;
	}

	public void setConfirmationEncryptedPath(String confirmationEncryptedPath) {
		this.confirmationEncryptedPath = confirmationEncryptedPath;
	}

	public String getConfirmationDecryptedPath() {
		return confirmationDecryptedPath;
	}

	public void setConfirmationDecryptedPath(String confirmationDecryptedPath) {
		this.confirmationDecryptedPath = confirmationDecryptedPath;
	}

	public String getInvoicePath() {
		return invoicePath;
	}

	public void setInvoicePath(String invoicePath) {
		this.invoicePath = invoicePath;
	}

	public String getInvoiceEncryptedPath() {
		return invoiceEncryptedPath;
	}

	public void setInvoiceEncryptedPath(String invoiceEncryptedPath) {
		this.invoiceEncryptedPath = invoiceEncryptedPath;
	}

	public String getInvoiceDecryptedPath() {
		return invoiceDecryptedPath;
	}

	public void setInvoiceDecryptedPath(String invoiceDecryptedPath) {
		this.invoiceDecryptedPath = invoiceDecryptedPath;
	}


	public String getLocalMessagesArchiveRootPath() {
		return localMessagesArchiveRootPath;
	}

	public String getPoppinPrivateKeyPath() {
		return poppinPrivateKeyPath;
	}

	public void setPoppinPrivateKeyPath(String poppinPrivateKeyPath) {
		this.poppinPrivateKeyPath = poppinPrivateKeyPath;
	}

	public void setLocalMessagesArchiveRootPath(String localMessagesArchiveRootPath) {
		this.localMessagesArchiveRootPath = localMessagesArchiveRootPath;
	}

	public String getPurchaseOrderAbbr() {
		return purchaseOrderAbbr;
	}

	public void setPurchaseOrderAbbr(String purchaseOrderAbbr) {
		this.purchaseOrderAbbr = purchaseOrderAbbr;
	}

	public String getFunctionalAckAbbr() {
		return functionalAckAbbr;
	}

	public void setFunctionalAckAbbr(String functionalAckAbbr) {
		this.functionalAckAbbr = functionalAckAbbr;
	}

	public String getConfirmationMessageAbbr() {
		return confirmationMessageAbbr;
	}

	public void setConfirmationMessageAbbr(String confirmationMessageAbbr) {
		this.confirmationMessageAbbr = confirmationMessageAbbr;
	}

	public String getInvoiceMessageAbbr() {
		return invoiceMessageAbbr;
	}

	public void setInvoiceMessageAbbr(String invoiceMessageAbbr) {
		this.invoiceMessageAbbr = invoiceMessageAbbr;
	}
	
	public String getInventoryMessageAbbr() {
		return inventoryMessageAbbr;
	}

	public void setInventoryMessageAbbr(String inventoryMessageAbbr) {
		this.inventoryMessageAbbr = inventoryMessageAbbr;
	}
	
	public String getFtpStoragePurchaseOrdersPath() {
		return ftpStoragePurchaseOrdersPath;
	}

	public void setFtpStoragePurchaseOrdersPath(String ftpStoragePurchaseOrdersPath) {
		this.ftpStoragePurchaseOrdersPath = ftpStoragePurchaseOrdersPath;
	}
	public String getFtpStorageFaPath() {
		return ftpStorageFaPath;
	}

	public void setFtpStorageFaPath(String ftpStorageFaPath) {
		this.ftpStorageFaPath = ftpStorageFaPath;
	}

	public String getFtpStorageComfirmationMessagePath() {
		return ftpStorageComfirmationMessagePath;
	}

	public void setFtpStorageComfirmationMessagePath(
			String ftpStorageComfirmationMessagePath) {
		this.ftpStorageComfirmationMessagePath = ftpStorageComfirmationMessagePath;
	}

	public String getFtpStorageInventoryMessagePath() {
		return ftpStorageInventoryMessagePath;
	}

	public void setFtpStorageInventoryMessagePath(
			String ftpStorageInventoryMessagePath) {
		this.ftpStorageInventoryMessagePath = ftpStorageInventoryMessagePath;
	}
	
	public String getFtpStorageInvoiceMessagePath() {
		return ftpStorageInvoiceMessagePath;
	}

	public void setFtpStorageInvoiceMessagePath(String ftpStorageInvoiceMessagePath) {
		this.ftpStorageInvoiceMessagePath = ftpStorageInvoiceMessagePath;
	}
	public String getStaplesPublicKeyPath() {
		return staplesPublicKeyPath;
	}

	public void setStaplesPublicKeyPath(String staplesPublicKeyPath) {
		this.staplesPublicKeyPath = staplesPublicKeyPath;
	}

	public String getSecretPassword() {
		return secretPassword;
	}

	public void setSecretPassword(String secretPassword) {
		this.secretPassword = secretPassword;
	}
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyInternalId() {
		return companyInternalId;
	}

	public void setCompanyInternalId(String companyInternalId) {
		this.companyInternalId = companyInternalId;
	}

	public String getRequestResponseDirectory() {
		return  WS_REQUEST_RESPONSE_DIRETORY;
	}

	public String getRequestResponseCurrentPath() {
		return requestResponseCurrentPath;
	}

	public void setRequestResponseCurrentPath(String requestResponseCurrentPath) {
		this.requestResponseCurrentPath = requestResponseCurrentPath;
	}

	public String getInventoryUpdateAutoretryAttempts() {
		return inventoryUpdateAutoretryAttempts;
	}

	public void setInventoryUpdateAutoretryAttempts(
			String inventoryUpdateAutoretryAttempts) {
		this.inventoryUpdateAutoretryAttempts = inventoryUpdateAutoretryAttempts;
	}

	public String getInventoryUpdateAutoretryInterval() {
		return inventoryUpdateAutoretryInterval;
	}

	public void setInventoryUpdateAutoretryInterval(
			String inventoryUpdateAutoretryInterval) {
		this.inventoryUpdateAutoretryInterval = inventoryUpdateAutoretryInterval;
	}

	public String getNsUsername() {
		return nsUsername;
	}

	public void setNsUsername(String nsUsername) {
		this.nsUsername = nsUsername;
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

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}
}
