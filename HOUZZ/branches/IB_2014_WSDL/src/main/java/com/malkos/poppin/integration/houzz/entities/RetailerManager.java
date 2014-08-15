package com.malkos.poppin.integration.houzz.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;

public class RetailerManager {
	private static Map<RetailerEnum,RetailerAbstract> _retailers; 
	public static synchronized void initializeRetailers() {		
		_retailers = new HashMap<>();
		_retailers.put(RetailerEnum.OLAPIC, new OlapicRetailer());
		_retailers.put(RetailerEnum.HOUZZ, new HouzzRetailer());
	}
	public static synchronized Collection<RetailerAbstract> get_retailers() {
		return _retailers.values();
	}
	public static synchronized RetailerAbstract get_retailer(RetailerEnum id) {
		return _retailers.get(id);
	}
}
