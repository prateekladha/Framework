package com.app.framework;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class RecieveMail {
	
	private static String emailaddress;
	private static String password;
	static Store store;
	
	RecieveMail(String emailaddress, String password){
		RecieveMail.emailaddress = emailaddress;
		RecieveMail.password = password;
	}
	
	public static Store connect() throws MessagingException{
		Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.trust", "*");
        Session session = Session.getInstance(props, null);
        store = session.getStore();
        store.connect("imap.gmail.com", emailaddress, password);
        return store;
	}
	
	public static void close() throws MessagingException{
		if(store != null)
			store.close();
	}
	
	public static Folder getFolder(String folderName) throws MessagingException{
		return store.getFolder(folderName);
	}
	
	public static Message[] getMessages(String folderName) throws Exception{
		
        Folder request = null;
        try {     
            request = getFolder(folderName);
            request.open(Folder.READ_WRITE);           
            return request.getMessages();
        } catch (Exception mex) {
            throw mex;
        }
        finally{
        	if(request != null)
        		request.close(true);
        }
	}
	
	public static void copyMessages(Folder from, Folder to, Message[] messages) throws MessagingException{
		from.copyMessages(messages, to);
	}
	
	public static void deleteMessage(Message msg) throws MessagingException{
		msg.setFlag(Flags.Flag.DELETED, true);
	}
}
