package com.malkos.poppin.transport;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javassist.expr.Instanceof;

import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.pattern.FullLocationPatternConverter;
import org.hibernate.cfg.SetSimpleValueTypeSecondPass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import com.erpguru.netsuite.NetSuiteClient;
import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.CustomerShppingAddressPojo;
import com.malkos.poppin.entities.InventoryPojo;
import com.malkos.poppin.entities.NSRrequestDetails;
import com.malkos.poppin.entities.OrderItemPojo;
import com.malkos.poppin.entities.PurchaseOrderPojo;
import com.malkos.poppin.entities.SoapSaverResponse;
import com.malkos.poppin.entities.UnprocessibleOrdersType;
import com.malkos.poppin.persistence.IPersistenceManager;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.persistence.dao.VendorSkuToModelNumMapDAO;
import com.malkos.poppin.utils.ErrorsCollector;
import com.malkos.poppin.utils.SoapMessageType;
import com.malkos.poppin.utils.SoapMessagesSaver;
import com.malkos.poppin.utils.XmlParserUtil;
import com.netsuite.webservices.lists.accounting_2012_1.InventoryItem;
import com.netsuite.webservices.lists.accounting_2012_1.InventoryItemLocations;
import com.netsuite.webservices.lists.accounting_2012_1.ItemMember;
import com.netsuite.webservices.lists.accounting_2012_1.ItemSearch;
import com.netsuite.webservices.lists.accounting_2012_1.ItemSearchAdvanced;
import com.netsuite.webservices.lists.accounting_2012_1.ItemSearchRow;
import com.netsuite.webservices.lists.accounting_2012_1.KitItem;
import com.netsuite.webservices.lists.relationships_2012_1.Customer;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerAddressbook;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerAddressbookList;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerSearch;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerSearchAdvanced;
import com.netsuite.webservices.lists.relationships_2012_1.CustomerSearchRow;
import com.netsuite.webservices.platform.common_2012_1.CustomerSearchBasic;
import com.netsuite.webservices.platform.common_2012_1.CustomerSearchRowBasic;
import com.netsuite.webservices.platform.common_2012_1.ItemSearchBasic;
import com.netsuite.webservices.platform.common_2012_1.ItemSearchRowBasic;
import com.netsuite.webservices.platform.common_2012_1.LocationSearchBasic;
import com.netsuite.webservices.platform.common_2012_1.TransactionSearchBasic;
import com.netsuite.webservices.platform.common_2012_1.TransactionSearchRowBasic;
import com.netsuite.webservices.platform.common_2012_1.types.Country;
import com.netsuite.webservices.platform.core_2012_1.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core_2012_1.CustomFieldList;
import com.netsuite.webservices.platform.core_2012_1.CustomFieldRef;
import com.netsuite.webservices.platform.core_2012_1.Passport;
import com.netsuite.webservices.platform.core_2012_1.Record;
import com.netsuite.webservices.platform.core_2012_1.RecordList;
import com.netsuite.webservices.platform.core_2012_1.RecordRef;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnCustomField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnCustomFieldList;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnDateField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnDoubleField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnSelectField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnStringCustomField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnStringField;
import com.netsuite.webservices.platform.core_2012_1.SearchCustomFieldList;
import com.netsuite.webservices.platform.core_2012_1.SearchEnumMultiSelectField;
import com.netsuite.webservices.platform.core_2012_1.SearchMultiSelectField;
import com.netsuite.webservices.platform.core_2012_1.SearchResult;
import com.netsuite.webservices.platform.core_2012_1.SearchRow;
import com.netsuite.webservices.platform.core_2012_1.SearchRowList;
import com.netsuite.webservices.platform.core_2012_1.SearchStringField;
import com.netsuite.webservices.platform.core_2012_1.SearchTextNumberField;
import com.netsuite.webservices.platform.core_2012_1.Status;
import com.netsuite.webservices.platform.core_2012_1.StatusDetail;
import com.netsuite.webservices.platform.core_2012_1.StringCustomFieldRef;
import com.netsuite.webservices.platform.core_2012_1.types.RecordType;
import com.netsuite.webservices.platform.core_2012_1.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2012_1.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2012_1.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.core_2012_1.types.SearchTextNumberFieldOperator;
import com.netsuite.webservices.platform.core_2012_1.SearchBooleanField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnEnumSelectField;
import com.netsuite.webservices.platform.core_2012_1.SearchColumnTextNumberField;
import com.netsuite.webservices.platform.faults_2012_1.ExceededRecordCountFault;
import com.netsuite.webservices.platform.faults_2012_1.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults_2012_1.ExceededRequestSizeFault;
import com.netsuite.webservices.platform.faults_2012_1.ExceededUsageLimitFault;
import com.netsuite.webservices.platform.faults_2012_1.InvalidCredentialsFault;
import com.netsuite.webservices.platform.faults_2012_1.InvalidSessionFault;
import com.netsuite.webservices.platform.faults_2012_1.UnexpectedErrorFault;
import com.netsuite.webservices.platform.messages_2012_1.Preferences;
import com.netsuite.webservices.platform.messages_2012_1.ReadResponse;
import com.netsuite.webservices.platform.messages_2012_1.SearchPreferences;
import com.netsuite.webservices.platform.messages_2012_1.WriteResponse;
import com.netsuite.webservices.platform_2012_1.NetSuiteBindingStub;
import com.netsuite.webservices.transactions.sales_2012_1.SalesOrder;
import com.netsuite.webservices.transactions.sales_2012_1.SalesOrderItem;
import com.netsuite.webservices.transactions.sales_2012_1.SalesOrderItemList;
import com.netsuite.webservices.transactions.sales_2012_1.TransactionSearch;
import com.netsuite.webservices.transactions.sales_2012_1.TransactionSearchAdvanced;
import com.netsuite.webservices.transactions.sales_2012_1.TransactionSearchRow;
import com.netsuite.webservices.transactions.sales_2012_1.types.SalesOrderOrderStatus;
import com.netsuite.webservices.transactions.sales_2012_1.types.TransactionStatus;

public class NetsuiteOperationsManager extends NetSuiteClient implements INetsuiteOperationsManager {

	private static Logger logger = LoggerFactory.getLogger(NetsuiteOperationsManager.class);
	private Passport passport = null;	
	
	private void prepeareNSBindingStub() {
		if (passport == null){
			this.passport = new Passport();
			this.passport.setAccount(getAccount());
			this.passport.setEmail(getEmail());
			this.passport.setPassword(getPassword());
			this.passport.setRole(new RecordRef(null, getRole() , null, null));
		}
		NetSuiteBindingStub stub =  getNetSuiteStub();
		stub.setMaintainSession(false);
		stub.clearHeaders();		
		
		SOAPHeaderElement searchPrefHeader = new SOAPHeaderElement("urn:messages_2012_1.platform.webservices.netsuite.com", "searchPreferences");
		SearchPreferences searchPrefs = new SearchPreferences();
		searchPrefs.setPageSize(new Integer(500));
		searchPrefs.setBodyFieldsOnly(false);		
		try {
			searchPrefHeader.setObjectValue(searchPrefs);
		} catch (SOAPException e1) {
			System.out.println("Wrong NetSuite Search Preferences configuration");
			logger.error(e1.getMessage());
		}
		stub.setHeader(searchPrefHeader);
		
		
		SOAPHeaderElement passportHeader = new SOAPHeaderElement("urn:messages_2012_1.platform.webservices.netsuite.com", "passport");
		try {
			passportHeader.setObjectValue(passport);
		} catch (SOAPException e) {
			System.out.println("Wrong NetSuite Passport configuration");
			logger.error(e.getMessage());
		}
		stub.setHeader(passportHeader);
	}
	
	private String addCustomer(PurchaseOrderPojo po)
			throws NetsuiteOperationException, NetsuiteNullResponseException {
		String customerId = "";
		GlobalProperties props = GlobalPropertiesProvider.getGlobalProperties();
		Customer cust = new Customer();
		cust.setIsPerson(true);
		//cust.setEmail(po.getCustomerEmail());
		cust.setEmail("staplesonline@poppin.com");
		cust.setCompanyName(po.getCustomerCompanyName());
		if(null != po.getCustomerEmail())
			cust.setComments(po.getCustomerEmail());
		//cust.setEntityId(props.getCompanyId());
		cust.setParent(new RecordRef(null, props.getCompanyInternalId(), null, null));
		
		String delimiter = " ";
		String firstName = null;
		String lastName = null;

		String[] temp = po.getCustomerName1().split(delimiter);
		for(int i = 0; i < temp.length; i++){
			if(!temp[i].toString().isEmpty()){
				if(null == firstName){
					firstName = temp[i];
				}
				if(temp[i] != firstName)
					lastName = temp[i];
			}
		}
		if(null == lastName) lastName = firstName;
		cust.setFirstName(firstName);
		cust.setLastName(lastName);
		//cust.setLastName(temp[1]);
		cust.setPhone(po.getCustomerDayPhone());

		/* set Customer Address */
		List<CustomerAddressbook> listCustomerAddressbook = new ArrayList<CustomerAddressbook>();
		CustomerAddressbookList addressbookList = new CustomerAddressbookList();
		CustomerAddressbook defaultBillingAddress = new CustomerAddressbook();
		CustomerAddressbook defaultShippingAddress = new CustomerAddressbook();
		
		defaultBillingAddress.setAddressee(po.getCustomerName1());
		defaultBillingAddress.setAddr1(po.getCustomerAddress1());
		if (po.getCustomerAddress2() != null)
			defaultBillingAddress.setAddr2(po.getCustomerAddress2());
		if (po.getCustomerAddress3() != null)
			defaultBillingAddress.setAddr3(po.getCustomerAddress3());
		
		defaultBillingAddress.setCity(po.getCustomerCity());
		defaultBillingAddress.setState(po.getCustomerState());
		defaultBillingAddress.setZip(po.getCustomerPostalCode());
		defaultBillingAddress.setPhone(po.getCustomerDayPhone());
		defaultBillingAddress.setCountry(Country._unitedStates);
		defaultBillingAddress.setDefaultBilling(true);
		
		listCustomerAddressbook.add(defaultBillingAddress);
		
		if( 
			(
				po.getCustomerAddress1().equals(po.getShipToAddress1()) && po.getCustomerCity().equals(po.getShipToCity())
				&& po.getCustomerName1().equals(po.getShipToName1())
			) == false
		)
		{
			defaultShippingAddress.setAddressee(po.getShipToName1());
			defaultShippingAddress.setAddr1(po.getShipToAddress1());
			if (po.getShipToAddress2() != null)
				defaultShippingAddress.setAddr2(po.getShipToAddress2());
			if (po.getShipToAddress3() != null)
				defaultShippingAddress.setAddr3(po.getShipToAddress3());
			
			defaultShippingAddress.setCity(po.getShipToCity());
			defaultShippingAddress.setState(po.getShipToState());
			defaultShippingAddress.setZip(po.getShipToPostalCode());
			defaultShippingAddress.setPhone(po.getShipToDayPhone());
			defaultShippingAddress.setCountry(Country._unitedStates);
			defaultShippingAddress.setDefaultShipping(true);
			listCustomerAddressbook.add(defaultShippingAddress);
		}
		CustomerAddressbook[] addressbook = new CustomerAddressbook[listCustomerAddressbook.size()];
		int index = 0;
		for(CustomerAddressbook book : listCustomerAddressbook){
			addressbook[index] = book;
			index++;
		}
		addressbookList.setAddressbook(addressbook);
		cust.setAddressbookList(addressbookList);

		logger.info("Adding customer : name=" + po.getCustomerName1()
				/*+ ", email=" + po.getCustomerEmail()*/ );
		WriteResponse response = null;
		boolean exceptionOccurs = false;
		String errorMessage = null;
		NSRrequestDetails requestDetails = new NSRrequestDetails();	
		requestDetails.setRequestType(SoapMessageType.ADD);
		try {
			prepeareNSBindingStub();
			response = getNetSuiteStub().add(cust);			
		} catch (RemoteException e) {			
			if (e instanceof AxisFault){
				AxisFault fault = (AxisFault)e;
				errorMessage = fault.getFaultReason();
			} else {
				errorMessage = e.getMessage();
			}	
			exceptionOccurs = true;			
		} finally {				
			Call call = getNetSuiteStub()._getCall();
			try {
				logger.info("Generating Request/Response messages.");
				Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
				Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
				SoapMessagesSaver messageSaver = new SoapMessagesSaver();
				SoapSaverResponse soapResp = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.ADD);
				requestDetails.setRequestFilePath(soapResp.getRequestFilePath());
				requestDetails.setResponseFilePath(soapResp.getResponseFilePath());
			} catch (Exception e) {
				logger.info("Could not generate Request/Response messages.");
			}
			if (exceptionOccurs){
				throw  new NetsuiteOperationException(errorMessage,requestDetails);
			}
		}
		
		//WriteResponse response = addWithRetryLogic(cust);			
		if(response == null)
			throw new NetsuiteNullResponseException("Failed to add customer.");
		
		Status status = response.getStatus();
		
		if (status.isIsSuccess()) {
			logger.info("Customer was succesfully added.");
			customerId = ((RecordRef) response.getBaseRef()).getInternalId();
			return customerId;
		} else {			
			StatusDetail[] details = status.getStatusDetail();
			StringBuilder builder = new StringBuilder();
			errorMessage = "Failed to add customer with name : " + po.getCustomerName1() + ". Reason : ";
			builder.append(errorMessage + "\r\n");
			for (StatusDetail detail : details) {
				builder.append(detail.getMessage() + "\r\n");
			}
			logger.warn(errorMessage);
			throw new NetsuiteOperationException(builder.toString(),requestDetails);
		}

	}

	private boolean isSalesOrderExistsForGivenPo(String po) throws NetsuiteNullResponseException,  NetsuiteOperationException{
		TransactionSearchBasic searchBasic = new TransactionSearchBasic();
		SearchTextNumberField seacrhText = new SearchTextNumberField();
		seacrhText.setSearchValue(po);
		seacrhText.setOperator(SearchTextNumberFieldOperator.equalTo);
		searchBasic.setOtherRefNum(seacrhText);

		TransactionSearch transactionSearch = new TransactionSearch();
		transactionSearch.setBasic(searchBasic);
		SearchResult searchResult = null;
		boolean exceptionOccurs = false;
		String errorMessage = null;
		NSRrequestDetails requestDetails = new NSRrequestDetails();	
		requestDetails.setRequestType(SoapMessageType.SEARCH);
		try {
			prepeareNSBindingStub();
			searchResult = getNetSuiteStub().search(transactionSearch);			
		} catch (RemoteException e) {
			errorMessage = null;
			if (e instanceof AxisFault){
				AxisFault fault = (AxisFault)e;
				errorMessage = fault.getFaultReason();
			} else {
				errorMessage = e.getMessage();
			}	
			exceptionOccurs = true;			
		} finally {				
			Call call = getNetSuiteStub()._getCall();
			try {
				logger.info("Generating Request/Response messages.");
				Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
				Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
				SoapMessagesSaver messageSaver = new SoapMessagesSaver();				
				SoapSaverResponse soapResp = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
				requestDetails.setRequestFilePath(soapResp.getRequestFilePath());
				requestDetails.setResponseFilePath(soapResp.getResponseFilePath());
			} catch (Exception e) {
				logger.info("Could not generate Request/Response messages.");
			}
			if (exceptionOccurs){
				throw  new NetsuiteOperationException(errorMessage,requestDetails);
			}
		}
		//SearchResult searchResult = searchWithRetryLogic(transactionSearch);
		if(searchResult == null)
			throw new NetsuiteNullResponseException("Failed to retrieve sales order by PO number.");
		RecordList recordList = searchResult.getRecordList();
		Record[] records = recordList.getRecord();
		if(null == records)
			return false;
		if(records.length == 0)
			return false;
		return true;
	}
	private boolean isSalesOrderExistsForGivenPoAdvanced(String po) throws NetsuiteNullResponseException, NetsuiteOperationException {
		TransactionSearchBasic searchBasic = new TransactionSearchBasic();
		SearchTextNumberField seacrhText = new SearchTextNumberField();
		seacrhText.setSearchValue(po);
		seacrhText.setOperator(SearchTextNumberFieldOperator.equalTo);
		searchBasic.setOtherRefNum(seacrhText);
		
		TransactionSearchAdvanced tsAdvanced = new TransactionSearchAdvanced();
		TransactionSearch transactionSearch = new TransactionSearch();
		transactionSearch.setBasic(searchBasic);
		
		TransactionSearchRow transactionSearchRow = new TransactionSearchRow();
		TransactionSearchRowBasic transactionSearchRowBasic = new TransactionSearchRowBasic();
		transactionSearchRowBasic.setInternalId(new SearchColumnSelectField[] { new SearchColumnSelectField()});
		transactionSearchRow.setBasic(transactionSearchRowBasic);
		
		tsAdvanced.setCriteria(transactionSearch);
		tsAdvanced.setColumns(transactionSearchRow);
		SearchResult searchResult = null;
		boolean exceptionOccurs = false;
		String errorMessage = null;
		NSRrequestDetails requestDetails = new NSRrequestDetails();	
		requestDetails.setRequestType(SoapMessageType.SEARCH);
		try {
			prepeareNSBindingStub();
			searchResult = getNetSuiteStub().search(transactionSearch);			
		} catch (RemoteException e) {
			errorMessage = null;
			if (e instanceof AxisFault){
				AxisFault fault = (AxisFault)e;
				errorMessage = fault.getFaultReason();
			} else {
				errorMessage = e.getMessage();
			}	
			exceptionOccurs = true;			
		} finally {				
			Call call = getNetSuiteStub()._getCall();
			try {
				logger.info("Generating Request/Response messages.");
				Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
				Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
				SoapMessagesSaver messageSaver = new SoapMessagesSaver();				
				SoapSaverResponse soapResp = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
				requestDetails.setRequestFilePath(soapResp.getRequestFilePath());
				requestDetails.setResponseFilePath(soapResp.getResponseFilePath());
			} catch (Exception e) {
				logger.info("Could not generate Request/Response messages.");
			}
			if (exceptionOccurs){
				throw  new NetsuiteOperationException(errorMessage, requestDetails);
			}
		}
		// SearchResult searchResult = search(transactionSearch);
		//SearchResult searchResult = searchWithRetryLogic(tsAdvanced);
		if(searchResult == null)
			throw new NetsuiteNullResponseException("Failed to retrieve sales order by PO number.");
		SearchRowList srList = searchResult.getSearchRowList();
		srList.getSearchRow();
		/*if(null == records)
			return false;
		if(records.length == 0)
			return false;*/
		return true;
	}

	@Override
	public boolean addSalesOrder(PurchaseOrderPojo po, Map<String, String> popNumToInternalIdMap) throws NetsuiteNullResponseException, NetsuiteOperationException {
		
		logger.info("Checking if Sales Order exists for given PO #");
		boolean salesOrderExists = isSalesOrderExistsForGivenPo(po.getPoNumber());
		//boolean salesOrderExists = isSalesOrderExistsForGivenPoAdvanced(po.getPoNumber());

		if (salesOrderExists == false) {
			logger.info("Sales order does not exist.");
			logger.info("Adding Sales order with PO:" + po.getPoNumber());

			String customerId = "";
			Customer customer = null;

			logger.info("Adding new customer for this sales order.");

			/* add new customer*/
			customerId = addCustomer(po);
			customer = getCustomerById(customerId);
			/* ********** add sales order **************** */
			
			SalesOrder order = prepareSalesOrder(po, customer, popNumToInternalIdMap);
			
			WriteResponse response = null;
			logger.info("Adding new sales order for PO # "+po.getPoNumber());			
			NSRrequestDetails requestDetails = new NSRrequestDetails();
			requestDetails.setRequestType(SoapMessageType.ADD);			
			boolean exceptionHasBeenThrown = false;			
			String errorMessage = null;			
			try {
				prepeareNSBindingStub();
				response = getNetSuiteStub().add(order);			
			} catch (RemoteException e) {
				if (e instanceof AxisFault){
					AxisFault fault = (AxisFault)e;
					errorMessage = fault.getFaultReason();
				} else {
					errorMessage = e.getMessage();
				}		
				//throw  new NetsuiteOperationException(errorMessage);
				exceptionHasBeenThrown = true;
			} finally {				
				Call call = getNetSuiteStub()._getCall();
				try {
					logger.info("Generating Request/Response messages.");
					Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
					SoapMessagesSaver messageSaver = new SoapMessagesSaver();
					SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.ADD);
					requestDetails.setRequestFilePath(soapResponse.getRequestFilePath());
					requestDetails.setResponseFilePath(soapResponse.getResponseFilePath());
				} catch (Exception e) {
					logger.info("Could not generate Request/Response messages.");
				}
			}
			if(exceptionHasBeenThrown)
				throw new NetsuiteOperationException(errorMessage, requestDetails);
			//response = addWithRetryLogic(order);
			/*if(response == null)
				throw new NetsuiteNullResponseException("Failed to add sales order with PO # : " + po.getPoNumber());*/
			if(response != null){	
				Status status = response.getStatus();
				if (status.isIsSuccess()) {
					String salesOrderId = ((RecordRef) response.getBaseRef()).getInternalId();
					logger.info("SalesOrder was succesfully added. SalesOrder # :" + salesOrderId);
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append("Failed to add SalesOrder with PO # " + po.getPoNumber() + ". Reason:\r\n");
					StatusDetail[] details = status.getStatusDetail();
					for (StatusDetail detail : details) {
						builder.append(detail.getMessage() + "\r\n");
					}
					throw new NetsuiteOperationException(builder.toString(),requestDetails);
				}
			}

		} else {
			throw new NetsuiteOrderAlreadyExistsException("Sales order with PO:" + po.getPoNumber() + " was added previously.", null);
		}

		return true;
	}

	private String getItemInternalId(String vendorSKU) throws NetsuiteNullResponseException, NetsuiteOperationException {
		String result = null;
		
			ItemSearchBasic searchBasic = new ItemSearchBasic();
			searchBasic.setItemId(new SearchStringField(vendorSKU, SearchStringFieldOperator.is));
			ItemSearch itemSearch = new ItemSearch();
			itemSearch.setBasic(searchBasic);
			SearchResult searchResult = null;
			NSRrequestDetails requestDetails = new NSRrequestDetails();
			requestDetails.setRequestType(SoapMessageType.ADD);			
			boolean exceptionHasBeenThrown = false;			
			String errorMessage = null;			
			try {
				prepeareNSBindingStub();
				searchResult = getNetSuiteStub().search(itemSearch);			
			} catch (RemoteException e) {
				errorMessage = null;
				if (e instanceof AxisFault){
					AxisFault fault = (AxisFault)e;
					errorMessage = fault.getFaultReason();
				} else {
					errorMessage = e.getMessage();
				}	
				exceptionHasBeenThrown = true;				
			} finally {				
				Call call = getNetSuiteStub()._getCall();
				try {
					logger.info("Generating Request/Response messages.");
					Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
					SoapMessagesSaver messageSaver = new SoapMessagesSaver();
					SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
					requestDetails.setRequestFilePath(soapResponse.getRequestFilePath());
					requestDetails.setResponseFilePath(soapResponse.getResponseFilePath());					
				} catch (Exception e) {
					logger.info("Could not generate Request/Response messages.");
				}
				if (exceptionHasBeenThrown){
					throw new NetsuiteOperationException(errorMessage, requestDetails);
				}
			}
			
			//SearchResult searchResult = searchWithRetryLogic(itemSearch);
			if(searchResult == null)
				throw new NetsuiteNullResponseException("Failed to retrieve item internaId by vendorSKU.");
			
			RecordList recordList = searchResult.getRecordList();
			
			Record[] records = recordList.getRecord();
			
			if (searchResult.getStatus().isIsSuccess() && records != null) {
				for (Record rec: records){
					if(rec instanceof KitItem){
						KitItem item = (KitItem)rec;
						result = item.getInternalId();
					}
					if(rec instanceof InventoryItem){	
						InventoryItem item = (InventoryItem) rec;
						result = item.getInternalId();
					}
				}
			}
		return result;
	}

	@Override
	public List<String> createFAMessage(List<PurchaseOrderPojo> poList) {
		return XmlParserUtil.convertPoListToFaXmlMessages(poList);
	}
	@Override
	public List<SalesOrder> getPendingBilledOrBilledSalesOrdersFromPoppin(){
		List<SalesOrder> result = new ArrayList<SalesOrder>();
		
		try{
			TransactionSearchBasic searchBasic = new TransactionSearchBasic();
			searchBasic.setDepartment(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "11", null, null) }, SearchMultiSelectFieldOperator.anyOf));
			searchBasic.setStatus(new SearchEnumMultiSelectField(new String[] {TransactionStatus._salesOrderBilled.toString(), TransactionStatus._salesOrderPendingBilling.toString() }, SearchEnumMultiSelectFieldOperator.anyOf));
			
			TransactionSearch transactionSearch = new TransactionSearch();
			transactionSearch.setBasic(searchBasic);
			
			prepeareNSBindingStub();
			SearchResult searchResult = getNetSuiteStub().search(transactionSearch);
			
			//SearchResult searchResult = searchWithRetryLogic(searchBasic);
			RecordList recordList = searchResult.getRecordList();
			Record[] records = recordList.getRecord();
		
			if (searchResult.getStatus().isIsSuccess() && records != null) {
				for (Record rec: records){
					SalesOrder salesOrder = (SalesOrder) rec;
					result.add(salesOrder);
				}
			}		
		} catch (Exception tfe) {
			String errorMessage = "Error in NetsuiteOperationsManager::getRecordsFromPoppin:" + tfe.getMessage();
			logger.info(errorMessage);
			ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
		}
		finally {				
			Call call = getNetSuiteStub()._getCall();
			try {
				logger.info("Generating Request/Response messages.");
				Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
				Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
				SoapMessagesSaver messageSaver = new SoapMessagesSaver();
				messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
			} catch (Exception e) {
				logger.info("Could not generate Request/Response messages.");
			}
		}			
		return result;
	}

	@Override
	public List<SalesOrder> getPendingBilledOrBilledSalesOrdersFromPoppin(List<String> poIds) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		List<SalesOrder> result = new ArrayList<SalesOrder>();
		
			TransactionSearchBasic searchBasic = new TransactionSearchBasic();
			searchBasic.setDepartment(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "11", null, null) }, SearchMultiSelectFieldOperator.anyOf));
			searchBasic.setStatus(new SearchEnumMultiSelectField(new String[] {TransactionStatus._salesOrderBilled.toString(), TransactionStatus._salesOrderPendingBilling.toString() }, SearchEnumMultiSelectFieldOperator.anyOf));
			
			TransactionSearch transactionSearch = new TransactionSearch();
			TimeZone est = TimeZone.getTimeZone("EST");
			
			for(String poId : poIds){
				searchBasic.setOtherRefNum(new SearchTextNumberField(poId, null, SearchTextNumberFieldOperator.equalTo));
				transactionSearch.setBasic(searchBasic);
				SearchResult searchResult = null;			
				NSRrequestDetails details = new NSRrequestDetails();
				details.setRequestType(SoapMessageType.SEARCH);			
				boolean exceptionHasBeenThrown = false;			
				String errorMessage = null;			
				try {
					prepeareNSBindingStub();
					searchResult = getNetSuiteStub().search(transactionSearch);
				} catch (RemoteException e) {
					if (e instanceof AxisFault){
						AxisFault fault = (AxisFault)e;
						errorMessage = fault.getFaultReason();
					} else {
						errorMessage = e.getMessage();
					}
					exceptionHasBeenThrown = true;
					//throw  new NetsuiteOperationException(errorMessage);
				}/* finally {				
					Call call = getNetSuiteStub()._getCall();
					try {
						logger.info("Generating Request/Response messages.");
						Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
						Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
						SoapMessagesSaver messageSaver = new SoapMessagesSaver();
						messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
					} catch (Exception e) {
						logger.info("Could not generate Request/Response messages.");
					}
				}*/
				finally {	
					Call call = getNetSuiteStub()._getCall();
					try {
						logger.info("Generating Request/Response messages.");
						Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
						Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
						SoapMessagesSaver messageSaver = new SoapMessagesSaver();
						SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
						details.setRequestFilePath(soapResponse.getRequestFilePath());
						details.setResponseFilePath(soapResponse.getResponseFilePath());
					} catch (Exception e) {
						logger.info("Could not generate Request/Response messages.");
					}
				}
				if(exceptionHasBeenThrown)
					throw new NetsuiteOperationException(errorMessage, details);
				//SearchResult searchResult = searchWithRetryLogic(searchBasic);
				/*if(searchResult == null)
					throw new NetsuiteNullResponseException("Failed to retrieve records from Poppin.");
				RecordList recordList = searchResult.getRecordList();
				Record[] records = null;
				if(recordList != null)
					records = recordList.getRecord();
		
				if (searchResult.getStatus().isIsSuccess() && records != null) {
					for (Record rec: records){
						SalesOrder salesOrder = (SalesOrder) rec;
						result.add(salesOrder);
					}
				}*/
				if(searchResult != null){
					RecordList recordList = searchResult.getRecordList();
					Record[] records = null;
					if(recordList != null)
						records = recordList.getRecord();
			
					if (searchResult.getStatus().isIsSuccess() && records != null) {
						for (Record rec: records){
							SalesOrder salesOrder = (SalesOrder) rec;
							result.add(salesOrder);
						}
					}
				}
			}
		return result;
	}
	
	private Customer getCustomerById(String id) throws NetsuiteNullResponseException, NetsuiteOperationException {
		//ReadResponse response = getWithRetryLogic(false, new RecordRef(null, id, null, RecordType.customer));
		
		ReadResponse response = null;
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestType(SoapMessageType.GET);			
		boolean exceptionHasBeenThrown = false;			
		String errorMessage = null;			
		try {
			prepeareNSBindingStub();
			response = getNetSuiteStub().get(new RecordRef(null, id, null, RecordType.customer));			
		} catch (RemoteException e) {
			errorMessage = null;
			if (e instanceof AxisFault){
				AxisFault fault = (AxisFault)e;
				errorMessage = fault.getFaultReason();
			} else {
				errorMessage = e.getMessage();
			}	
			exceptionHasBeenThrown = true;
			//throw  new NetsuiteOperationException(errorMessage);
		} finally {				
			Call call = getNetSuiteStub()._getCall();
			try {
				logger.info("Generating Request/Response messages.");
				Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
				Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
				SoapMessagesSaver messageSaver = new SoapMessagesSaver();
				SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.GET);
				details.setRequestFilePath(soapResponse.getRequestFilePath());
				details.setResponseFilePath(soapResponse.getResponseFilePath());
			} catch (Exception e) {
				logger.info("Could not generate Request/Response messages.");
			}
			if (exceptionHasBeenThrown){
				throw  new NetsuiteOperationException(errorMessage, details);
			}
		}
		
		if(response == null)
			throw new NetsuiteNullResponseException("Failed to retrieve customer by id.");
		
		Record rec = response.getRecord();
		Customer customer = null;
		
		if (response.getStatus().isIsSuccess() && rec != null) {
			customer = (Customer) rec;
		}
		return customer;
	}
	private SalesOrder prepareSalesOrder(PurchaseOrderPojo po, Customer customer, Map<String, String> popNumToInternalIdMap) throws NetsuiteNullResponseException, NetsuiteOperationException{
		SalesOrder order = new SalesOrder();

		/* set cutomer */
		RecordRef customerRef = new RecordRef();
		customerRef.setInternalId(customer.getInternalId());
		order.setEntity(customerRef);

		/* set Customer order # */
		CustomFieldList customBodyList = new CustomFieldList();
		CustomFieldRef[] customBodyFields = new CustomFieldRef[3];
		customBodyFields[0] = new StringCustomFieldRef("custbody14",Integer.toString(po.getOrderId()));
		customBodyFields[1] = new BooleanCustomFieldRef("custbody_is_urgent", true);
		customBodyFields[2] = new StringCustomFieldRef("custbodypartner_person_place_id", po.getShipToPartnerPersonPlaceId());
		customBodyList.setCustomField(customBodyFields);
		order.setCustomFieldList(customBodyList);
		

		Calendar createdDate = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setTimeZone(TimeZone.getTimeZone(GlobalProperties.TIME_ZONE));
		try {
			createdDate.setTime(format.parse(po.getOrderDate()));
		} catch (Exception e) {
			throw new NetsuiteOrderValidationException("Could not parse the date String provided. Reason:" + e.getMessage(), null);
		}
		/* set PO# */
		order.setOtherRefNum(po.getPoNumber());
		order.setTranDate(createdDate);

		/* set Status */
		order.setOrderStatus(SalesOrderOrderStatus._pendingFulfillment);
		/* set department */
		RecordRef department = new RecordRef();
		department.setInternalId("11");
		order.setDepartment(department);
		
		//ship method
		RecordRef shipMethod = new RecordRef();
		shipMethod.setInternalId("3253");
		order.setShipMethod(shipMethod);
		order.setShippingCost(0.00);
		
		
		order.setIsTaxable(false);
		order.setTerms(new RecordRef(null, "8", null, RecordType.term));
		
		/* set Shipping Address */
		String shippingAddressInternalId = null;
		CustomerAddressbookList addressBookList = customer.getAddressbookList();
		CustomerAddressbook[] addressBook = addressBookList.getAddressbook();
		if(addressBook.length == 1)
			shippingAddressInternalId = addressBook[0].getInternalId();
		else{
			for(CustomerAddressbook book : addressBook){
				if(book.getDefaultShipping()){
					shippingAddressInternalId = book.getInternalId();
					break;
				}
			}
		}
		
		if(shippingAddressInternalId == null){
			throw new NetsuiteOrderValidationException("Could add sales order. Reason: could not find default shipping address for customer " + customer.getEmail(), null);
		}
		order.setShipAddressList(new RecordRef("shipaddresslist", shippingAddressInternalId, null, null));

		SalesOrderItemList itemList = new SalesOrderItemList();
		List<SalesOrderItem> salesOrderItemList = new ArrayList<SalesOrderItem>();
		
		DecimalFormat doublePrecision = new DecimalFormat("#.##");
		
		for (OrderItemPojo orderItemPo : po.getOrderItems()) {

			SalesOrderItem orderItem = new SalesOrderItem();
			
			RecordRef item = new RecordRef();
			
			
			/*String vendorSKU = persitenceManager.getVendorSkuByModelNum(orderItemPo.getModelNum());
			if(vendorSKU == null){
				throw new NetsuiteOperationException("Failed to add order with PO # " + po.getPoNumber()  + ". Reason : there is no item in inventory with vendorSKU =" + orderItemPo.getModelNum() +" .");
			}
			if(vendorSkuToItemInternalId.containsKey(vendorSKU)){
				itemInternalId = vendorSkuToItemInternalId.get(vendorSKU);
			}
			else{
				itemInternalId = getItemInternalId(vendorSKU);
				vendorSkuToItemInternalId.put(vendorSKU, itemInternalId);
			}*/
			
			String itemInternalId = popNumToInternalIdMap.get(orderItemPo.getModelNum());
			if(itemInternalId != null)
				item.setInternalId(itemInternalId);
			//else{
			//	throw new NetsuiteOrderValidationException("Failed to add order with PO # " + po.getPoNumber()  + ". Reason : there is no item in inventory with vendorSKU =" + orderItemPo.getModelNum() +" .");
			//}
			/* vendor line # , merchant_SKU*/
			CustomFieldList customList = new CustomFieldList();
			CustomFieldRef[] customFields = new CustomFieldRef[2];
			
			customFields[0] = new StringCustomFieldRef("custcol11", Integer.toString(orderItemPo.getMerchantLineNumber()));
			customFields[1] = new StringCustomFieldRef("custcolmerchant_sku", orderItemPo.getMerchantSKU());
			
			customList.setCustomField(customFields);
			orderItem.setCustomFieldList(customList);
			
			orderItem.setItem(item);
			RecordRef r = new RecordRef();
			r.setInternalId("-1");
			orderItem.setPrice(r);
			
			/* set Quantity */
			orderItem.setQuantity(Double.parseDouble(Integer
					.toString(orderItemPo.getQtyOrdered())));
			
			/*double unitCost = orderItemPo.getUnitCost();
			unitCost = unitCost * 100;
			unitCost = Math.round(unitCost);
			unitCost = unitCost / 100;*/
			
			BigDecimal unitCostDec = BigDecimal.valueOf(orderItemPo.getUnitCost()).setScale(2, RoundingMode.HALF_EVEN);
			orderItem.setRate(unitCostDec.toString());
			
			//orderItem.setRate(doublePrecision.format(orderItemPo.getUnitCost()));
			//orderItem.setRate(Double.toString(unitCost));
			
			
			//orderItem.setAmount(orderItemPo.getQtyOrdered() * unitCostDec.doubleValue());
			//orderItem.setAmount(orderItemPo.getQtyOrdered() * unitCost);
			orderItem.setAmount(BigDecimal.valueOf(orderItemPo.getQtyOrdered() * unitCostDec.doubleValue()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());

			/* set desc */ 
			orderItem.setDescription(orderItemPo.getDescription());
			

			salesOrderItemList.add(orderItem);
		}
		SalesOrderItem[] salesOrderItemArray = new SalesOrderItem[salesOrderItemList
				.size()];
		salesOrderItemList.toArray(salesOrderItemArray);
		itemList.setItem(salesOrderItemArray);
		order.setItemList(itemList);
		return order;
	}

	@Override
	public List<SalesOrder> getBilledSalesOrdersFromPoppin(List<String> poIds)
			throws NetsuiteNullResponseException, NetsuiteOperationException {
		List<SalesOrder> result = new ArrayList<SalesOrder>();
		
		TransactionSearchBasic searchBasic = new TransactionSearchBasic();
		searchBasic.setDepartment(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "11", null, null) }, SearchMultiSelectFieldOperator.anyOf));
		searchBasic.setStatus(new SearchEnumMultiSelectField(new String[] {TransactionStatus._salesOrderBilled.toString() }, SearchEnumMultiSelectFieldOperator.anyOf));
		
		TransactionSearch transactionSearch = new TransactionSearch();
		ItemSearchBasic itemJoin = new ItemSearchBasic();
		itemJoin.setItemId(new SearchStringField());
		
		
		for(String poId : poIds){
			searchBasic.setOtherRefNum(new SearchTextNumberField(poId, null, SearchTextNumberFieldOperator.equalTo));
			transactionSearch.setBasic(searchBasic);
			transactionSearch.setItemJoin(itemJoin);
			SearchResult searchResult = null;
			NSRrequestDetails details = new NSRrequestDetails();
			details.setRequestType(SoapMessageType.SEARCH);			
			boolean exceptionHasBeenThrown = false;			
			String errorMessage = null;			
			try {
				prepeareNSBindingStub();
				searchResult = getNetSuiteStub().search(transactionSearch);			
			} catch (RemoteException e) {
				errorMessage = null;
				if (e instanceof AxisFault){
					AxisFault fault = (AxisFault)e;
					errorMessage = fault.getFaultReason();
				} else {
					errorMessage = e.getMessage();
				}	
				exceptionHasBeenThrown = true;
				//throw  new NetsuiteOperationException(errorMessage);
			} finally {				
				Call call = getNetSuiteStub()._getCall();
				try {
					logger.info("Generating Request/Response messages.");
					Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
					SoapMessagesSaver messageSaver = new SoapMessagesSaver();
					SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
					details.setRequestFilePath(soapResponse.getRequestFilePath());
					details.setResponseFilePath(soapResponse.getResponseFilePath());
				} catch (Exception e) {
					logger.info("Could not generate Request/Response messages.");
				}
				if (exceptionHasBeenThrown){
					throw  new NetsuiteOperationException(errorMessage, details);
				}
			}
			//SearchResult searchResult = searchWithRetryLogic(searchBasic);
			if(searchResult == null)
				throw new NetsuiteNullResponseException("Failed to retrieve billed sales orders from Poppin.");
			RecordList recordList = searchResult.getRecordList();
			Record[] records = null;
			if(recordList != null)
				records = recordList.getRecord();
			
			if (searchResult.getStatus().isIsSuccess() && records != null) {
				for (Record rec: records){
					SalesOrder salesOrder = (SalesOrder) rec;
					CustomFieldList soCustomList = salesOrder.getCustomFieldList();
					CustomFieldRef[] customFieldRef = soCustomList.getCustomField();
					StringCustomFieldRef stringCustomFieldRef = null;
					String partnerPersonPlaceIdstr = null;
					for(CustomFieldRef cRef : customFieldRef){
						if(cRef.getClass() == StringCustomFieldRef.class){
							stringCustomFieldRef = (StringCustomFieldRef)cRef;
							String customFieldInternalId = stringCustomFieldRef.getInternalId();
							if(customFieldInternalId.equalsIgnoreCase("custbodypartner_person_place_id")){
								partnerPersonPlaceIdstr = stringCustomFieldRef.getValue();
								break;
							}
						}
					}
					if(partnerPersonPlaceIdstr != null)
						result.add(salesOrder);
				}
			}
		}
	return result;
	}

	@Override
	public Map<String, String> getItemInternalIdToItemNumberMap(List<SalesOrder> records) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		Map<String, String> itemInternalIdToItemNumberMap = new HashMap<String, String>();
		ItemSearchAdvanced adv = new ItemSearchAdvanced();
		ItemSearch itemSearch = new ItemSearch();
		ItemSearchBasic basic = new ItemSearchBasic();
		
		ItemSearchRow searchRow = new ItemSearchRow();
		ItemSearchRowBasic searchRowBasic = new ItemSearchRowBasic();
		searchRowBasic.setItemId(new SearchColumnStringField[] { new SearchColumnStringField()});
		searchRow.setBasic(searchRowBasic);
		
		adv.setColumns(searchRow);
		
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestType(SoapMessageType.SEARCH);		
		boolean exceptionHasBeenThrown = false;	
		String errorMessage = null;

		for(SalesOrder so : records){
			SalesOrderItem[] items = so.getItemList().getItem();
			for(SalesOrderItem item : items){
				String internalId = item.getItem().getInternalId();
				if(itemInternalIdToItemNumberMap.containsKey(internalId) == false){
					basic.setInternalId(new SearchMultiSelectField(new RecordRef[] {new RecordRef(null, internalId, null, RecordType.inventoryItem) }, SearchMultiSelectFieldOperator.anyOf));
					itemSearch.setBasic(basic);
					adv.setCriteria(itemSearch);
					SearchResult searchResult = null;
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().search(adv);			
					} catch (RemoteException e) {
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}		
						//throw  new NetsuiteOperationException(errorMessage);
						exceptionHasBeenThrown = true;
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							//messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
					}
					if(exceptionHasBeenThrown)
						throw new NetsuiteOperationException(errorMessage, details);
					//SearchResult result = searchWithRetryLogic(adv);
					/*if(searchResult == null)
						throw new NetsuiteNullResponseException("Failed to retrieve item number by internal id from Poppin.");*/
					if(searchResult != null){
						SearchRow[] row = searchResult.getSearchRowList().getSearchRow();
						if(null != row && row.length > 0){
							ItemSearchRow searched = (ItemSearchRow)row[0];
							SearchColumnStringField[] searchedColumns = searched.getBasic().getItemId();
							String itemNumber = searchedColumns[0].getSearchValue();
							itemInternalIdToItemNumberMap.put(internalId, itemNumber);
						}
					}
				}
			}
		}
		return itemInternalIdToItemNumberMap;
	}

	@Override
	public Map<String, CustomerShppingAddressPojo> getsalesOrderIdToShippingAddressMap(List<SalesOrder> records) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		Map<String, CustomerShppingAddressPojo> salesOrderIdToShippingAddressMap = new HashMap<String, CustomerShppingAddressPojo>();
		CustomerSearchAdvanced customerSearchAdvanced = new CustomerSearchAdvanced();
		CustomerSearch customerSearch = new CustomerSearch();
		CustomerSearchBasic customerSearchBasic = new CustomerSearchBasic();
		CustomerSearchRowBasic customerSearchRowBasic = new CustomerSearchRowBasic();
		customerSearchRowBasic.setShipAddressee(new SearchColumnStringField[]{new SearchColumnStringField()});
		customerSearchRowBasic.setShipAddress1(new SearchColumnStringField[]{new SearchColumnStringField()});
		customerSearchRowBasic.setShipCity(new SearchColumnStringField[]{new SearchColumnStringField()});
		customerSearchRowBasic.setShipState(new SearchColumnStringField[]{new SearchColumnStringField()});
		customerSearchRowBasic.setShipZip(new SearchColumnStringField[]{new SearchColumnStringField()});
		CustomerSearchRow customerSearchRow = new CustomerSearchRow();
		customerSearchRow.setBasic(customerSearchRowBasic);
		customerSearchAdvanced.setColumns(customerSearchRow);		
		
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestType(SoapMessageType.SEARCH);		
		boolean exceptionHasBeenThrown = false;		
		String errorMessage = null;		
		for(SalesOrder so : records){
			if(salesOrderIdToShippingAddressMap.containsKey(so.getInternalId()) == false){
				customerSearchBasic.setInternalId(new SearchMultiSelectField(new RecordRef[]{new RecordRef(null, so.getEntity().getInternalId(), null, RecordType.customer)}, SearchMultiSelectFieldOperator.anyOf));
				customerSearch.setBasic(customerSearchBasic);
				customerSearchAdvanced.setCriteria(customerSearch);
				SearchResult searchResult = null;
				try {
					prepeareNSBindingStub();
					searchResult = getNetSuiteStub().search(customerSearchAdvanced);			
				} catch (RemoteException e) {
					//String errorMessage = null;
					if (e instanceof AxisFault){
						AxisFault fault = (AxisFault)e;
						errorMessage = fault.getFaultReason();
					} else {
						errorMessage = e.getMessage();
					}		
					//throw  new NetsuiteOperationException(errorMessage);
					exceptionHasBeenThrown = true;
				} finally {				
					Call call = getNetSuiteStub()._getCall();
					try {
						logger.info("Generating Request/Response messages.");
						Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
						Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
						SoapMessagesSaver messageSaver = new SoapMessagesSaver();
						//messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
						SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
						details.setRequestFilePath(soapResponse.getRequestFilePath());
						details.setResponseFilePath(soapResponse.getResponseFilePath());
					} catch (Exception e) {
						logger.info("Could not generate Request/Response messages.");
					}
				}
				if(exceptionHasBeenThrown)
					throw new NetsuiteOperationException(errorMessage, details);
				//SearchResult result = searchWithRetryLogic(customerSearchAdvanced);
				/*if(searchResult == null)
					throw new NetsuiteNullResponseException("Failed to customer shipping address data by customer internal id from Poppin.");*/
				if(searchResult != null){
					SearchRow[] row = searchResult.getSearchRowList().getSearchRow();
					if(null != row && row.length > 0){
						CustomerShppingAddressPojo customerAddressPojo = new CustomerShppingAddressPojo();
						CustomerSearchRow searched = (CustomerSearchRow)row[0];
						SearchColumnStringField[] searchedAdressee = searched.getBasic().getShipAddressee();
						customerAddressPojo.setName1(searchedAdressee[0].getSearchValue());
						SearchColumnStringField[] searchedAddress1 = searched.getBasic().getShipAddress1();
						customerAddressPojo.setAddress1(searchedAddress1[0].getSearchValue());
						SearchColumnStringField[] searchedCity = searched.getBasic().getShipCity();
						customerAddressPojo.setCity(searchedCity[0].getSearchValue());
						SearchColumnStringField[] searchedState = searched.getBasic().getShipState();
						customerAddressPojo.setState(searchedState[0].getSearchValue());
						SearchColumnStringField[] searchedZipCode = searched.getBasic().getShipZip();
						customerAddressPojo.setPostalCode(searchedZipCode[0].getSearchValue());
						
						salesOrderIdToShippingAddressMap.put(so.getInternalId(), customerAddressPojo);
					}
				}
			}
		}
		
		return salesOrderIdToShippingAddressMap;
	}

	@Override
	public List<InventoryItem> getInventoryFromPoppin() {
		
		List<InventoryItem> result = new ArrayList<InventoryItem>();
		try{
			ItemSearchBasic searchBasic = new ItemSearchBasic();
			//searchBasic.setItemId(new SearchStringField("846680000110", SearchStringFieldOperator.contains));
			searchBasic.setType(new SearchEnumMultiSelectField(new String[] {"_inventoryItem"}, SearchEnumMultiSelectFieldOperator.anyOf));	
			ItemSearch itemSearch = new ItemSearch();
			itemSearch.setBasic(searchBasic);
			
			SearchResult searchResult = null;

			do {
				if (searchResult == null) {
					NSRrequestDetails details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);			
					boolean exceptionHasBeenThrown = false;			
					String errorMessage = null;		
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().search(itemSearch);			
					} catch (RemoteException e) {
						errorMessage = null;
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}	
						exceptionHasBeenThrown = true;
						//throw  new NetsuiteOperationException(errorMessage);
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
						if (exceptionHasBeenThrown){
							throw  new NetsuiteOperationException(errorMessage, details);
						}
					}
					//searchResult = searchWithRetryLogic(itemSearch);
				} else 
				{
					NSRrequestDetails details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);			
					boolean exceptionHasBeenThrown = false;			
					String errorMessage = null;	
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().searchMoreWithId(searchResult.getSearchId(),searchResult.getPageIndex() + 1);			
					} catch (RemoteException e) {
						errorMessage = null;
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}	
						exceptionHasBeenThrown = true;
						//throw  new NetsuiteOperationException(errorMessage);
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
						if (exceptionHasBeenThrown){
							throw  new NetsuiteOperationException(errorMessage, details);
						}
					}
					//searchResult = searchMoreWithIdWithRetryLogic(searchResult.getSearchId(),searchResult.getPageIndex() + 1);
				}

				RecordList recordList = searchResult.getRecordList();
				Record[] records = recordList.getRecord();
				
				if (searchResult.getStatus().isIsSuccess() && records != null) {
					for (Record rec : records) {
						InventoryItem item = (InventoryItem) rec;

						result.add(item);
					}
				}
			} while (searchResult.getPageIndex() < searchResult.getTotalPages());
		

		}
		catch (Exception tfe) {
			String errorMessage = "Error in NetsuiteOperationsManager::getRecordsFromPoppin:" + tfe.getMessage(); 
			logger.info(errorMessage);
			//ErrorsCollector.addCommonErrorMessage(errorMessage);
			ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			return result;
		}
		return result;
	}
	
	@Override
	public List<InventoryPojo> getInventoryFromPoppinAdvanced(Map<String,String> VendorSkuToModelNumMap) throws NetsuiteNullResponseException, NetsuiteOperationException {
		List<InventoryPojo> result = new ArrayList<InventoryPojo>();
		
			ItemSearchBasic searchBasic = new ItemSearchBasic();
			searchBasic.setType(new SearchEnumMultiSelectField(new String[] {"_inventoryItem","_kitItem"}, SearchEnumMultiSelectFieldOperator.anyOf));
			//searchBasic.setLocation(new SearchMultiSelectField(new RecordRef[]{new RecordRef(null, "4", null, RecordType.location)}, SearchMultiSelectFieldOperator.anyOf));
			
			ItemSearch itemSearch = new ItemSearch();
			itemSearch.setBasic(searchBasic);
			
			ItemSearchAdvanced itemSearchAdvanced = new ItemSearchAdvanced();
			ItemSearchRow itemSearchRow = new ItemSearchRow();
						
			ItemSearchRowBasic itemSearchRowBasic = new ItemSearchRowBasic();
			
			LocationSearchBasic locationSearchBasic = new LocationSearchBasic();
			locationSearchBasic.setInternalId(new SearchMultiSelectField(new RecordRef[]{ new RecordRef(null, "4", null, RecordType.location)}, SearchMultiSelectFieldOperator.anyOf));
			itemSearch.setInventoryLocationJoin(locationSearchBasic);
			
			itemSearchRowBasic.setItemId(new SearchColumnStringField[] {new SearchColumnStringField()});
			itemSearchRowBasic.setUpcCode(new SearchColumnStringField[] {new SearchColumnStringField()});
			itemSearchRowBasic.setLocationQuantityAvailable(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
			//itemSearchRowBasic.setStoreDescription(new SearchColumnStringField[] {new SearchColumnStringField()});
			itemSearchRowBasic.setPurchaseDescription(new SearchColumnStringField[] {new SearchColumnStringField()});
			
			itemSearchRow.setBasic(itemSearchRowBasic);
			itemSearchAdvanced.setColumns(itemSearchRow);
			itemSearchAdvanced.setCriteria(itemSearch);
			
			SearchResult searchResult = null;
			
			do {
				if (searchResult == null) {
					NSRrequestDetails details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);			
					boolean exceptionHasBeenThrown = false;			
					String errorMessage = null;	
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().search(itemSearchAdvanced);			
					} catch (RemoteException e) {
						errorMessage = null;
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}	
						exceptionHasBeenThrown = true;
						//throw  new NetsuiteOperationException(errorMessage);
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
						if (exceptionHasBeenThrown){
							throw  new NetsuiteOperationException(errorMessage, details);
						}
					}
					//searchResult = searchWithRetryLogic(itemSearchAdvanced);
					
					if(searchResult == null)
						throw new NetsuiteNullResponseException("Failed to retrieve inventory from Poppin.");
				} else 
				{
					NSRrequestDetails details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);			
					boolean exceptionHasBeenThrown = false;			
					String errorMessage = null;	
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().searchMoreWithId(searchResult.getSearchId(),searchResult.getPageIndex() + 1);			
					} catch (RemoteException e) {
						errorMessage = null;
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}	
						exceptionHasBeenThrown = true;
						//throw  new NetsuiteOperationException(errorMessage);
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
						if (exceptionHasBeenThrown){
							throw  new NetsuiteOperationException(errorMessage, details);
						}
					}
					//searchResult = searchMoreWithIdWithRetryLogic(searchResult.getSearchId(),searchResult.getPageIndex() + 1);
				}

				if(searchResult == null)
					throw new NetsuiteNullResponseException("Failed to retrieve inventory from Poppin.");
				SearchRow[] row = searchResult.getSearchRowList().getSearchRow();
				
				if(null != row){
					for(SearchRow searchedItem : row ){
						InventoryPojo pojo = new InventoryPojo();
						ItemSearchRow searchedRow = (ItemSearchRow)searchedItem;
						
						ItemSearchRowBasic upc = searchedRow.getBasic();
						String upcCode = null;
						SearchColumnStringField[] searchedUpc = upc.getUpcCode();
						if(null != searchedUpc && searchedUpc.length > 0){
							upcCode = searchedUpc[0].getSearchValue();
							pojo.setUPC(upcCode);
						}
						
						ItemSearchRowBasic itemId = searchedRow.getBasic();
						SearchColumnStringField[] searchedItemId = itemId.getItemId();
						if(null != searchedItemId && searchedItemId.length > 0){
							String vendorSKU = searchedItemId[0].getSearchValue();
							if(VendorSkuToModelNumMap.containsKey(vendorSKU))
								pojo.setVendorSKU(VendorSkuToModelNumMap.get(vendorSKU));
							else continue;
							//pojo.setVendorSKU(searchedItemId[0].getSearchValue());
						}
						
						ItemSearchRowBasic qty = searchedRow.getBasic();
						SearchColumnDoubleField[] searchedQty = qty.getLocationQuantityAvailable();
						if(null != searchedQty && searchedQty.length > 0)
							pojo.setQtyonhand(Double.toString(searchedQty[0].getSearchValue()));
						else
							pojo.setQtyonhand("0.0");
						
						ItemSearchRowBasic desc = searchedRow.getBasic();
						//SearchColumnStringField[] searchedDesc = desc.getStoreDescription();
						SearchColumnStringField[] searchedDesc = desc.getPurchaseDescription();
						if(null != searchedDesc && searchedDesc.length > 0)
							pojo.setDescription(searchedDesc[0].getSearchValue());
						
						result.add(pojo);
						
					}
				}
			} while (searchResult.getPageIndex() < searchResult.getTotalPages());
		
		return result;
	}

	@Override
	public List<SalesOrder> getBilledSalesOrdersFromPoppinAdvanced(List<String> poIds) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		List<SalesOrder> soList = new ArrayList<>();
		
		TransactionSearchAdvanced advanced = new TransactionSearchAdvanced();
		TransactionSearch search = new TransactionSearch();
		TransactionSearchBasic basic = new TransactionSearchBasic();
		TransactionSearchRow tsRow = new TransactionSearchRow();
		TransactionSearchRowBasic rowBasic = new TransactionSearchRowBasic();
		
		
		basic.setType(new SearchEnumMultiSelectField(new String[] { "_salesOrder" }, SearchEnumMultiSelectFieldOperator.anyOf));
		basic.setAccount(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "54", null, null) }, SearchMultiSelectFieldOperator.anyOf));
		basic.setRecordType(new SearchStringField(RecordType._salesOrder , SearchStringFieldOperator.is));
		basic.setStatus(new SearchEnumMultiSelectField(new String[] {TransactionStatus._salesOrderBilled.toString() }, SearchEnumMultiSelectFieldOperator.anyOf));
		search.setBasic(basic);
		
		TransactionSearchRowBasic billingTransactionSearchRowBasic = new TransactionSearchRowBasic();
		billingTransactionSearchRowBasic.setTranId(new SearchColumnStringField[]{new SearchColumnStringField()});
		//billingTransactionSearchRowBasic.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		billingTransactionSearchRowBasic.setActualShipDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		
		tsRow.setBillingTransactionJoin(billingTransactionSearchRowBasic);
		
		TransactionSearchRowBasic fulfillinfTransactionJoin = new TransactionSearchRowBasic();
		fulfillinfTransactionJoin.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		tsRow.setFulfillingTransactionJoin(fulfillinfTransactionJoin);
		
		rowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setActualShipDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		rowBasic.setShipDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		rowBasic.setQuantity(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		rowBasic.setRate(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		rowBasic.setTrackingNumbers(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setItem(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setEntity(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		
		SearchColumnCustomFieldList customFieldList = new SearchColumnCustomFieldList();
		SearchColumnStringCustomField personPlaceId = new SearchColumnStringCustomField(null, "custbodypartner_person_place_id", null);
		SearchColumnStringCustomField itemVendorLineNumber = new SearchColumnStringCustomField(null, "custcol11", null);
		SearchColumnStringCustomField itemMerchantSKU = new SearchColumnStringCustomField(null, "custcolmerchant_sku", null);
		
		customFieldList.setCustomField(new SearchColumnCustomField[]{personPlaceId, itemVendorLineNumber, itemMerchantSKU});
		rowBasic.setCustomFieldList(customFieldList);
		
		tsRow.setBasic(rowBasic);
		
		advanced.setColumns(tsRow);
		
		List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();
		SearchResult searchResult = null;
		
		advanced.setCriteria(search);
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestType(SoapMessageType.SEARCH);		
		boolean exceptionHasBeenThrown = false;		
		String errorMessage = null;
		
		for(String poId : poIds){
			advanced.getCriteria().getBasic().setOtherRefNum(new SearchTextNumberField(poId, null, SearchTextNumberFieldOperator.equalTo));
			try {
				prepeareNSBindingStub();
				searchResult = getNetSuiteStub().search(advanced);
			} catch (RemoteException  e) {
				if (e instanceof AxisFault){
					AxisFault fault = (AxisFault)e;
					errorMessage = fault.getFaultReason();
				} else {
					errorMessage = e.getMessage();
				}		
				//throw  new NetsuiteOperationException(errorMessage);
				exceptionHasBeenThrown = true;
			} finally {				
				Call call = getNetSuiteStub()._getCall();
				try {
					logger.info("Generating Request/Response messages.");
					Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
					SoapMessagesSaver messageSaver = new SoapMessagesSaver();
					//messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
					SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
					details.setRequestFilePath(soapResponse.getRequestFilePath());
					details.setResponseFilePath(soapResponse.getResponseFilePath());
				} catch (Exception e) {
					logger.info("Could not generate Request/Response messages.");
				}
			}
			if(exceptionHasBeenThrown)
				throw new NetsuiteOperationException(errorMessage, details);
			//searchResult = searchWithRetryLogic(advanced);
			if(null != searchResult && searchResult.getStatus().isIsSuccess() && searchResult.getTotalRecords() > 0){
				SearchRowList searchRowList = searchResult.getSearchRowList();
				if(null != searchRowList){
					SalesOrder salesOrder = null;
					SearchColumnCustomFieldList searchedCustomFieldList = null;
					List<SalesOrderItem> salesOrderItemList = new ArrayList<>();
					Calendar fulfilmentTranDate = null;
					SearchColumnDateField[] dateField = null;

					for(SearchRow searchRow : searchRowList.getSearchRow()){
						TransactionSearchRow row = (TransactionSearchRow) searchRow;
						TransactionSearchRowBasic searchedBasic = row.getBasic();
						TransactionSearchRowBasic joinedBilledTransaction = row.getBillingTransactionJoin();
						TransactionSearchRowBasic fulfillinfTransaction = row.getFulfillingTransactionJoin();
						
						if(null == joinedBilledTransaction || null == fulfillinfTransaction){
							logger.info("There is no billing or fulfilling transaction for  this order PO# " + poId + " .");
							break;
						}
						
						searchedCustomFieldList = searchedBasic.getCustomFieldList();
						if(null == searchedCustomFieldList){
							logger.info("There are no required custom field list for sales order PO# " + poId + " .");
							break;
						}
						SearchColumnCustomField[] customFields = searchedCustomFieldList.getCustomField();
						
						if(null == salesOrder){
							salesOrder = new SalesOrder();
							salesOrder.setOtherRefNum(poId);
							SearchColumnSelectField[] selectColumnField = searchedBasic.getInternalId();
							if(selectColumnField != null){
								salesOrder.setInternalId(selectColumnField[0].getSearchValue().getInternalId());
							}
							SearchColumnStringField[] stringField = joinedBilledTransaction.getTranId();
							if(null != stringField){
								salesOrder.setTranId(stringField[0].getSearchValue());
							}
							dateField = searchedBasic.getTranDate();
							if(null != dateField){
								salesOrder.setTranDate(dateField[0].getSearchValue());
							}
							//dateField = fulfillinfTransaction.getTranDate();
							if(null != dateField){
								//salesOrder.setActualShipDate(dateField[0].getSearchValue());
							}
							dateField = searchedBasic.getShipDate();
							if(null != dateField){
								salesOrder.setShipDate(dateField[0].getSearchValue());
							}
							stringField = searchedBasic.getTrackingNumbers();
							if(null != stringField){
								salesOrder.setLinkedTrackingNumbers(stringField[0].getSearchValue());
							}
							selectColumnField = searchedBasic.getEntity();
							if(null != selectColumnField){
								salesOrder.setEntity(selectColumnField[0].getSearchValue());
							}
							
							CustomFieldRef[] soCustomFields = new CustomFieldRef[1];
							
							for(SearchColumnCustomField customField : customFields){
								SearchColumnStringCustomField field = (SearchColumnStringCustomField)customField;
								if(null != field){
									if(field.getInternalId().equalsIgnoreCase("custbodypartner_person_place_id")){
										soCustomFields[0] = new StringCustomFieldRef("custbodypartner_person_place_id", field.getSearchValue());
									}
								}
							}
							salesOrder.setCustomFieldList(new CustomFieldList(soCustomFields));
						}
						SalesOrderItem soItem = new SalesOrderItem();
						SearchColumnDoubleField[] doubleField = searchedBasic.getQuantity();
						if(null!= doubleField){
							soItem.setQuantity(doubleField[0].getSearchValue());
						}
						doubleField = searchedBasic.getRate();
						if(null!= doubleField){
							soItem.setRate(Double.toString(doubleField[0].getSearchValue()));
						}
						//List<StringCustomFieldRef> soItemCustomFieldList = new ArrayList<StringCustomFieldRef>();
						CustomFieldRef[] soItemCustomFields = new CustomFieldRef[2];
						for(SearchColumnCustomField customField : customFields){
							SearchColumnStringCustomField field = (SearchColumnStringCustomField)customField;
							if(null != field){
								if(field.getInternalId().equalsIgnoreCase("custcol11")){
									soItemCustomFields[0] = new StringCustomFieldRef("custcol11", field.getSearchValue());
								}
								if(field.getInternalId().equalsIgnoreCase("custcolmerchant_sku")){
									soItemCustomFields[1] = new StringCustomFieldRef("custcolmerchant_sku", field.getSearchValue());
								}
							}
						}
						soItem.setCustomFieldList(new CustomFieldList(soItemCustomFields));
						SearchColumnSelectField[] itemRecordRefArray = searchedBasic.getItem();
						RecordRef itemRecordRef = itemRecordRefArray[0].getSearchValue();
						soItem.setItem(itemRecordRef);
						salesOrderItemList.add(soItem);
						
						dateField = fulfillinfTransaction.getTranDate();
						if(dateField != null){
							if(null == fulfilmentTranDate){
								fulfilmentTranDate = dateField[0].getSearchValue();
							}
							if(dateField[0].getSearchValue().getTimeInMillis() > fulfilmentTranDate.getTimeInMillis())
								fulfilmentTranDate = dateField[0].getSearchValue();
						}
					}
					if(null != salesOrder){
						soList.add(salesOrder);
						SalesOrderItem[] salesOrderItemArray = new SalesOrderItem[salesOrderItemList.size()];
						salesOrderItemList.toArray(salesOrderItemArray);
						salesOrder.setItemList(new SalesOrderItemList(salesOrderItemArray, false));
						if(null != fulfilmentTranDate)
							salesOrder.setActualShipDate(fulfilmentTranDate);
					}
				}
			}
		}
		
		return soList;
	}

	
	//service code below ...
	/*@Override
	public void retrieveInventoryInternalIdFromPoppin(List<VendorSkuToModelNumMapDAO> inventoryDAOs) {
		int counter = 0;
		for (VendorSkuToModelNumMapDAO inventoryDAO:inventoryDAOs){
			counter++;
			System.out.println("Retrieving inventory #"+counter);
			ItemSearchAdvanced itemAdv = new ItemSearchAdvanced();			
			ItemSearchBasic itembasicCriteria = new ItemSearchBasic();
			ItemSearch isCriteria = new ItemSearch();		
			isCriteria.setBasic(itembasicCriteria);	
			//itembasicCriteria.setItemId(new SearchStringField("846680017828", SearchStringFieldOperator.is));
			itembasicCriteria.setItemId(new SearchStringField(String.valueOf(inventoryDAO.getVendorSku()), SearchStringFieldOperator.is));
			//itembasicCriteria.setType(new SearchEnumMultiSelectField(new String[] {"_inventoryItem","_kitItem"}, SearchEnumMultiSelectFieldOperator.anyOf));
			
			ItemSearchRow itemRow = new ItemSearchRow();
			ItemSearchRowBasic itemRowBasic = new ItemSearchRowBasic();
			itemRowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
			itemRow.setBasic(itemRowBasic);
			
			itemAdv.setCriteria(isCriteria);
			itemAdv.setColumns(itemRow);
			SearchResult result = null;		
			try {
				prepeareNSBindingStub();
				result = getNetSuiteStub().search(itemAdv);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
			try {
				if (result.getStatus().isIsSuccess()&&result.getTotalRecords()==1){
					SearchRowList rowList = result.getSearchRowList();
					ItemSearchRow itemRowResult = (ItemSearchRow) rowList.getSearchRow()[0];
					inventoryDAO.setItemInternalId(itemRowResult.getBasic().getInternalId()[0].getSearchValue().getInternalId());
				}
			} catch (Exception e){
				System.out.println("itemSKU = '"+inventoryDAO.getVendorSku()+"' error");
			}			
		}		
	}*/

	@Override
	public List<InventoryPojo> getInventoryFromPoppinUpdated(List<VendorSkuToModelNumMapDAO> inventoryDAOs) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		
		List<KitItem> kitItemsList = new ArrayList<>();
		List<InventoryItem> inventoryItemsList = new ArrayList<>();
		List<InventoryPojo> inventoryPojoList  = new ArrayList<>();
		
		retrieveInventories(inventoryDAOs,kitItemsList,inventoryItemsList);			
		
		Map<String, VendorSkuToModelNumMapDAO> internalIdToInventoryDAOMap = new HashMap<>();		
		for (VendorSkuToModelNumMapDAO inventoryDAO:inventoryDAOs){
			internalIdToInventoryDAOMap.put(String.valueOf(inventoryDAO.getItemInternalId()), inventoryDAO);
		}	
		
		extractInventoryPojoListFromInventoryItemList(inventoryItemsList,inventoryPojoList, internalIdToInventoryDAOMap);
		
		Set<String> setInventoriesIdsToPreload = getInventoriesInternalIdsToPreload(inventoryItemsList, kitItemsList);
		
		preloadKitInventory(inventoryItemsList,setInventoriesIdsToPreload);
		
		extractInventoryPojoListFromKitItemList(inventoryItemsList, kitItemsList, inventoryPojoList, internalIdToInventoryDAOMap);
		
		return inventoryPojoList;
	}

	private void extractInventoryPojoListFromKitItemList(List<InventoryItem> inventoryItemsList, List<KitItem> kitItemsList, List<InventoryPojo> inventoryPojoList,	Map<String, VendorSkuToModelNumMapDAO> internalIdToInventoryDAOMap) {
		Map<String,InventoryItem> internalIdToInventoryItemMap = new HashMap<>();
		
		for (InventoryItem invItem:inventoryItemsList){
			internalIdToInventoryItemMap.put(invItem.getInternalId(),invItem);
		}
		
		for (KitItem kitItem:kitItemsList){
			InventoryPojo invPojo = new InventoryPojo();			
			List<Double> memberQtys = new ArrayList<>();			
			for (ItemMember itemMember:kitItem.getMemberList().getItemMember()){
				InventoryItem itemInvMember =  internalIdToInventoryItemMap.get(itemMember.getItem().getInternalId());
				Double locationQty = new Double(0);
				for (InventoryItemLocations location:itemInvMember.getLocationsList().getLocations()){
					if (location.getLocationId().getInternalId().equalsIgnoreCase("4")){ //DOT COM
						locationQty = location.getQuantityAvailable();
						break;
					}
				}	
				if (locationQty == null){
					memberQtys.add(new Double(0));
				} else {
					memberQtys.add(Math.floor(locationQty/itemMember.getQuantity()));
				}				
			}
			Double kitAvailableQty = new Double(Double.MAX_VALUE);
			for (Double mememberQty:memberQtys){
				if (mememberQty<kitAvailableQty){
					kitAvailableQty = mememberQty;
				}
			}
			
			if (kitAvailableQty != Double.MAX_VALUE){
				invPojo.setQtyonhand(String.valueOf(kitAvailableQty));
			} else {
				invPojo.setQtyonhand("0.0");
			}			
			
			String searchedDesc = kitItem.getDescription();
			if(null != searchedDesc && searchedDesc.length() > 0)
				invPojo.setDescription(searchedDesc);	
			
						
			String internalId = kitItem.getInternalId();
			if (internalIdToInventoryDAOMap.containsKey(internalId)){
				VendorSkuToModelNumMapDAO inventoryDAO = internalIdToInventoryDAOMap.get(internalId);
				invPojo.setVendorSKU(inventoryDAO.getModelNum());
			} else {
				continue;
			}	
			
			String sku = kitItem.getItemId();
			if(null != sku && sku.length() > 0){
				invPojo.setUPC(sku);				
			} 
			
			inventoryPojoList.add(invPojo);
		}
	}

	private void preloadKitInventory(List<InventoryItem> inventoryItemsList, Set<String> setInventoriesIdsToPreload) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		RecordRef[] inventoryRefs = new RecordRef[setInventoriesIdsToPreload.size()];
		Iterator<String> setStringIterator = setInventoriesIdsToPreload.iterator();	
		int counter=0;
		while (setStringIterator.hasNext()){			
			inventoryRefs[counter] = new RecordRef(null, setStringIterator.next(), null, RecordType.inventoryItem);
			counter++;
		}
		
		ItemSearch itemSearch = new ItemSearch();		
		ItemSearchBasic itemSearchBasic = new ItemSearchBasic();
		itemSearchBasic.setInternalId(new SearchMultiSelectField(inventoryRefs, SearchMultiSelectFieldOperator.anyOf));
		itemSearch.setBasic(itemSearchBasic);
		SearchResult searchResult = null;		
		do {
			if (searchResult == null) {
				NSRrequestDetails details = new NSRrequestDetails();
				details.setRequestType(SoapMessageType.SEARCH);			
				boolean exceptionHasBeenThrown = false;			
				String errorMessage = null;	
				try {
					prepeareNSBindingStub();
					searchResult = getNetSuiteStub().search(itemSearch);			
				} catch (RemoteException e) {
					errorMessage = null;
					if (e instanceof AxisFault){
						AxisFault fault = (AxisFault)e;
						errorMessage = fault.getFaultReason();
					} else {
						errorMessage = e.getMessage();
					}	
					exceptionHasBeenThrown = true;
					//throw  new NetsuiteOperationException(errorMessage);
				} finally {				
					Call call = getNetSuiteStub()._getCall();
					try {
						logger.info("Generating Request/Response messages.");
						Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
						Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
						SoapMessagesSaver messageSaver = new SoapMessagesSaver();
						SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
						details.setRequestFilePath(soapResponse.getRequestFilePath());
						details.setResponseFilePath(soapResponse.getResponseFilePath());
					} catch (Exception e) {
						logger.info("Could not generate Request/Response messages.");
					}
					if (exceptionHasBeenThrown){
						throw  new NetsuiteOperationException(errorMessage, details);
					}
				}				
				/*if(searchResult == null)
					throw new NetsuiteNullResponseException("Failed to retrieve inventory from Poppin.");*/
			} else 
			{
				NSRrequestDetails details = new NSRrequestDetails();
				details.setRequestType(SoapMessageType.SEARCH);			
				boolean exceptionHasBeenThrown = false;			
				String errorMessage = null;	
				try {
					prepeareNSBindingStub();
					searchResult = getNetSuiteStub().searchMoreWithId(searchResult.getSearchId(),searchResult.getPageIndex() + 1);			
				} catch (RemoteException e) {
					errorMessage = null;
					if (e instanceof AxisFault){
						AxisFault fault = (AxisFault)e;
						errorMessage = fault.getFaultReason();
					} else {
						errorMessage = e.getMessage();
					}	
					exceptionHasBeenThrown = true;
					//throw  new NetsuiteOperationException(errorMessage);
				} finally {				
					Call call = getNetSuiteStub()._getCall();
					try {
						logger.info("Generating Request/Response messages.");
						Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
						Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
						SoapMessagesSaver messageSaver = new SoapMessagesSaver();
						SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
						details.setRequestFilePath(soapResponse.getRequestFilePath());
						details.setResponseFilePath(soapResponse.getResponseFilePath());
					} catch (Exception e) {
						logger.info("Could not generate Request/Response messages.");
					}
					if (exceptionHasBeenThrown){
						throw  new NetsuiteOperationException(errorMessage, details);
					}
				}				
			}

			/*if(searchResult == null)
				throw new NetsuiteNullResponseException("Failed to retrieve inventory from Poppin.");*/
			if(searchResult != null){
				Record [] records = searchResult.getRecordList().getRecord();
				
				if(null != records){
					for(Record record : records ){
						if (record instanceof InventoryItem){
							InventoryItem inventoryItem = (InventoryItem)record;
							inventoryItemsList.add(inventoryItem);
						} 		
					}
				}
			}
		} while (searchResult.getPageIndex() < searchResult.getTotalPages());		
	}

	private Set<String> getInventoriesInternalIdsToPreload(	List<InventoryItem> inventoryItemsList, List<KitItem> kitItemsList) {
		Set<String> result = new HashSet<String>();
		for(KitItem kit: kitItemsList){
			for (ItemMember member:kit.getMemberList().getItemMember()){
				result.add(member.getItem().getInternalId());
			}
		}
		for (InventoryItem invItem:inventoryItemsList){
			result.remove(invItem.getInternalId());
		}
		return result;
	}

	private void extractInventoryPojoListFromInventoryItemList(List<InventoryItem> inventoryItemsList, List<InventoryPojo> inventoryPojoList, Map<String, VendorSkuToModelNumMapDAO> internalIdToInventoryDAOMap) {
		for (InventoryItem invItem : inventoryItemsList){			
			InventoryPojo invPojo = new InventoryPojo();
			
			String searchedDesc = invItem.getPurchaseDescription();
			if(null != searchedDesc && searchedDesc.length() > 0)
				invPojo.setDescription(searchedDesc);
			
			String qtyOnHand = null;
						
			for (InventoryItemLocations location: invItem.getLocationsList().getLocations()){
				if (location.getLocationId().getInternalId().equalsIgnoreCase("4")){ // DOT COM
					Double qtyAvailable = location.getQuantityAvailable();
					if (qtyAvailable!=null){
						qtyOnHand = qtyAvailable.toString();
					}					
					break;
				}
			}
			if (qtyOnHand!=null){
				invPojo.setQtyonhand(qtyOnHand);
			} else {
				invPojo.setQtyonhand("0.0");
			}
			
			String upc = invItem.getUpcCode();
			if(null != upc && upc.length() > 0){
				invPojo.setUPC(upc);
			} 		
			
			String internalId = invItem.getInternalId();
			if (internalIdToInventoryDAOMap.containsKey(internalId)){
				VendorSkuToModelNumMapDAO inventoryDAO = internalIdToInventoryDAOMap.get(internalId);
				invPojo.setVendorSKU(inventoryDAO.getModelNum());
			} else {
				continue;
			}		
		
			/*String sku = invItem.getItemId();
			if(null != sku && sku.length() > 0){
				if (skuToInventoryDAOMap.containsKey(sku)){
					VendorSkuToModelNumMapDAO inventoryDAO = skuToInventoryDAOMap.get(sku);
					invPojo.setVendorSKU(inventoryDAO.getModelNum());
				} else {
					continue;
				}			
			} else {
				continue;
			}*/
			inventoryPojoList.add(invPojo);			
		}
	}

	private void retrieveInventories(List<VendorSkuToModelNumMapDAO> inventoryDAOs,	List<KitItem> kitItemsList, List<InventoryItem> inventoryItemsList) throws /*NetsuiteNullResponseException,*/ NetsuiteOperationException {
		SearchResult searchResult = null;
		List<RecordList> recListList = new ArrayList<>();		
		int counter = 1;
		List<VendorSkuToModelNumMapDAO> bufferList = new ArrayList<>();
		List<List<VendorSkuToModelNumMapDAO>> bufferListList = new ArrayList<>();
		for (VendorSkuToModelNumMapDAO lineItemDAO:inventoryDAOs){			
			if (counter % 101 ==0 ){
				bufferListList.add(bufferList);
				bufferList = new ArrayList<>();
				counter = 1;
			}
			bufferList.add(lineItemDAO);
			counter++;
		}
		bufferListList.add(bufferList);	
		 for (List<VendorSkuToModelNumMapDAO> itemsBufferedList : bufferListList){
			 if (itemsBufferedList.size()>0){
				 RecordRef[] inventoriesRefs = new RecordRef[itemsBufferedList.size()];				
					for (int i=0; i<inventoriesRefs.length;i++){					
						inventoriesRefs[i] = new RecordRef(null,itemsBufferedList.get(i).getItemInternalId(),null,RecordType.inventoryItem);
					}		
					ItemSearchBasic itembasic = new ItemSearchBasic();
					ItemSearch is = new ItemSearch();		
					is.setBasic(itembasic);		
					itembasic.setInternalId(new SearchMultiSelectField(inventoriesRefs, SearchMultiSelectFieldOperator.anyOf));		
					int pageIndex=2;
					NSRrequestDetails details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);			
					boolean exceptionHasBeenThrown = false;			
					String errorMessage = null;	
					try {
						prepeareNSBindingStub();
						searchResult = getNetSuiteStub().search(is);			
					} catch (RemoteException e) {
						errorMessage = null;
						if (e instanceof AxisFault){
							AxisFault fault = (AxisFault)e;
							errorMessage = fault.getFaultReason();
						} else {
							errorMessage = e.getMessage();
						}		
						exceptionHasBeenThrown = true;
						//throw  new NetsuiteOperationException(errorMessage);
					} finally {				
						Call call = getNetSuiteStub()._getCall();
						try {
							logger.info("Generating Request/Response messages.");
							Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
							Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
							SoapMessagesSaver messageSaver = new SoapMessagesSaver();
							SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
							details.setRequestFilePath(soapResponse.getRequestFilePath());
							details.setResponseFilePath(soapResponse.getResponseFilePath());
						} catch (Exception e) {
							logger.info("Could not generate Request/Response messages.");
						}
						if (exceptionHasBeenThrown){
							throw  new NetsuiteOperationException(errorMessage, details);
						}
					}				
					/*if(searchResult == null)
						throw new NetsuiteNullResponseException("Failed to retrieve inventory from Poppin.");*/	
					if(searchResult != null)
					{
						recListList.add(searchResult.getRecordList());
						int totalPages = searchResult.getTotalPages();
						if(totalPages > 1){					
							while(pageIndex <= totalPages){
								NSRrequestDetails details2 = new NSRrequestDetails();
								details2.setRequestType(SoapMessageType.SEARCH);			
								boolean exceptionHasBeenThrown2 = false;			
								String errorMessage2 = null;
								try {	
									prepeareNSBindingStub();
									searchResult = getNetSuiteStub().searchMoreWithId(searchResult.getSearchId(),searchResult.getPageIndex() + 1);			
								} catch (RemoteException e) {
									errorMessage2 = null;
									if (e instanceof AxisFault){
										AxisFault fault = (AxisFault)e;
										errorMessage2 = fault.getFaultReason();
									} else {
										errorMessage2 = e.getMessage();
									}
									exceptionHasBeenThrown2 = true;
									//throw  new NetsuiteOperationException(errorMessage);
								} finally {				
									Call call = getNetSuiteStub()._getCall();
									try {
										logger.info("Generating Request/Response messages.");
										Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
										Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
										SoapMessagesSaver messageSaver = new SoapMessagesSaver();
										SoapSaverResponse soapResponse = messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
										details2.setRequestFilePath(soapResponse.getRequestFilePath());
										details2.setResponseFilePath(soapResponse.getResponseFilePath());
									} catch (Exception e) {
										logger.info("Could not generate Request/Response messages.");
									}
									if (exceptionHasBeenThrown2){
										throw  new NetsuiteOperationException(errorMessage2, details2);
									}
								}
								recListList.add(searchResult.getRecordList());
								pageIndex++;
							}
						}
			 		}
			 	}			 				
		 	}
		 for (RecordList recList:recListList){
			 for (Record rec:recList.getRecord()){
				 if (rec instanceof InventoryItem){
					 InventoryItem invItem = (InventoryItem) rec;
					 inventoryItemsList.add(invItem);
				 } else if (rec instanceof KitItem){
					 KitItem kitItem = (KitItem) rec;
					 kitItemsList.add(kitItem);
				 }
			 }
		 }		 
	}
	
	@Override
	public Map<String, UnprocessibleOrdersType> retrieveCancelledClosedOrders(List<PurchaseOrderDAO> purchaseOrderDAOs) {
		List<SearchRowList> recListList = new ArrayList<>();
		for (PurchaseOrderDAO poDAO : purchaseOrderDAOs){		
			TransactionSearchAdvanced advanced = new TransactionSearchAdvanced();
			TransactionSearch search = new TransactionSearch();
			TransactionSearchBasic basic = new TransactionSearchBasic();
			basic.setOtherRefNum(new SearchTextNumberField(poDAO.getPurchaseOrderNumber(), null, SearchTextNumberFieldOperator.equalTo));		
			basic.setMainLine(new SearchBooleanField(true));
			basic.setStatus(new SearchEnumMultiSelectField(new String[]{TransactionStatus._salesOrderCancelled.toString(), TransactionStatus._salesOrderClosed.toString() }, SearchEnumMultiSelectFieldOperator.anyOf));
			advanced.setCriteria(search);
			search.setBasic(basic);
			TransactionSearchRow rowSearch = new TransactionSearchRow();
			TransactionSearchRowBasic rowBasic = new TransactionSearchRowBasic();
			rowBasic.setStatus(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
			rowBasic.setOtherRefNum(new SearchColumnTextNumberField[]{new SearchColumnTextNumberField()});
			rowSearch.setBasic(rowBasic);
			advanced.setColumns(rowSearch);			
			SearchResult result = null;
			try {
				prepeareNSBindingStub();
				result = getNetSuiteStub().search(advanced);
				if (result.getStatus().isIsSuccess()){
					if (result.getSearchRowList().getSearchRow()!=null)
						recListList.add(result.getSearchRowList());					
				} else {
					//ErrorsCollector.addCommonErrorMessage(result.getStatus().getStatusDetail()[0].getMessage());
					ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(result.getStatus().getStatusDetail()[0].getMessage()));
				}
			} catch (RemoteException e) {
				String errorMessage = null;
				if (e instanceof AxisFault){
					AxisFault fault = (AxisFault)e;
					errorMessage = fault.getFaultReason();
				} else {
					errorMessage = e.getMessage();
				}		
				//ErrorsCollector.addCommonErrorMessage(errorMessage);
				ErrorsCollector.addCommonErrorMessage(new CHIntegrationError(errorMessage));
			} finally {				
				Call call = getNetSuiteStub()._getCall();
				try {
					logger.info("Generating Request/Response messages.");
					Document requestSoapXML = call.getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					Document responseSoapXML = call.getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();
					SoapMessagesSaver messageSaver = new SoapMessagesSaver();
					messageSaver.saveSoapMessage(requestSoapXML, responseSoapXML, SoapMessageType.SEARCH);
				} catch (Exception e) {
					logger.info("Could not generate Request/Response messages.");
				}
			}			
		}		
		Map<String,UnprocessibleOrdersType> poNumberToOrderStatusMap = new HashMap<>();
		for (SearchRowList seacrhRowList:recListList){
			for (SearchRow row:seacrhRowList.getSearchRow()){
				if (row != null){
					TransactionSearchRow transactionRow = (TransactionSearchRow) row;
					if (transactionRow != null){
						TransactionSearchRowBasic transactionRowbasic = transactionRow.getBasic();
						if (transactionRowbasic != null){
							SearchColumnEnumSelectField[] enumFields = null;
							SearchColumnTextNumberField[] textFields = null;
							enumFields = transactionRowbasic.getStatus();
							textFields = transactionRowbasic.getOtherRefNum();
							if (enumFields!= null && textFields!= null){
								UnprocessibleOrdersType type;
								if (enumFields[0].getSearchValue().equalsIgnoreCase(TransactionStatus.__salesOrderCancelled)){
									type = UnprocessibleOrdersType.CANCELLED;
								} else {
									type = UnprocessibleOrdersType.CLOSED;
								}
								poNumberToOrderStatusMap.put(textFields[0].getSearchValue(),type);
							}
						}
					}
				}
			}
		}
		return poNumberToOrderStatusMap;
	}
}
