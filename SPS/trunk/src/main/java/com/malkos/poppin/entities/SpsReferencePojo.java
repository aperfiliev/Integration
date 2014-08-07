package com.malkos.poppin.entities;

import java.util.ArrayList;
import java.util.List;

public class SpsReferencePojo {
	private String ReferenceQual;
	private String ReferenceID;
	private String description;
	
	private SpsReferencePojo innerReferencePojo;
	
	private List<SpsReferencePojo> referenceList;
	
	
	public String getReferenceID() {
		return ReferenceID;
	}
	public void setReferenceID(String referenceID) {
		ReferenceID = referenceID;
	}
	public List<SpsReferencePojo> getReferenceList() {
		return referenceList;
	}
	public void setReferenceList(List<SpsReferencePojo> referenceList) {
		this.referenceList = referenceList;
	}
	public String getReferenceQual() {
		return ReferenceQual;
	}
	public void setReferenceQual(String referenceQual) {
		ReferenceQual = referenceQual;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return this.description;
	}
	/**
	 * @return the innerReferencePojo
	 */
	public SpsReferencePojo getInnerReferencePojo() {
		return innerReferencePojo;
	}
	/**
	 * @param innerReferencePojo the innerReferencePojo to set
	 */
	public void setInnerReferencePojo(SpsReferencePojo innerReferencePojo) {
		this.innerReferencePojo = innerReferencePojo;
	}
}
