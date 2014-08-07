package com.malkos.poppin.integration.houzz.transport;

import java.io.IOException;
import java.io.InputStream;

import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;

public interface IRemoteFileManager {
	void openSession(RetailerAbstract retailer) throws IOException;
	void closeSession() throws IOException;
	boolean sendFileContent(InputStream streamToSend, String pathToContentFile) throws IOException;

}
