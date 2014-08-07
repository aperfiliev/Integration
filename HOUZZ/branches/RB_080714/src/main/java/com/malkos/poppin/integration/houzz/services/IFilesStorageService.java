package com.malkos.poppin.integration.houzz.services;

import java.util.List;

import com.malkos.poppin.integration.houzz.entities.RetailerAbstract;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;

public interface IFilesStorageService {	
	List<OutgoingMessageDAO> pushFilesToStorage(List<OutgoingMessageDAO> houzzFiles, RetailerAbstract retailer);	
}
