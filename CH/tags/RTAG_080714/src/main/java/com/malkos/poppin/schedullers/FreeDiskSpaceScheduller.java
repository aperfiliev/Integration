package com.malkos.poppin.schedullers;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;



public class FreeDiskSpaceScheduller {
	
	private static Logger logger = LoggerFactory.getLogger(FreeDiskSpaceScheduller.class);
	private static GlobalProperties properties;
	
	static{
		properties = GlobalPropertiesProvider.getGlobalProperties();	
	}
	
	@Autowired
	private SimpleMailMessage notificationMailMessage;
	
	@Autowired
	private MailSender mailSender;

	public void processTask(){	
		logger.info("Free Disk Space Flow execution started ...");
		List<String> errorsList = new ArrayList<>();
		List<String> directoriesToDelete = new ArrayList<>();
		
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		String localArchivePath = properties.getLocalMessagesArchiveRootPath();
		String requestResponseDirectoryPath = properties.getRequestResponseDirectory();
				
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -14);
		for (int  i = 0; i< 30; i++){		
			String directory = localArchivePath + File.separator + dateFormat.format(calendar.getTime()) + File.separator + requestResponseDirectoryPath;
			directoriesToDelete.add(directory);
			calendar.add(Calendar.DATE, -1);
		}	
		try{				
			for (String directory:directoriesToDelete){
				try{
					FileUtils.deleteDirectory(new File(directory));
				} catch (Exception ex){
					String errorMessage = "Couldn't remove directory '"+directory+"'. Reason: "+ex.getMessage()+". Please remove it manually.";
					errorsList.add(errorMessage);
				}				
			}
			StringBuilder sBuilder = new StringBuilder();
			
			if(! errorsList.isEmpty()){
				int count = 1;
				sBuilder.append("The following common errors has been appeared during processing the Free Disk Space flow : \r\n");				
				for(String message : errorsList){
					sBuilder.append(count+". "+message + "\r\n");
					count++;
				}
			}			
			if(sBuilder.length() > 0){
				SimpleMailMessage mailMessage = new SimpleMailMessage(notificationMailMessage);
				String todayNow = new SimpleDateFormat("MMddyyyy_HHmm").format(new Date());
				mailMessage.setSubject(mailMessage.getSubject() + " - " + todayNow);
				mailMessage.setText(sBuilder.toString());
				mailSender.send(mailMessage);
			}
		}catch(Exception e){
			String errorMessage = "An unexpected error has occurred while executing the Free Disk Space flow. Error message : \r\n" + e.getMessage();
			logger.warn(errorMessage);
			if(logger.isDebugEnabled())
				logger.debug(e.getMessage(), e);
			SimpleMailMessage mailMessage = new SimpleMailMessage(notificationMailMessage);
			mailMessage.setText(errorMessage);
			mailSender.send(mailMessage);
		}finally{
			logger.info("Releasing the occuped resources.");			
		}
	}
}
