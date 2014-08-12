package com.malkos.poppin.bootstrap;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.malkos.poppin.schedullers.CancelledOrdersFlowScheduller;
import com.malkos.poppin.schedullers.FreeDiskSpaceScheduller;
import com.malkos.poppin.schedullers.InventoryUpdateFlowScheduller;
import com.malkos.poppin.schedullers.InvoiceMessageFlowScheduller;
import com.malkos.poppin.schedullers.PullFilesFromCHScheduller;
import com.malkos.poppin.schedullers.PurchaseOrderFlowScheduller;
import com.malkos.poppin.schedullers.PushFilesToCHScheduller;
import com.malkos.poppin.schedullers.ResendEmailFlowScheduller;
import com.malkos.poppin.schedullers.ShippingConfirmationFlowScheduller;
import com.malkos.poppin.utils.EnvironmentInitializer;


public class IntegrationBootStraper {
	public static void main(String args[]){
		Logger logger = LoggerFactory.getLogger(IntegrationBootStraper.class);
		logger.info("Starting the main application.");	
		try{
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/META-INF/spring/app-context.xml");	
			//PullFilesFromCHScheduller pullFiles = (PullFilesFromCHScheduller)context.getBean("pullFilesFromCHScheduler");
			//pullFiles.processTask();
			
			//FreeDiskSpaceScheduller freeSpace = (FreeDiskSpaceScheduller) context.getBean("clearDiskFlowScheduler");
			//freeSpace.processTask();
			
			//PurchaseOrderFlowScheduller PoFlow = (PurchaseOrderFlowScheduller)context.getBean("poFlowScheduler");
			//PoFlow.processTask();						
			
			//ShippingConfirmationFlowScheduller scFlow = (ShippingConfirmationFlowScheduller)context.getBean("scFlowScheduler");
			//scFlow.processTask();	
			
			//InvoiceMessageFlowScheduller oiFlow = (InvoiceMessageFlowScheduller)context.getBean("oiFlowScheduler");
			//oiFlow.processTask();	
			
			//InventoryUpdateFlowScheduller iuFlow = (InventoryUpdateFlowScheduller)context.getBean("iuFlowScheduler");
			//iuFlow.processTask();	
			
			//PushFilesToCHScheduller pushFiles = (PushFilesToCHScheduller)context.getBean("pushFilesToCHScheduler");
			//pushFiles.processTask();	
			
			//CancelledOrdersFlowScheduller cancelledOrdersFlowScheduller = (CancelledOrdersFlowScheduller)context.getBean("cancelledOrdersFlowScheduler");
			//cancelledOrdersFlowScheduller.processTask();
			
			//ResendEmailFlowScheduller resendScheduller = context.getBean("resendEmailFlowScheduler", ResendEmailFlowScheduller.class);
			//resendScheduller.processTask();
		} catch (Exception e){
			System.out.println("Couldn't initialize application context. Check application properties. Application will be terminated...");
			e.printStackTrace();
			System.exit(0);
		}
		logger.info("Staples-Poppin integration application V 4.0 \r\n" +
		"Initializing finished. Waiting for one of the task to get started...");	
			
	}
}
