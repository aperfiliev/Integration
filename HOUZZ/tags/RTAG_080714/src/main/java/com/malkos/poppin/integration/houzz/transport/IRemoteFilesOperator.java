package com.malkos.poppin.integration.houzz.transport;

import java.util.List;

import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;

public interface IRemoteFilesOperator {
	List<OutgoingMessageDAO> pushFilesToFilesStorage(List<OutgoingMessageDAO> outgoingMessages, RetailerAbstract retailer);
}
