package com.malkos.poppin.transport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.expr.Instanceof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.documents.PoDocument;
import com.malkos.poppin.documents.PoDocumentItem;
import com.malkos.poppin.entities.AddCustomerResultsPojo;
import com.malkos.poppin.entities.AddSalesOrderResultPojo;
import com.malkos.poppin.entities.ErrorMessageWraped;
import com.malkos.poppin.entities.FulfillmentItemPojo;
import com.malkos.poppin.entities.FulfillmentPojo;
import com.malkos.poppin.entities.InventoryItemPojo;
import com.malkos.poppin.entities.InventoryKitPojo;
import com.malkos.poppin.entities.InventoryKitSubItemPojo;
import com.malkos.poppin.entities.InvoiceItemPojo;
import com.malkos.poppin.entities.InvoicePojo;
import com.malkos.poppin.entities.OrderErrorDetails;
import com.malkos.poppin.entities.PurchaseOrderPreloadedFieldsPojo;
import com.malkos.poppin.entities.SPSIntegrationError;
import com.malkos.poppin.entities.SearchResultWrapped;
import com.malkos.poppin.entities.ShippingAddressPojo;
import com.malkos.poppin.entities.WriteResponseListWrapped;
import com.malkos.poppin.entities.WriteResponseWrapped;
import com.malkos.poppin.entities.enums.NSSalesOrderStatus;
import com.malkos.poppin.entities.enums.UnprocessibleOrdersType;
import com.malkos.poppin.entities.OrderItemPojo;
import com.malkos.poppin.entities.PackagePojo;
import com.malkos.poppin.integration.retailers.RetailerAbstract;
import com.malkos.poppin.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.persistence.dao.PurchaseOrderDAO;
import com.malkos.poppin.util.ErrorMessageWrapper;
import com.malkos.poppin.util.ErrorsCollector;
import com.netsuite.webservices.lists.accounting_2014_2.InventoryItem;
import com.netsuite.webservices.lists.accounting_2014_2.InventoryItemLocations;
import com.netsuite.webservices.lists.accounting_2014_2.ItemMember;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearch;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearchAdvanced;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearchRow;
import com.netsuite.webservices.lists.accounting_2014_2.KitItem;
import com.netsuite.webservices.lists.relationships_2014_2.Customer;
import com.netsuite.webservices.lists.relationships_2014_2.CustomerAddressbook;
import com.netsuite.webservices.lists.relationships_2014_2.CustomerAddressbookList;
import com.netsuite.webservices.lists.relationships_2014_2.CustomerSearch;
import com.netsuite.webservices.lists.relationships_2014_2.CustomerSearchAdvanced;
import com.netsuite.webservices.lists.relationships_2014_2.CustomerSearchRow;
import com.netsuite.webservices.platform.common_2014_2.Address;
import com.netsuite.webservices.platform.common_2014_2.CustomerSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.CustomerSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_2.InventoryDetailSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.ItemSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.ItemSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_2.LocationSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.TransactionSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.TransactionSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_2.types.Country;
import com.netsuite.webservices.platform.core_2014_2.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.CustomFieldList;
import com.netsuite.webservices.platform.core_2014_2.CustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.GetItemAvailabilityResult;
import com.netsuite.webservices.platform.core_2014_2.Record;
import com.netsuite.webservices.platform.core_2014_2.RecordList;
import com.netsuite.webservices.platform.core_2014_2.RecordRef;
import com.netsuite.webservices.platform.core_2014_2.SearchBooleanField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnBooleanField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnCustomField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnCustomFieldList;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnDateField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnDoubleField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnEnumSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnLongField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnStringCustomField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnStringField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnTextNumberField;
import com.netsuite.webservices.platform.core_2014_2.SearchDoubleField;
import com.netsuite.webservices.platform.core_2014_2.SearchEnumMultiSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchLongField;
import com.netsuite.webservices.platform.core_2014_2.SearchMultiSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchResult;
import com.netsuite.webservices.platform.core_2014_2.SearchRow;
import com.netsuite.webservices.platform.core_2014_2.SearchRowList;
import com.netsuite.webservices.platform.core_2014_2.SearchStringField;
import com.netsuite.webservices.platform.core_2014_2.SearchTextNumberField;
import com.netsuite.webservices.platform.core_2014_2.StatusDetail;
import com.netsuite.webservices.platform.core_2014_2.StringCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.types.RecordType;
import com.netsuite.webservices.platform.core_2014_2.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchLongFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchTextNumberFieldOperator;
import com.netsuite.webservices.platform.messages_2014_2.WriteResponse;
import com.netsuite.webservices.platform.messages_2014_2.WriteResponseList;
import com.netsuite.webservices.setup.customization_2014_2.CustomRecord;
import com.netsuite.webservices.setup.customization_2014_2.types.CustomizationFilterCompareType;
import com.netsuite.webservices.transactions.sales_2014_2.ItemFulfillment;
import com.netsuite.webservices.transactions.sales_2014_2.ItemFulfillmentItem;
import com.netsuite.webservices.transactions.sales_2014_2.ItemFulfillmentPackage;
import com.netsuite.webservices.transactions.sales_2014_2.ItemFulfillmentPackageList;
import com.netsuite.webservices.transactions.sales_2014_2.SalesOrder;
import com.netsuite.webservices.transactions.sales_2014_2.SalesOrderItem;
import com.netsuite.webservices.transactions.sales_2014_2.SalesOrderItemList;
import com.netsuite.webservices.transactions.sales_2014_2.TransactionSearch;
import com.netsuite.webservices.transactions.sales_2014_2.TransactionSearchAdvanced;
import com.netsuite.webservices.transactions.sales_2014_2.TransactionSearchRow;
import com.netsuite.webservices.transactions.sales_2014_2.types.SalesOrderOrderStatus;
import com.netsuite.webservices.transactions.sales_2014_2.types.TransactionStatus;
import com.netsuite.webservices.transactions.sales_2014_2.types.TransactionType;

public class NetsuiteOperationsManager implements INetsuiteOperationsManager {
	
	private static Logger logger = LoggerFactory.getLogger(NetsuiteOperationsManager.class);
	
	private NetsuiteService netsuiteService;

	public NetsuiteService getNetsuiteService() {
		return netsuiteService;
	}

	public void setNetsuiteService(NetsuiteService netsilteClient) {
		this.netsuiteService = netsilteClient;
	}
	
	@Override
	public Map<String, String> loadProcessedPurchaseOrderNumbers(long lastProcessedPoSalesOrderInternalId) throws NetsuiteOperationException {
		logger.info("Preparing transaction search.");
		Map<String, String> processedPoNumberToSoInteranIdMap = new HashMap<String, String>();
		
		TransactionSearchAdvanced advanced = new TransactionSearchAdvanced();
		TransactionSearch search = new TransactionSearch();
		TransactionSearchBasic basic = new TransactionSearchBasic();
		TransactionSearchRow tsRow = new TransactionSearchRow();
		TransactionSearchRowBasic rowBasic = new TransactionSearchRowBasic();
		
		
		basic.setType(new SearchEnumMultiSelectField(new String[] { "_salesOrder" }, SearchEnumMultiSelectFieldOperator.anyOf));
		basic.setAccount(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "54", null, null) }, SearchMultiSelectFieldOperator.anyOf));
		basic.setRecordType(new SearchStringField(RecordType._salesOrder , SearchStringFieldOperator.is));
		//basic.setLocation(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "4", null, null) }, SearchMultiSelectFieldOperator.anyOf));
		basic.setOtherRefNum(new SearchTextNumberField(null, null, SearchTextNumberFieldOperator.notEmpty));
		basic.setInternalIdNumber(new SearchLongField(lastProcessedPoSalesOrderInternalId, null, SearchLongFieldOperator.greaterThan));
		search.setBasic(basic);
		
		rowBasic.setOtherRefNum(new SearchColumnTextNumberField[]{new SearchColumnTextNumberField()});
		rowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		tsRow.setBasic(rowBasic);
		
		advanced.setColumns(tsRow);
		List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();
		SearchResultWrapped result = null;
		
		advanced.setCriteria(search);
		//start searchMore from pageIndex 2 because we will search on pageIndex 1 on basic search
		int pageIndex = 2;
		logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
		try {
			result = netsuiteService.search(advanced, true);
		
			searchResultList.add(result.getSearchResult().getSearchRowList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, true);
					searchResultList.add(result.getSearchResult().getSearchRowList());
					pageIndex++;
				}
		}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}
		if(result.getSearchResult().getTotalRecords() > 0){
			for(SearchRowList searchRowList : searchResultList){
				SearchRow[] searchedRows = searchRowList.getSearchRow();
				for(SearchRow searchedRow : searchedRows){
					TransactionSearchRow row = (TransactionSearchRow) searchedRow;
					String poNumber = row.getBasic().getOtherRefNum(0).getSearchValue();
					RecordRef soRecord = row.getBasic().getInternalId(0).getSearchValue();
					String soInternalId = soRecord.getInternalId();
					processedPoNumberToSoInteranIdMap.put(poNumber, soInternalId);
				}
			}
		}
		return processedPoNumberToSoInteranIdMap;
	}

	@Override
	public Map<String, String> loadInventoryMapping() throws NetsuiteOperationException{
			logger.info("Preparing inventory search.");	
			Map<String, String> inventoryVendorSKUtoIntenrlIdMap = new HashMap<String, String>();
			
			ItemSearchAdvanced itemSearchAdvanced = new ItemSearchAdvanced();
			
			ItemSearchBasic searchBasic = new ItemSearchBasic();
			ItemSearch itemSearch = new ItemSearch();
			ItemSearchRow itemSearchRow = new ItemSearchRow();
			ItemSearchRowBasic itemSearchRowBasic = new ItemSearchRowBasic();
			
			//searchBasic.setType(new SearchEnumMultiSelectField(new String[] {"_inventoryItem"}, SearchEnumMultiSelectFieldOperator.anyOf));
			//searchBasic.setPreferredLocation(new SearchMultiSelectField(new RecordRef[]{new RecordRef(null, "4", null, null)}, SearchMultiSelectFieldOperator.anyOf));
			
			itemSearchRowBasic.setInternalId(new SearchColumnSelectField[] {new SearchColumnSelectField()});
			itemSearchRowBasic.setUpcCode(new SearchColumnStringField[] {new SearchColumnStringField()});
			
			itemSearch.setBasic(searchBasic);
			itemSearchRow.setBasic(itemSearchRowBasic);
			
			itemSearchAdvanced.setColumns(itemSearchRow);
			itemSearchAdvanced.setCriteria(itemSearch);
			
			SearchResultWrapped result = null;
			List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();
			
			logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
			//start searchMore from pageIndex 2 because we will search on pageIndex 1 on basic search
			int pageIndex = 2;
			try {
				result = netsuiteService.search(itemSearchAdvanced, false);
				searchResultList.add(result.getSearchResult().getSearchRowList());
				int totalPages = result.getSearchResult().getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchResult().getSearchId();
					while(pageIndex <= totalPages){
						result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
						searchResultList.add(result.getSearchResult().getSearchRowList());
						pageIndex++;
					}
				}
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}
			if(result.getSearchResult().getTotalRecords() > 0){
				for(SearchRowList searchRowList : searchResultList){
					SearchRow[] searchedRows = searchRowList.getSearchRow();
					for(SearchRow searchedRow : searchedRows){
						ItemSearchRow item = (ItemSearchRow)searchedRow;
						ItemSearchRowBasic itemBasic = item.getBasic();
						if(null != itemBasic){
							SearchColumnSelectField[] internalIdField = itemBasic.getInternalId();
							SearchColumnStringField[] upcCodeField = itemBasic.getUpcCode();
							if(internalIdField != null && upcCodeField != null){
								String internalId =internalIdField[0].getSearchValue().getInternalId();
								String vendorSKU = upcCodeField[0].getSearchValue();
								inventoryVendorSKUtoIntenrlIdMap.put(vendorSKU, internalId);
							}
						}
					}
				}
			}
		return inventoryVendorSKUtoIntenrlIdMap;
	}

	private Map<String, String> searchAddedCustomersShippingAddressInternalId(List<String> customersInternalIdList, String messageDetails) throws NetsuiteOperationException{
		logger.info("Searching for Customer InternalIDs/Shipping Address InternalIDs for just added customers.");
		Map<String,String> customerIntenalIdToShippingAddressInternalId = new HashMap<String, String>();		
		RecordRef[] customers = new RecordRef[customersInternalIdList.size()];
		int index = 0;
		for(String customerInternalId : customersInternalIdList){
			customers[index] = new RecordRef(null, customerInternalId, null, null);
			index++;
		}
		logger.info("Preparing customer search.");
		CustomerSearchAdvanced advanced = new CustomerSearchAdvanced();
		CustomerSearchBasic basic = new CustomerSearchBasic();
		CustomerSearch search = new CustomerSearch();
		CustomerSearchRowBasic rowBasic = new CustomerSearchRowBasic();
		CustomerSearchRow rowSearch = new CustomerSearchRow();
		
		basic.setInternalId(new SearchMultiSelectField(customers, SearchMultiSelectFieldOperator.anyOf));
		basic.setIsDefaultShipping(new SearchBooleanField(true));
		search.setBasic(basic);
		
		rowBasic.setAddressInternalId(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		
		rowSearch.setBasic(rowBasic);
		advanced.setCriteria(search);
		advanced.setColumns(rowSearch);
		
		SearchResultWrapped result = null;
		List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();
		int pageIndex = 2;
		logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
		try {
			result = netsuiteService.search(advanced, true, messageDetails);
			String basicExceptionMessage = "Cannot proccess PO's during PO flow. Reason: ";
			checkSearchResult(result, basicExceptionMessage);
			if (result.getSearchResult().getSearchRowList().getSearchRow()!=null)
				searchResultList.add(result.getSearchResult().getSearchRowList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, true, messageDetails);
					checkSearchResult(result, basicExceptionMessage);
					if (result.getSearchResult().getSearchRowList().getSearchRow()!=null)
						searchResultList.add(result.getSearchResult().getSearchRowList());
					pageIndex++;
			}
		}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}
		for(SearchRowList srList : searchResultList){
			for(SearchRow sr : srList.getSearchRow()){
				CustomerSearchRow row = (CustomerSearchRow) sr;
				if(null != row){
					String internalId = null;
					SearchColumnSelectField[] internalIdField = row.getBasic().getInternalId();
					if(internalIdField.length > 0)
						internalId = internalIdField[0].getSearchValue().getInternalId();
					
					String addressInternalId = null;
					SearchColumnStringField[] addressIdField = row.getBasic().getAddressInternalId();
					if(addressIdField.length > 0)
						addressInternalId = addressIdField[0].getSearchValue();
					if(null != internalId && null != addressInternalId)
						customerIntenalIdToShippingAddressInternalId.put(internalId, addressInternalId);
				}
			}
		}
		
		return customerIntenalIdToShippingAddressInternalId;
	}

	@Override
	public List<AddCustomerResultsPojo> addCustomers(List<PoDocument> poDocumentList, RetailerAbstract retailer) throws NetsuiteOperationException {
		logger.info("Preparing customers to add.");
		List<Customer> customerList = new ArrayList<Customer>();
		List<String> customersInternalIdList = new ArrayList<String>();
		for(PoDocument document : poDocumentList){
			Customer customer = prepareCustomer(document, retailer);
			customerList.add(customer);
		}
		List<Record[]> recordsToAdd = new ArrayList<Record[]>();
		int lastSublistFromIndex = 0;
		//split Customers List on bunches of 100 records
		for(int i = 0; i < customerList.size(); i++ ){
			if(i == customerList.size() - 1){
				List<Customer> subList = customerList.subList(lastSublistFromIndex, customerList.size());
				Record[] recToAdd = new Record[subList.size()];
				subList.toArray(recToAdd);
				recordsToAdd.add(recToAdd);
			}
			else if(i / 100 == 99){
				List<Customer> subList = customerList.subList(i - 99, i + 1);
				Record[] recToAdd = new Record[subList.size()];
				recordsToAdd.add(recToAdd);
				lastSublistFromIndex = i + 1;
			}
		}
		List<WriteResponseListWrapped> writeResponseList = new ArrayList<WriteResponseListWrapped>();
		logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
		for(Record[] rec : recordsToAdd){
			try {
				writeResponseList.add(netsuiteService.addList(rec,retailer.getShortName()));
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}
		}
		List<AddCustomerResultsPojo> salesOrderMappingCustomerDetailsPojoList = new ArrayList<AddCustomerResultsPojo>();
		Map<String, AddCustomerResultsPojo> customerInternalIdTpsalesOrderMappingCustomerDetailsPojo = new HashMap<String, AddCustomerResultsPojo>();
		for(WriteResponseListWrapped wrl : writeResponseList){
			for(WriteResponse wr : wrl.getWriteResponseList().getWriteResponse()){
				AddCustomerResultsPojo somcd = new AddCustomerResultsPojo();
				if(wr.getStatus().isIsSuccess()){
					RecordRef addedCustomerRef = (RecordRef) wr.getBaseRef();
					somcd.setInternalId(addedCustomerRef.getInternalId());
					customerInternalIdTpsalesOrderMappingCustomerDetailsPojo.put(addedCustomerRef.getInternalId(), somcd);
					customersInternalIdList.add(addedCustomerRef.getInternalId());
				}
				else{
					somcd.setReadyForMapping(false);
					StatusDetail[] details = wr.getStatus().getStatusDetail();
					StringBuilder builder = new StringBuilder();
					String errorMessage = "Failed to add customer. Reason : ";
					builder.append(errorMessage + "\r\n");
					for (StatusDetail detail : details) {
						builder.append(detail.getMessage() + "\r\n");
					}
					somcd.setProblemDescription(builder.toString());
					throw new NetsuiteOperationException(builder.toString(),wrl.getRequestDeatils());
				}
				salesOrderMappingCustomerDetailsPojoList.add(somcd);
			}
		}
		Map<String,String> customerIntenalIdToShippingAddressInternalId = new HashMap<>();
		if (customersInternalIdList.size()>0){
			customerIntenalIdToShippingAddressInternalId = searchAddedCustomersShippingAddressInternalId(customersInternalIdList, retailer.getShortName());		
			for(Entry<String,String> entry : customerIntenalIdToShippingAddressInternalId.entrySet()){
				if(customerInternalIdTpsalesOrderMappingCustomerDetailsPojo.containsKey(entry.getKey())){
					AddCustomerResultsPojo somcd = customerInternalIdTpsalesOrderMappingCustomerDetailsPojo.get(entry.getKey());
					somcd.setShippingAddressInternalId(entry.getValue());
				}
					
			}
		}
		return salesOrderMappingCustomerDetailsPojoList;
	}

	private Customer prepareCustomer(PoDocument document, RetailerAbstract retailer) {
		Customer cust = new Customer();
		cust.setIsPerson(true);
		cust.setEmail(retailer.getCompanyEmail());
		//cust.setCompanyName(poPojo.getCustomerCompanyName());
		if(null != document.getCustomerEmail())
			cust.setComments(document.getCustomerEmail());
		//cust.setEntityId(props.getCompanyId());
		cust.setParent(new RecordRef(null, retailer.getCompanyInternalId(), null, null));
		
		String delimiter = " ";
		String firstName = null;
		String lastName = null;
		String customerName = null;
		
		if(document.getCustomerName() != null)
			customerName = document.getCustomerName();
		else
			customerName = document.getShipToName();
		
		String[] temp = customerName.split(delimiter);
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
		if(null != document.getCustomerPhone())
			cust.setPhone(document.getCustomerPhone());

		/* set Customer Address */
		List<CustomerAddressbook> listCustomerAddressbook = new ArrayList<CustomerAddressbook>();
		CustomerAddressbookList addressbookList = new CustomerAddressbookList();
		CustomerAddressbook defaultBillingAddress = new CustomerAddressbook();
		CustomerAddressbook defaultShippingAddress = new CustomerAddressbook();
		
		Address shippingAddress = new Address();
		shippingAddress.setAddressee(document.getShipToName());
		shippingAddress.setAddr1(document.getShipToAddress1());
		if (document.getShipToAddress2() != null)
			shippingAddress.setAddr2(document.getShipToAddress2());
		
		shippingAddress.setCity(document.getShipToCity());
		shippingAddress.setState(document.getShipToState());
		shippingAddress.setZip(document.getShipToPostalCode());
		if(null != document.getCustomerPhone())
			shippingAddress.setAddrPhone(document.getCustomerPhone());
		shippingAddress.setCountry(Country._unitedStates);
		defaultShippingAddress.setAddressbookAddress(shippingAddress);		
		defaultShippingAddress.setDefaultShipping(true);
		listCustomerAddressbook.add(defaultShippingAddress);
		
		Address billingAddress = new Address();
		
		if((document.getBillToName() != null) && (document.getBillToAddress1() != null) && (document.getBillToCity() != null) && (document.getBillToState() != null) 
				&& (document.getBillToPostalCode() != null)/* && (poPojo.getCustomerPhone() != null)*/){
			billingAddress.setAddressee(document.getBillToName());
			billingAddress.setAddr1(document.getBillToAddress1());
			if (document.getBillToAddress2() != null)
				billingAddress.setAddr2(document.getBillToAddress2());
			if (document.getBillToAddress3() != null)
				billingAddress.setAddr3(document.getBillToAddress3());
			
			billingAddress.setCity(document.getBillToCity());
			billingAddress.setState(document.getBillToState());
			billingAddress.setZip(document.getBillToPostalCode());
			if(null != document.getCustomerPhone())
				billingAddress.setAddrPhone(document.getCustomerPhone());
			billingAddress.setCountry(Country._unitedStates);
			defaultBillingAddress.setDefaultBilling(true);
			defaultBillingAddress.setAddressbookAddress(billingAddress);
		}
		
		if(document.getBillToName() == null)
			defaultShippingAddress.setDefaultBilling(true);
		
		listCustomerAddressbook.add(defaultBillingAddress);
		
		CustomerAddressbook[] addressbook = new CustomerAddressbook[listCustomerAddressbook.size()];
		int index = 0;
		for(CustomerAddressbook book : listCustomerAddressbook){
			addressbook[index] = book;
			index++;
		}
		addressbookList.setAddressbook(addressbook);
		cust.setAddressbookList(addressbookList);
		return cust;
	}

	@Override
	public void addSalesOrders(List<PoDocument> filteredAfterCustomerAddPurchaseOrderPojoList, RetailerAbstract retailer) throws NetsuiteOperationException {
		logger.info("Preparing Sales Orders to add.");
		List<SalesOrder> salesOrderToAdd = new ArrayList<SalesOrder>();
		for(PoDocument document : filteredAfterCustomerAddPurchaseOrderPojoList){
			salesOrderToAdd.add(prepareSalesOrder(document, retailer));
		}
		List<Record[]> recordsToAdd = new ArrayList<Record[]>();
		int lastSublistFromIndex = 0;
		//split Customers List on bunches of 30 records
		for(int i = 0; i < salesOrderToAdd.size(); i++ ){
			if(i == salesOrderToAdd.size() - 1){
				List<SalesOrder> subList = salesOrderToAdd.subList(lastSublistFromIndex, salesOrderToAdd.size());
				Record[] recToAdd = new Record[subList.size()];
				subList.toArray(recToAdd);
				recordsToAdd.add(recToAdd);
			}
			else if(i != 0 && i % 30 == 0){
				List<SalesOrder> subList = salesOrderToAdd.subList(i - 30, i);
				Record[] recToAdd = new Record[subList.size()];
				subList.toArray(recToAdd);
				recordsToAdd.add(recToAdd);
				lastSublistFromIndex = i;
			}
		}
		List<WriteResponseListWrapped> writeResponseListsList = new ArrayList<WriteResponseListWrapped>();
		List<WriteResponseWrapped> writeResponseList = new ArrayList<WriteResponseWrapped>();
		logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
		for(Record[] rec : recordsToAdd){
			try {
				writeResponseListsList.add(netsuiteService.addList(rec,retailer.getShortName()));
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}
		}
		for(WriteResponseListWrapped wrl : writeResponseListsList){			
			for(WriteResponse wr : wrl.getWriteResponseList().getWriteResponse()){
				WriteResponseWrapped wrWrapped = new WriteResponseWrapped(wrl.getRequestDeatils(),wr);
				writeResponseList.add(wrWrapped);
			}
		}
		int writeResponseLoopCounter = 0;
		for(WriteResponseWrapped wr : writeResponseList){
			PoDocument doc = filteredAfterCustomerAddPurchaseOrderPojoList.get(writeResponseLoopCounter);
			if (doc.getErrorDetails() == null){
				doc.setErrorDetails(new OrderErrorDetails());
			}
			doc.getErrorDetails().setRequestFilePath(wr.getRequestDeatils().getRequestFilePath());
			doc.getErrorDetails().setResponseFilePath(wr.getRequestDeatils().getResponseFilePath());
			doc.getErrorDetails().setRequestType(wr.getRequestDeatils().getRequestType().toString());	
			doc.getErrorDetails().setRequestTime(wr.getRequestDeatils().getRequestDateTime());
			AddSalesOrderResultPojo addSalesOrderResultPojo = doc.getAddSalesOrderResultPojo();			
			if(wr.getWriteResponse().getStatus().isIsSuccess()){
				RecordRef addedSalesOrderRef = (RecordRef) wr.getWriteResponse().getBaseRef();
				addSalesOrderResultPojo.setSoInternalId(addedSalesOrderRef.getInternalId());
				addSalesOrderResultPojo.setAddedSuccessifully(true);
			}
			else{
				StatusDetail[] details = wr.getWriteResponse().getStatus().getStatusDetail();
				StringBuilder builder = new StringBuilder();
				String errorMessage = "Failed to add Sales Order. Reason : ";
				builder.append(errorMessage + "\r\n");
				for (StatusDetail detail : details) {
					builder.append(detail.getMessage() + "\r\n");
				}
				addSalesOrderResultPojo.setAddedSuccessifully(false);
				addSalesOrderResultPojo.setProblemDescription(builder.toString());				
			}
			writeResponseLoopCounter++;
		}
		
	}

	private SalesOrder prepareSalesOrder(PoDocument document,RetailerAbstract retailer) {
		
		SalesOrder order = new SalesOrder();
		
		AddCustomerResultsPojo addCustomerResultsPojo = document.getAddSalesOrderResultPojo().getAddCustomerResultsPojo();

		/* set cutomer */
		RecordRef customerRef = new RecordRef();
		customerRef.setInternalId(addCustomerResultsPojo.getInternalId());
		order.setEntity(customerRef);

		/* set Customer order # */
		CustomFieldList customBodyList = new CustomFieldList();
		//CustomFieldRef[] customBodyFields = new CustomFieldRef[3];
		CustomFieldRef[] customBodyFields = new CustomFieldRef[1];
		customBodyFields[0] = new BooleanCustomFieldRef(null, "custbody_is_urgent", true);
		//customBodyFields[0] = new StringCustomFieldRef("custbody14",Integer.toString(poPojo.getOrderId()));
		//customBodyFields[1] = new BooleanCustomFieldRef("custbody_is_urgent", true);
		//customBodyFields[2] = new StringCustomFieldRef("custbodypartner_person_place_id", poPojo.getShipToPartnerPersonPlaceId());
		customBodyList.setCustomField(customBodyFields);
		order.setCustomFieldList(customBodyList);
		
		//order.setTerms(new RecordRef(null, "90", null, RecordType.term));
		

		Calendar createdDate = Calendar.getInstance();
		createdDate.setTime(document.getPoDate());
		createdDate.set(Calendar.HOUR, 23);
		
		/* set PO# */
		order.setOtherRefNum(document.getPoNumber());
		order.setTranDate(createdDate);

		/* set Status */
		order.setOrderStatus(retailer.getSalesOrderStatus());
		/* set department */
		RecordRef department = new RecordRef();
		department.setInternalId(retailer.getDepartmentInternalId());
		order.setDepartment(department);
		
		//ship method
		RecordRef shipMethod = new RecordRef();
		shipMethod.setInternalId(retailer.getShippingMethodInternalId());
		order.setShipMethod(shipMethod);
		order.setShippingCost(0.00);
		
		
		order.setIsTaxable(false);
		order.setTerms(new RecordRef(null, retailer.getTermInternalId(), null, RecordType.term));
		
		/* set Shipping Address */
		order.setShipAddressList(new RecordRef("shipaddresslist", addCustomerResultsPojo.getShippingAddressInternalId(), null, null));
		if (addCustomerResultsPojo.getBillingAddressInternalId() != null){
			order.setBillAddressList(new RecordRef("billaddresslist", addCustomerResultsPojo.getBillingAddressInternalId(), null, null));
		}
		
		SalesOrderItemList itemList = new SalesOrderItemList();
		List<SalesOrderItem> salesOrderItemList = new ArrayList<SalesOrderItem>();
		DecimalFormat doublePrecision = new DecimalFormat("#.##");

		for (PoDocumentItem orderItemPo : document.getPoDocumentItemList()) {

			SalesOrderItem orderItem = new SalesOrderItem();
			
			RecordRef item = new RecordRef();
			/* set SKU */
			item.setInternalId(orderItemPo.getItemInternalId());

			/* vendor line # , merchant_SKU*/
			CustomFieldList customList = new CustomFieldList();
			List<CustomFieldRef> customFieldRefList = new ArrayList<CustomFieldRef>();
			customFieldRefList.add(new StringCustomFieldRef(null, "custcol11", orderItemPo.getVendorlineNumber()));
			
			if(orderItemPo.getMerchantSKU() != null)
				customFieldRefList.add(new StringCustomFieldRef(null, "custcolmerchant_sku", orderItemPo.getMerchantSKU()));
			CustomFieldRef[] customFields = new CustomFieldRef[customFieldRefList.size()];
			customFieldRefList.toArray(customFields);
			
			customList.setCustomField(customFields);
			orderItem.setCustomFieldList(customList);
			
			orderItem.setItem(item);
			RecordRef r = new RecordRef();
			r.setInternalId("-1");
			orderItem.setPrice(r);
			
			/* set Quantity */
			orderItem.setQuantity(orderItemPo.getOrderQty());
			
			String unitPriceRoundedAsString = doublePrecision.format(orderItemPo.getUnitPrice());
			double unitPriceRounded = Double.valueOf(unitPriceRoundedAsString);
			//orderItem.setRate(Float.toString(orderItemPo.getUnitCost()));
			//orderItem.setRate(Double.toString(orderItemPo.getUnitPrice()));
			orderItem.setRate(unitPriceRoundedAsString);
			
			orderItem.setAmount((double) (orderItemPo.getOrderQty() * unitPriceRounded));

			/* set desc */ 
			//orderItem.setDescription(orderItemPo.getDescription());
			
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
	public List<FulfillmentPojo> getReadyForASNItemFulfillments(Set<String> orderInternalIds, Set<String> proccessedFulfillmentsId)
			throws NetsuiteOperationException {		
		
		RecordRef[] ordersRef = new RecordRef[orderInternalIds.size()];
		RecordRef[] fulfillmentsRef = new RecordRef[proccessedFulfillmentsId.size()] ;
		int counter = 0;
		for (String orderInternalId : orderInternalIds){
			ordersRef[counter]= new RecordRef(null,orderInternalId,null,RecordType.salesOrder);
			counter++;
		}	
		counter=0;
		for (String fulfillmentInternalId : proccessedFulfillmentsId){
			fulfillmentsRef[counter]= new RecordRef(null, fulfillmentInternalId, null, RecordType.salesOrder);
			counter++;
		}	
		
		TransactionSearch trSearch = new TransactionSearch();
		TransactionSearchBasic trBasic = new TransactionSearchBasic();
		if (fulfillmentsRef.length > 0){
			trBasic.setInternalId(new SearchMultiSelectField(fulfillmentsRef, SearchMultiSelectFieldOperator.noneOf));
		}		
		trBasic.setCreatedFrom(new SearchMultiSelectField(ordersRef, SearchMultiSelectFieldOperator.anyOf));
		trBasic.setType(new SearchEnumMultiSelectField(new String[]{TransactionType.__itemFulfillment}, SearchEnumMultiSelectFieldOperator.anyOf));				
		trBasic.setTrackingNumbers(new SearchStringField(null, SearchStringFieldOperator.notEmpty));
		trSearch.setBasic(trBasic);			
		
		SearchResultWrapped result=null;
		List<RecordList> searchResultList = new ArrayList<RecordList>();
		int pageIndex = 2;
		
		try {
			result = netsuiteService.search(trSearch, false);
			String basicException = "Failed to retrieve fulfillments for predifined Sales Orders. Reason: ";
			checkSearchResult(result, basicException);
			if(result.getSearchResult().getRecordList().getRecord() != null)
				searchResultList.add(result.getSearchResult().getRecordList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					checkSearchResult(result, basicException);
					if(result.getSearchResult().getRecordList().getRecord() != null)
						searchResultList.add(result.getSearchResult().getRecordList());
					pageIndex++;
			}
		}
		} catch (NetsuiteServiceException e) {
			String errorMessage = "Failed to retrieve fulfillments for predifined Sales Orders. Reason: " + e.getMessage();
			logger.info(errorMessage);
			throw new NetsuiteOperationException(errorMessage,e.getRequestDetails());
		}
		return getFulfillmentPojoFromSearchResultsForAsnMessage(searchResultList);
	}

	private List<FulfillmentPojo> getFulfillmentPojoFromSearchResultsForAsnMessage(List<RecordList> searchResultList){		
		List<FulfillmentPojo> fulfillmentPojoList = new ArrayList<FulfillmentPojo>();
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		if(searchResultList.size() > 0){
			for(RecordList recordList : searchResultList){
				for(Record record : recordList.getRecord()){
					ItemFulfillment fulfillment = (ItemFulfillment) record;
					if(null != fulfillment){
						FulfillmentPojo fulfillmentPojo = new FulfillmentPojo();
						
						/*fulfillmentPojo.setShipToAddress1(fulfillment.getTransactionShipAddress().getShipAddr1());
						
						if (null != fulfillment.getTransactionShipAddress().getShipAddr2()){
							fulfillmentPojo.setShipToAddress2(fulfillment.getTransactionShipAddress().getShipAddr2());
						}
						fulfillmentPojo.setShipToCity(fulfillment.getTransactionShipAddress().getShipCity());
						fulfillmentPojo.setShipToPostalCode(fulfillment.getTransactionShipAddress().getShipZip());
						fulfillmentPojo.setShipToState(fulfillment.getTransactionShipAddress().getShipState());
						fulfillmentPojo.setShipToName(fulfillment.getTransactionShipAddress().getShipAddressee());						
						
						String NSshipcountry = fulfillment.getTransactionShipAddress().getShipCountry().toString();*/
						
						fulfillmentPojo.setShipToAddress1(fulfillment.getShippingAddress().getAddr1());
						if (null != fulfillment.getShippingAddress().getAddr2()){
							fulfillmentPojo.setShipToAddress2(fulfillment.getShippingAddress().getAddr2());
						}
						fulfillmentPojo.setShipToCity(fulfillment.getShippingAddress().getCity());
						fulfillmentPojo.setShipToPostalCode(fulfillment.getShippingAddress().getZip());
						fulfillmentPojo.setShipToState(fulfillment.getShippingAddress().getState());
						fulfillmentPojo.setShipToName(fulfillment.getShippingAddress().getAddressee());						
						
						String NSshipcountry = fulfillment.getShippingAddress().getCountry().toString();
						
						String country = properties.countryMappings.containsKey(NSshipcountry) ? properties.countryMappings.get(NSshipcountry) : properties.DEFAULT_SHIPPING_COUNTRY; 
						fulfillmentPojo.setShipToCountry(country);						
						
						fulfillmentPojo.setNsInternalId(fulfillment.getInternalId());
						fulfillmentPojo.setSalesorderNsInternalId(fulfillment.getCreatedFrom().getInternalId());
						
						List<FulfillmentItemPojo> itemList = new ArrayList<FulfillmentItemPojo>();
						ItemFulfillmentPackage[] packages = null;
						if (fulfillment.getPackageList()!=null){
							packages = fulfillment.getPackageList().get_package();
						}						
						for (ItemFulfillmentItem item : fulfillment.getItemList().getItem()){
							FulfillmentItemPojo orderItem = new FulfillmentItemPojo();
							orderItem.setShipQty(item.getQuantity());
							orderItem.setItemInternalId(item.getItem().getInternalId());
							orderItem.setItemNumber(item.getOrderLine().toString());
							if (item.getCustomFieldList()!=null){
								CustomFieldRef[] itemFulfillmentsCustomFieldRefs =  item.getCustomFieldList().getCustomField();
								for (CustomFieldRef cfr : itemFulfillmentsCustomFieldRefs){
									if (cfr instanceof StringCustomFieldRef){
										StringCustomFieldRef scfr = (StringCustomFieldRef)cfr;
										if (scfr.getScriptId().equalsIgnoreCase("custcol_tracking_number")){
											orderItem.setTrackingNumber(scfr.getValue());
										}
									}								
								}
							}							
							if(null == orderItem.getTrackingNumber()){								
								fulfillmentPojo.setExceptionDescription("There is no Tracking Number for item with line number " + orderItem.getItemNumber() + " in SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
										"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
							}
							if (packages!=null){
								boolean trackNumberCorrect = false;
								for (ItemFulfillmentPackage itemPackage:packages){
									if ((orderItem.getTrackingNumber()!=null)&&(!orderItem.getTrackingNumber().isEmpty())&&
											(itemPackage.getPackageTrackingNumber()!=null)&&(!itemPackage.getPackageTrackingNumber().isEmpty())&&
											(orderItem.getTrackingNumber().equalsIgnoreCase(itemPackage.getPackageTrackingNumber()))){
										trackNumberCorrect = true;
										break;
									}
									if (!trackNumberCorrect){
										fulfillmentPojo.setExceptionDescription("There is no Tracking Number for item (or Tracking Number incorrect) with line number " + orderItem.getItemNumber() + " in SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
												"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
									}
								}	
							}
							itemList.add(orderItem);
						}
						fulfillmentPojo.setOrderItems(itemList);
						
						List<PackagePojo> packageList = new ArrayList<PackagePojo>();						
						if (packages!=null){
							for (ItemFulfillmentPackage itemPackage:packages){
								PackagePojo packagePojo = new PackagePojo();
								packagePojo.setPackageDescription(itemPackage.getPackageDescr());
								if ((itemPackage.getPackageTrackingNumber()==null)||(itemPackage.getPackageTrackingNumber().isEmpty())){
									fulfillmentPojo.setExceptionDescription("There is no Tracking Number for package in shipped SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
											"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
								}								
								packagePojo.setTrackingNumber(itemPackage.getPackageTrackingNumber());
								if ((itemPackage.getPackageWeight()==null)||(itemPackage.getPackageWeight()<=0)){
									fulfillmentPojo.setExceptionDescription("There is no Package Weight(or Package Weight incorrect) for package in shipped SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
											"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
								}
								packagePojo.setPackageWeight(itemPackage.getPackageWeight());
								packageList.add(packagePojo);
							}
						} else {
							fulfillmentPojo.setExceptionDescription("There is no any packages for shipped SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
									"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
						}
						if (packageList.isEmpty()){
							fulfillmentPojo.setExceptionDescription("There is no any packages for shipped SalesOrder order #" + fulfillmentPojo.getSalesorderNsInternalId() +". Will skip this order " +
									"and will no longer try to generate ASNs and Invoices for it - does not make sence. ");
						}						
						fulfillmentPojo.setPackageItems(packageList);						
						fulfillmentPojoList.add(fulfillmentPojo);
					}	
				}
			}
		}	
		return fulfillmentPojoList;
	}
	@Override
	public List<PurchaseOrderPreloadedFieldsPojo> preloadSOfields(Set<String> orderIdsToPreload) throws NetsuiteOperationException {
		RecordRef[] orderInternalIds = new RecordRef[orderIdsToPreload.size()];
		int counter=0;
		for (String internalId:orderIdsToPreload){
			orderInternalIds[counter]= new RecordRef(null,internalId,null,RecordType.salesOrder);
			counter++;
		}	
		
		TransactionSearchAdvanced trAdvanced = new TransactionSearchAdvanced();
		//SET CRITERIA
		TransactionSearch trCriteria= new TransactionSearch();
		TransactionSearchBasic trBasicCriteria = new TransactionSearchBasic();
	    trBasicCriteria.setAccount(new SearchMultiSelectField(new RecordRef[] { new RecordRef(null, "54", null, null) }, SearchMultiSelectFieldOperator.anyOf));
		trBasicCriteria.setInternalId(new SearchMultiSelectField(orderInternalIds,SearchMultiSelectFieldOperator.anyOf));				
		trCriteria.setBasic(trBasicCriteria);		
		trAdvanced.setCriteria(trCriteria);
		//SET COLUMNS
		TransactionSearchRow trSearchRow = new TransactionSearchRow();
		TransactionSearchRowBasic trRowBasic = new TransactionSearchRowBasic();
		trRowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		trRowBasic.setOtherRefNum(new SearchColumnTextNumberField[]{new SearchColumnTextNumberField()});		
		trRowBasic.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		trRowBasic.setDepartment(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
		trRowBasic.setStatus(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
		
		trRowBasic.setLine(new SearchColumnLongField[]{new SearchColumnLongField()});
		//trRowBasic.setLineSequenceNumber(new SearchColumnLongField[]{new SearchColumnLongField()});
		trRowBasic.setShipDate(new SearchColumnDateField[]{new SearchColumnDateField()});		
		
		trRowBasic.setQuantity(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		trRowBasic.setRate(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});		
		trRowBasic.setTranId(new SearchColumnStringField[]{new SearchColumnStringField()});
		
		SearchColumnCustomFieldList sccfl = new SearchColumnCustomFieldList();
		SearchColumnStringCustomField merchantSKU = new SearchColumnStringCustomField("1475", "custcolmerchant_sku", null,  null);
		
		SearchColumnStringCustomField vendorLineNumber = new SearchColumnStringCustomField("1471", "custcol11", null, null);
		sccfl.setCustomField(new SearchColumnCustomField[] { merchantSKU, vendorLineNumber});		
		trRowBasic.setCustomFieldList(sccfl);
		
		trSearchRow.setBasic(trRowBasic);
		//SET ITEM JOIN
		ItemSearchRowBasic itemRowBasic = new ItemSearchRowBasic();
		itemRowBasic.setUpcCode(new SearchColumnStringField[]{new SearchColumnStringField()});
		itemRowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		trSearchRow.setItemJoin(itemRowBasic);
		
		
		trAdvanced.setColumns(trSearchRow);
		
		SearchResultWrapped result=null;
		List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();		
		
		int pageIndex = 2;
		try {
			result = netsuiteService.search(trAdvanced, false);
			String basicException = "Failed to retrieve fulfillments for predifined Sales Orders. Reason: ";
			checkSearchResult(result, basicException);			
			if(result.getSearchResult().getSearchRowList().getSearchRow() != null)
				searchResultList.add(result.getSearchResult().getSearchRowList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					checkSearchResult(result, basicException);			
					if(result.getSearchResult().getSearchRowList().getSearchRow() != null)
						searchResultList.add(result.getSearchResult().getSearchRowList());
					pageIndex++;
				}
			}
		} catch (NetsuiteServiceException e) {
			String errorMessage = "Failed to retrieve sales orders that are ready for Invoice message. Reason:" + e.getMessage();
			logger.info(errorMessage);
			throw new NetsuiteOperationException(errorMessage,e.getRequestDetails());			
		}
		return getPoPojoFieldsListFromSearchResults(searchResultList);		
	}
	private List<PurchaseOrderPreloadedFieldsPojo> getPoPojoFieldsListFromSearchResults(List<SearchRowList> searchResultList){
		
		Map<String, PurchaseOrderPreloadedFieldsPojo> poNumberToPoPojo = new HashMap<String,PurchaseOrderPreloadedFieldsPojo>();
		List<PurchaseOrderPreloadedFieldsPojo> poList = new ArrayList<PurchaseOrderPreloadedFieldsPojo>();
		for(SearchRowList srList : searchResultList){
			for(SearchRow sr : srList.getSearchRow()){
				TransactionSearchRow row = (TransactionSearchRow) sr;
				if(null != row){
					PurchaseOrderPreloadedFieldsPojo po = null;
					TransactionSearchRowBasic tsBasic = row.getBasic();
					if(null != tsBasic){
						SearchColumnTextNumberField[] searchColumnTextNumberField = null;
						SearchColumnDateField[] searchColumnDateField = null;
						SearchColumnSelectField[] searchColumnSelectField = null;
						SearchColumnStringField[] searchColumnStringField = null;
						SearchColumnDoubleField[] searchColumnDoubleField = null;
						SearchColumnCustomField[] searchCustomColumnField = null;
						SearchColumnEnumSelectField[] searchColumnEnumSelectField = null;
						
						searchColumnTextNumberField = tsBasic.getOtherRefNum();
						String poNumber = searchColumnTextNumberField[0].getSearchValue();
						
						if(poNumberToPoPojo.containsKey(poNumber))
							po = poNumberToPoPojo.get(poNumber);
						else{
							po = new PurchaseOrderPreloadedFieldsPojo();
							poNumberToPoPojo.put(poNumber, po);
							po.setPoNumber(poNumber);							
							
							po.setOrderItems(new ArrayList<OrderItemPojo>());
							
							searchColumnDateField = tsBasic.getTranDate();
							po.setPoDate(searchColumnDateField[0].getSearchValue().getTime());
							
							searchColumnSelectField = tsBasic.getInternalId();
							po.setSalesOrderNsInternalId(searchColumnSelectField[0].getSearchValue().getInternalId());
							
							searchColumnSelectField = tsBasic.getDepartment();
							po.setDepartmentNsInternalId(searchColumnSelectField[0].getSearchValue().getInternalId());
							
							searchColumnEnumSelectField=tsBasic.getStatus();
							if (null!=searchColumnEnumSelectField){
								String status=searchColumnEnumSelectField[0].getSearchValue();
								if (status.equalsIgnoreCase(NSSalesOrderStatus.FULLYBILED.toString())|| status.equalsIgnoreCase(NSSalesOrderStatus.PENDINGBILLING.toString())){
									po.setASNGenerated(true);
								}else{
									po.setASNGenerated(false);
								}
							}
														
							searchColumnDateField = tsBasic.getShipDate();
							po.setShipDate(searchColumnDateField[0].getSearchValue().getTime());							
							
							searchColumnStringField = tsBasic.getTranId();
							if(searchColumnStringField != null)
								po.setSalesOrderTransactionId(searchColumnStringField[0].getSearchValue());
							
							poList.add(po);
						}
						List<OrderItemPojo> itemList = po.getOrderItems();
						ItemSearchRowBasic itemSearchRowJoin = row.getItemJoin();
						if(null != itemSearchRowJoin){								
							OrderItemPojo oiPojo = new OrderItemPojo();
							searchColumnDoubleField = tsBasic.getRate();
							oiPojo.setUnitPrice(searchColumnDoubleField[0].getSearchValue());
							oiPojo.setItemNumber(tsBasic.getLine()[0].getSearchValue().toString());
							searchColumnSelectField=itemSearchRowJoin.getInternalId();
							if (null!=searchColumnSelectField)
								oiPojo.setItemInternalId(searchColumnSelectField[0].getSearchValue().getInternalId());
							searchColumnDoubleField = tsBasic.getQuantity();
							oiPojo.setOrderQty(searchColumnDoubleField[0].getSearchValue());
							
							searchColumnStringField = itemSearchRowJoin.getUpcCode();
							oiPojo.setUPC(searchColumnStringField[0].getSearchValue());
							if (tsBasic.getCustomFieldList()!=null){
								searchCustomColumnField = tsBasic.getCustomFieldList().getCustomField();
								for(SearchColumnCustomField sccf : searchCustomColumnField){
									SearchColumnStringCustomField scsFielf = (SearchColumnStringCustomField)sccf;
								
									if(scsFielf.getScriptId().equalsIgnoreCase("custcolmerchant_sku"))
										oiPojo.setMerchantSKU(scsFielf.getSearchValue());
									else if(scsFielf.getScriptId().equalsIgnoreCase("custcol11"))
										oiPojo.setVendorlineNumber(scsFielf.getSearchValue());									
								}
							}							
							itemList.add(oiPojo);
						}
					}
				}
			}
		}		
		return poList;
	}
	@Override
	public List<InvoicePojo> getReadyForMessagingInvoices(Set<String> orderInternalId, Set<String> processedInvoices)
			throws NetsuiteOperationException {
		
		RecordRef[] invoicesRef = new RecordRef[processedInvoices.size()];
		RecordRef[] orderInternalIds = new RecordRef[orderInternalId.size()];
		int counter=0;
		for (String internalId:orderInternalId){
			orderInternalIds[counter]= new RecordRef(null,internalId,null,null);
			counter++;
		}
		counter=0;
		for (String internalId:processedInvoices){
			invoicesRef[counter]= new RecordRef(null, internalId, null, null);
			counter++;
		}
		
		TransactionSearchAdvanced advanced = new TransactionSearchAdvanced();
		TransactionSearchBasic basic = new TransactionSearchBasic();
		TransactionSearch search = new TransactionSearch();
		TransactionSearchRowBasic rowBasic = new TransactionSearchRowBasic();
		TransactionSearchRow rowSearch = new TransactionSearchRow();		
		
		basic.setInternalId(new SearchMultiSelectField(orderInternalIds,SearchMultiSelectFieldOperator.anyOf));		
		
		TransactionSearchBasic bilingCriteria = new TransactionSearchBasic();
		if (invoicesRef.length>0){
			bilingCriteria.setInternalId(new SearchMultiSelectField(invoicesRef, SearchMultiSelectFieldOperator.noneOf));			
		}		
		bilingCriteria.setQuantity(new SearchDoubleField(new Double(0),null, SearchDoubleFieldOperator.greaterThan));
		search.setBillingTransactionJoin(bilingCriteria);
				
		rowBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setOtherRefNum(new SearchColumnTextNumberField[]{new SearchColumnTextNumberField()});
		rowBasic.setDateCreated(new SearchColumnDateField[]{new SearchColumnDateField()});
		rowBasic.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		rowBasic.setTranId(new SearchColumnStringField[]{new SearchColumnStringField()});
		
		rowBasic.setDepartment(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
		rowBasic.setCreatedFrom(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setStatus(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
		
		rowBasic.setShipAddressee(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipAddress1(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipAddress2(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipCity(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipState(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipZip(new SearchColumnStringField[]{new SearchColumnStringField()});
		rowBasic.setShipCountry(new SearchColumnEnumSelectField[]{new SearchColumnEnumSelectField()});
		rowBasic.setShipDate(new SearchColumnDateField[]{new SearchColumnDateField()});
		rowBasic.setTotal(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		/*rowBasic.setTaxAmount(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		rowBasic.setTaxCode(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setTaxLine(new SearchColumnBooleanField[]{new SearchColumnBooleanField()});
		rowBasic.setTaxPeriod(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		rowBasic.setTaxTotal(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});*/
		
		search.setBasic(basic);
		rowSearch.setBasic(rowBasic);
		
		ItemSearchRowBasic itemRowJoin = new ItemSearchRowBasic();
		itemRowJoin.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		itemRowJoin.setUpcCode(new SearchColumnStringField[]{new SearchColumnStringField()});
		
		rowSearch.setItemJoin(itemRowJoin);							
		
		TransactionSearchRowBasic invoiceRowBasicJoin = new TransactionSearchRowBasic();		
		invoiceRowBasicJoin.setTranId(new SearchColumnStringField[]{new SearchColumnStringField()});
		invoiceRowBasicJoin.setTranDate(new SearchColumnDateField[]{new SearchColumnDateField()});	
		invoiceRowBasicJoin.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		invoiceRowBasicJoin.setQuantity(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		invoiceRowBasicJoin.setRate(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		//ADD TAX		
		invoiceRowBasicJoin.setTaxTotal(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});				
		invoiceRowBasicJoin.setTaxAmount(new SearchColumnDoubleField[]{new SearchColumnDoubleField()});
		
		SearchColumnCustomFieldList sccfl = new SearchColumnCustomFieldList();
		SearchColumnStringCustomField merchantSKU = new SearchColumnStringCustomField(null, "custcolmerchant_sku", null, null);
		SearchColumnStringCustomField vendorLineNumber = new SearchColumnStringCustomField(null, "custcol11",  null, null);
		sccfl.setCustomField(new SearchColumnCustomField[] { merchantSKU, vendorLineNumber});
		invoiceRowBasicJoin.setCustomFieldList(sccfl);
		rowSearch.setBillingTransactionJoin(invoiceRowBasicJoin);		
			
		advanced.setCriteria(search);
		advanced.setColumns(rowSearch);			
		
		SearchResultWrapped result=null;
		List<SearchRowList> searchResultList = new ArrayList<SearchRowList>();
		int pageIndex = 2;
		
		try {
			result = netsuiteService.search(advanced, false);	
			String errorString = "Failed to retrieve new Invoices for Sales Orders. Reason:";
			checkSearchResult(result, errorString);			
			if(result.getSearchResult().getSearchRowList().getSearchRow() != null)
				searchResultList.add(result.getSearchResult().getSearchRowList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					checkSearchResult(result, errorString);			
					if(result.getSearchResult().getSearchRowList().getSearchRow() != null)
						searchResultList.add(result.getSearchResult().getSearchRowList());					
					pageIndex++;
				}
			}					
		} catch (NetsuiteServiceException e) {
			String errorMessage = "Failed to retrieve new Invoices for Sales Orders. Reason:" + e.getMessage();
			logger.info(errorMessage);
			throw new NetsuiteOperationException(errorMessage,e.getRequestDetails());			
		}
		return getInvoicePojoListFromSearchResultForInvoiceMessage(searchResultList);
	}
	
	private void checkSearchResult(SearchResultWrapped result, String basicExceptionMessage) throws NetsuiteOperationException{
		if(!result.getSearchResult().getStatus().isIsSuccess()){			
			for (StatusDetail detail :result.getSearchResult().getStatus().getStatusDetail()){
				basicExceptionMessage+= detail.getMessage()+"/r/n";
			}
			throw new NetsuiteOperationException(basicExceptionMessage,result.getRequestDeatils());
		}
	}
	
	
	private List<InvoicePojo> getInvoicePojoListFromSearchResultForInvoiceMessage(List<SearchRowList> searchResults){		
		Map<String,InvoicePojo> mapInternaIdToInvoice = new HashMap<String,InvoicePojo>();
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		if (searchResults.size()>0){
			for (SearchRowList searchRowList:searchResults){
				for (SearchRow sr:searchRowList.getSearchRow()){
					TransactionSearchRow row = (TransactionSearchRow)sr;				
					if(null != row){
						InvoicePojo invoice = null;
						TransactionSearchRowBasic tsBasic = row.getBasic();
						TransactionSearchRowBasic tsBiling =row.getBillingTransactionJoin();
						ItemSearchRowBasic itBasic = row.getItemJoin();
						if ((null!=tsBiling)&&(null!=tsBasic)&&(null!=itBasic)){
							SearchColumnStringField[] searchColumnStringField = null;
							String invoiceInternalId = tsBiling.getInternalId()[0].getSearchValue().getInternalId();
							if (mapInternaIdToInvoice.containsKey(invoiceInternalId)){
								invoice=mapInternaIdToInvoice.get(invoiceInternalId);
							}else{
								invoice = new InvoicePojo();
								invoice.setDepartmentNsInternalId(tsBasic.getDepartment()[0].getSearchValue().getInternalId());
								invoice.setInvoiceNumber(tsBiling.getTranId()[0].getSearchValue());
								invoice.setNsInternalId(tsBiling.getInternalId()[0].getSearchValue().getInternalId());
								invoice.setPoDate(tsBasic.getTranDate()[0].getSearchValue().getTime());
								invoice.setPoNumber(tsBasic.getOtherRefNum()[0].getSearchValue());
								invoice.setSalesorderNsInternalId(tsBasic.getInternalId()[0].getSearchValue().getInternalId());
								invoice.setShipDate(tsBasic.getShipDate()[0].getSearchValue().getTime());
								invoice.setShipToAddress1(tsBasic.getShipAddress1()[0].getSearchValue());
								
								searchColumnStringField=tsBasic.getShipAddress2();
								if (null!=searchColumnStringField){
									invoice.setShipToAddress2(searchColumnStringField[0].getSearchValue());
								}
								invoice.setShipToCity(tsBasic.getShipCity()[0].getSearchValue());
								String NSshipcountry = tsBasic.getShipCountry()[0].getSearchValue();
								String country = properties.countryMappings.containsKey(NSshipcountry) ? properties.countryMappings.get(NSshipcountry) : properties.DEFAULT_SHIPPING_COUNTRY; 
								invoice.setShipToCountry(country);
								invoice.setShipToName(tsBasic.getShipAddressee()[0].getSearchValue());
								invoice.setShipToPostalCode(tsBasic.getShipZip()[0].getSearchValue());
								invoice.setShipToState(tsBasic.getShipState()[0].getSearchValue());
								invoice.setSoTransactionId(tsBasic.getTranId()[0].getSearchValue());
								invoice.setTotalSalesOrderAmount(tsBasic.getTotal()[0].getSearchValue());
								invoice.setItemList(new ArrayList<InvoiceItemPojo>());
								
								String orderStatus=tsBasic.getStatus()[0].getSearchValue();
								if (orderStatus.equalsIgnoreCase(NSSalesOrderStatus.FULLYBILED.toString())){									
									invoice.setProcessingClosed(true);
								}else{
									invoice.setProcessingClosed(false);
								}								
								SearchColumnDoubleField[] taxTotalColumn = tsBiling.getTaxTotal();
								if (null!=taxTotalColumn){
									if (taxTotalColumn[0].getSearchValue()>0){
										invoice.setTaxTotal(taxTotalColumn[0].getSearchValue());
									}
								}						
								invoice.setInvoiceDate(tsBiling.getTranDate()[0].getSearchValue().getTime());
								mapInternaIdToInvoice.put(invoiceInternalId, invoice);
							}
							List<InvoiceItemPojo> itemList = invoice.getItemList();								
							InvoiceItemPojo iiPojo = new InvoiceItemPojo();							
							iiPojo.setUnitPrice(tsBiling.getRate()[0].getSearchValue());							
							iiPojo.setItemInternalId(itBasic.getInternalId()[0].getSearchValue().getInternalId());
							iiPojo.setInvoiceQty(tsBiling.getQuantity()[0].getSearchValue());						
							iiPojo.setUPC(itBasic.getUpcCode()[0].getSearchValue());
							SearchColumnDoubleField[] taxAmountColumn = tsBiling.getTaxAmount();
							if (null!=taxAmountColumn){
								if (taxAmountColumn[0].getSearchValue()>0){
									iiPojo.setTaxAmount(taxAmountColumn[0].getSearchValue());
								}
							}							
							for(SearchColumnCustomField sccf :  tsBiling.getCustomFieldList().getCustomField()){
								SearchColumnStringCustomField scsFielf = (SearchColumnStringCustomField)sccf;
								
								if(scsFielf.getScriptId().equalsIgnoreCase("billingtransaction_custcolmerchant_sku"))
									iiPojo.setMerchantSKU(scsFielf.getSearchValue());
								else if(scsFielf.getScriptId().equalsIgnoreCase("billingtransaction_custcol11"))
									iiPojo.setVendorlineNumber(scsFielf.getSearchValue());								
							}
							itemList.add(iiPojo);
						}						
					}
				}
			}
		}
		List<InvoicePojo> result = new ArrayList<InvoicePojo>();
		for (InvoicePojo inv:mapInternaIdToInvoice.values()){
			result.add(inv);
		}
		return result;
	}
	
	public Map<String,List> loadInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList) throws NetsuiteOperationException{
		SearchResultWrapped result=null;
		List<RecordList> recListList = new ArrayList<>();		
		RecordRef[] inventoriesRefs = new RecordRef[lineItemDAOList.size()];
		Map<String,LineItemIntegrationIdentifierDAO> internalIdtoLineItemDAOMap = new HashMap<>();
		for (int i=0; i<inventoriesRefs.length;i++){
			internalIdtoLineItemDAOMap.put(lineItemDAOList.get(i).getItemInternalId(), lineItemDAOList.get(i));
			inventoriesRefs[i] = new RecordRef(null,lineItemDAOList.get(i).getItemInternalId(),null,RecordType.inventoryItem);
		}		
		ItemSearchBasic itembasic = new ItemSearchBasic();
		ItemSearch is = new ItemSearch();		
		is.setBasic(itembasic);		
		itembasic.setInternalId(new SearchMultiSelectField(inventoriesRefs, SearchMultiSelectFieldOperator.anyOf));		
		int pageIndex=2;		
		try {			
			result = netsuiteService.search(is, false);		
			recListList.add(result.getSearchResult().getRecordList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					recListList.add(result.getSearchResult().getRecordList());
					pageIndex++;
				}
		}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}		
		return proccessSearchResultsForInventoryMessage(internalIdtoLineItemDAOMap, recListList);
	}
	
	private Map<String,List> proccessSearchResultsForInventoryMessage(Map<String,LineItemIntegrationIdentifierDAO> internalIdtolineItemDAOMap,List<RecordList> recListList) throws NetsuiteOperationException{
		 Map<String,List> inventoryTypeClassNameToInventoryPojoListMap = getSearchResultsFromLoadInventorySearch(recListList);
		 
		 Set<String> iiIdsToPreloadSet = new HashSet<String>();
		 
		 List<InventoryKitPojo> invKitList = inventoryTypeClassNameToInventoryPojoListMap.get(InventoryKitPojo.class.getName());
		 List<InventoryItemPojo> invItemList = inventoryTypeClassNameToInventoryPojoListMap.get(InventoryItemPojo.class.getName());
		 for (InventoryKitPojo invKit:invKitList){
			 for (InventoryKitSubItemPojo subItem:invKit.getSubItemsList()){
				 iiIdsToPreloadSet.add(subItem.getNsInternalId());
			 }
		 }
		 List <String> invItemIdsToPreloadList = new ArrayList<>();
		 for (String intID:iiIdsToPreloadSet){
			 if (!internalIdtolineItemDAOMap.containsKey(intID)){
				 invItemIdsToPreloadList.add(intID); 
			 }
		 }
		 List<InventoryItemPojo> kitRequiredInventoryList = new ArrayList<>();
		 if (invItemIdsToPreloadList.size()>0){			
			 kitRequiredInventoryList = loadKitPackageInventory(invItemIdsToPreloadList);
		 }
		 
		 Map<String,Double> internalIdToInventoryAvailableQtyMap = new HashMap<>();
		 for (InventoryItemPojo invItemPojo:kitRequiredInventoryList){
			 internalIdToInventoryAvailableQtyMap.put(invItemPojo.getNsInternalId(), invItemPojo.getQtyAvailable());
		 }
		 for (InventoryItemPojo invItemPojo:invItemList){
			 internalIdToInventoryAvailableQtyMap.put(invItemPojo.getNsInternalId(), invItemPojo.getQtyAvailable());
		 }
		 
		 for (InventoryKitPojo invKitPojo:invKitList){
			 for (InventoryItemPojo invitemPojo:invKitPojo.getSubItemsList()){
				 invitemPojo.setQtyAvailable(internalIdToInventoryAvailableQtyMap.get(invitemPojo.getNsInternalId()));
			 }
		 }		 
		return inventoryTypeClassNameToInventoryPojoListMap;
	}
	
	private Map<String,List> getSearchResultsFromLoadInventorySearch(List<RecordList> recListList){
		Map<String,List> inventoryTypeClassNameToInventoryPojoListMap = new HashMap<>();
		inventoryTypeClassNameToInventoryPojoListMap.put(InventoryItemPojo.class.getName(), new ArrayList<InventoryItemPojo>());
		inventoryTypeClassNameToInventoryPojoListMap.put(InventoryKitPojo.class.getName(), new ArrayList<InventoryKitPojo>());
		
		for (RecordList recList:recListList){
			for (Record rec:recList.getRecord()){
				if (rec instanceof InventoryItem){
					InventoryItem invItem = (InventoryItem)rec;
					InventoryItemPojo itemPojo = new InventoryItemPojo();
					InventoryItemLocations[] invItemsLocations=invItem.getLocationsList().getLocations();
					for (InventoryItemLocations location:invItemsLocations){
						if (location.getLocationId().getInternalId().equalsIgnoreCase("4")){
							Double quantityAvailable=location.getQuantityAvailable();
							if (quantityAvailable!=null){
								itemPojo.setQtyAvailable(quantityAvailable);
							} else{
								itemPojo.setQtyAvailable(0);
							}
							break;
						}
					}
					itemPojo.setNsInternalId(invItem.getInternalId());	
					inventoryTypeClassNameToInventoryPojoListMap.get(InventoryItemPojo.class.getName()).add(itemPojo);
					}
				else if (rec instanceof KitItem){
					KitItem kitItem = (KitItem)rec;
					ItemMember[] itemMembers = kitItem.getMemberList().getItemMember();
					List<InventoryKitSubItemPojo> subItemsList = new ArrayList<>();
					for (ItemMember itemMember:itemMembers){
						InventoryKitSubItemPojo subItem = new InventoryKitSubItemPojo();
						subItem.setNsInternalId(itemMember.getItem().getInternalId());
						subItem.setQtyInKit(itemMember.getQuantity());
						subItemsList.add(subItem);
					}
					InventoryKitPojo invKit = new InventoryKitPojo();
					invKit.setSubItemsList(subItemsList);
					invKit.setNsInternalId(kitItem.getInternalId());
					inventoryTypeClassNameToInventoryPojoListMap.get(InventoryKitPojo.class.getName()).add(invKit);
				}
			}
		}		
		return inventoryTypeClassNameToInventoryPojoListMap;
	}
	
	private List<InventoryItemPojo> loadKitPackageInventory(List<String> internalIds) throws NetsuiteOperationException{
		SearchResultWrapped result=null;
		List<RecordList> recListList = new ArrayList<>();
		if (internalIds.size()>0){
			RecordRef[] inventoriesRefs = new RecordRef[internalIds.size()];			
			for (int i=0; i<inventoriesRefs.length;i++){				
				inventoriesRefs[i] = new RecordRef(null,internalIds.get(i),null,RecordType.inventoryItem);
			}		
			ItemSearchBasic itembasic = new ItemSearchBasic();
			ItemSearch is = new ItemSearch();		
			is.setBasic(itembasic);		
			itembasic.setInternalId(new SearchMultiSelectField(inventoriesRefs, SearchMultiSelectFieldOperator.anyOf));		
			int pageIndex=2;		
			try {			
				result = netsuiteService.search(is, false);		
				recListList.add(result.getSearchResult().getRecordList());
				int totalPages = result.getSearchResult().getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchResult().getSearchId();
					while(pageIndex <= totalPages){
						result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
						recListList.add(result.getSearchResult().getRecordList());
						pageIndex++;
					}
			}
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}	
		}
		return getSearchResultsFromKitPackageInventory(recListList);	
	}
	
	private List<InventoryItemPojo> getSearchResultsFromKitPackageInventory(List<RecordList> recListList){
		List<InventoryItemPojo> result = new ArrayList<>();
		for (RecordList recList:recListList){
			for (Record rec:recList.getRecord()){
				if (rec instanceof InventoryItem){
					InventoryItem invItem = (InventoryItem)rec;
					InventoryItemPojo invItemPojo = new InventoryItemPojo();
					invItemPojo.setNsInternalId(invItem.getInternalId());
					for (InventoryItemLocations invLocations:invItem.getLocationsList().getLocations()){
						if (invLocations.getLocationId().getInternalId().equalsIgnoreCase("4")){
							Double quantityAvailable=invLocations.getQuantityAvailable();
							if (quantityAvailable!=null){
								invItemPojo.setQtyAvailable(quantityAvailable);
							} else{
								invItemPojo.setQtyAvailable(0);
							}
							break;
						}
					}
					result.add(invItemPojo);
				}				
			}
		}		
		return result;
	}

	@Override
	public void updateRetailersInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList)throws NetsuiteOperationException {
		SearchResultWrapped result=null;
		List<SearchRowList> recListList = new ArrayList<>();		
		Map<Integer,Map<String,LineItemIntegrationIdentifierDAO>> retailerIdToskuToLineItemDAOMap = new HashMap<Integer,Map<String,LineItemIntegrationIdentifierDAO>>();
		for (LineItemIntegrationIdentifierDAO lineItemDAO:lineItemDAOList){
			int retailerId = lineItemDAO.getRetailer().getIdRetailer();
			if (retailerIdToskuToLineItemDAOMap.containsKey(retailerId)){
				Map<String,LineItemIntegrationIdentifierDAO> skuToLineItemDAOMap=retailerIdToskuToLineItemDAOMap.get(retailerId);			
				skuToLineItemDAOMap.put(lineItemDAO.getVendorSKU(), lineItemDAO);
			} else {
				Map<String,LineItemIntegrationIdentifierDAO> skuToLineItemDAOMap = new HashMap<>();
				skuToLineItemDAOMap.put(lineItemDAO.getVendorSKU(), lineItemDAO);
				retailerIdToskuToLineItemDAOMap.put(retailerId, skuToLineItemDAOMap);
			}						
		}
		
		ItemSearchAdvanced itemAdv = new ItemSearchAdvanced();
		ItemSearchBasic itembasic = new ItemSearchBasic();
		ItemSearch is = new ItemSearch();
		itemAdv.setCriteria(is);				
		is.setBasic(itembasic);			
		ItemSearchRow itemRows = new ItemSearchRow();
		ItemSearchRowBasic itemRowsBasic = new ItemSearchRowBasic();
		itemRowsBasic.setItemId(new SearchColumnStringField[]{new SearchColumnStringField()});
		itemRowsBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		itemRows.setBasic(itemRowsBasic);
		itemAdv.setColumns(itemRows);
		
		int pageIndex=2;		
		try {			
			result = netsuiteService.search(itemAdv, false);		
			recListList.add(result.getSearchResult().getSearchRowList());
			int totalPages = result.getSearchResult().getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchResult().getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					recListList.add(result.getSearchResult().getSearchRowList());
					pageIndex++;
				}
			}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}		
		for (SearchRowList rowList:recListList){
			for (SearchRow row:rowList.getSearchRow()){						
				ItemSearchRow itemSearchRow = (ItemSearchRow)row;
				ItemSearchRowBasic itemSearchRowBasic=itemSearchRow.getBasic();
				String internalId=itemSearchRowBasic.getInternalId()[0].getSearchValue().getInternalId();
				String itemId=itemSearchRowBasic.getItemId()[0].getSearchValue();
				if (itemId!=null){
					Iterator iterator = retailerIdToskuToLineItemDAOMap.values().iterator();
					while (iterator.hasNext()){						
						Map<String,LineItemIntegrationIdentifierDAO> skuToLineItemDAOMap = (HashMap<String,LineItemIntegrationIdentifierDAO>)iterator.next();						
						if (skuToLineItemDAOMap.containsKey(itemId)){
							LineItemIntegrationIdentifierDAO lineItemDAO=skuToLineItemDAOMap.get(itemId);
							lineItemDAO.setItemInternalId(internalId);
						}
					}
				}
			}
		}
		
	}

	@Override
	public Map<String, String> getInventoryInternalIdsByUpcCodes(Set<String> upcCodes, RetailerAbstract retailer) throws NetsuiteOperationException {
		logger.info("Preparing inventory search.");	
		Map<String, String> inventoryUpcToIntenrlIdMap = new HashMap<String, String>();
		
		ItemSearchAdvanced itemSearchAdvanced = new ItemSearchAdvanced();
		
		ItemSearchBasic searchBasic = new ItemSearchBasic();
		ItemSearch itemSearch = new ItemSearch();
		ItemSearchRow itemSearchRow = new ItemSearchRow();
		ItemSearchRowBasic itemSearchRowBasic = new ItemSearchRowBasic();
		
		itemSearchRowBasic.setInternalId(new SearchColumnSelectField[] {new SearchColumnSelectField()});
		
		itemSearch.setBasic(searchBasic);
		itemSearchRow.setBasic(itemSearchRowBasic);
		
		itemSearchAdvanced.setColumns(itemSearchRow);
		itemSearchAdvanced.setCriteria(itemSearch);
		
		SearchResultWrapped result = null;
		
		for(String upcCode : upcCodes){
			logger.info("Searching for Inventory Item with UPC code : " + upcCode);
			logger.info("Making a WS-SOAP request to Poppin-Netsuite. Please wait, it might take some time...");
			searchBasic.setItemId(new SearchStringField(upcCode, SearchStringFieldOperator.is));
			try {
				result = netsuiteService.search(itemSearchAdvanced, false,retailer.getShortName());
				if(null != result && null != result.getSearchResult().getSearchRowList()){
					SearchRowList searchRowList = result.getSearchResult().getSearchRowList();
					if(null != searchRowList.getSearchRow()){
						for(SearchRow searchRow : searchRowList.getSearchRow()){
							ItemSearchRow itemSearchRowResult = (ItemSearchRow)searchRow;
							ItemSearchRowBasic basic = itemSearchRowResult.getBasic();
							SearchColumnSelectField[] selectField =  basic.getInternalId();
							if(null != selectField){
								inventoryUpcToIntenrlIdMap.put(upcCode, selectField[0].getSearchValue().getInternalId());
							}
						}
					}
				}
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}
		}
		return inventoryUpcToIntenrlIdMap;
	}

	@Override
	public Map<String, UnprocessibleOrdersType> retrieveCancelledClosedOrders(List<PurchaseOrderDAO> purchaseOrderDAOs) {
		RecordRef[] ordersRef = new RecordRef[purchaseOrderDAOs.size()];
		int	counter=0;
		for (PurchaseOrderDAO poDAO : purchaseOrderDAOs){
			ordersRef[counter]= new RecordRef(null, poDAO.getSalesOrderNsInternald(), null, RecordType.salesOrder);
			counter++;
		}		
		TransactionSearchAdvanced advanced = new TransactionSearchAdvanced();
		TransactionSearch search = new TransactionSearch();
		TransactionSearchBasic basic = new TransactionSearchBasic();
		basic.setInternalId(new SearchMultiSelectField(ordersRef, SearchMultiSelectFieldOperator.anyOf));		
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
		List<SearchRowList> recListList = new ArrayList<>();
		SearchResultWrapped result = null;
		int pageIndex=2;		
		try {			
			result = netsuiteService.search(advanced, false);
			if (result.getSearchResult().getStatus().isIsSuccess()){
				if (result.getSearchResult().getSearchRowList().getSearchRow()!=null)
					recListList.add(result.getSearchResult().getSearchRowList());
				int totalPages = result.getSearchResult().getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchResult().getSearchId();
					while(pageIndex <= totalPages){
						result = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
						if (result.getSearchResult().getStatus().isIsSuccess()){
							recListList.add(result.getSearchResult().getSearchRowList());
							pageIndex++;
						}						
					}
				}
			} else {				
				SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(result.getSearchResult().getStatus().getStatusDetail()[0].getMessage(), result.getRequestDeatils());					
				ErrorsCollector.addCommonErrorMessage(error);
			}			
		} catch (NetsuiteServiceException e) {			
			SPSIntegrationError error = ErrorMessageWrapper.wrapCommonError(e.getMessage(), e.getRequestDetails());					
			ErrorsCollector.addCommonErrorMessage(error);
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
	@Override
	public void sendBarnesAndNobleInvoiceCustomRecords(List<PoDocument> documents, Map<String, ShippingAddressPojo> addressMappings) throws NetsuiteOperationException{
		WriteResponseListWrapped response = null;
		CustomRecord[] records = new CustomRecord[documents.size()];
		for(int i = 0; i < documents.size(); i++){
			
			PoDocument doc = documents.get(i);
			String bnnStoreEmail = addressMappings.get(doc.getShipLocationNumber()).getEmail();
			
			CustomFieldRef[] fieldsArray = new CustomFieldRef[2];
			fieldsArray[0] = new StringCustomFieldRef(null, "custrecord_so_internal_id", doc.getAddSalesOrderResultPojo().getSoInternalId());
			fieldsArray[1] = new StringCustomFieldRef(null, "custrecord_store_email", bnnStoreEmail);
			CustomFieldList fields = new CustomFieldList();
			fields.setCustomField(fieldsArray);
			
			CustomRecord record = new CustomRecord();
			record.setRecType(new RecordRef(null, GlobalPropertiesProvider.getGlobalProperties().getBnnRetailerNsMappingCustomRecordId(), null, null));
			record.setCustomFieldList(fields);
			
			records[i] = record;
		}
		try {
			response = netsuiteService.addList(records);
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}
	}
}
