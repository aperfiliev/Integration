package com.malkos.poppin.integration.houzz.transport;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPHeaderElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.integration.houzz.entities.NSRrequestDetails;
import com.malkos.poppin.integration.houzz.entities.SearchResultWrapped;
import com.netsuite.webservices.platform.core_2014_2.DataCenterUrls;
import com.netsuite.webservices.platform.core_2014_2.Passport;
import com.netsuite.webservices.platform.core_2014_2.Record;
import com.netsuite.webservices.platform.core_2014_2.RecordRef;
import com.netsuite.webservices.platform.core_2014_2.SearchRecord;
import com.netsuite.webservices.platform.core_2014_2.SearchResult;
import com.netsuite.webservices.platform.faults_2014_2.ExceededRecordCountFault;
import com.netsuite.webservices.platform.faults_2014_2.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults_2014_2.ExceededRequestSizeFault;
import com.netsuite.webservices.platform.faults_2014_2.ExceededUsageLimitFault;
import com.netsuite.webservices.platform.faults_2014_2.InvalidCredentialsFault;
import com.netsuite.webservices.platform.faults_2014_2.InvalidSessionFault;
import com.netsuite.webservices.platform.faults_2014_2.UnexpectedErrorFault;
import com.netsuite.webservices.platform.messages_2014_2.SearchPreferences;
import com.netsuite.webservices.platform.messages_2014_2.WriteResponseList;
import com.netsuite.webservices.platform_2014_2.NetSuiteBindingStub;
import com.netsuite.webservices.platform_2014_2.NetSuitePortType;
import com.netsuite.webservices.platform_2014_2.NetSuiteServiceLocator;


public class NetsuiteService {
	/**
	 * Proxy class that abstracts the communication with the NetSuite Web
	 * Services. All NetSuite operations are invoked as methods of this class.
	 */
	private NetSuitePortType port;

	private String NSaccount;
	private String NSlogin;
	private String NSpassword;
	private String NSrole;
	private String NSwsURL;
	
	private Passport passport;
	
	private static Logger logger = LoggerFactory.getLogger(NetsuiteService.class);
	
	public NetSuitePortType getPort() {
		return port;
	}

	public String getNSwsURL() {
		return NSwsURL;
	}

	public void setNSwsURL(String nSwsURL) {
		NSwsURL = nSwsURL;
	}

	
	public void InitializeNetsuiteService(){
		logger.info("Initializing the Netsuite WS service.");
		logger.info("Identifiyng the Data Center location.");
		NetSuiteServiceLocator service = new DataCenterAwareNetSuiteServiceLocator(NSaccount);		
		// Get the service port (to the correct datacenter)
		try {
			logger.info("Getting the Netsuite Proxy port. It might take some time, please wait...");
			port = service.getNetSuitePort(new URL(NSwsURL));
			// Setting client timeout to 2 hours for long running operations
			((NetSuiteBindingStub) port).setTimeout(1000 * 60 * 60 * 2);
			
			passport = new Passport();
			passport.setAccount(NSaccount);
			passport.setEmail(NSlogin);
			passport.setPassword(NSpassword);
			passport.setRole(new RecordRef(null, NSrole , null, null));
			logger.info("Netsuite WS service is initialized and ready for use.");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}
	private void prepareNetsuiteStub(boolean searchBodyFieldsOnly) throws SOAPException{
		NetSuiteBindingStub stub = (NetSuiteBindingStub) port;
		stub.setMaintainSession(false);
		stub.clearHeaders();
		SOAPHeaderElement searchPrefHeader = new SOAPHeaderElement("urn:messages_2014_2.platform.webservices.netsuite.com", "searchPreferences");
		SearchPreferences searchPrefs = new SearchPreferences();
		searchPrefs.setPageSize(new Integer(500));
		searchPrefs.setBodyFieldsOnly(searchBodyFieldsOnly);
		searchPrefHeader.setObjectValue(searchPrefs);
		stub.setHeader(searchPrefHeader);
		
		SOAPHeaderElement passportHeader = new SOAPHeaderElement("urn:messages_2014_2.platform.webservices.netsuite.com", "passport");
		passportHeader.setObjectValue(passport);
		stub.setHeader(passportHeader);
	}
	private void prepareNetsuiteStub() throws SOAPException{
		NetSuiteBindingStub stub = (NetSuiteBindingStub) port;
		stub.setMaintainSession(false);
		stub.clearHeaders();
		
		SOAPHeaderElement passportHeader = new SOAPHeaderElement("urn:messages_2014_2.platform.webservices.netsuite.com", "passport");
		passportHeader.setObjectValue(passport);
		stub.setHeader(passportHeader);
	}
	
	public String getNSaccount() {
		return NSaccount;
	}

	public void setNSaccount(String nSaccount) {
		NSaccount = nSaccount;
	}

	public String getNSlogin() {
		return NSlogin;
	}

	public void setNSlogin(String nSlogin) {
		NSlogin = nSlogin;
	}

	public String getNSpassword() {
		return NSpassword;
	}

	public void setNSpassword(String nSpassword) {
		NSpassword = nSpassword;
	}

	public String getNSrole() {
		return NSrole;
	}

	public void setNSrole(String nSrole) {
		NSrole = nSrole;
	}
	public SearchResultWrapped search(SearchRecord searchRecord, boolean searchBodyFieldsOnly) throws NetsuiteServiceException{
		logger.info("Attemting to make a search WS-SOAP request to Netsuite.");
		SearchResult result = null;
		SearchResultWrapped resultWrapped = null;
		TimeZone est = TimeZone.getTimeZone("EST");
		Date requestDateTime = Calendar.getInstance(est).getTime();			
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestDateTime(requestDateTime);		
		try {
			prepareNetsuiteStub(searchBodyFieldsOnly);
			result = port.search(searchRecord);
			resultWrapped = new SearchResultWrapped(details, result);
		} catch (RemoteException | SOAPException e ) {
			String errorMessage = e.getMessage();
			AxisFault fault = (AxisFault)e;
			if(null != fault) errorMessage = fault.getFaultReason();			
			throw new NetsuiteServiceException(errorMessage, details);
		}
		return resultWrapped;		
	}
	public SearchResultWrapped searchMoreWithId(String searchId, int pageIndex, boolean searchBodyFieldsOnly) throws NetsuiteServiceException{
		logger.info("Attemting to make a search-more WS-SOAP request to Netsuite.");
		SearchResult result = null;
		SearchResultWrapped resultWrapped = null;
		TimeZone est = TimeZone.getTimeZone("EST");
		Date requestDateTime = Calendar.getInstance(est).getTime();			
		NSRrequestDetails details = new NSRrequestDetails();
		details.setRequestDateTime(requestDateTime);		
		try {
			prepareNetsuiteStub(searchBodyFieldsOnly);
			result =  port.searchMoreWithId(searchId, pageIndex);
			resultWrapped = new SearchResultWrapped(details, result);
		} catch (RemoteException | SOAPException e) {
			String errorMessage = e.getMessage();
			AxisFault fault = (AxisFault)e;
			if(null != fault) errorMessage = fault.getFaultReason();
			throw new NetsuiteServiceException(errorMessage, details);
		}
		return resultWrapped;
	}	
	private static class DataCenterAwareNetSuiteServiceLocator extends NetSuiteServiceLocator
	{
		private String account;

		public DataCenterAwareNetSuiteServiceLocator(String account)
		{
			this.account = account;
		}

		@Override
		public NetSuitePortType getNetSuitePort(URL defaultWsDomainURL)
		{
			try
			{
				NetSuitePortType _port = super.getNetSuitePort(defaultWsDomainURL);
				// Get the webservices domain for your account
				DataCenterUrls urls = _port.getDataCenterUrls(account).getDataCenterUrls();
				String wsDomain = urls.getWebservicesDomain();

				// Return URL appropriate for the specific account
				return super.getNetSuitePort(new URL(wsDomain.concat(defaultWsDomainURL.getPath())));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
