package com.malkos.poppin.integration.houzz.services.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.ErrorMessageWraped;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitSubItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzUnavailableInventories;
import com.malkos.poppin.integration.houzz.entities.IntegrationError;
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;
import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.IPersistenceManager;
import com.malkos.poppin.integration.houzz.persistence.PersistanceManagerProvider;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.integration.houzz.services.IInventoryListUpdateble;
import com.malkos.poppin.integration.houzz.services.IInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.transport.INetsuiteOperationsManager;
import com.malkos.poppin.integration.houzz.transport.NetsuiteOperationException;
import com.malkos.poppin.integration.houzz.transport.NetsuiteOperationsManager;
import com.malkos.poppin.integration.houzz.util.ErrorMessageWrapper;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.malkos.poppin.integration.houzz.util.MailMessagingService;
import com.malkos.poppin.integration.houzz.util.csv.generators.CsvGenerationException;
import com.malkos.poppin.integration.houzz.util.csv.generators.ICsvMessageGenerator;
import com.malkos.poppin.integration.houzz.util.csv.generators.impl.HouzzInventoryUpdateMessageGenerator;


public class HouzzInventoryUpdateFlowService implements IInventoryUpdateFlowService, IInventoryListUpdateble{
	
	private static Logger logger = LoggerFactory.getLogger(HouzzInventoryUpdateFlowService.class);
	private static INetsuiteOperationsManager netsuiteOperationsManager = new NetsuiteOperationsManager();
	private static IPersistenceManager persistenceManager = PersistanceManagerProvider.getInstance();
	private static ICsvMessageGenerator csvMessageGenerator = new HouzzInventoryUpdateMessageGenerator();
	private RetailerAbstract houzzRetailer = RetailerManager.get_retailer(RetailerEnum.HOUZZ);
	
	@Override
	public void updateInventory() {		
		logger.info("Updating Inventory.");
		//1. Retrieve inventoryMapping
		List<LineItemIntegrationIdentifierDAO> lineItemDAOs = persistenceManager.getHouzzAssortment();
		//2. Retrieve updated inventory quantities		
		Map<String,InventoryPojo> internalIdToInventoryMap = null;
		String messagePath = null;
		try {
			internalIdToInventoryMap = netsuiteOperationsManager.loadHouzzInventory(lineItemDAOs);			
		} catch (NetsuiteOperationException e) {
			e.getRequestDetails().setRetailer(RetailerEnum.HOUZZ.toString());
			IntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),e.getRequestDetails() );
			ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);
			return;
		}	
		//List<HouzzInventoryPojo> prepearedInventoryUpdateList;
		if (internalIdToInventoryMap!=null){
			//3. Generating inventory messages
			//prepearedInventoryUpdateList = prepeareInventoryUpdateList(inventoryTypeClassToInventoryListMap);		
			try {
				messagePath = csvMessageGenerator.generateMessage(internalIdToInventoryMap.values());				
			} catch (CsvGenerationException e) {
				NSRrequestDetails details = new NSRrequestDetails();
				details.setRetailer(RetailerEnum.HOUZZ.toString());
				IntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),details);
				ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);
				return;
			}
			HouzzUnavailableInventories unavailableInventories = extractUnavailableInventories(internalIdToInventoryMap.values());	
			String message = buildNotificationEmail(unavailableInventories);
			String mailSubject = buildNotificationEmailSubject();
			try {
				if (message!=null){
					MailMessagingService.sendMessage(InternetAddress.parse(houzzRetailer.getInventoryNotificationEmailsTo()), mailSubject, message,houzzRetailer);
				}	
			} catch (MessagingException e) {
				persistenceManager.persistNotificationEmail(message, mailSubject, houzzRetailer.getInventoryNotificationEmailsTo(), null, houzzRetailer.getIdentifier());
				NSRrequestDetails details = new NSRrequestDetails();
				details.setRetailer(RetailerEnum.HOUZZ.toString());
				IntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(),details);
				ErrorsCollector.addCommonInventoryUpdateErrorMessage(error);				
			}
			finally {
				ErrorsCollector.cleanNsInventoryConfigurationErrors();
			}
		}		
		persistenceManager.persistOutgoingMessage(messagePath,RetailerEnum.HOUZZ);		
	}
	
	private String buildNotificationEmail(HouzzUnavailableInventories unavailableInventories) {
		String inactiveItemsMessage = null;
		String lowQuantityItemsMessage = null;
		String wrongConfiguredItemsMessage = null;
		DecimalFormat basicDecimalFormat = new DecimalFormat("#.##");
		DecimalFormat basicWholeDecimalFormat = new DecimalFormat("#");
		
		if (ErrorsCollector.hasNsInventoryConfigurationErrors()){
			wrongConfiguredItemsMessage="<h2>NetSuite Item Configuration Errors:</h2><ol>";
			for (String error:ErrorsCollector.getNsInventoryConfigurationError()){
				wrongConfiguredItemsMessage+="<li>"+error+"</li>";
			}
			wrongConfiguredItemsMessage+="</ol>";
		}
		String message = null;
		if ((!unavailableInventories.getInactiveItemList().isEmpty())){
			inactiveItemsMessage = "<h2>Inactive items in NetSuite:</h2>"+"<table style=\"border:1px solid black; border-collapse:collapse;\"><tr>" +
					"<td style=\"border:1px solid black;\"><b>SKU</b></td><td style=\"border:1px solid black;\"><b>Price</b></td><td style=\"border:1px solid black;\">" +
					"<b>Quantity</b></td><td style=\"border:1px solid black;\"><b>Status</b></td><td style=\"border:1px solid black;\">" +
					"<b>Keywords</b></td><td style=\"border:1px solid black;\"><b>Manufacturer</b></td><td style=\"border:1px solid black;\"><b>MSRP</b></td></tr>";
			for (HouzzInventoryPojo iiPojo:unavailableInventories.getInactiveItemList()){
				inactiveItemsMessage+="<tr><td style=\"border:1px solid black;\">"+iiPojo.getSku()+"</td><td style=\"border:1px solid black;\">"+
						basicDecimalFormat.format(iiPojo.getPrice())+"</td><td style=\"border:1px solid black;\">"+basicWholeDecimalFormat.format(iiPojo.getCorrectedProperties().getQuantity())+"</td>" +
						"<td style=\"border:1px solid black;\">"+"Inactive"+"</td><td style=\"border:1px solid black;\"></td><td style=\"border:1px solid black;\">Poppin</td>" +
						"<td style=\"border:1px solid black;\">"+basicDecimalFormat.format(iiPojo.getPrice())+"</td></tr>";					
		
			}	
			inactiveItemsMessage+="</table><br><br>";
		}	
		if ((!unavailableInventories.getLowQuantityItemList().isEmpty())){
			lowQuantityItemsMessage = "<h2>Items with low inventory in NetSuite:</h2>"+"<table style=\"border:1px solid black; border-collapse:collapse;\"><tr>" +
					"<td style=\"border:1px solid black;\"><b>SKU</b></td><td style=\"border:1px solid black;\"><b>Price</b></td><td style=\"border:1px solid black;\">" +
					"<b>Quantity</b></td><td style=\"border:1px solid black;\"><b>Status</b></td><td style=\"border:1px solid black;\"><b>Keywords</b></td>" +
					"<td style=\"border:1px solid black;\"><b>Manufacturer</b></td><td style=\"border:1px solid black;\"><b>MSRP</b></td></tr>";			
			for (HouzzInventoryPojo iiPojo:unavailableInventories.getLowQuantityItemList()){				
				lowQuantityItemsMessage+="<tr><td style=\"border:1px solid black;\">"+iiPojo.getSku()+"</td><td style=\"border:1px solid black;\">"+basicDecimalFormat.format(iiPojo.getPrice())+"</td>" +
						"<td style=\"border:1px solid black;\">"+basicWholeDecimalFormat.format(iiPojo.getCorrectedProperties().getQuantity())+"</td><td style=\"border:1px solid black;\">"+
						"Inactive"+"</td><td style=\"border:1px solid black;\"></td><td style=\"border:1px solid black;\">Poppin</td><td style=\"border:1px solid black;\">"+basicDecimalFormat.format(iiPojo.getPrice())+"</td></tr>";					
			}	
			lowQuantityItemsMessage+="</table><br><br>";
		}		
		if ((inactiveItemsMessage!=null)||(lowQuantityItemsMessage!=null)){
			message = "The Houzz inventory upload contained Poppin items that either are low on inventory or are marked as 'inactive' in NetSuite. The items listed below were sent to Houzz with a status = 'inactive' and quantity = 0.<br><br>";
		}
		if (lowQuantityItemsMessage!=null)
			message+=lowQuantityItemsMessage;
		if (inactiveItemsMessage!=null)
			message+=inactiveItemsMessage;
		if (wrongConfiguredItemsMessage!=null)
			message +=wrongConfiguredItemsMessage;
		return message;
	}

	private String buildNotificationEmailSubject(){
		Date todayNow = Calendar.getInstance().getTime();
		DateFormat basicDatePlusTimeFormatEST = new SimpleDateFormat("MM/dd/yyyy - KK:mm aa");		
		basicDatePlusTimeFormatEST.setTimeZone(TimeZone.getTimeZone("GMT-5"));			
		String mailSubject =GlobalPropertiesProvider.getGlobalProperties().getEnvironmentName()+ " - Houzz inventory upload "+basicDatePlusTimeFormatEST.format(todayNow)+" EST : SKU's sent to Houzz with an Inactive status";
		return mailSubject;
	}	

	@Override  // This is a service method for first time application start for database fill.
	public void updateItemList() {		
		List<LineItemIntegrationIdentifierDAO> lineItemDAOList = persistenceManager.getHouzzAssortment();	
		for (LineItemIntegrationIdentifierDAO lineItemDao:lineItemDAOList){
			if (lineItemDao.getItemInternalId()==null){
				try {
					netsuiteOperationsManager.updateInventoryInternalId(lineItemDao);
					persistenceManager.updateHouzzAssortment(lineItemDAOList);
				} catch (NetsuiteOperationException e) {				
					e.printStackTrace();
				}
			}
		}	
	}	
		
	private HouzzUnavailableInventories extractUnavailableInventories(Collection<InventoryPojo> prepearedInventoryItemPojoList){
			HouzzUnavailableInventories result = new HouzzUnavailableInventories();
			for (InventoryPojo iiPojoBasic:prepearedInventoryItemPojoList){
				if (iiPojoBasic instanceof HouzzInventoryPojo){
					HouzzInventoryPojo iiPojo = (HouzzInventoryPojo)iiPojoBasic;
					if (!iiPojo.isWrongConfigured()){
						if (iiPojo.isInactive()){
							result.getInactiveItemList().add(iiPojo);
						}
						if (iiPojo.getQtyAvailable()<=15){
							result.getLowQuantityItemList().add(iiPojo);
						}
					}	
				}							
			}
			return result;
		}
	
}
