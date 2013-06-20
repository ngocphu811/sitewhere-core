package com.sitewhere.core.device;

import java.util.Comparator;

import com.sitewhere.spi.device.IDeviceEvent;

/**
 * Compares device event by event date.
 * 
 * @author Derek Adams
 */
public class DeviceEventDateComparator implements Comparator<IDeviceEvent> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(IDeviceEvent arg0, IDeviceEvent arg1) {
		return arg0.getEventDate().compareTo(arg1.getEventDate());
	}
}