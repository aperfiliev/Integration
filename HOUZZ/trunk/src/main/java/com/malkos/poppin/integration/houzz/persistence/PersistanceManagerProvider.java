package com.malkos.poppin.integration.houzz.persistence;

public class PersistanceManagerProvider {
	private static IPersistenceManager instance;
	
	public static IPersistenceManager getInstance(){
		if (instance==null){
			instance = new PersistenceManager();
		}
		return instance;
	}

}
