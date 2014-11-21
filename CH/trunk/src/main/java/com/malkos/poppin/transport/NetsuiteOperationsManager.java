package com.malkos.poppin.transport;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import javassist.expr.Instanceof;

import javax.xml.rpc.ServiceException;
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

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.entities.CHIntegrationError;
import com.malkos.poppin.entities.CustomerShppingAddressPojo;
import com.malkos.poppin.entities.InventoryItemPojo;
import com.malkos.poppin.entities.InventoryKitPojo;
import com.malkos.poppin.entities.InventoryKitSubItemPojo;
import com.malkos.poppin.entities.InventoryPojo;
import com.malkos.poppin.entities.LocationQuantitiesAvailiable;
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
import com.netsuite.webservices.lists.accounting_2014_1.InventoryItem;
import com.netsuite.webservices.lists.accounting_2014_1.InventoryItemLocations;
import com.netsuite.webservices.lists.accounting_2014_1.ItemMember;
import com.netsuite.webservices.lists.accounting_2014_1.ItemSearch;
import com.netsuite.webservices.lists.accounting_2014_1.ItemSearchAdvanced;
import com.netsuite.webservices.lists.accounting_2014_1.ItemSearchRow;
import com.netsuite.webservices.lists.accounting_2014_1.KitItem;
import com.netsuite.webservices.lists.relationships_2014_1.Customer;
import com.netsuite.webservices.lists.relationships_2014_1.CustomerAddressbook;
import com.netsuite.webservices.lists.relationships_2014_1.CustomerAddressbookList;
import com.netsuite.webservices.lists.relationships_2014_1.CustomerSearch;
import com.netsuite.webservices.lists.relationships_2014_1.CustomerSearchAdvanced;
import com.netsuite.webservices.lists.relationships_2014_1.CustomerSearchRow;
//import com.netsuite.webservices.platform.common_2014_1.Address;
import com.netsuite.webservices.platform.common_2014_1.CustomerSearchBasic;
import com.netsuite.webservices.platform.common_2014_1.CustomerSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_1.ItemSearchBasic;
import com.netsuite.webservices.platform.common_2014_1.ItemSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_1.LocationSearchBasic;
import com.netsuite.webservices.platform.common_2014_1.TransactionSearchBasic;
import com.netsuite.webservices.platform.common_2014_1.TransactionSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_1.types.Country;
import com.netsuite.webservices.platform.common_2014_1.types.GlobalSubscriptionStatus;
import com.netsuite.webservices.platform.common_2014_1.LocationSearchRowBasic;
import com.netsuite.webservices.platform.core_2014_1.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_1.CustomFieldList;
import com.netsuite.webservices.platform.core_2014_1.CustomFieldRef;
import com.netsuite.webservices.platform.core_2014_1.DataCenterUrls;
import com.netsuite.webservices.platform.core_2014_1.Passport;
import com.netsuite.webservices.platform.core_2014_1.Record;
import com.netsuite.webservices.platform.core_2014_1.RecordList;
import com.netsuite.webservices.platform.core_2014_1.RecordRef;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnCustomField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnCustomFieldList;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnDateField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnDoubleField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnSelectField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnStringCustomField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnStringField;
import com.netsuite.webservices.platform.core_2014_1.SearchCustomFieldList;
import com.netsuite.webservices.platform.core_2014_1.SearchEnumMultiSelectField;
import com.netsuite.webservices.platform.core_2014_1.SearchMultiSelectField;
import com.netsuite.webservices.platform.core_2014_1.SearchResult;
import com.netsuite.webservices.platform.core_2014_1.SearchRow;
import com.netsuite.webservices.platform.core_2014_1.SearchRowList;
import com.netsuite.webservices.platform.core_2014_1.SearchStringField;
import com.netsuite.webservices.platform.core_2014_1.SearchTextNumberField;
import com.netsuite.webservices.platform.core_2014_1.Status;
import com.netsuite.webservices.platform.core_2014_1.StatusDetail;
import com.netsuite.webservices.platform.core_2014_1.StringCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_1.types.RecordType;
import com.netsuite.webservices.platform.core_2014_1.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2014_1.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2014_1.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.core_2014_1.types.SearchTextNumberFieldOperator;
import com.netsuite.webservices.platform.core_2014_1.SearchBooleanField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnEnumSelectField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnTextNumberField;
import com.netsuite.webservices.platform.core_2014_1.SearchColumnBooleanField;
import com.netsuite.webservices.platform.faults_2014_1.ExceededRecordCountFault;
import com.netsuite.webservices.platform.faults_2014_1.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults_2014_1.ExceededRequestSizeFault;
import com.netsuite.webservices.platform.faults_2014_1.ExceededUsageLimitFault;
import com.netsuite.webservices.platform.faults_2014_1.InvalidCredentialsFault;
import com.netsuite.webservices.platform.faults_2014_1.InvalidSessionFault;
import com.netsuite.webservices.platform.faults_2014_1.UnexpectedErrorFault;
import com.netsuite.webservices.platform.messages_2014_1.Preferences;
import com.netsuite.webservices.platform.messages_2014_1.ReadResponse;
import com.netsuite.webservices.platform.messages_2014_1.SearchPreferences;
import com.netsuite.webservices.platform.messages_2014_1.WriteResponse;
import com.netsuite.webservices.platform_2014_1.NetSuiteBindingStub;
import com.netsuite.webservices.platform_2014_1.NetSuitePortType;
import com.netsuite.webservices.platform_2014_1.NetSuiteServiceLocator;
import com.netsuite.webservices.transactions.purchases_2014_1.types.TransactionBillVarianceStatus;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrder;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrderItem;
import com.netsuite.webservices.transactions.sales_2014_1.SalesOrderItemList;
import com.netsuite.webservices.transactions.sales_2014_1.TransactionSearch;
import com.netsuite.webservices.transactions.sales_2014_1.TransactionSearchAdvanced;
import com.netsuite.webservices.transactions.sales_2014_1.TransactionSearchRow;
import com.netsuite.webservices.transactions.sales_2014_1.types.SalesOrderOrderStatus;
import com.netsuite.webservices.transactions.sales_2014_1.types.TransactionStatus;

public class NetsuiteOperationsManager implements INetsuiteOperationsManager {

	private static Logger logger = LoggerFactory.getLogger(NetsuiteOperationsManager.class);
	private Passport passport = null;	
	private String email;
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private String password;
	private String account;
	private String role;
	private String url;
	private NetSuitePortType port=null;
	
	private static class DataCenterAwareNetSuiteServiceLocator extends NetSuiteServiceLocator
	{
		private String account;

		public DataCenterAwareNetSuiteServiceLocator(String account)
		{
			this.account = account;
		}

		@Override
		public NetSuitePortType getNetSuitePort(URL defaultWsDomainURL)
		{
			try
			{
				NetSuitePortType _port = super.getNetSuitePort(defaultWsDomainURL);
				// Get the webservices domain for your account
				DataCenterUrls urls = _port.getDataCenterUrls(account).getDataCenterUrls();
				String wsDomain = urls.getWebservicesDomain();

				// Return URL appropriate for the specific account
				return super.getNetSuitePort(new URL(wsDomain.concat(defaultWsDomainURL.getPath())));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}	
	
	private void initializeWs(){
		logger.info("Initializing the Netsuite WS service.");
		logger.info("Identifiyng the Data Center location.");
		NetSuiteServiceLocator service = new DataCenterAwareNetSuiteServiceLocator(account);	
		logger.info("Getting the Netsuite Proxy port. It might take some time, please wait...");
		try {
			port = service.getNetSuitePort(new URL(url));
		} catch (MalformedURLException | ServiceException e) {			
			e.printStackTrace();
		}		
		((NetSuiteBindingStub) port).setTimeout(1000 * 60 * 60 * 2);
		logger.info("Netsuite WS service is initialized and ready for use.");
	}
	
	private void prepeareNSBindingStub() {				
		if (port ==null){
			initializeWs();
		}
		passport = new Passport();
		passport.setAccount(account);
		passport.setEmail(email);
		passport.setPassword(password);
		passport.setRole(new RecordRef(null, role , null, null));			
		NetSuiteBindingStub stub =  (NetSuiteBindingStub)port;
		stub.setMaintainSession(false);
		stub.clearHeaders();		
		SOAPHeaderElement searchPrefHeader = new SOAPHeaderElement("urn:messages_2014_1.platform.webservices.netsuite.com", "searchPreferences");
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
		SOAPHeaderElement passportHeader = new SOAPHeaderElement("urn:messages_2014_1.platform.webservices.netsuite.com", "passport");
		try {
			passportHeader.setObjectValue(passport);
		} catch (SOAPException e) {
			System.out.println("Wrong NetSuite Passport configuration");
			logger.error(e.getMessage());
		}
		stub.setHeader(passportHeader);
	}
	
	private NetSuiteBindingStub getNetSuiteStub(){
		return (NetSuiteBindingStub)port;
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
		//Address billAddress = new Address();
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
		//defaultBillingAddress.setAddressbookAddress(billAddress);
		defaultBillingAddress.setDefaultBilling(true);		
		listCustomerAddressbook.add(defaultBillingAddress);
		
		if( 
			(
				po.getCustomerAddress1().equals(po.getShipToAddress1()) && po.getCustomerCity().equals(po.getShipToCity())
				&& po.getCustomerName1().equals(po.getShipToName1())
			) == false
		)
		{
			//Address shipAddress = new Address();			
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
			//defaultShippingAddress.setAddressbookAddress(shipAddress);
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

		cust.setGlobalSubscriptionStatus(GlobalSubscriptionStatus._softOptOut);
		
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
		customBodyFields[0] = new StringCustomFieldRef(null, "custbody14",Integer.toString(po.getOrderId()));
		customBodyFields[1] = new BooleanCustomFieldRef(null, "custbody_is_urgent", true);
		customBodyFields[2] = new StringCustomFieldRef(null, "custbodypartner_person_place_id", po.getShipToPartnerPersonPlaceId());
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
			
			customFields[0] = new StringCustomFieldRef(null, "custcol11", Integer.toString(orderItemPo.getMerchantLineNumber()));
			customFields[1] = new StringCustomFieldRef(null, "custcolmerchant_sku", orderItemPo.getMerchantSKU());
			
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
	
	/*@Override
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
	}*/

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
		SearchColumnStringCustomField personPlaceId = new SearchColumnStringCustomField("custbodypartner_person_place_id","1474", null, null);
		SearchColumnStringCustomField itemVendorLineNumber = new SearchColumnStringCustomField("custcol11", "1471" ,null, null);
		SearchColumnStringCustomField itemMerchantSKU = new SearchColumnStringCustomField("custcolmerchant_sku", "1475", null, null);
		
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
									if(field.getScriptId().equalsIgnoreCase("custbodypartner_person_place_id")){
										soCustomFields[0] = new StringCustomFieldRef(null, "custbodypartner_person_place_id", field.getSearchValue());
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
								if(field.getScriptId().equalsIgnoreCase("custcol11")){
									soItemCustomFields[0] = new StringCustomFieldRef(null, "custcol11", field.getSearchValue());
								}
								if(field.getScriptId().equalsIgnoreCase("custcolmerchant_sku")){
									soItemCustomFields[1] = new StringCustomFieldRef(null, "custcolmerchant_sku", field.getSearchValue());
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
	
	public Collection<InventoryPojo> getInventoryFromPoppinUpdated(List<VendorSkuToModelNumMapDAO> lineItemDAOList) throws NetsuiteOperationException{
		SearchResult result= null;		
		List<SearchRowList> searchRowListList = new ArrayList<SearchRowList>();
		Map<String,VendorSkuToModelNumMapDAO> internalIdtoLineItemDAOMap = new HashMap<>();			
		for (VendorSkuToModelNumMapDAO lineItemDAO:lineItemDAOList){
			internalIdtoLineItemDAOMap.put(lineItemDAO.getItemInternalId(), lineItemDAO);			
		}		
		RecordRef[] inventoriesRefs = new RecordRef[lineItemDAOList.size()];
		int counter = 0;
		for (VendorSkuToModelNumMapDAO lineItemDAO : lineItemDAOList){											
			inventoriesRefs[counter] = new RecordRef(null,lineItemDAO.getItemInternalId(),null,RecordType.inventoryItem);	
			counter++;
		}		
		ItemSearchAdvanced itemAdv = new ItemSearchAdvanced();
		//CRITERIA
		ItemSearch is = new ItemSearch();
		ItemSearchBasic isBasic = new ItemSearchBasic();				
		isBasic.setInternalId(new SearchMultiSelectField(inventoriesRefs, SearchMultiSelectFieldOperator.anyOf));
		is.setBasic(isBasic);
		itemAdv.setCriteria(is);
		//COLUMNS	
		ItemSearchRow itemRow = new ItemSearchRow();
		ItemSearchRowBasic basicRow= new ItemSearchRowBasic();
		basicRow.setType(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
		basicRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		basicRow.setPreferredLocation(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		basicRow.setLocation(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		basicRow.setLocationQuantityAvailable(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		basicRow.setIsInactive(new SearchColumnBooleanField[]{new SearchColumnBooleanField()});
		basicRow.setBasePrice(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		basicRow.setMemberItem(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		basicRow.setMemberQuantity(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		basicRow.setUpcCode(new SearchColumnStringField[]{new SearchColumnStringField()});
		basicRow.setItemId(new SearchColumnStringField[]{new SearchColumnStringField()});
		basicRow.setPurchaseDescription(new SearchColumnStringField[]{new SearchColumnStringField()});		
		basicRow.setSalesDescription(new SearchColumnStringField[]{new SearchColumnStringField()});		
		LocationSearchRowBasic locRow = new LocationSearchRowBasic();
		locRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
		itemRow.setInventoryLocationJoin(locRow);
		itemRow.setBasic(basicRow);
		itemAdv.setColumns(itemRow);
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestType(SoapMessageType.SEARCH);		
		int pageIndex=2;	
		boolean exceptionHasBeenThrown = false;
		String errorMessage = null;
		try {
			prepeareNSBindingStub();
			result = getNetSuiteStub().search(itemAdv);
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
		if(null != result && result.getStatus().isIsSuccess() && result.getTotalRecords() > 0){
			searchRowListList.add(result.getSearchRowList());
			int totalPages = result.getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchId();
				while(pageIndex <= totalPages){
					details = new NSRrequestDetails();
					details.setRequestType(SoapMessageType.SEARCH);					
					exceptionHasBeenThrown = false;
					errorMessage = null;
					try {
						prepeareNSBindingStub();
						result = getNetSuiteStub().searchMoreWithId(searchId, pageIndex);
					} catch (RemoteException  e) {
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
					if(null != result && result.getStatus().isIsSuccess() && result.getTotalRecords() > 0){
						searchRowListList.add(result.getSearchRowList());
					} else {
						int i = 1;	
						errorMessage = new String();
						for (StatusDetail detail:result.getStatus().getStatusDetail()){
							errorMessage+=i+". "+detail.getMessage()+"\r\n";
							i++;
						}
						throw new NetsuiteOperationException(errorMessage, details);
					}
					pageIndex++;
				}
			}		
		} else {
			int i = 1;	
			errorMessage = new String();
			for (StatusDetail detail:result.getStatus().getStatusDetail()){
				errorMessage+=i+". "+detail.getMessage()+"\r\n";
				i++;
			}
			throw new NetsuiteOperationException(errorMessage, details);
		}		
		return proccessSearchResultsForInventoryUpdate(searchRowListList, internalIdtoLineItemDAOMap);
	}
	
	private Map<String,InventoryPojo> wrapSearchResultsForInventorySearch(List<SearchRowList> searchRowListList) {		
		Map<String,InventoryPojo> intIdToRecordMap = new HashMap<>();
		for (SearchRowList srList:searchRowListList){
			for (SearchRow sr: srList.getSearchRow()){
				if (sr instanceof ItemSearchRow){
					ItemSearchRow isr = (ItemSearchRow)sr;
					ItemSearchRowBasic isrb = isr.getBasic();
					LocationSearchRowBasic locationJoin = isr.getInventoryLocationJoin();
					String internalId = isrb.getInternalId()[0].getSearchValue().getInternalId();
					String type = isrb.getType()[0].getSearchValue();
					if (type.equalsIgnoreCase("_kit")){
						if (intIdToRecordMap.containsKey(internalId)){
							InventoryKitPojo kitPojo = (InventoryKitPojo) intIdToRecordMap.get(internalId);
							InventoryKitSubItemPojo subItem = new InventoryKitSubItemPojo();
							subItem.setQtyInKit(isrb.getMemberQuantity()[0].getSearchValue());
							subItem.setInternalId(isrb.getMemberItem()[0].getSearchValue().getInternalId());							
							kitPojo.getSubItemsList().add(subItem);
						} else {							
							InventoryKitPojo kitPojo = new InventoryKitPojo();
							//kitPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							kitPojo.setInternalId(internalId);
							kitPojo.setUPC(isrb.getItemId()[0].getSearchValue());
							//kitPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());
							kitPojo.setDescription(isrb.getSalesDescription()[0].getSearchValue());
							kitPojo.setSubItemsList(new ArrayList<InventoryKitSubItemPojo>());
							InventoryKitSubItemPojo subItem = new InventoryKitSubItemPojo();
							subItem.setQtyInKit(isrb.getMemberQuantity()[0].getSearchValue());
							subItem.setInternalId(isrb.getMemberItem()[0].getSearchValue().getInternalId());							
							kitPojo.getSubItemsList().add(subItem);
							if (isrb.getLocation()!=null){
								kitPojo.setNsLocationId(isrb.getLocation()[0].getSearchValue().getInternalId());
							} else {
								CHIntegrationError error = new CHIntegrationError();
								error.setErrorMessage("Location is 'NULL' for Kit item (internalId='"+internalId+"') . It will be excluded from vendors daily inventory update file.");
								ErrorsCollector.addCommonErrorMessage(error);
								continue;
							}	
							intIdToRecordMap.put(internalId, kitPojo);
						}
					} else if (type.equalsIgnoreCase("_inventoryItem")){
						if (intIdToRecordMap.containsKey(internalId)){	
							InventoryItemPojo itemPojo = (InventoryItemPojo) intIdToRecordMap.get(internalId);							
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}							
							itemPojo.getLocQtyList().add(locQty);
						} else {							
							InventoryItemPojo itemPojo = new InventoryItemPojo();
							//itemPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							itemPojo.setInternalId(internalId);
							//itemPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());
							itemPojo.setDescription(isrb.getPurchaseDescription()[0].getSearchValue());
							itemPojo.setUPC(isrb.getUpcCode()[0].getSearchValue());							
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}
							itemPojo.setLocQtyList(new ArrayList<LocationQuantitiesAvailiable>());
							itemPojo.getLocQtyList().add(locQty);
							if (isrb.getPreferredLocation()!=null){
								itemPojo.setPreferedLocationId(isrb.getPreferredLocation()[0].getSearchValue().getInternalId());
							} else {								
								CHIntegrationError error = new CHIntegrationError();
								error.setErrorMessage("PrefferedLocation is 'NULL' for Inventory item (internalId='"+internalId+"') . It will be excluded from vendors daily inventory update file.");
								ErrorsCollector.addCommonErrorMessage(error);
								continue;
							}	
							intIdToRecordMap.put(internalId, itemPojo);
						}
					}					
				}
			}
		}
		return intIdToRecordMap;
	}

	//here we separate search results for combined search and make list of nested Inventory items (inside KIT/PACKAGE items) that are required to load data from NS
	private Collection<InventoryPojo> proccessSearchResultsForInventoryUpdate(List<SearchRowList> searchRowListList , Map<String,VendorSkuToModelNumMapDAO> internalIdtolineItemDAOMap) throws NetsuiteOperationException{
		Map<String, InventoryPojo> internalIdToInventoryPojoMap = null;
		 Set<String> iiIdsToPreloadSet = new HashSet<String>();			
		 Map<String,InventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap = new HashMap<>();		
		 
		 internalIdToInventoryPojoMap = wrapSearchResultsForInventorySearch(searchRowListList);			
		 
		 for (InventoryPojo invPojo:internalIdToInventoryPojoMap.values()){
			 if (invPojo instanceof InventoryKitPojo){
				 InventoryKitPojo kitPojo = (InventoryKitPojo)invPojo;				 
				 for (InventoryKitSubItemPojo subItem:kitPojo.getSubItemsList()){
					 if (!internalIdToInventoryPojoMap.containsKey(subItem.getInternalId())){
						 iiIdsToPreloadSet.add(subItem.getInternalId());
					 }
				 }			 
			 } else if (invPojo instanceof InventoryItemPojo){
				 InventoryItemPojo invItemPojo = (InventoryItemPojo)invPojo;
				 internalIdToHouzInventoryItemPojoFullMap.put(invItemPojo.getInternalId(), invItemPojo);
			 }
		 }
		 if (iiIdsToPreloadSet.size()>0){			
			 loadKitPackageInventory(iiIdsToPreloadSet, internalIdToHouzInventoryItemPojoFullMap);
		 }	
		 for (InventoryPojo invPojo:internalIdToInventoryPojoMap.values()){
			 if (invPojo instanceof InventoryKitPojo){
				 InventoryKitPojo kitPojo = (InventoryKitPojo)invPojo;				
				 for (InventoryKitSubItemPojo invSubItemPojo:kitPojo.getSubItemsList()){
					 InventoryItemPojo apropriateNsInventory = internalIdToHouzInventoryItemPojoFullMap.get(invSubItemPojo.getInternalId());				 
					 for (LocationQuantitiesAvailiable location:apropriateNsInventory.getLocQtyList()){
						 if (location.getLocationInternalId().equalsIgnoreCase(kitPojo.getNsLocationId())){
							 invSubItemPojo.setQtyAvailiable(location.getLocationQtyAvailiable());
						 }
					 }							
					// invSubItemPojo.setInactive(apropriateNsInventory.isInactive());						
				 }
			  }			
		 } 		
		for (Entry<String,InventoryPojo> invPojo:internalIdToInventoryPojoMap.entrySet()){ 
		 if (internalIdtolineItemDAOMap.containsKey(invPojo.getKey())){
			 invPojo.getValue().setVendorSKU(internalIdtolineItemDAOMap.get(invPojo.getKey()).getModelNum());
		 } 
		}
		return internalIdToInventoryPojoMap.values();
	}

	// here we make another one request to NS for items contains in KIT/PACKAGE
	private void loadKitPackageInventory(Set<String> internalIds, Map<String,InventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap) throws NetsuiteOperationException{
		SearchResult result=null;
		List<SearchRowList> rowListList = new ArrayList<>();
		if (internalIds.size()>0){			
			String[] idsArray = internalIds.toArray(new String[internalIds.size()]);
			RecordRef[] inventoriesRefs = new RecordRef[idsArray.length];
			for (int i=0; i<idsArray.length;i++){				
				inventoriesRefs[i] = new RecordRef(null,idsArray[i],null,RecordType.inventoryItem);
			}		
			ItemSearchBasic itembasic = new ItemSearchBasic();
			ItemSearch is = new ItemSearch();		
			is.setBasic(itembasic);		
			itembasic.setInternalId(new SearchMultiSelectField(inventoriesRefs, SearchMultiSelectFieldOperator.anyOf));	
			ItemSearchAdvanced itemAdv = new ItemSearchAdvanced();
			itemAdv.setCriteria(is);
			ItemSearchRow itemRow = new ItemSearchRow();
			ItemSearchRowBasic basicRow= new ItemSearchRowBasic();
			basicRow.setType(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
			basicRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
			basicRow.setPreferredLocation(new SearchColumnSelectField[]{new SearchColumnSelectField()});			
			basicRow.setLocationQuantityAvailable(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
			basicRow.setIsInactive(new SearchColumnBooleanField[]{new SearchColumnBooleanField()});
			basicRow.setBasePrice(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});			
			basicRow.setUpcCode(new SearchColumnStringField[]{new SearchColumnStringField()});
			basicRow.setPurchaseDescription(new SearchColumnStringField[]{new SearchColumnStringField()});
			LocationSearchRowBasic locRow = new LocationSearchRowBasic();
			locRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
			itemRow.setInventoryLocationJoin(locRow);
			itemRow.setBasic(basicRow);
			itemAdv.setColumns(itemRow);	
			NSRrequestDetails details = new NSRrequestDetails();
			details.setRequestType(SoapMessageType.SEARCH);		
			int pageIndex=2;	
			boolean exceptionHasBeenThrown = false;
			String errorMessage = null;
			try {
				prepeareNSBindingStub();
				result = getNetSuiteStub().search(itemAdv);
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
			if(null != result && result.getStatus().isIsSuccess() && result.getTotalRecords() > 0){
				rowListList.add(result.getSearchRowList());
				int totalPages = result.getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchId();
					while(pageIndex <= totalPages){
						details = new NSRrequestDetails();
						details.setRequestType(SoapMessageType.SEARCH);					
						exceptionHasBeenThrown = false;
						errorMessage = null;
						try {
							prepeareNSBindingStub();
							result = getNetSuiteStub().searchMoreWithId(searchId, pageIndex);
						} catch (RemoteException  e) {
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
						if(null != result && result.getStatus().isIsSuccess() && result.getTotalRecords() > 0){
							rowListList.add(result.getSearchRowList());
						} else {
							int i = 1;	
							errorMessage = new String();
							for (StatusDetail detail:result.getStatus().getStatusDetail()){
								errorMessage+=i+". "+detail.getMessage()+"\r\n";
								i++;
							}
							throw new NetsuiteOperationException(errorMessage, details);
						}
						pageIndex++;
					}
				}		
			} else {
				int i = 1;	
				errorMessage = new String();
				for (StatusDetail detail:result.getStatus().getStatusDetail()){
					errorMessage+=i+". "+detail.getMessage()+"\r\n";
					i++;
				}
				throw new NetsuiteOperationException(errorMessage, details);
			}		
		}
		processSearchResultsFromKitPackageInventory(rowListList, internalIdToHouzInventoryItemPojoFullMap);	
	}
	
	//here we process search results for loadKitPackageInventory()
	private void processSearchResultsFromKitPackageInventory(List<SearchRowList> rowListList, Map<String,InventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap){		
		for (SearchRowList srList:rowListList){
			for (SearchRow sr: srList.getSearchRow()){
				if (sr instanceof ItemSearchRow){
					ItemSearchRow isr = (ItemSearchRow)sr;
					ItemSearchRowBasic isrb = isr.getBasic();
					LocationSearchRowBasic locationJoin = isr.getInventoryLocationJoin();					
					String internalId = isrb.getInternalId()[0].getSearchValue().getInternalId();
					String type = isrb.getType()[0].getSearchValue();
					if (type.equalsIgnoreCase("_inventoryItem")){
						if (internalIdToHouzInventoryItemPojoFullMap.containsKey(internalId)){								
							InventoryItemPojo itemPojo = (InventoryItemPojo) internalIdToHouzInventoryItemPojoFullMap.get(internalId);							
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}												
							itemPojo.getLocQtyList().add(locQty);
						} else {
							InventoryItemPojo itemPojo = new InventoryItemPojo();
							//itemPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							itemPojo.setInternalId(internalId);
							//itemPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());
							if (isrb.getUpcCode()!=null){
								itemPojo.setUPC(isrb.getUpcCode()[0].getSearchValue());	
							}													
							itemPojo.setDescription(isrb.getPurchaseDescription()[0].getSearchValue());
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());							
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}														
							itemPojo.setLocQtyList(new ArrayList<LocationQuantitiesAvailiable>());
							itemPojo.getLocQtyList().add(locQty);
							if (isrb.getPreferredLocation()!=null){
								itemPojo.setPreferedLocationId(isrb.getPreferredLocation()[0].getSearchValue().getInternalId());
							}
							internalIdToHouzInventoryItemPojoFullMap.put(internalId, itemPojo);
						}
					}					
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
