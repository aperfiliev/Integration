package com.malkos.poppin.integration.houzz.transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;

import com.malkos.poppin.integration.houzz.bootstrap.GlobalProperties;
import com.malkos.poppin.integration.houzz.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.util.ErrorsCollector;

public class FTPFileManager implements IRemoteFileManager {
	FTPClient ftpClient;	

	@Override
	public void openSession(RetailerAbstract retailer) throws IOException {
		ftpClient= new FTPClient();		
		ftpClient.connect(retailer.getFtpConfigHost(), Integer.parseInt(retailer.getFtpConfigPort()));	
		checkReply(ftpClient);
		ftpClient.login(retailer.getFtpConfigUse(), retailer.getFtpConfigPassword());
		checkReply(ftpClient);
		ftpClient.enterLocalPassiveMode();
		checkReply(ftpClient);		
	}


	@Override
	public void closeSession() throws IOException {
		ftpClient.logout();
		ftpClient.disconnect();		
	}


	@Override
	public boolean sendFileContent(InputStream streamToSend, String pathToContentFile) throws IOException {
		 boolean isUploaded = false;          	
	     isUploaded = ftpClient.storeFile(pathToContentFile, streamToSend);	
	     checkReply(ftpClient);
	     return isUploaded;
	}
	
	private void checkReply( FTPClient ftpClient){
		if ((ftpClient.getReplyCode() /100) >= 4 ){
	    	 ErrorsCollector.addCommonPushFilesErrorMessage(ftpClient.getReplyString());
	     }
	}
}
