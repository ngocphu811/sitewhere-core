/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

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