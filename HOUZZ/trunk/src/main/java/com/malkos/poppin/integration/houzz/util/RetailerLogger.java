package com.malkos.poppin.integration.houzz.util;

import org.slf4j.Logger;

public abstract class RetailerLogger {
	public RetailerLogger(){};
	protected static Logger logger;// = LoggerFactory.getLogger(OlapicInventoryUpdateLogger.class);	
	public abstract void addMessage(String message);
	public abstract void addError(String message);
}
