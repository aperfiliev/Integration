package com.malkos.poppin.integration.houzz.bootstrap;


import org.quartz.JobExecutionException;

import com.malkos.poppin.integration.houzz.schedullers.HouzzInventoryUpdateFlowScheduller;
import com.malkos.poppin.integration.houzz.schedullers.OlapicInventoryUpdateFlowScheduller;
import com.malkos.poppin.integration.houzz.schedullers.PushFilesToStorageScheduller;
import com.malkos.poppin.integration.houzz.schedullers.ResendEmailFlowScheduller;
import com.malkos.poppin.integration.houzz.services.IInventoryListUpdateble;
import com.malkos.poppin.integration.houzz.services.IInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.services.impl.HouzzInventoryUpdateFlowService;
import com.malkos.poppin.integration.houzz.util.EnvironmentInitializer;

public class IntegrationBootstraper {
	public static void main(String[] args) {		
		//This is a service portion of code for first time application start for database fill.
		if (GlobalPropertiesProvider.getGlobalProperties().isFirstRun()){
			IInventoryListUpdateble inventoryUpdateService = new HouzzInventoryUpdateFlowService();
			inventoryUpdateService.updateItemList();
		} else {
			EnvironmentInitializer.initializeSchedullers();
		}
		
		/*HouzzInventoryUpdateFlowScheduller invUpdate = new HouzzInventoryUpdateFlowScheduller();
		try {
			invUpdate.execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*OlapicInventoryUpdateFlowScheduller invUpdateOlapic = new OlapicInventoryUpdateFlowScheduller();
		try {
			invUpdateOlapic.execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*PushFilesToStorageScheduller pushFiles = new PushFilesToStorageScheduller();
		try {
			pushFiles.execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*ResendEmailFlowScheduller resendEmail = new ResendEmailFlowScheduller();
		try {
			resendEmail.execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}	
}
