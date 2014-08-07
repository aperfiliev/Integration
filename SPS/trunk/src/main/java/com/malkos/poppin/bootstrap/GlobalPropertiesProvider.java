package com.malkos.poppin.bootstrap;

import org.springframework.context.ApplicationContext;

public class GlobalPropertiesProvider {
	
	private String purchaseOrdersPath;
	
	private static GlobalProperties properties;
	
	public String getPurchaseOrdersPath() {
		return purchaseOrdersPath;
	}

	public void setPurchaseOrdersPath(String purchaseOrdersPath) {
		this.purchaseOrdersPath = purchaseOrdersPath;
	}
	public static synchronized GlobalProperties getGlobalProperties(){
		if(properties == null){
			ApplicationContext context = ApplicationContextProvider
					.getApplicationContext();
			properties = (GlobalProperties) context
					.getBean("globalProperties");
		}
		return properties;
	}
}
