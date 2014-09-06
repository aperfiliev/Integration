package com.malkos.poppin.entities;

import com.netsuite.webservices.platform.core_2014_1.SearchResult;

public class SearchResultWrapped {
	private NSRrequestDetails requestDeatils;
	private SearchResult searchResult;	
	
	public SearchResultWrapped(NSRrequestDetails requestDeatils, SearchResult searchResult){
		this.setSearchResult(searchResult);
		this.requestDeatils = requestDeatils;
	}
	
	public NSRrequestDetails getRequestDeatils() {
		return requestDeatils;
	}
	public void setRequestDeatils(NSRrequestDetails requestDeatils) {
		this.requestDeatils = requestDeatils;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(SearchResult searchResult) {
		this.searchResult = searchResult;
	}
		
}
