package com.malkos.poppin.integration.houzz.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.malkos.poppin.integration.houzz.entities.enums.EmailMessageStatus;
import com.malkos.poppin.integration.houzz.entities.enums.OutgoingMessageStatus;
import com.malkos.poppin.integration.houzz.entities.enums.RetailerEnum;
import com.malkos.poppin.integration.houzz.persistence.dao.LineItemIntegrationIdentifierDAO;
import com.malkos.poppin.integration.houzz.persistence.dao.NotificationEmailDAO;
import com.malkos.poppin.integration.houzz.persistence.dao.OutgoingMessageDAO;




public class PersistenceManager implements IPersistenceManager{
	private static PersistenceManager instance;
	
	@Override
	public List<LineItemIntegrationIdentifierDAO> getHouzzAssortment() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<LineItemIntegrationIdentifierDAO> lineItemIntegrationIdentifierDAOs = session.createQuery("from LineItemIntegrationIdentifierDAO").list();
		session.close();
		return lineItemIntegrationIdentifierDAOs;
	}

	@Override
	public void updateHouzzAssortment(List<LineItemIntegrationIdentifierDAO> lineItemDAOList) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		for(LineItemIntegrationIdentifierDAO liiDao : lineItemDAOList)
			session.update(liiDao);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public void persistOutgoingMessage(String messagePath, RetailerEnum retailer) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();		
		OutgoingMessageDAO omDAO = new OutgoingMessageDAO();
		omDAO.setCreatedDate(new Date());
		omDAO.setOutgoingMessagePath(messagePath);			
		omDAO.setOutgoingMessageStatus(OutgoingMessageStatus.PENDING_FOR_SENDING);
		omDAO.setRetailer(retailer);
		session.save(omDAO);		
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public List<OutgoingMessageDAO> getOutgoingMessagesToSend() {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		List<OutgoingMessageDAO> outgoingMessagesToSend = session.createQuery("from OutgoingMessageDAO where outgoingMessageStatus='" + OutgoingMessageStatus.PENDING_FOR_SENDING+"'").list();
		session.close();
		return outgoingMessagesToSend;
	}
	@Override
	public void updateOutgoingMessagesStatuses(List<OutgoingMessageDAO> sentMessages) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(OutgoingMessageDAO oiD : sentMessages){
			oiD.setOutgoingMessageStatus(OutgoingMessageStatus.SENT);
			session.update(oiD);
			if ( counter % 20 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
			counter++;
		}
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public void persistNotificationEmail(String text,String subject,String recepients, List<String> attachments, RetailerEnum retailer) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		NotificationEmailDAO notDAO = new NotificationEmailDAO();
		notDAO.setMessage(text);
		notDAO.setSubject(subject);
		notDAO.setRecepients(recepients);
		notDAO.setStatus(EmailMessageStatus.PENDING_FOR_SENDING);
		notDAO.setRetailer(retailer);
		if ((attachments!=null)&&(!attachments.isEmpty())){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();		    
		    Blob blob = null;
			try {
				ObjectOutputStream oos = new ObjectOutputStream(bos);			
			    oos.writeObject(attachments);
			    byte[] bytes = bos.toByteArray();
				blob = new SerialBlob(bytes);
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			notDAO.setAttachments(blob);		
		}		
		session.save(notDAO);
		session.getTransaction().commit();
		session.close();
	}
	@Override
	public List<NotificationEmailDAO> getPendingNotificationEmails() {
		List<NotificationEmailDAO> neDAOs = new ArrayList<NotificationEmailDAO>();
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		neDAOs = session.createQuery("from NotificationEmailDAO where status='" + EmailMessageStatus.PENDING_FOR_SENDING + "'").list();
		session.close();
		return neDAOs;
	}
	
	@Override
	public void updateNotificationEmails(List<NotificationEmailDAO> neToUpdate) {
		SessionFactory factory = HibernateSessionProvider.getSessionFactory();
		Session session = factory.openSession();
		session.beginTransaction();
		int counter = 1;
		for(NotificationEmailDAO neDAO : neToUpdate){			
			session.update(neDAO);
			if ( counter % 20 == 0 ) { 		       
		        session.flush();
		        session.clear();
		    }
			counter++;
		}
		session.getTransaction().commit();
		session.close();
	}
}
