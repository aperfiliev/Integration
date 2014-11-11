package com.malkos.poppin.integration.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.InventoryPojo;
import com.malkos.poppin.entities.MessageType;
import com.malkos.poppin.entities.OutgoingMessageStatus;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;
import com.malkos.poppin.transport.INetsuiteOperationsManager;
import com.malkos.poppin.transport.NetsuiteNullResponseException;
import com.malkos.poppin.utils.ErrorMessageWrapper;
import com.malkos.poppin.utils.ErrorsCollector;

public class InventoryUpdateFlowService implements IInventoryUpdateFlowService {
	
	private INetsuiteOperationsManager netsuiteOperationsManager;
	public static final Logger logger = LoggerFactory.getLogger(InventoryUpdateFlowService.class);
	@Autowired
	IPersistenceManager persistanceManager;	
	
	//private EncryptionManager encManager; 
	

	@Override
	public void processTask() {
		String invMessage = retrieveInventoryFromPoppin();
		if (!invMessage.isEmpty()){
			persistanceManager.persistOutgoingMessages(saveInventory(invMessage));
		}
	}

	private String retrieveInventoryFromPoppin() {
		logger.info("Poppin Client starts retrieving Inventory.");
		GlobalProperties properties = GlobalPropertiesProvider
				.getGlobalProperties();
		String todayNow = new SimpleDateFormat(
				GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT)
				.format(new Date());
		String output = "";
		
		//THIS IS SERVICE PART OF CODE DO NOT UNCOMMENT IT IF NOT REQUIRED!!!		
		//List<VendorSkuToModelNumMapDAO> inventoryDAOs =  persitenceManager.getInventoryDAO();
		//netsuiteManager.retrieveInventoryInternalIdFromPoppin(inventoryDAOs);
		//persitenceManager.updateInventoryDAO(inventoryDAOs);		
		//THIS IS SERVICE PART OF CODE DO NOT UNCOMMENT IT IF NOT REQUIRED!!!
		
		Collection<InventoryPojo> records = new ArrayList<InventoryPojo>();
		logger.info("Retrieving vendorSKUs and ModelNum values for items that Staplse merchand sales.");
		//Map<String,String> VendorSkuToModelNumMap = persitenceManager.getVendorSkuToModelNumMap();
		List<VendorSkuToModelNumMapDAO> inventoryDAOs =  persistanceManager.getInventoryDAO();
		logger.info("Retrieving inventory items from poppin. It might take some time. Please wait...");
		try {
			records = getNetsuiteOperationsManager().getInventoryFromPoppinUpdated(inventoryDAOs);
		} /*catch (NetsuiteNullResponseException e) {
			String errorMessage = "Failed to retrieve inventory items from poppin. Reason : " + e.getMessage();
			logger.warn(errorMessage);
			//ErrorsCollector.addCommonErrorMessage(errorMessage);
			ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
		}*/
		catch (Exception ex){
			String errorMessage = "Failed to retrieve inventory items from poppin. Reason : " + ex.getMessage();
			logger.warn(errorMessage);
			//ErrorsCollector.addCommonErrorMessage(errorMessage);
			CHIntegrationError wrappedError =  ErrorMessageWrapper.wrapCommonError(ex.getMessage());
			ErrorsCollector.addCommonErrorMessage(wrappedError);
		}
		
		String messagePath=null;
		
		if (!records.isEmpty()) {
			try {

				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				Document doc = docBuilder.newDocument();

				Element adviceFile = doc.createElement("advice_file");
				doc.appendChild(adviceFile);

				Element adviceFileCN = doc
						.createElement("advice_file_control_number");
				// todo
				adviceFileCN.appendChild(doc
						.createTextNode(todayNow));
				adviceFile.appendChild(adviceFileCN);

				Element vendorMerchantId = doc.createElement("vendorMerchID");
				vendorMerchantId.appendChild(doc.createTextNode("staples"));
				adviceFile.appendChild(vendorMerchantId);

				Integer productCount = 0;
				Double multiplier = Double.valueOf(properties.getMultiplierToInvetoryAmount());
				Double multiplierMoreThen100 = Double.valueOf(properties.getMultiplierToInvetoryAmountMoreThen100());
				
				Double inventoryThreshold = Double.valueOf(properties.getInventoryThreshold());
				for (InventoryPojo item : records) {
					productCount++;
					
					Element product = doc.createElement("product");
					// todo vendor_SKU
					Element vendorSKU = doc.createElement("vendor_SKU");
					vendorSKU.appendChild(doc.createTextNode(item.getVendorSKU()));
					product.appendChild(vendorSKU);

					// qtyonhand
					Double qty = 0.0;
					Element qtyOnHand = doc.createElement("qtyonhand");
					try {
						qty = Double.valueOf(item.getQtyAvailable());
						if (qty > 100 ){
							qty = qty * multiplierMoreThen100;
						}
						else {
							if(qty < inventoryThreshold){
								qty = 0.0; 
							}
							else{
								qty = qty * multiplier;
							}
							//qty = qty * multiplier;
						}
						//if (qty < inventoryThreshold) qty = 0.0 ;
						qty = Math.floor(qty);
					} catch (Exception e) {;
						//logger.info("getQuantityOnHand - " + Double.toString(qty));
					}
					qtyOnHand.appendChild(doc.createTextNode(Integer.toString(qty.intValue())));
					product.appendChild(qtyOnHand);

					// available
					try {
						String isAvailabeValue = (qty != 0) ? "Yes"
								: "No";
						Element isAvailable = doc.createElement("available");
						isAvailable.appendChild(doc
								.createTextNode(isAvailabeValue));
						product.appendChild(isAvailable);
					} catch (Exception e) {
						//logger.info("isAvailable - error");
					}

					try {
						Element upc = doc.createElement("UPC");
						if (item.getUPC() != null) upc.appendChild(doc.createTextNode(item.getUPC()));
						product.appendChild(upc);
					} catch (Exception e) {
						//logger.info("getUpcCode - " + item.getUpcCode());
					}
					
					// todo description
					Element description = doc.createElement("description");
					String descrptionStr = " ";
					if (item.getDescription() != null)
						descrptionStr = item.getDescription();
					description.appendChild(doc.createTextNode(descrptionStr));
					
					product.appendChild(description);
					/*
					// todo next_available_date
					Element nextAvailableDate = doc
							.createElement("next_available_date");
					nextAvailableDate.appendChild(doc.createTextNode(""));
					product.appendChild(nextAvailableDate);

					// next_available_qty
					Element nextAvailableQty = doc.createElement("next_available_qty");
					nextAvailableQty.appendChild(doc.createTextNode(""));
					product.appendChild(nextAvailableQty);

					// todo discontinued_date
					Element partnerTrxID = doc
							.createElement("discontinued_date");
					partnerTrxID.appendChild(doc.createTextNode(""));
					product.appendChild(partnerTrxID);
					*/

					adviceFile.appendChild(product);

				}
				
				Element adviceFileCount = doc
						.createElement("advice_file_count");
				adviceFileCount.appendChild(doc.createTextNode(Integer
						.toString(productCount)));
				adviceFile.appendChild(adviceFileCount);
				
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				// create file
				DOMSource source = new DOMSource(doc);

				messagePath = properties.getInventoryDecryptedPath()
						+ GlobalProperties.STAPLES_INVENTORY_MESSAGE_PREFIX
						+ todayNow + ".xml";
				StreamResult result = new StreamResult(
						new File(messagePath));

				/*
				 * transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				 * "yes");
				 */
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source, result);
				
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(doc), new StreamResult(
						writer));							
				logger.info("Inventory message has created properly.");

			} catch (ParserConfigurationException pce) {
				String errorMessage = "Inventory message hasn`t created properly. Reason: "
						+ pce.getMessage();
				logger.info(errorMessage);
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));

			} catch (TransformerException tfe) {
				String errorMessage = "Inventory message hasn`t created properly. Reason: "
						+ tfe.getMessage();
				logger.info(errorMessage);
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			}
		}
		return messagePath;
	}
	
	public List<OutgoingMessageDAO> saveInventory(String path) {
		logger.info("Saving inventory messages.");
		List<OutgoingMessageDAO> messageList = new ArrayList<>();
		OutgoingMessageDAO omDAo = new OutgoingMessageDAO();
		omDAo.setMessagePath(path);
		omDAo.setMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
		omDAo.setMessageType(MessageType.INVENTORY_UPDATE);	
		return messageList;
	}

	public INetsuiteOperationsManager getNetsuiteOperationsManager() {
		return netsuiteOperationsManager;
	}

	public void setNetsuiteOperationsManager(INetsuiteOperationsManager netsuiteOperationsManager) {
		this.netsuiteOperationsManager = netsuiteOperationsManager;
	}
}
