package com.malkos.poppin.integration.houzz.entities;

public class HouzzInventoryItemPropertiesCorrector implements IInventoryItemPropertiesCorrector{

	@Override
	public HouzzItemCorrectedProperties correctItemProperties(HouzzInventoryPojo item) {
		HouzzItemCorrectedProperties correctedProperties = new HouzzItemCorrectedProperties();
		double correctedQty;
		boolean correctedInactiveState;
		if (item.isInactive()){
			correctedQty=0;
			correctedInactiveState=true;
		} else {
			double quantity = item.getQtyAvailable();
			if (quantity > 100){
				correctedQty = Math.floor(quantity*0.5);
			}
			else if (quantity <= 15){
				correctedQty = 0;
			} else {
				correctedQty =  Math.floor(quantity*0.25);
			}
			if (correctedQty==0){
				correctedInactiveState=true;
			} else {
				correctedInactiveState=false;
			}
		}		
		correctedProperties.setInactive(correctedInactiveState);
		correctedProperties.setQuantity(correctedQty);
		return correctedProperties;
	}
}
