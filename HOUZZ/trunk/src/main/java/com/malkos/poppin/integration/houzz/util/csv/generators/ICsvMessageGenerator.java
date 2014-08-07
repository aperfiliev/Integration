package com.malkos.poppin.integration.houzz.util.csv.generators;

import java.util.List;

import com.malkos.poppin.integration.houzz.entities.HouzzInventoryItemPojo;
import com.malkos.poppin.integration.houzz.entities.InventoryItemPojo;

public interface ICsvMessageGenerator {
	String generateMessage(List<InventoryItemPojo> prepearedInventoryList) throws CsvGenerationException;
}
