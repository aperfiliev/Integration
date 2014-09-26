package com.malkos.poppin.transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.List;

public interface IRemoteFileManager {
	void openSession() throws IOException;
	void closeSession() throws IOException;
	boolean sendFileContent(InputStream streamToSend, String pathToContentFile) throws IOException;	
	InputStream getFileContent(String fileFromPath);
	List<String> getDirectoryFilesNames();	
	void setWorkingDirectory(String directoryPath) throws IOException;
	String getWorkingDirectory();

}
