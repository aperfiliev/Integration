package com.malkos.poppin.utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.CustomerShppingAddressPojo;
import com.malkos.poppin.entities.MessageBatchTransfer;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.entities.PurchaseOrderStatus;
import com.malkos.poppin.persistence.PersistenceManager;
import com.netsuite.webservices.platform.common_2014_1.InventoryDetail;
import com.netsuite.webservices.platform.core_2014_1.CustomFieldList;
import com.netsuite.webservices.platform.core_2014_1.CustomFieldRef;
import com.netsuite.webservices.platform.core_2014_1.RecordRef;
import com.netsuite.webservices.platform.core_2014_1.StringCustomFieldRef;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrder;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrderItem;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrderItemList;

public class XmlParserUtil {
	private static Logger logger = LoggerFactory.getLogger(XmlParserUtil.class);
	public static MessageBatchTransfer convertXmlStringToPurchaseOrderPojo(InputStream xml) throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		PurchaseOrderSaxEventHandler handler = new PurchaseOrderSaxEventHandler();
		try {
		
			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			//parse the file and also register this class for call backs
			sp.parse(xml, handler);
		}	
		catch(SAXException | IOException | ParserConfigurationException ex){
			throw ex;
		}
		finally{
			try {
				xml.close();
			} catch (IOException e) {
				logger.warn("Could not close the Purchase Orders input stream provided for parsing.");
			}
		}
		return handler.getMessageBatchTransfer();
	}
	public static List<String> convertPoListToFaXmlMessages(List<PurchaseOrderPojo> poList){
		List<String> outputs = new ArrayList<String>();
		GlobalProperties properties =  GlobalPropertiesProvider.getGlobalProperties();
		
		if(poList.size() > 0){
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
				Document doc = docBuilder.newDocument();
				Element hubFA = doc.createElement("hubFA");
				Element rootElement = doc.createElement("FAMessageBatch");
				String batchNumber = "";
				String previosBatchNumber = "";
				int receivedCount = 0;
				int acceptedCount = 0;
				int rejectedCount = 0;
				for (PurchaseOrderPojo po : poList) {
					String todayNow = new SimpleDateFormat(GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
					batchNumber = po.getOrderMessageBatch();
					if ((batchNumber != previosBatchNumber) && (previosBatchNumber != "")){
						// messageBatchDisposition elements
						Element messageBatchDisposition = doc
								.createElement("messageBatchDisposition");
						if ((acceptedCount == receivedCount)
								&& (acceptedCount != 0)) {
							messageBatchDisposition.setAttribute("type", "A");
						} else if (rejectedCount == receivedCount) {
							messageBatchDisposition.setAttribute("type", "R");
						} else {
							messageBatchDisposition.setAttribute("type", "P");
						}
						Element trxReceivedCount = doc
								.createElement("trxReceivedCount");
						trxReceivedCount.appendChild(doc.createTextNode(Integer
								.toString(receivedCount)));
						messageBatchDisposition.appendChild(trxReceivedCount);
						Element trxAcceptedCount = doc
								.createElement("trxAcceptedCount");
						trxAcceptedCount.appendChild(doc.createTextNode(Integer
								.toString(acceptedCount)));
						messageBatchDisposition.appendChild(trxAcceptedCount);
						hubFA.appendChild(messageBatchDisposition);
	
						// messageCount elements
						Element messageCount = doc.createElement("messageCount");
						messageCount.appendChild(doc.createTextNode("1"));
						rootElement.appendChild(messageCount);
	
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory
								.newInstance();
						Transformer transformer = transformerFactory
								.newTransformer();
						// create file
						DOMSource source = new DOMSource(doc);
						
						/*StreamResult result = new StreamResult(new File(
								"resources/xml/fa_" + po.getOrderMessageBatch()
										+ Integer.toString(outputs.size()) +".xml"));*/
						StreamResult result = new StreamResult(new File(
								properties.getFuncAckDecryptedPath() + GlobalProperties.STAPLES_FA_MESSAGE_PREFIX +  todayNow + "_" + Integer.toString(outputs.size()) +".xml"));
						transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.transform(source, result);
	
						/*transformer.setOutputProperty(
								OutputKeys.OMIT_XML_DECLARATION, "yes");*/
						StringWriter writer = new StringWriter();
						transformer.transform(new DOMSource(doc), new StreamResult(
								writer));
						String output = "";
						output = writer.getBuffer().toString();
						outputs.add(output);
						
						acceptedCount = 0;
						rejectedCount = 0;
						receivedCount = 0;
					}
					if (batchNumber != previosBatchNumber) {
						previosBatchNumber = batchNumber;
						// root elements
						doc = docBuilder.newDocument();
						rootElement = doc.createElement("FAMessageBatch");
						rootElement.setAttribute("batchNumber", batchNumber);
						doc.appendChild(rootElement);
	
						// partnerID elements
						Element partnerID = doc.createElement("partnerID");
						partnerID.appendChild(doc.createTextNode(po
								.getPartnerIDName()));
						rootElement.appendChild(partnerID);
	
						// hubFA elements
						hubFA = doc.createElement("hubFA");
						rootElement.appendChild(hubFA);
	
						// messageBatchLink AND trxSetID
						Element messageBatchLink = doc
								.createElement("messageBatchLink");
						Element trxSetID = doc.createElement("trxSetID");
						trxSetID.appendChild(doc.createTextNode(batchNumber));
						messageBatchLink.appendChild(trxSetID);
						hubFA.appendChild(messageBatchLink);
					}
					
					receivedCount ++;
					// messageAck elements
					Element messageAck = doc.createElement("messageAck");
					messageAck.setAttribute("type", "order");
					Element trxID = doc.createElement("trxID");
					trxID.appendChild(doc.createTextNode(Integer.toString(po.getTransactionID())));
					messageAck.appendChild(trxID);
					Element messageDisposition = doc
							.createElement("messageDisposition");
					if (po.getStatus() == PurchaseOrderStatus.POPPIN_PENDING_CONFIRMATION){
						messageDisposition.setAttribute("status", "A");
						acceptedCount++;
					}else{
						messageDisposition.setAttribute("status", "R");
						rejectedCount++;
					}				
					messageAck.appendChild(messageDisposition);
	
					// detailException
					if (po.getStatus() == PurchaseOrderStatus.POPPIN_REJECTED || po.getStatus() == PurchaseOrderStatus.UNPROCESSIBLE_REJECTED || !po.getExceptionDesc().isEmpty()) {
						Element detailException = doc
								.createElement("detailException");
	
						//Element detailID = doc.createElement("detailID");
						//detailID.appendChild(doc.createTextNode(po.getDetailId()));
						//detailException.appendChild(detailID);
	
						Element exceptionDesc = doc.createElement("exceptionDesc");
						exceptionDesc.appendChild(doc.createTextNode(po
								.getExceptionDesc()));
						detailException.appendChild(exceptionDesc);
	
						messageAck.appendChild(detailException);
					}
					hubFA.appendChild(messageAck);
				}
				
				// messageBatchDisposition elements
				Element messageBatchDisposition = doc
						.createElement("messageBatchDisposition");
				if ((acceptedCount == receivedCount) && (acceptedCount != 0)) {
					messageBatchDisposition.setAttribute("type", "A");
				} else if (rejectedCount == receivedCount) {
					messageBatchDisposition.setAttribute("type", "R");
				} else {
					messageBatchDisposition.setAttribute("type", "P");
				}
				Element trxReceivedCount = doc.createElement("trxReceivedCount");
				trxReceivedCount.appendChild(doc.createTextNode(Integer
						.toString(receivedCount)));
				messageBatchDisposition.appendChild(trxReceivedCount);
				Element trxAcceptedCount = doc.createElement("trxAcceptedCount");
				trxAcceptedCount.appendChild(doc.createTextNode(Integer
						.toString(acceptedCount)));
				messageBatchDisposition.appendChild(trxAcceptedCount);
				hubFA.appendChild(messageBatchDisposition);
	
				// messageCount elements
				Element messageCount = doc.createElement("messageCount");
				messageCount.appendChild(doc.createTextNode("1"));
				rootElement.appendChild(messageCount);
	
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				//create file
				DOMSource source = new DOMSource(doc);
				String todayNow = new SimpleDateFormat(GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
				StreamResult result = new StreamResult(new
				//File("resources/xml/fa_"+batchNumber+ Integer.toString(outputs.size()) +".xml"));
				File(properties.getFuncAckDecryptedPath() + GlobalProperties.STAPLES_FA_MESSAGE_PREFIX + todayNow + "_" + Integer.toString(outputs.size()) +".xml"));
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source, result);
	
				/*transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"yes");*/
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(doc), new StreamResult(writer));
				String output = writer.getBuffer().toString();
				outputs.add(output);
				logger.info("FA message has been created properly.");
				// logger.info(output);
			} /*catch (ParserConfigurationException pce) {
				String errorMessage = "FA message hasn`t created properly.";
				logger.info(errorMessage);
				ErrorsCollector.addErrorMessage(errorMessage);
			} catch (TransformerException tfe) {
				String errorMessage = "FA message hasn`t created properly.";
				logger.info(errorMessage);
				ErrorsCollector.addErrorMessage(errorMessage);
			}*/
			catch (Exception ex) {
				String errorMessage = "Could not create FA message Reason : " + ex.getMessage();
				logger.info(errorMessage);
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			}
		}
		return outputs;
	}
	public static String convertSalesOrderListToConfirmationMessage(List<SalesOrder> soList) throws ParserConfigurationException, TransformerException{
		
		String output = "";
		GlobalProperties properties =  GlobalPropertiesProvider.getGlobalProperties();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();

		Element confirmMessageBatch = doc
				.createElement("ConfirmMessageBatch");
		confirmMessageBatch.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		// todo
		confirmMessageBatch
				.setAttribute("xsi:noNamespaceSchemaLocation",
						"C:/DOCUME~1/cwojcicki/Desktop/NEWDOC~1/XML/Staples/Staples_Confirmation.xsd");
		doc.appendChild(confirmMessageBatch);

		Element partnerID = doc.createElement("partnerID");
		partnerID.setAttribute("name", "Poppin");
		partnerID.setAttribute("roleType", "vendor");
		partnerID.appendChild(doc.createTextNode("poppininc"));
		confirmMessageBatch.appendChild(partnerID);

		Integer msgCount = 0;
		for (SalesOrder so : soList) {
			String partnerTrxIDStr = "";
			so.getLinkedTrackingNumbers();
			CustomFieldList customList1 = so.getCustomFieldList();
			CustomFieldRef[] ref1 = customList1.getCustomField();
			StringCustomFieldRef stringCustomRef1 = null;
			for(CustomFieldRef cRef : ref1){
				if(cRef.getClass() == StringCustomFieldRef.class){
					stringCustomRef1 = (StringCustomFieldRef)cRef;
					if(stringCustomRef1.getScriptId().equalsIgnoreCase("custbody14"))
						partnerTrxIDStr = stringCustomRef1.getValue();
					break;
				}
			}
			if (so.getTranId() != null)
				partnerTrxIDStr = so.getTranId();

			String partnerTrxDateStr = "";
			if (so.getTranDate() != null){
				String strdate = null;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

				//if (so.getTranDate() != null) {
					strdate = sdf.format(so.getTranDate().getTime());
				//} 
				partnerTrxDateStr = strdate;
			}

			String poNumberStr = "";
			if (so.getOtherRefNum() != null){
				poNumberStr = so.getOtherRefNum();
			}
			msgCount++;
			Element hubConfirm = doc.createElement("hubConfirm");

			Element participatingParty = doc
					.createElement("participatingParty");
			participatingParty.setAttribute("name", "Staples");
			participatingParty.setAttribute("participationCode", "To:");
			participatingParty.setAttribute("roleType", "merchant");
			participatingParty.appendChild(doc.createTextNode("staples"));
			hubConfirm.appendChild(participatingParty);

			Element partnerTrxID = doc.createElement("partnerTrxID");
			partnerTrxID.appendChild(doc.createTextNode(partnerTrxIDStr));
			hubConfirm.appendChild(partnerTrxID);

			Element partnerTrxDate = doc.createElement("partnerTrxDate");
			partnerTrxDate.appendChild(doc
					.createTextNode(partnerTrxDateStr));
			hubConfirm.appendChild(partnerTrxDate);

			Element poNumber = doc.createElement("poNumber");
			poNumber.appendChild(doc.createTextNode(poNumberStr));
			hubConfirm.appendChild(poNumber);

			SalesOrderItemList itemList = new SalesOrderItemList();
			itemList = so.getItemList();
			SalesOrderItem[] items = itemList.getItem();
			Integer itemsNumber = 1;
			for (SalesOrderItem actionItem : items) {

				int trxQtyStr = 0;
				if (actionItem.getQuantity().toString() != null)
					trxQtyStr = (int) Double.parseDouble(actionItem.getQuantity().toString());

				String merchantLineNumberStr = " ";
				CustomFieldList customList = actionItem.getCustomFieldList();
				CustomFieldRef[] ref = customList.getCustomField();
				StringCustomFieldRef stringCustomRef = null;
				for(CustomFieldRef cRef : ref){
					if(cRef.getClass() == StringCustomFieldRef.class){
						stringCustomRef = (StringCustomFieldRef)cRef;
						if(stringCustomRef.getScriptId().equalsIgnoreCase("custcol11"))
							merchantLineNumberStr = stringCustomRef.getValue();
						break;
					}
				}

				String trackingNumberStr = "";
				if (so.getLinkedTrackingNumbers() != null){
					trackingNumberStr = so.getLinkedTrackingNumbers();
				}
		

				String shipDateStr = "";
				if (so.getActualShipDate().toString() != null){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					shipDateStr = sdf.format(so.getActualShipDate().getTime());
				}
				
				Element hubAction = doc.createElement("hubAction");

				Element action = doc.createElement("action");
				action.appendChild(doc.createTextNode("v_ship"));
				hubAction.appendChild(action);

				Element merchantLineNumber = doc.createElement("merchantLineNumber");
				merchantLineNumber.appendChild(doc.createTextNode(merchantLineNumberStr));
				hubAction.appendChild(merchantLineNumber);

				Element trxQty = doc.createElement("trxQty");
				trxQty.appendChild(doc.createTextNode(Integer.toString(trxQtyStr)));
				hubAction.appendChild(trxQty);


				String packageDetailID = "P_00"+msgCount.toString()+itemsNumber.toString();
				Element packageDetailLink = doc
						.createElement("packageDetailLink");
				packageDetailLink.setAttribute("packageDetailID",
						packageDetailID);
				hubAction.appendChild(packageDetailLink);

				hubConfirm.appendChild(hubAction);

				/* packageDetail */
				Element packageDetail = doc.createElement("packageDetail");
				packageDetail.setAttribute("packageDetailID",
						packageDetailID);

				Element shipDate = doc.createElement("shipDate");
				shipDate.appendChild(doc.createTextNode(shipDateStr));
				packageDetail.appendChild(shipDate);

				Element serviceLevel1 = doc.createElement("serviceLevel1");
				serviceLevel1.appendChild(doc.createTextNode("UPSN_CG"));
				packageDetail.appendChild(serviceLevel1);

				Element trackingNumber = doc
						.createElement("trackingNumber");
				trackingNumber.appendChild(doc
						.createTextNode(trackingNumberStr));
				packageDetail.appendChild(trackingNumber);
				hubConfirm.appendChild(packageDetail);
				itemsNumber++;
			}

			confirmMessageBatch.appendChild(hubConfirm);

		}

		Element messageCount = doc.createElement("messageCount");
		messageCount.appendChild(doc.createTextNode(Integer
				.toString(msgCount)));
		confirmMessageBatch.appendChild(messageCount);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// create file
		DOMSource source = new DOMSource(doc);


		String todayNow = new SimpleDateFormat(GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
		StreamResult result = new StreamResult(new File(
				properties.getConfirmationDecryptedPath() + GlobalProperties.STAPLES_CONFIRM_MESSAGE_PREFIX +  todayNow + "_" + ".xml"));
		
		/*transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				"yes");*/
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		output = writer.getBuffer().toString();

		logger.info("Confirmation message has been created properly.");
		
		return output;
	}
	public static String convertSalesOrderListToOrderInvoicesMessage(List<SalesOrder> soList, Map<String, String> itemInternalIdToItemNumberMap, Map<String, CustomerShppingAddressPojo> salesOrderIdToShippingAddressMap ) throws ParserConfigurationException, TransformerException{
		String output = "";
		
		GlobalProperties properties =  GlobalPropertiesProvider.getGlobalProperties();
		PersistenceManager manager = new PersistenceManager();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();

		Element invoiceMessageBatch = doc.createElement("InvoiceMessageBatch");
		invoiceMessageBatch.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		
		invoiceMessageBatch.setAttribute("xsi:noNamespaceSchemaLocation", "C:/DOCUME~1/cwojcicki/Desktop/NEWDOC~1/XML/Staples/Staples_Invoice.xsd");
		doc.appendChild(invoiceMessageBatch);

		Element partnerID = doc.createElement("partnerID");
		partnerID.setAttribute("name", "Poppin");
		partnerID.setAttribute("roleType", "vendor");
		partnerID.appendChild(doc.createTextNode("poppininc"));
		invoiceMessageBatch.appendChild(partnerID);
		
		Integer msgCount = 0;
		for (SalesOrder so : soList) {
			msgCount++;
			//<hubInvoice>
			Element hubInvoice = doc.createElement("hubInvoice");
			String shipmentDetailID = "SD_00" + msgCount;
			String packageDetailID = "P_00" + msgCount;
			String shipTopersonPlaceID = "ST_00" + msgCount;
			String trackingNumberStr = " ";
			
			String partnerPersonPlaceIdstr = " ";
			
			hubInvoice.setAttribute("shipmentDetailID", shipmentDetailID);
			//<participatingParty> 
			Element participatingParty = doc.createElement("participatingParty");
			participatingParty.setAttribute("name", "Staples");
			participatingParty.setAttribute("participationCode", "To:");
			participatingParty.setAttribute("roleType", "merchant");
			participatingParty.appendChild(doc.createTextNode("staples"));
			hubInvoice.appendChild(participatingParty);
			
			String partnerTrxDateStr =  "";
			String trxDueDateStr = " ";
			double balanceDue = 0;
			int orderTotalQunatity = 0;
			String shipDateStr = " ";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			if (so.getTranDate() != null){
				String strdate = null;

				Calendar dueDate = (Calendar) so.getTranDate().clone();
				dueDate.add(Calendar.DAY_OF_MONTH, 30);
				
				strdate = sdf.format(so.getTranDate().getTime());
				partnerTrxDateStr = strdate;
				
				trxDueDateStr = sdf.format(dueDate.getTime());
			}
			if(so.getActualShipDate() != null){
				shipDateStr = sdf.format(so.getActualShipDate().getTime());
			}
			else{
				shipDateStr = sdf.format(so.getShipDate().getTime());
			}

			String poNumberStr = "";
			if (so.getOtherRefNum() != null){
				poNumberStr = so.getOtherRefNum();
			}
			
			Element partnerTrxID = doc.createElement("partnerTrxID");
			partnerTrxID.appendChild(doc.createTextNode(so.getTranId()));
			hubInvoice.appendChild(partnerTrxID);

			Element partnerTrxDate = doc.createElement("partnerTrxDate");
			partnerTrxDate.appendChild(doc.createTextNode(partnerTrxDateStr));
			hubInvoice.appendChild(partnerTrxDate);
			
			Element poNumber = doc.createElement("poNumber");
			poNumber.appendChild(doc.createTextNode(poNumberStr));
			hubInvoice.appendChild(poNumber);
						
			Element trxDueDate = doc.createElement("trxDueDate");
			trxDueDate.appendChild(doc.createTextNode(trxDueDateStr));
			hubInvoice.appendChild(trxDueDate);

			Element trxBalanceDue = doc.createElement("trxBalanceDue");
			hubInvoice.appendChild(trxBalanceDue);
			Calendar netDueDate = Calendar.getInstance();
			netDueDate.add(Calendar.DAY_OF_MONTH, 90);
			String netDueDateFormatted = sdf.format(netDueDate.getTime());
			
			Element trxData = doc.createElement("trxData");
			Element discountBreakout = doc.createElement("discountBreakout");
			discountBreakout.setAttribute("discTypeCode", "14");
			discountBreakout.setAttribute("discDateCode", "Z");
			discountBreakout.setAttribute("discPercent", "0");
			discountBreakout.setAttribute("discDueDate", netDueDateFormatted);
			discountBreakout.setAttribute("discDaysDue", "90");
			discountBreakout.setAttribute("netDueDate", netDueDateFormatted);
			
			trxData.appendChild(discountBreakout);
			hubInvoice.appendChild(trxData);
			
			
			
			SalesOrderItemList soItemList = so.getItemList();
			SalesOrderItem[] soItems = soItemList.getItem();
			
			CustomFieldList soCustomList = so.getCustomFieldList();
			CustomFieldRef[] customFieldRef = soCustomList.getCustomField();
			StringCustomFieldRef stringCustomFieldRef = null;
			for(CustomFieldRef cRef : customFieldRef){
				if(null!= cRef && cRef.getClass() == StringCustomFieldRef.class){
					stringCustomFieldRef = (StringCustomFieldRef)cRef;
					String customFieldInternalId = stringCustomFieldRef.getScriptId();
					if(customFieldInternalId.equalsIgnoreCase("custbodypartner_person_place_id")){
						partnerPersonPlaceIdstr = stringCustomFieldRef.getValue();
						break;
					}
				}
			}
			
			for(SalesOrderItem soItem : soItems){
				int trxQtyStr = 0;
				
				String merchantSKUstr = " ";
				String merchantLineNumberStr = " ";
				
				CustomFieldList customList = soItem.getCustomFieldList();
				CustomFieldRef[] ref = customList.getCustomField();
				StringCustomFieldRef stringCustomRef = null;
				for(CustomFieldRef cRef : ref){
					if(null != cRef && cRef.getClass() == StringCustomFieldRef.class){
						stringCustomRef = (StringCustomFieldRef)cRef;
						String customFieldInternalId = stringCustomRef.getScriptId();
						if(customFieldInternalId.equalsIgnoreCase("custcol11"))
							merchantLineNumberStr = stringCustomRef.getValue();
						else if(customFieldInternalId.equalsIgnoreCase("custcolmerchant_sku"))
							merchantSKUstr = stringCustomRef.getValue();
					}
				}
				String itemNumber = itemInternalIdToItemNumberMap.get(soItem.getItem().getInternalId());
				String vendorSKUstr = "";
				String modelNum = manager.getModelNumByItemInternalId(itemNumber);
				if(null != modelNum)
					vendorSKUstr = modelNum;
				
				double quantity = soItem.getQuantity();
				orderTotalQunatity += quantity;
				trxQtyStr = (int) quantity;
				double rate = Double.parseDouble(soItem.getRate());
				balanceDue += rate * quantity;
				
				Element hubAction = doc.createElement("hubAction");

				Element action = doc.createElement("action");
				action.appendChild(doc.createTextNode("v_invoice"));
				hubAction.appendChild(action);

				Element merchantLineNumber = doc.createElement("merchantLineNumber");
				merchantLineNumber.appendChild(doc.createTextNode(merchantLineNumberStr));
				hubAction.appendChild(merchantLineNumber);
				
				Element trxVendorSKU = doc.createElement("trxVendorSKU");
				trxVendorSKU.appendChild(doc.createTextNode(vendorSKUstr));
				hubAction.appendChild(trxVendorSKU);
				
				Element trxMerchantSKU = doc.createElement("trxMerchantSKU");
				trxMerchantSKU.appendChild(doc.createTextNode(merchantSKUstr));
				hubAction.appendChild(trxMerchantSKU);
				
				Element trxQty = doc.createElement("trxQty");
				trxQty.appendChild(doc.createTextNode(Integer.toString(trxQtyStr)));
				hubAction.appendChild(trxQty);
				
				Element trxUnitCost = doc.createElement("trxUnitCost");
				trxUnitCost.appendChild(doc.createTextNode(Double.toString(rate)));
				hubAction.appendChild(trxUnitCost);
				
				Element packageDetailLink = doc.createElement("packageDetailLink");
				packageDetailLink.setAttribute("packageDetailID", "P_00" + msgCount);
				hubAction.appendChild(packageDetailLink);
				
				hubInvoice.appendChild(hubAction);
			}
			trxBalanceDue.appendChild(doc.createTextNode(String.format("%.2f", balanceDue)));
			invoiceMessageBatch.appendChild(hubInvoice);
			
			Element shipmentDetail = doc.createElement("shipmentDetail");
			shipmentDetail.setAttribute("shipmentDetailID", "SD_00" + msgCount);
			
			Element shipmentDetailShipDate = doc.createElement("shipDate");
			shipmentDetailShipDate.appendChild(doc.createTextNode(shipDateStr));
			shipmentDetail.appendChild(shipmentDetailShipDate);
			
			Element shipTo = doc.createElement("shipTo");
			shipTo.setAttribute("personPlaceID", "ST_00" + msgCount);
			shipmentDetail.appendChild(shipTo);
			
			hubInvoice.appendChild(shipmentDetail);
			
			Element packageDetail = doc.createElement("packageDetail");
			packageDetail.setAttribute("packageDetailID", "P_00" + msgCount);
			
			Element pkgQty = doc.createElement("pkgQty");
			pkgQty.appendChild(doc.createTextNode(Integer.toString(orderTotalQunatity)));
			packageDetail.appendChild(pkgQty);
			
			Element packageDetailShipDate = doc.createElement("shipDate");
			packageDetailShipDate.appendChild(doc.createTextNode(shipDateStr));
			packageDetail.appendChild(packageDetailShipDate);
			
			Element serviceLevel1 = doc.createElement("serviceLevel1");
			serviceLevel1.appendChild(doc.createTextNode("UPSN_CG"));
			packageDetail.appendChild(serviceLevel1);
			
		
			Element trackingNumber = doc.createElement("trackingNumber");
			if(null != so.getLinkedTrackingNumbers()){
				trackingNumberStr = so.getLinkedTrackingNumbers();
			}
			trackingNumber.appendChild(doc.createTextNode(trackingNumberStr));
			packageDetail.appendChild(trackingNumber);
			
			hubInvoice.appendChild(packageDetail);
			
			CustomerShppingAddressPojo csaPojo = salesOrderIdToShippingAddressMap.get(so.getInternalId());
			
			Element personPlace = doc.createElement("personPlace");
			personPlace.setAttribute("personPlaceID", "ST_00" + msgCount);
			
			Element name1 = doc.createElement("name1");
			name1.appendChild(doc.createTextNode(csaPojo.getName1()));
			personPlace.appendChild(name1);
			
			Element address1 = doc.createElement("address1");
			address1.appendChild(doc.createTextNode(csaPojo.getAddress1()));
			personPlace.appendChild(address1);
			
			Element city = doc.createElement("city");
			city.appendChild(doc.createTextNode(csaPojo.getCity()));
			personPlace.appendChild(city);
			
			Element state = doc.createElement("state");
			state.appendChild(doc.createTextNode(csaPojo.getState()));
			personPlace.appendChild(state);
			
			Element postalCode = doc.createElement("postalCode");
			postalCode.appendChild(doc.createTextNode(csaPojo.getPostalCode()));
			personPlace.appendChild(postalCode);
			
			Element partnerPersonPlaceId = doc.createElement("partnerPersonPlaceId");
			partnerPersonPlaceId.appendChild(doc.createTextNode(partnerPersonPlaceIdstr));
			personPlace.appendChild(partnerPersonPlaceId);
			
			hubInvoice.appendChild(personPlace);
			 
			orderTotalQunatity = 0;
		}
		Element messageCount = doc.createElement("messageCount");
		messageCount.appendChild(doc.createTextNode(Integer.toString(msgCount)));
		invoiceMessageBatch.appendChild(messageCount);
		
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// create file
		DOMSource source = new DOMSource(doc);


		String todayNow = new SimpleDateFormat(GlobalProperties.SPECIAL_FILE_NAME_DATE_FORMAT).format(new Date());
		StreamResult result = new StreamResult(new File(
				properties.getInvoiceDecryptedPath() + GlobalProperties.STAPLES_INVOICE_MESSAGE_PREFIX +  todayNow + "_" + ".xml"));
		
		/*transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				"yes");*/
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		output = writer.getBuffer().toString();

		logger.info("Order Invoices message has been created properly.");

		return output;
	}
}
