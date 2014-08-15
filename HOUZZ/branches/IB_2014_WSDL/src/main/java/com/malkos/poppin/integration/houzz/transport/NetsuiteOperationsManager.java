package com.malkos.poppin.integration.houzz.transport;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitSubItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;
import com.malkos.poppin.integration.houzz.entities.LocationQuantitiesAvailiable;
import com.malkos.poppin.integration.houzz.entities.OlapicInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.SearchResultWrapped;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.netsuite.webservices.lists.accounting_2014_2.InventoryItem;
import com.netsuite.webservices.lists.accounting_2014_2.InventoryItemLocations;
import com.netsuite.webservices.lists.accounting_2014_2.ItemMember;
import com.netsuite.webservices.lists.accounting_2014_2.ItemMemberList;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearch;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearchAdvanced;
import com.netsuite.webservices.lists.accounting_2014_2.ItemSearchRow;
import com.netsuite.webservices.lists.accounting_2014_2.KitItem;
import com.netsuite.webservices.lists.accounting_2014_2.Pricing;
import com.netsuite.webservices.platform.common_2014_2.ItemSearchBasic;
import com.netsuite.webservices.platform.common_2014_2.ItemSearchRowBasic;
import com.netsuite.webservices.platform.common_2014_2.LocationSearchRowBasic;
import com.netsuite.webservices.platform.core_2014_2.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.CustomFieldList;
import com.netsuite.webservices.platform.core_2014_2.CustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.Record;
import com.netsuite.webservices.platform.core_2014_2.RecordList;
import com.netsuite.webservices.platform.core_2014_2.RecordRef;
import com.netsuite.webservices.platform.core_2014_2.SearchBooleanField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnBooleanField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnCustomField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnCustomFieldList;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnDoubleField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnEnumSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnStringCustomField;
import com.netsuite.webservices.platform.core_2014_2.SearchColumnStringField;
import com.netsuite.webservices.platform.core_2014_2.SearchMultiSelectField;
import com.netsuite.webservices.platform.core_2014_2.SearchResult;
import com.netsuite.webservices.platform.core_2014_2.SearchRow;
import com.netsuite.webservices.platform.core_2014_2.SearchRowList;
import com.netsuite.webservices.platform.core_2014_2.SearchStringField;
import com.netsuite.webservices.platform.core_2014_2.StringCustomFieldRef;
import com.netsuite.webservices.platform.core_2014_2.types.RecordType;
import com.netsuite.webservices.platform.core_2014_2.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2014_2.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform_2014_2.NetSuiteServiceLocator;


public class NetsuiteOperationsManager implements INetsuiteOperationsManager {
	
	private static Logger logger = LoggerFactory.getLogger(NetsuiteOperationsManager.class);
	
	private NetsuiteService netsuiteService;

	public NetsuiteOperationsManager(){
		netsuiteService = new NetsuiteService();
		netsuiteService.setNSaccount(GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigAccount());
		netsuiteService.setNSlogin(GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigEmail());
		netsuiteService.setNSpassword(GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigPassword());
		netsuiteService.setNSrole(GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigRole());
		netsuiteService.setNSwsURL(GlobalPropertiesProvider.getGlobalProperties().getNetsuiteConfigUrl());
		netsuiteService.InitializeNetsuiteService();		
	}
	
	public NetsuiteService getNetsuiteService() {
		return netsuiteService;
	}

	public void setNetsuiteService(NetsuiteService netsilteClient) {
		this.netsuiteService = netsilteClient;
	}
	
	public Map<String,InventoryPojo> loadHouzzInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList) throws NetsuiteOperationException{
		SearchResult result= null;		
		List<SearchRowList> searchRowListList = new ArrayList<SearchRowList>();
		Map<String,LineItemIntegrationIdentifierDAO> internalIdtoLineItemDAOMap = new HashMap<>();			
		for (LineItemIntegrationIdentifierDAO lineItemDAO:lineItemDAOList){
			internalIdtoLineItemDAOMap.put(lineItemDAO.getItemInternalId(), lineItemDAO);			
		}		
		RecordRef[] inventoriesRefs = new RecordRef[lineItemDAOList.size()];
		int counter = 0;
		for (LineItemIntegrationIdentifierDAO lineItemDAO : lineItemDAOList){											
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
		basicRow.setItemId(new SearchColumnStringField[]{new SearchColumnStringField()});
		LocationSearchRowBasic locRow = new LocationSearchRowBasic();
		locRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
		itemRow.setInventoryLocationJoin(locRow);
		itemRow.setBasic(basicRow);
		itemAdv.setColumns(itemRow);	
		int pageIndex=2;		
		try {				
			result = netsuiteService.search(itemAdv, false).getSearchResult();							
			searchRowListList.add(result.getSearchRowList());
			int totalPages = result.getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchId();
				while(pageIndex <= totalPages){
					result = netsuiteService.searchMoreWithId(searchId, pageIndex, false).getSearchResult();
					searchRowListList.add(result.getSearchRowList());
					pageIndex++;
				}
			}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
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
							HouzzInventoryKitPojo kitPojo = (HouzzInventoryKitPojo) intIdToRecordMap.get(internalId);
							HouzzInventoryKitSubItemPojo subItem = new HouzzInventoryKitSubItemPojo();
							subItem.setQtyInKit(isrb.getMemberQuantity()[0].getSearchValue());
							subItem.setInternalId(isrb.getMemberItem()[0].getSearchValue().getInternalId());
							kitPojo.getSubItemsList().add(subItem);
						} else {
							HouzzInventoryKitPojo kitPojo = new HouzzInventoryKitPojo();
							kitPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							kitPojo.setInternalId(internalId);
							kitPojo.setSku(isrb.getItemId()[0].getSearchValue());
							kitPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());
							kitPojo.setSubItemsList(new ArrayList<HouzzInventoryKitSubItemPojo>());
							HouzzInventoryKitSubItemPojo subItem = new HouzzInventoryKitSubItemPojo();
							subItem.setQtyInKit(isrb.getMemberQuantity()[0].getSearchValue());
							subItem.setInternalId(isrb.getMemberItem()[0].getSearchValue().getInternalId());
							kitPojo.getSubItemsList().add(subItem);
							if (isrb.getLocation()!=null){
								kitPojo.setNsLocationId(isrb.getLocation()[0].getSearchValue().getInternalId());
							} else {
								kitPojo.setWrongConfigured(true);
								ErrorsCollector.getNsInventoryConfigurationError().add("Location is 'null' for kit with ItemInternalId = '"+kitPojo.getInternalId()+"' (SKU = '"+kitPojo.getSku()+"')");
							}	
							intIdToRecordMap.put(internalId, kitPojo);
						}
					} else if (type.equalsIgnoreCase("_inventoryItem")){
						if (intIdToRecordMap.containsKey(internalId)){	
							HouzzInventoryItemPojo itemPojo = (HouzzInventoryItemPojo) intIdToRecordMap.get(internalId);
							
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}							
							itemPojo.getLocQtyList().add(locQty);
						} else {
							HouzzInventoryItemPojo itemPojo = new HouzzInventoryItemPojo();
							itemPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							itemPojo.setInternalId(internalId);
							itemPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());							
							itemPojo.setSku(isrb.getItemId()[0].getSearchValue());							
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
								itemPojo.setWrongConfigured(true);
								ErrorsCollector.getNsInventoryConfigurationError().add("Prefered location is 'null' for inventory with ItemInternalId = '"+itemPojo.getInternalId()+"' (SKU = '"+itemPojo.getSku()+"')");
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
	private Map<String,InventoryPojo> proccessSearchResultsForInventoryUpdate(List<SearchRowList> searchRowListList , Map<String,LineItemIntegrationIdentifierDAO> internalIdtolineItemDAOMap) throws NetsuiteOperationException{
		 Map<String, InventoryPojo> internalIdToInventoryPojoMap = null;
		 Set<String> iiIdsToPreloadSet = new HashSet<String>();			
		 Map<String,HouzzInventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap = new HashMap<>();		
		 
		 internalIdToInventoryPojoMap = wrapSearchResultsForInventorySearch(searchRowListList);
		 checkForUnapropriateItems(internalIdtolineItemDAOMap, internalIdToInventoryPojoMap);	
		 
		 for (InventoryPojo invPojo:internalIdToInventoryPojoMap.values()){
			 if (invPojo instanceof HouzzInventoryKitPojo){
				 HouzzInventoryKitPojo kitPojo = (HouzzInventoryKitPojo)invPojo;
				 if (!kitPojo.isWrongConfigured()){
					 for (HouzzInventoryKitSubItemPojo subItem:kitPojo.getSubItemsList()){
						 if (!internalIdToInventoryPojoMap.containsKey(subItem.getInternalId())){
							 iiIdsToPreloadSet.add(subItem.getInternalId());
						 }
					 }
				 }
			 } else if (invPojo instanceof HouzzInventoryItemPojo){
				 HouzzInventoryItemPojo invItemPojo = (HouzzInventoryItemPojo)invPojo;
				 internalIdToHouzInventoryItemPojoFullMap.put(invItemPojo.getInternalId(), invItemPojo);
			 }
		 }
		 if (iiIdsToPreloadSet.size()>0){			
			 loadKitPackageInventory(iiIdsToPreloadSet, internalIdToHouzInventoryItemPojoFullMap);
		 }	
		 for (InventoryPojo invPojo:internalIdToInventoryPojoMap.values()){
			 if (invPojo instanceof HouzzInventoryKitPojo){
				 HouzzInventoryKitPojo kitPojo = (HouzzInventoryKitPojo)invPojo;
				 if (!kitPojo.isWrongConfigured()){
					 for (HouzzInventoryKitSubItemPojo invSubItemPojo:kitPojo.getSubItemsList()){
						 HouzzInventoryItemPojo apropriateNsInventory = internalIdToHouzInventoryItemPojoFullMap.get(invSubItemPojo.getInternalId());				 
						 for (LocationQuantitiesAvailiable location:apropriateNsInventory.getLocQtyList()){
							 if (location.getLocationInternalId().equalsIgnoreCase(kitPojo.getNsLocationId())){
								 invSubItemPojo.setQtyAvailiable(location.getLocationQtyAvailiable());
							 }
						 }							
						 invSubItemPojo.setInactive(apropriateNsInventory.isInactive());						
					 }
				 }
			 }
		 } 
		return internalIdToInventoryPojoMap;
	}

	private void checkForUnapropriateItems(Map<String,LineItemIntegrationIdentifierDAO> internalIdtolineItemDAOMap, Map<String,InventoryPojo> internalIdToInventoryPojoMap) {
		for (Entry<String,LineItemIntegrationIdentifierDAO> lineItemDAO:internalIdtolineItemDAOMap.entrySet()){			
			if (!internalIdToInventoryPojoMap.containsKey(lineItemDAO.getKey())){
				ErrorsCollector.getNsInventoryConfigurationError().add("Couldn't find appropriate to ItemInternalId = '"+lineItemDAO.getValue().getItemInternalId()+"' (SKU='"+lineItemDAO.getValue().getSKU()+"') inventory in Poppin NetSuite database");
			}
		}		
	}
	
	// here we make another one request to NS for items contains in KIT/PACKAGE
	private void loadKitPackageInventory(Set<String> internalIds, Map<String,HouzzInventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap) throws NetsuiteOperationException{
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
			basicRow.setItemId(new SearchColumnStringField[]{new SearchColumnStringField()});
			LocationSearchRowBasic locRow = new LocationSearchRowBasic();
			locRow.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});		
			itemRow.setInventoryLocationJoin(locRow);
			itemRow.setBasic(basicRow);
			itemAdv.setColumns(itemRow);	
			int pageIndex=2;		
			try {			
				result = netsuiteService.search(itemAdv, false).getSearchResult();		
				rowListList.add(result.getSearchRowList());
				int totalPages = result.getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchId();
					while(pageIndex <= totalPages){
						result = netsuiteService.searchMoreWithId(searchId, pageIndex, false).getSearchResult();
						rowListList.add(result.getSearchRowList());
						pageIndex++;
					}
			}
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}	
		}
		processSearchResultsFromKitPackageInventory(rowListList, internalIdToHouzInventoryItemPojoFullMap);	
	}
	
	//here we process search results for loadKitPackageInventory()
	private void processSearchResultsFromKitPackageInventory(List<SearchRowList> rowListList, Map<String,HouzzInventoryItemPojo> internalIdToHouzInventoryItemPojoFullMap){		
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
							HouzzInventoryItemPojo itemPojo = (HouzzInventoryItemPojo) internalIdToHouzInventoryItemPojoFullMap.get(internalId);
							
							LocationQuantitiesAvailiable locQty = new LocationQuantitiesAvailiable();
							locQty.setLocationInternalId(locationJoin.getInternalId()[0].getSearchValue().getInternalId());
							if ((isrb.getLocationQuantityAvailable()!=null)&&(isrb.getLocationQuantityAvailable()[0]!=null)&&(isrb.getLocationQuantityAvailable()[0].getSearchValue()!=null)){
								locQty.setLocationQtyAvailiable(isrb.getLocationQuantityAvailable()[0].getSearchValue());
							} else {
								locQty.setLocationQtyAvailiable(0d);
							}							
							itemPojo.getLocQtyList().add(locQty);
						} else {
							HouzzInventoryItemPojo itemPojo = new HouzzInventoryItemPojo();
							itemPojo.setInactive(isrb.getIsInactive()[0].getSearchValue());
							itemPojo.setInternalId(internalId);
							itemPojo.setPrice(isrb.getBasePrice()[0].getSearchValue());							
							itemPojo.setSku(isrb.getItemId()[0].getSearchValue());							
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

	// this is first run maintenance code
	
	@Override
	public LineItemIntegrationIdentifierDAO updateInventoryInternalId(LineItemIntegrationIdentifierDAO lineItemDAO)	throws NetsuiteOperationException {
		ItemSearchBasic itembasic = new ItemSearchBasic();
		ItemSearch is = new ItemSearch();		
		is.setBasic(itembasic);		
		itembasic.setItemId(new SearchStringField(lineItemDAO.getSKU(), SearchStringFieldOperator.is));
		SearchResult result = null;		
		try {
			result = netsuiteService.search(is, false).getSearchResult();
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}	
		
		if (result.getStatus().isIsSuccess()&&result.getTotalRecords()>0){
			Record rec = result.getRecordList().getRecord()[0];	
			String itemInternalId = null;
			if (rec instanceof InventoryItem){
				InventoryItem invItem = (InventoryItem)rec;
				itemInternalId = invItem.getInternalId();
			} else if (rec instanceof KitItem){
				KitItem kitItem = (KitItem)rec;
				itemInternalId = kitItem.getInternalId();
			}
			if (itemInternalId!=null){
				lineItemDAO.setItemInternalId(itemInternalId);
			}
		}
		return lineItemDAO;
	}

	
	@Override
	public List<InventoryPojo> loadOlapicInventory() throws NetsuiteOperationException {
		ItemSearchAdvanced itemSearchAdvanced = new ItemSearchAdvanced();	
		SearchResult result = null;
		SearchResultWrapped resultWrapped = null;
		List<SearchRowList> recListList = new ArrayList<>();
		
		//Criteria
		ItemSearch searchCriteria = new ItemSearch();
		ItemSearchBasic searchCriteriaBasic = new ItemSearchBasic();		
		searchCriteriaBasic.setComponent(new SearchMultiSelectField(new RecordRef[]{new RecordRef(null,"@NONE@",null,null)},SearchMultiSelectFieldOperator.anyOf));
		searchCriteriaBasic.setIsOnline(new SearchBooleanField(true));
		searchCriteriaBasic.setWebSite(new SearchMultiSelectField(new RecordRef[]{new RecordRef(null,"1",null,null)},SearchMultiSelectFieldOperator.anyOf)); // Poppin Shop web site only
		searchCriteria.setBasic(searchCriteriaBasic);		
		itemSearchAdvanced.setCriteria(searchCriteria);
		
		//Columns
		ItemSearchRow columns = new ItemSearchRow();
		ItemSearchRowBasic columnsBasic = new ItemSearchRowBasic();
		columnsBasic.setStoreDisplayName(new SearchColumnStringField[]{new SearchColumnStringField()});
		columnsBasic.setItemUrl(new SearchColumnStringField[]{new SearchColumnStringField()});
		columnsBasic.setInternalId(new SearchColumnSelectField[]{new SearchColumnSelectField()});
		SearchColumnCustomFieldList sccfl = new SearchColumnCustomFieldList();
		SearchColumnStringCustomField itemDisplayImage = new SearchColumnStringCustomField("custitem_display_image", "1352", null, null);		
		sccfl.setCustomField(new SearchColumnCustomField[] { itemDisplayImage});		
		columnsBasic.setCustomFieldList(sccfl);		
		columns.setBasic(columnsBasic);		
		itemSearchAdvanced.setColumns(columns);
		
		int pageIndex=2;		
		try {
			resultWrapped = netsuiteService.search(itemSearchAdvanced, false);
			result = resultWrapped.getSearchResult();	
			if (!result.getStatus().isIsSuccess()){
				throw new NetsuiteServiceException(result.getStatus().getStatusDetail()[0].getMessage(),resultWrapped.getRequestDeatils());
			}
			recListList.add(result.getSearchRowList());
			int totalPages = result.getTotalPages();
			if(totalPages > 1){
				String searchId = result.getSearchId();
				while(pageIndex <= totalPages){
					resultWrapped = netsuiteService.searchMoreWithId(searchId, pageIndex, false);
					result = resultWrapped.getSearchResult();
					if (!result.getStatus().isIsSuccess()){
						throw new NetsuiteServiceException(result.getStatus().getStatusDetail()[0].getMessage(),resultWrapped.getRequestDeatils());
					}
					recListList.add(result.getSearchRowList());
					pageIndex++;
				}
		}
		} catch (NetsuiteServiceException e) {
			throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
		}
		List<InventoryPojo> pojoResults = processOlapicInventorySearchResults(recListList);		
		return filterOlapicInventory(pojoResults);
	}

	private List<InventoryPojo> filterOlapicInventory(List<InventoryPojo> pojoResults) {
		List<InventoryPojo> filteredList = new ArrayList<>();
		for (InventoryPojo item:pojoResults){
			if (item instanceof OlapicInventoryItemPojo){
				OlapicInventoryItemPojo olapicItem = (OlapicInventoryItemPojo) item;
				//if (!olapicItem.getItemUrl().contains("http://shopping.")){
					filteredList.add(olapicItem);
				//}
			}
		}
		return filteredList;
	}

	private List<InventoryPojo> processOlapicInventorySearchResults(List<SearchRowList> recListList) {
		List<InventoryPojo> result = new ArrayList<>();
		for (SearchRowList rowList:recListList){
			if (rowList!=null&&rowList.getSearchRow()!=null){
				for (SearchRow row : rowList.getSearchRow()){
					if (row!=null){
						ItemSearchRow itemRow = (ItemSearchRow) row;
						ItemSearchRowBasic itemRowBasic = itemRow.getBasic();
						SearchColumnSelectField[] scself = null;
						SearchColumnStringField[] scstrf = null;						
						OlapicInventoryItemPojo itemPojo = new OlapicInventoryItemPojo();
						scself = itemRowBasic.getInternalId(); 
						if (scself!=null){
							itemPojo.setInternalId(scself[0].getSearchValue().getInternalId());
						}
						scstrf = itemRowBasic.getItemUrl(); 
						if (scstrf!=null){
							itemPojo.setItemUrl(scstrf[0].getSearchValue());
						}
						scstrf = itemRowBasic.getStoreDisplayName(); 
						if (scstrf!=null){
							String incorrectName = scstrf[0].getSearchValue();
							String correctName;
							if (incorrectName.contains(","))
							{
								correctName="\""+incorrectName+"\"";
							}
							else {
								correctName = incorrectName;
							}							
							itemPojo.setDisplayName(correctName);
						}
						if (itemRowBasic.getCustomFieldList()!=null){
							SearchColumnCustomFieldList itemCustomFieldList =  itemRowBasic.getCustomFieldList();
							if (itemCustomFieldList!=null){
								SearchColumnCustomField[] sccfs = itemCustomFieldList.getCustomField();
								if (sccfs!=null){
									for (SearchColumnCustomField sccf :sccfs ){
										if (sccf instanceof SearchColumnStringCustomField){
											SearchColumnStringCustomField scscf = (SearchColumnStringCustomField)sccf;
											if (scscf!=null){
												if (scscf.getScriptId().equalsIgnoreCase("custitem_display_image")){
													itemPojo.setItemDisplayImage(scscf.getSearchValue());
												}
											}
										}
									}
								}
							}							
						}
						result.add(itemPojo);
					}
				}
			}			
		}
		return result;
	}
	
	
} 
