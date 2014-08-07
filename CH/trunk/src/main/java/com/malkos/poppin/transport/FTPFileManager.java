package com.malkos.poppin.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPFileManager implements IRemoteFileManager {
	FTPClient ftpClient = new FTPClient();	
	
	private static Logger logger = LoggerFactory
			.getLogger(FTPFileManager.class);
	
	private String hostName;
	private int port;
	private String userName;
	private String password;
	
	private Boolean setBinaryFileType;
	
	@Override
	public void openSession() throws IOException {			
		logger.info("Connectiong to remote FTP host : " + getHostName()
				+ ", port : " + getPort());
		ftpClient.connect(getHostName(), getPort());
		ftpClient.enterLocalPassiveMode();
		if(getSetBinaryFileType())
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		
		logger.info("Loging in to server with credentials : login=" + getUserName() + ", password=" + getPassword());
		try{
			if(!ftpClient.login(getUserName(), getPassword()))
				throw new IOException("Failed to login to host with provided login and password. FTP server Reply String : " + ftpClient.getReplyString());
		}
		catch(Exception ex){
			throw new IOException("Failed to login to host with provided login and password. FTP server Reply String : " + ftpClient.getReplyString());
		}
	}


	@Override
	public void closeSession() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}


	@Override
	public boolean sendFileContent(InputStream streamToSend, String pathToContentFile) throws IOException {
		 boolean isUploaded = false;          	
	     isUploaded = ftpClient.storeFile(pathToContentFile, streamToSend);			   
	     return isUploaded;
	}


	@Override
	public InputStream getFileContent(String fileFromPath) {
		InputStream content = null;
		try {
			content = ftpClient.retrieveFileStream(fileFromPath);
			ftpClient.completePendingCommand();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		return content;
	}


	@Override
	public List<String> getDirectoryFilesNames() {
		List<String> filesNames = new ArrayList<String>();
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = ftpClient.listFiles();
			for (FTPFile ftpFile : ftpFiles) {
				if (!ftpFile.getName().equalsIgnoreCase(".") && !ftpFile.getName().equalsIgnoreCase(".."))
					filesNames.add(ftpFile.getName());
			}
		} catch (IOException e) {
			logger.warn("Failed to get file names in current working directory ,reason :"
					+ e.getMessage());
		}
		return filesNames;
	}


	@Override
	public void setWorkingDirectory(String directoryPath) throws IOException {
		ftpClient.changeWorkingDirectory(directoryPath);
	}


	@Override
	public String getWorkingDirectory() {
		try {
			return ftpClient.printWorkingDirectory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn("Failed to get current working directory, reason :"
					+ e.getMessage());
		}
		return null;
	}


	public Boolean getSetBinaryFileType() {
		return setBinaryFileType;
	}


	public void setSetBinaryFileType(Boolean setBinaryFileType) {
		this.setBinaryFileType = setBinaryFileType;
	}


	public String getHostName() {
		return hostName;
	}


	public void setHostName(String hostName) {
		this.hostName = hostName;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
}
