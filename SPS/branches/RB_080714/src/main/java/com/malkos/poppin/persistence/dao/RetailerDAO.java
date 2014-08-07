package com.malkos.poppin.persistence.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="retailer")
public class RetailerDAO {
	
		@Id
		@Column(name="idRetailer")
		private int idRetailer;
		
		@Column (name="RetailerName")
		private String retailerName;
		
		@Column (name="DepartmentNsInternalId")
		private String departmentNsInternalId;
		private String departmentNsName;		
		private String companyNsInternalId;
		private String companyEmail;
		private String departmentAliasName;
		
		public int getIdRetailer() {
			return idRetailer;
		}
		public void setIdRetailer(int idRetailer) {
			this.idRetailer = idRetailer;
		}
		public String getRetailerName() {
			return retailerName;
		}
		public void setRetailerName(String retailerName) {
			this.retailerName = retailerName;
		}
		public String getDepartmentNsInternalId() {
			return departmentNsInternalId;
		}
		public void setDepartmentNsInternalId(String departmentNsInternalId) {
			this.departmentNsInternalId = departmentNsInternalId;
		}
		public String getDepartmentNsName() {
			return departmentNsName;
		}
		public void setDepartmentNsName(String departmentNsName) {
			this.departmentNsName = departmentNsName;
		}
		public String getCompanyNsInternalId() {
			return this.companyNsInternalId;
		}
		public void setCompanyNsInternalId(String companyNsInternalId) {
			this.companyNsInternalId = companyNsInternalId;
		}
		public String getCompanyEmail() {
			return companyEmail;
		}
		public void setCompanyEmail(String companyEmail) {
			this.companyEmail = companyEmail;
		}
		public String getDepartmentAliasName() {
			return departmentAliasName;
		}
		public void setDepartmentAliasName(String departmentAliasName) {
			this.departmentAliasName = departmentAliasName;
		}
}
