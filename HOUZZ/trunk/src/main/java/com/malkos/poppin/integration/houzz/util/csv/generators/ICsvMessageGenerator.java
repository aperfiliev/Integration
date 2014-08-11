package com.malkos.poppin.integration.houzz.util.csv.generators;

import java.util.Collection;
import java.util.List;

import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryPojo;

public interface ICsvMessageGenerator {
	String generateMessage(Collection<InventoryPojo> prepearedInventoryList) throws CsvGenerationException;
}
