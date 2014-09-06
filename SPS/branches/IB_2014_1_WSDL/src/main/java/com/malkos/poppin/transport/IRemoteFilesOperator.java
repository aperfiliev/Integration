package com.malkos.poppin.transport;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.malkos.poppin.persistence.dao.OutgoingMessageDAO;

public interface IRemoteFilesOperator {
	Map<String, ByteArrayInputStream> pullFilesFromFileStorage();
	Set<String> saveFilesToLocalFileSystem(Map<String, ByteArrayInputStream> incomingPoFileNameToStreamMap);
	List<OutgoingMessageDAO> pushFilesToFilesStorage(List<OutgoingMessageDAO> outgoingMessages);
}
