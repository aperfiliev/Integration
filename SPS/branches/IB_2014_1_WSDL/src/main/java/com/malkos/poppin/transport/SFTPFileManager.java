package com.malkos.poppin.transport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.hibernate.annotations.Synchronize;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPFileManager implements IRemoteFileManager{
	
	private JSch jsch;
	
	private String SFTPHost;
	private String SFTPUser;
	private String SFTPPass;
	private int SFTPPort;
	
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;
	
	public SFTPFileManager(){
		jsch = new JSch();
	}
	
	public int getSFTPPort() {
		return SFTPPort;
	}
	public void setSFTPPort(int sFTPPort) {
		SFTPPort = sFTPPort;
	}
	public String getSFTPUser() {
		return SFTPUser;
	}
	public void setSFTPUser(String sFTPUser) {
		SFTPUser = sFTPUser;
	}
	public String getSFTPPass() {
		return SFTPPass;
	}
	public void setSFTPPass(String sFTPPass) {
		SFTPPass = sFTPPass;
	}
	public String getSFTPHost() {
		return SFTPHost;
	}
	public void setSFTPHost(String sFTPHost) {
		SFTPHost = sFTPHost;
	}

	
	@Override
	public void openSession() throws JSchException {
		session = jsch.getSession(SFTPUser, SFTPHost,getSFTPPort());
		session.setPassword(getSFTPPass());
		session.setTimeout(50000);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
		channel = session.openChannel("sftp");
		channel.connect();
		channelSftp = (ChannelSftp)channel;

	}
	@Override
	public void closeSession() throws IOException {
		if(session.isConnected()){
			session.disconnect();
			channelSftp.disconnect();
			channel.disconnect();
		}
	}
	
	@Override
	public void sendFileContent(InputStream streamToSend, String pathToContentFile) throws SftpException {
		channelSftp.put(streamToSend, pathToContentFile);
	}
	@Override
	public List<String> getDirectoryFilesNames() throws SftpException {
		List<String> filesNames = new ArrayList<String>();
			Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*");
			for(ChannelSftp.LsEntry entry : list){
				filesNames.add(entry.getFilename());
			}
		return filesNames;
	}
	
	@Override
	public InputStream getFileContent(String fileName) throws SftpException {
		return channelSftp.get(fileName);
	}
	@Override
	public void setWorkingDirectory(String directoryName) throws SftpException{
		channelSftp.cd(directoryName);
	}

	@Override
	public void deleteFileContent(String FileName) throws SftpException {
		channelSftp.rm(FileName);
	}
}
