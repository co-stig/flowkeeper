package org.flowkeeper.server.api;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ServerUtilities {

    static XMLGregorianCalendar dateToXmlGregorianCalendar(Calendar date) {
    	GregorianCalendar c = new GregorianCalendar();
    	c.setTime(date.getTime());
    	try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			// Will never happen -- ignore
			return null;
		}
    }
    
    static boolean sameDay(Calendar c1, Calendar c2) {
    	return 
    			c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
    			c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
