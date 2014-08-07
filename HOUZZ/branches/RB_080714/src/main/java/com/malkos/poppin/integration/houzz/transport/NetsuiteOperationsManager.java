package com.malkos.poppin.integration.houzz.transport;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitPojo;
import com.malkos.poppin.integration.houzz.entities.HouzzInventoryKitSubItemPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.OlapicInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.SearchResultWrapped;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;
import com.netsuite.webservices.lists.accounting_2013_1.InventoryItem;
import com.netsuite.webservices.lists.accounting_2013_1.InventoryItemLocations;
import com.netsuite.webservices.lists.accounting_2013_1.ItemMember;
import com.netsuite.webservices.lists.accounting_2013_1.ItemSearch;
import com.netsuite.webservices.lists.accounting_2013_1.ItemSearchAdvanced;
import com.netsuite.webservices.lists.accounting_2013_1.ItemSearchRow;
import com.netsuite.webservices.lists.accounting_2013_1.KitItem;
import com.netsuite.webservices.lists.accounting_2013_1.Pricing;
import com.netsuite.webservices.platform.common_2013_1.ItemSearchBasic;
import com.netsuite.webservices.platform.common_2013_1.ItemSearchRowBasic;
import com.netsuite.webservices.platform.core_2013_1.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core_2013_1.CustomFieldList;
import com.netsuite.webservices.platform.core_2013_1.CustomFieldRef;
import com.netsuite.webservices.platform.core_2013_1.Record;
import com.netsuite.webservices.platform.core_2013_1.RecordList;
import com.netsuite.webservices.platform.core_2013_1.RecordRef;
import com.netsuite.webservices.platform.core_2013_1.SearchBooleanField;
import com.netsuite.webservices.platform.core_2013_1.SearchColumnCustomField;
import com.netsuite.webservices.platform.core_2013_1.SearchColumnCustomFieldList;
import com.netsuite.webservices.platform.core_2013_1.SearchColumnSelectField;
import com.netsuite.webservices.platform.core_2013_1.SearchColumnStringCustomField;
import com.netsuite.webservices.platform.core_2013_1.SearchColumnStringField;
import com.netsuite.webservices.platform.core_2013_1.SearchMultiSelectField;
import com.netsuite.webservices.platform.core_2013_1.SearchResult;
import com.netsuite.webservices.platform.core_2013_1.SearchRow;
import com.netsuite.webservices.platform.core_2013_1.SearchRowList;
import com.netsuite.webservices.platform.core_2013_1.SearchStringField;
import com.netsuite.webservices.platform.core_2013_1.StringCustomFieldRef;
import com.netsuite.webservices.platform.core_2013_1.types.RecordType;
import com.netsuite.webservices.platform.core_2013_1.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core_2013_1.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform_2013_1.NetSuiteServiceLocator;


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
	
	public Map<String,List> loadHouzzInventory(List<LineItemIntegrationIdentifierDAO> lineItemDAOList) throws NetsuiteOperationException{
		SearchResult result=null;
		List<RecordList> recListList = new ArrayList<>();
		Map<String,LineItemIntegrationIdentifierDAO> internalIdtoLineItemDAOMap = new HashMap<>();
		
		int counter = 1;
		List<LineItemIntegrationIdentifierDAO> bufferList = new ArrayList<>();
		List<List<LineItemIntegrationIdentifierDAO>> bufferListList = new ArrayList<>();
		for (LineItemIntegrationIdentifierDAO lineItemDAO:lineItemDAOList){
			internalIdtoLineItemDAOMap.put(lineItemDAO.getItemInternalId(), lineItemDAO);
			if (counter % 101 ==0 ){
				bufferListList.add(bufferList);
				bufferList = new ArrayList<>();
				counter = 1;
			}
			bufferList.add(lineItemDAO);
			counter++;
		}
		bufferListList.add(bufferList);		
		 for (List<LineItemIntegrationIdentifierDAO> itemsBufferedList : bufferListList){
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
					try {						
						result = netsuiteService.search(is, false).getSearchResult();							
						recListList.add(result.getRecordList());
						int totalPages = result.getTotalPages();
						if(totalPages > 1){
							String searchId = result.getSearchId();
							while(pageIndex <= totalPages){
								result = netsuiteService.searchMoreWithId(searchId, pageIndex, false).getSearchResult();
								recListList.add(result.getRecordList());
								pageIndex++;
							}
					}
					} catch (NetsuiteServiceException e) {
						throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
					}
			 }			 				
		 }		
		return proccessSearchResultsForInventoryUpdate(internalIdtoLineItemDAOMap, recListList, lineItemDAOList);
	}
	
	//here we separate search results for combined search and make list of nested Inventory items (inside KIT/PACKAGE items) that are required to load data from NS
	private Map<String,List> proccessSearchResultsForInventoryUpdate(Map<String,LineItemIntegrationIdentifierDAO> internalIdtolineItemDAOMap,List<RecordList> recListList, List<LineItemIntegrationIdentifierDAO> lineItemDAOList) throws NetsuiteOperationException{
		 List<InventoryItem> originNsDownloadedInventoryList = new ArrayList<InventoryItem>();
		 Map<String,List> inventoryTypeClassNameToInventoryPojoListMap = getSearchResultsFromLoadInventorySearch(recListList, originNsDownloadedInventoryList);		  
		 Set<String> iiIdsToPreloadSet = new HashSet<String>();		 
		 List<HouzzInventoryKitPojo> invKitList = inventoryTypeClassNameToInventoryPojoListMap.get(HouzzInventoryKitPojo.class.getName());
		 List<HouzzInventoryItemPojo> invItemList = inventoryTypeClassNameToInventoryPojoListMap.get(HouzzInventoryItemPojo.class.getName());
		 
		 checkForUnapropriateItems(lineItemDAOList, invKitList, invItemList);	
		 
		 for (HouzzInventoryKitPojo invKit:invKitList){
			 if (!invKit.isWrongConfigured()){
				 for (HouzzInventoryKitSubItemPojo subItem:invKit.getSubItemsList()){
					 iiIdsToPreloadSet.add(subItem.getInternalId());
				 }
			 }			
		 }
		 List <String> invItemIdsToPreloadList = new ArrayList<>();
		 for (String intID:iiIdsToPreloadSet){
			 if (!internalIdtolineItemDAOMap.containsKey(intID)){
				 invItemIdsToPreloadList.add(intID); 
			 }
		 }
		 
		 if (invItemIdsToPreloadList.size()>0){			
			 loadKitPackageInventory(invItemIdsToPreloadList, originNsDownloadedInventoryList);
		 }
		 
		 Map<String,InventoryItem> internalIdToInventoryItemMap = new HashMap<>();
		 for (InventoryItem invItem:originNsDownloadedInventoryList){
			 internalIdToInventoryItemMap.put(invItem.getInternalId(), invItem);
		 }
		 		 
		 for (HouzzInventoryKitPojo invKitPojo:invKitList){
			 if (!invKitPojo.isWrongConfigured()){
				 for (HouzzInventoryItemPojo invSubItemPojo:invKitPojo.getSubItemsList()){
					 InventoryItem apropriateNsInventory = internalIdToInventoryItemMap.get(invSubItemPojo.getInternalId());				 
					 for (InventoryItemLocations location:apropriateNsInventory.getLocationsList().getLocations()){
						 if (location.getLocationId().getInternalId().equalsIgnoreCase(invKitPojo.getNsLocationId())){
							 invSubItemPojo.setQtyAvailable(location.getQuantityAvailable());
						 }
					 }	
					 if (apropriateNsInventory.getIsInactive()!=null){
						 invSubItemPojo.setInactive(apropriateNsInventory.getIsInactive());
					 } else {
						 invSubItemPojo.setInactive(false);
					 }
				 }
			 }			 
		 }		 
		return inventoryTypeClassNameToInventoryPojoListMap;
	}

	private void checkForUnapropriateItems(	List<LineItemIntegrationIdentifierDAO> lineItemDAOList,	List<HouzzInventoryKitPojo> invKitList,	List<HouzzInventoryItemPojo> invItemList) {
		for (LineItemIntegrationIdentifierDAO lineItemDAO:lineItemDAOList){			
			boolean isItemInKitList = false;
			boolean isItemInInventoryList = false;
			for (HouzzInventoryKitPojo kitPojo:invKitList){
				if (kitPojo.getNsInternalId().equalsIgnoreCase(lineItemDAO.getItemInternalId())){
					isItemInKitList = true;
					break;
				}
			}
			for (HouzzInventoryItemPojo itemPojo:invItemList){
				if (itemPojo.getInternalId().equalsIgnoreCase(lineItemDAO.getItemInternalId())){
					isItemInInventoryList = true;
					break;
				}
			}
			if (!isItemInKitList && !isItemInInventoryList){
				ErrorsCollector.getNsInventoryConfigurationError().add("Couldn't find appropriate to ItemInternalId = '"+lineItemDAO.getItemInternalId()+"' (SKU='"+lineItemDAO.getSKU()+"') inventory in Poppin NetSuite database");
			}
		}		
	}

	//here we process search results for combined (KIT/PACKAGE and INVENTORY ITEM) Inventory search
	private Map<String,List> getSearchResultsFromLoadInventorySearch(List<RecordList> recListList,  List<InventoryItem> originNsDownloadedInventoryList){
		Map<String,List> inventoryTypeClassNameToInventoryPojoListMap = new HashMap<>(); 			
		inventoryTypeClassNameToInventoryPojoListMap.put(HouzzInventoryItemPojo.class.getName(), new ArrayList<HouzzInventoryItemPojo>());
		inventoryTypeClassNameToInventoryPojoListMap.put(HouzzInventoryKitPojo.class.getName(), new ArrayList<HouzzInventoryKitPojo>());
		
		for (RecordList recList:recListList){
			if (recList!=null){
				for (Record rec:recList.getRecord()){
					if (rec!=null){
						if (rec instanceof InventoryItem){
							InventoryItem invItem = (InventoryItem)rec;
							originNsDownloadedInventoryList.add(invItem);
							HouzzInventoryItemPojo itemPojo = new HouzzInventoryItemPojo();							
							InventoryItemLocations[] invItemsLocations=invItem.getLocationsList().getLocations();							
							if (invItem.getPreferredLocation()!=null){
								for (InventoryItemLocations location:invItemsLocations){								
									if (location.getLocationId().getInternalId().equalsIgnoreCase(invItem.getPreferredLocation().getInternalId())){ //Inventory preferred location
										Double quantityAvailable=location.getQuantityAvailable();
										if (quantityAvailable!=null){
											itemPojo.setQtyAvailable(quantityAvailable);
										} else{
											itemPojo.setQtyAvailable(0);
										}
										break;
									}
								}
							} else {
								ErrorsCollector.getNsInventoryConfigurationError().add("Prefered location is 'null' for inventory with ItemInternalId = '"+invItem.getInternalId()+"' (SKU = '"+invItem.getItemId()+"')");
								itemPojo.setQtyAvailable(0);
								itemPojo.setWrongConfigured(true);
							}					
							itemPojo.setInternalId(invItem.getInternalId());	
							itemPojo.setSKU(invItem.getItemId());
							if (invItem.getIsInactive()!=null){
								itemPojo.setInactive(invItem.getIsInactive());
							} else {
								itemPojo.setInactive(false);
							}							
							for (Pricing pricing:invItem.getPricingMatrix().getPricing()){
								if (pricing.getPriceLevel().getInternalId().equalsIgnoreCase("1")){ //Base price level
									itemPojo.setPrice(pricing.getPriceList().getPrice()[0].getValue()); // Get price value for base price level
								}
							}	
							inventoryTypeClassNameToInventoryPojoListMap.get(HouzzInventoryItemPojo.class.getName()).add(itemPojo);
							}
						else if (rec instanceof KitItem){
							KitItem kitItem = (KitItem)rec;
							ItemMember[] itemMembers = kitItem.getMemberList().getItemMember();
							List<HouzzInventoryKitSubItemPojo> subItemsList = new ArrayList<>();
							for (ItemMember itemMember:itemMembers){
								HouzzInventoryKitSubItemPojo subItem = new HouzzInventoryKitSubItemPojo();
								subItem.setInternalId(itemMember.getItem().getInternalId());
								subItem.setQtyInKit(itemMember.getQuantity());
								subItemsList.add(subItem);
							}
							HouzzInventoryKitPojo invKit = new HouzzInventoryKitPojo();
							invKit.setSubItemsList(subItemsList);
							invKit.setNsInternalId(kitItem.getInternalId());
							invKit.setSKU(kitItem.getItemId());
							if (kitItem.getIsInactive()!=null){
								invKit.setInactive(kitItem.getIsInactive());
							} else {
								invKit.setInactive(false);
							}
							if (kitItem.getLocation()!=null){
								invKit.setNsLocationId(kitItem.getLocation().getInternalId());
							} else {
								ErrorsCollector.getNsInventoryConfigurationError().add("Location is 'null' for kit with ItemInternalId = '"+invKit.getNsInternalId()+"' (SKU = '"+invKit.getSKU()+"')");
								invKit.setWrongConfigured(true);
							}						
							for (Pricing pricing:kitItem.getPricingMatrix().getPricing()){
								if (pricing.getPriceLevel().getInternalId().equalsIgnoreCase("1")){ //Base price level
									invKit.setPrice(pricing.getPriceList().getPrice()[0].getValue()); // Get price value for base price level
								}
							}					
							inventoryTypeClassNameToInventoryPojoListMap.get(HouzzInventoryKitPojo.class.getName()).add(invKit);
						}
					}
				}
			}
		}		
		return inventoryTypeClassNameToInventoryPojoListMap;
	}
	
	// here we make another one request to NS for items contains in KIT/PACKAGE
	private void loadKitPackageInventory(List<String> internalIds, List<InventoryItem> originNsDownloadedInventoryList) throws NetsuiteOperationException{
		SearchResult result=null;
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
				result = netsuiteService.search(is, false).getSearchResult();		
				recListList.add(result.getRecordList());
				int totalPages = result.getTotalPages();
				if(totalPages > 1){
					String searchId = result.getSearchId();
					while(pageIndex <= totalPages){
						result = netsuiteService.searchMoreWithId(searchId, pageIndex, false).getSearchResult();
						recListList.add(result.getRecordList());
						pageIndex++;
					}
			}
			} catch (NetsuiteServiceException e) {
				throw new NetsuiteOperationException(e.getMessage(),e.getRequestDetails());
			}	
		}
		processSearchResultsFromKitPackageInventory(recListList, originNsDownloadedInventoryList);	
	}
	
	//here we process search results for loadKitPackageInventory()
	private void processSearchResultsFromKitPackageInventory(List<RecordList> recListList, List<InventoryItem> originNsDownloadedInventoryList){
		List<HouzzInventoryItemPojo> result = new ArrayList<>();
		for (RecordList recList:recListList){
			if (recList!=null){
				for (Record rec:recList.getRecord()){
					if (rec!=null){
						if (rec instanceof InventoryItem){
							InventoryItem invItem = (InventoryItem)rec;
							originNsDownloadedInventoryList.add(invItem);							
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
	public List<InventoryItemPojo> loadOlapicInventory() throws NetsuiteOperationException {
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
		SearchColumnStringCustomField itemDisplayImage = new SearchColumnStringCustomField(null, "custitem_display_image", null);		
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
		List<InventoryItemPojo> pojoResults = processOlapicInventorySearchResults(recListList);		
		return filterOlapicInventory(pojoResults);
	}

	private List<InventoryItemPojo> filterOlapicInventory(List<InventoryItemPojo> pojoResults) {
		List<InventoryItemPojo> filteredList = new ArrayList<>();
		for (InventoryItemPojo item:pojoResults){
			if (item instanceof OlapicInventoryItemPojo){
				OlapicInventoryItemPojo olapicItem = (OlapicInventoryItemPojo) item;
				if (!olapicItem.getItemUrl().contains("http://shopping.")){
					filteredList.add(olapicItem);
				}
			}
		}
		return filteredList;
	}

	private List<InventoryItemPojo> processOlapicInventorySearchResults(List<SearchRowList> recListList) {
		List<InventoryItemPojo> result = new ArrayList<>();
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
												if (scscf.getInternalId().equalsIgnoreCase("custitem_display_image")){
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
