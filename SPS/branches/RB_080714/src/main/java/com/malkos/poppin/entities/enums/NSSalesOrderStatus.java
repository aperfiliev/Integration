package com.malkos.poppin.entities.enums;

public enum NSSalesOrderStatus {
	//CANCELED,
	//CLOSED;
	FULLYBILED,
	PENDINGBILLING;
	
	@Override
	public String toString(){
		String status = new String();
		switch(this){
		//case CANCELED: return status="canceled"; 
		//case CLOSED: return status="closed"; 
		case FULLYBILED: return status="fullybilled"; 
		case PENDINGBILLING: return status="pendingbilling"; 		
		}
		return status;
	}
}