package com.malkos.poppin.bootstrap;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.malkos.poppin.schedullers.AdvanceShippingFlowScheduller;
import com.malkos.poppin.schedullers.CancelledOrdersFlowScheduller;
import com.malkos.poppin.schedullers.OrderInvoicesFlowScheduller;
import com.malkos.poppin.schedullers.PullFilesFromSpsScheduller;
import com.malkos.poppin.schedullers.PurchaseOrderFlowScheduller;
import com.malkos.poppin.schedullers.PushFilesToSpsScheduller;
import com.malkos.poppin.schedullers.FreeDiskSpaceScheduller;
import com.malkos.poppin.schedullers.ResendEmailFlowScheduller;
public class IntegrationBootstraper {
	private static Logger logger = LoggerFactory.getLogger(IntegrationBootstraper.class);
	public static void main(String args[]){
		logger.info("Starting the main application.");
		logger.info("Initializing Spring context: it might take some time because of searching for Netsuite datacenter location.");
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/META-INF/spring/app-context.xml");
			logger.info("Context initialized.");
			PoppinDataLoader poppinDataLoader = context.getBean("poppinDataLoader", PoppinDataLoader.class);
			poppinDataLoader.loadIntegrationRequiredPoppinData();
			logger.info("Evnvironment is ready. Releasing the schedulled tasks blocker.");
			SchedulledTasksSynchronizer.setEnvironmentIsReady(true);			
			//FreeDiskSpaceScheduller clearDiskScheduller = context.getBean("clearDiskFlowScheduler", FreeDiskSpaceScheduller.class);
			//clearDiskScheduller.processTask();
			//PullFilesFromSpsScheduller pullFilesScheduller = context.getBean("pullFilesFromSpsScheduler", PullFilesFromSpsScheduller.class);
			//pullFilesScheduller.processTask();
			//PurchaseOrderFlowScheduller poScheduller = context.getBean("purchaseOrderFlowScheduler", PurchaseOrderFlowScheduller.class);
			//poScheduller.processTask();				
			//AdvanceShippingFlowScheduller asnScheduller = context.getBean("advanceShippingFlowScheduler", AdvanceShippingFlowScheduller.class);
			//asnScheduller.processTask();
			//OrderInvoicesFlowScheduller oiScheduller = context.getBean("orderInvoicesFlowScheduller", OrderInvoicesFlowScheduller.class);
			//oiScheduller.processTask();
			//PushFilesToSpsScheduller pushFilesScheduller = context.getBean("pushFilesToSpsScheduler", PushFilesToSpsScheduller.class);
			//pushFilesScheduller.processTask();
			//CancelledOrdersFlowScheduller cancelledOrdersScheduller = context.getBean("cancelledOrdersFlowScheduler", CancelledOrdersFlowScheduller.class);
			//cancelledOrdersScheduller.processTask();	
			//ResendEmailFlowScheduller resendScheduller = context.getBean("resendEmailFlowScheduler", ResendEmailFlowScheduller.class);
			//resendScheduller.processTask();
		} catch (Exception e) {
			logger.error("Could not initialize spring context. Reason : " + e.getMessage() + ". Exiting from application.");
			System.exit(0);
		}
	}
}
