package com.malkos.poppin.integration.houzz.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.ThreadPool;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalProperties;
import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.RetailerManager;
import com.malkos.poppin.integration.houzz.schedullers.HouzzInventoryUpdateFlowScheduller;
import com.malkos.poppin.integration.houzz.schedullers.OlapicInventoryUpdateFlowScheduller;
import com.malkos.poppin.integration.houzz.schedullers.PushFilesToStorageScheduller;
import com.malkos.poppin.integration.houzz.schedullers.ResendEmailFlowScheduller;

public class EnvironmentInitializer {
	
	private static GlobalProperties properties;
	static{
		properties = GlobalPropertiesProvider.getGlobalProperties();
		RetailerManager.initializeRetailers();
	}
		
	public static void initializeDirectoriesEnvironment(){
		String messagesDirectoryRootPath = properties.getMessagesDirectoryRoot();
		String outgoingMessagesDirectoryPath = messagesDirectoryRootPath + File.separator + properties.getOutgoingMessagesDirectory();
		File messagesRootDirectory = new File(messagesDirectoryRootPath);
		if(!messagesRootDirectory.exists()){
			messagesRootDirectory.mkdirs();			
			new File(outgoingMessagesDirectoryPath).mkdir();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String currentYear = Integer.toString(calendar.get(Calendar.YEAR));
		String currentMonth = new SimpleDateFormat("M").format(calendar.getTime());
		String currentDay = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		
		File outgoingMessagesCurrentYearDirectory = new File(outgoingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
		
		if(!outgoingMessagesCurrentYearDirectory.exists()){			
			outgoingMessagesCurrentYearDirectory.mkdirs();
		}		
		properties.setCurrentOutgoingMessagesDirectory(outgoingMessagesDirectoryPath + File.separator + currentYear + File.separator + currentMonth + File.separator + currentDay);
	}
	
	public static void initializeSchedullers() {
		SchedulerFactory sf = new StdSchedulerFactory();		
		Scheduler sched;
		try {			
			JobDetail jobHouzzInventoryUpdateFlow = new JobDetail("HouzzInventoryUpdateJob", Scheduler.DEFAULT_GROUP, HouzzInventoryUpdateFlowScheduller.class);
			CronTrigger triggerHouzzInventoryUpdateFlow = new CronTrigger("HouzzInventoryUpdateTrigger", Scheduler.DEFAULT_GROUP, "HouzzInventoryUpdateJob", Scheduler.DEFAULT_GROUP, GlobalPropertiesProvider.getGlobalProperties().getIntegrationHouzzInventoryUpdateSchedulingCron());
			
			JobDetail jobOlapicInventoryUpdateFlow = new JobDetail("OlapicInventoryUpdateJob", Scheduler.DEFAULT_GROUP, OlapicInventoryUpdateFlowScheduller.class);
			CronTrigger triggerOlapicInventoryUpdateFlow = new CronTrigger("OlapicInventoryUpdateTrigger", Scheduler.DEFAULT_GROUP, "OlapicInventoryUpdateJob", Scheduler.DEFAULT_GROUP, GlobalPropertiesProvider.getGlobalProperties().getIntegrationOlapicInventoryUpdateSchedulingCron());
			
			JobDetail jobPushFilesFlow = new JobDetail("PushFilesJob", Scheduler.DEFAULT_GROUP, PushFilesToStorageScheduller.class);
			CronTrigger triggerPushFilesFlow = new CronTrigger("PushFilesTrigger", Scheduler.DEFAULT_GROUP, "PushFilesJob", Scheduler.DEFAULT_GROUP, GlobalPropertiesProvider.getGlobalProperties().getIntegrationPushFilesSchedulingCron());
			
			JobDetail jobResendEmailFlow = new JobDetail("ResendEmailJob", Scheduler.DEFAULT_GROUP, ResendEmailFlowScheduller.class);
			CronTrigger triggerResendEmailFlow = new CronTrigger("ResendEmailTrigger", Scheduler.DEFAULT_GROUP, "ResendEmailJob", Scheduler.DEFAULT_GROUP, GlobalPropertiesProvider.getGlobalProperties().getResendEmailSchedulingCron());
			
			sched = sf.getScheduler();			
			sched.start();
			sched.scheduleJob(jobResendEmailFlow, triggerResendEmailFlow);
			sched.scheduleJob(jobHouzzInventoryUpdateFlow, triggerHouzzInventoryUpdateFlow);
			sched.scheduleJob(jobOlapicInventoryUpdateFlow, triggerOlapicInventoryUpdateFlow);
			sched.scheduleJob(jobPushFilesFlow, triggerPushFilesFlow);					
			
		} catch (SchedulerException | ParseException e) {			
			e.printStackTrace();
		}	

	}
}
