package com.malkos.poppin.transport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public interface IRemoteFileManager {
	void openSession() throws JSchException;
	void closeSession() throws IOException;
	List<String> getDirectoryFilesNames() throws SftpException;
	InputStream getFileContent(String fileName) throws SftpException;
	void setWorkingDirectory(String directoryName) throws SftpException;
	void sendFileContent(InputStream streamToSend, String pathToContentFile) throws SftpException;
	void  deleteFileContent(String FileName) throws SftpException;
}
