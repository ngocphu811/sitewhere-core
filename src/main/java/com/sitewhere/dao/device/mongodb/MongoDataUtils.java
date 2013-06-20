package com.sitewhere.dao.device.mongodb;

import java.util.Calendar;
import java.util.Date;

/**
 * Data conversion utils used with MongoDB.
 * 
 * @author dadams
 */
public class MongoDataUtils {

	/**
	 * Convert date to calendar.
	 * 
	 * @param input
	 * @return
	 */
	public static Calendar dateAsCalendar(Date input) {
		if (input == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(input);
		return cal;
	}

	/**
	 * Get Calendar as a Date.
	 * 
	 * @param input
	 * @return
	 */
	public static Date calendarAsDate(Calendar input) {
		if (input == null) {
			return null;
		}
		return input.getTime();
	}
}