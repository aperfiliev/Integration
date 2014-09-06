package com.malkos.poppin.integration.houzz.bootstrap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream("dev.properties"));
			} catch (IOException e) {
				System.out.println("Can't load application properties file. Application will be terminated");
				System.exit(0);
			}
			properties = new GlobalProperties();
			//Common
			properties.setEnvironmentName(prop.getProperty("environment_name"));
			properties.setIntegrationPushFilesSchedulingCron(prop.getProperty("integration_push_files_scheduling_cron"));
			properties.setResendEmailSchedulingCron(prop.getProperty("integration_resend_email_scheduling_cron"));
			properties.setNetsuiteConfigAccount(prop.getProperty("netsuite_config_account"));			
			properties.setNetsuiteConfigEmail(prop.getProperty("netsuite_config_email"));
			properties.setNetsuiteConfigPassword(prop.getProperty("netsuite_config_password"));
			properties.setNetsuiteConfigRole(prop.getProperty("netsuite_config_role"));
			properties.setNetsuiteConfigUrl(prop.getProperty("netsuite_config_url"));	
			properties.setMessagesDirectoryRoot(prop.getProperty("messages_directory_root"));
			properties.setOutgoingMessagesDirectory(prop.getProperty("outgoing_messages_directory"));
			properties.setFirstRun(Boolean.parseBoolean(prop.getProperty("is_first_run")));	
			properties.setInventoryUpdateAutoretryAttempts(Integer.parseInt(prop.getProperty("inventory_update_autoretry_attempts")));
			properties.setInventoryUpdateAutoretryIntervalMinutes(Integer.parseInt(prop.getProperty("inventory_update_autoretry_interval_minutes")));
			
			
			//HOUZZ
			properties.setIntegrationHouzzInventoryUpdateSchedulingCron(prop.getProperty("integration_houzz_inventory_update_scheduling_cron"));					
			properties.setHouzzFtpConfigHost(prop.getProperty("houzz_ftp_config_host"));
			properties.setHouzzFtpConfigUse(prop.getProperty("houzz_ftp_config_user"));
			properties.setHouzzFtpConfigPassword(prop.getProperty("houzz_ftp_config_password"));
			properties.setHouzzFtpConfigPort(prop.getProperty("houzz_ftp_config_port"));			
			properties.setHouzzIncomingFilesDirName(prop.getProperty("houzz_incoming_files_dir_name"));						
			properties.setHouzzNotificationEmailsTo(prop.getProperty("houzz_notification_emails_to"));
			properties.setHouzzInventoryNotificationEmailsTo(prop.getProperty("houzz_inventory_notification_emails_to"));
			properties.setHouzzNotificationEmailFrom(prop.getProperty("houzz_notification_email_from"));
			properties.setHouzzNotificationEmailSubject(prop.getProperty("houzz_notification_email_subject"));			
			properties.setHouzzNotificationEmailServerHost(prop.getProperty("houzz_notification_email_server_host"));
			properties.setHouzzNotificationEmailServerPort(prop.getProperty("houzz_notification_email_server_port"));
			properties.setHouzzNotificationEmailServerSmtpUser(prop.getProperty("houzz_notification_email_server_smtp_user"));
			properties.setHouzzNotificationEmailServerStmpPassword(prop.getProperty("houzz_notification_email_server_stmp_password"));
			
			
			//OLAPIC
			properties.setIntegrationOlapicInventoryUpdateSchedulingCron(prop.getProperty("integration_olapic_inventory_update_scheduling_cron"));					
			properties.setOlapicFtpConfigHost(prop.getProperty("olapic_ftp_config_host"));
			properties.setOlapicFtpConfigUse(prop.getProperty("olapic_ftp_config_user"));
			properties.setOlapicFtpConfigPassword(prop.getProperty("olapic_ftp_config_password"));
			properties.setOlapicFtpConfigPort(prop.getProperty("olapic_ftp_config_port"));			
			properties.setOlapicIncomingFilesDirName(prop.getProperty("olapic_incoming_files_dir_name"));						
			properties.setOlapicNotificationEmailsTo(prop.getProperty("olapic_notification_emails_to"));
			properties.setOlapicInventoryNotificationEmailsTo(prop.getProperty("olapic_inventory_notification_emails_to"));
			properties.setOlapicNotificationEmailFrom(prop.getProperty("olapic_notification_email_from"));
			properties.setOlapicNotificationEmailSubject(prop.getProperty("olapic_notification_email_subject"));			
			properties.setOlapicNotificationEmailServerHost(prop.getProperty("olapic_notification_email_server_host"));
			properties.setOlapicNotificationEmailServerPort(prop.getProperty("olapic_notification_email_server_port"));
			properties.setOlapicNotificationEmailServerSmtpUser(prop.getProperty("olapic_notification_email_server_smtp_user"));
			properties.setOlapicNotificationEmailServerStmpPassword(prop.getProperty("olapic_notification_email_server_stmp_password"));
		}
		return properties;
	}
}
