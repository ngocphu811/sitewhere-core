/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.dao.mongodb.device;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.rest.model.device.DeviceLocation;
import com.sitewhere.spi.device.IDeviceLocation;

/**
 * Used to load or save device location data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceLocation {

	/** Element that holds location information */
	public static final String PROP_LATLONG = "latLong";

	/** Property for latitude */
	public static final String PROP_LATITUDE = "latitude";

	/** Property for longitude */
	public static final String PROP_LONGITUDE = "longitude";

	/** Property for elevation */
	public static final String PROP_ELEVATION = "elevation";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IDeviceLocation source, BasicDBObject target) {
		MongoDeviceEvent.toDBObject(source, target);

		BasicDBObject locFields = new BasicDBObject();
		locFields.append(PROP_LONGITUDE, source.getLongitude());
		locFields.append(PROP_LATITUDE, source.getLatitude());
		target.append(PROP_LATLONG, locFields);
		if (source.getElevation() != null) {
			target.append(PROP_ELEVATION, source.getElevation());
		}
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	public static void fromDBObject(DBObject source, DeviceLocation target) {
		MongoDeviceEvent.fromDBObject(source, target);

		DBObject location = (DBObject) source.get(PROP_LATLONG);
		Double latitude = (Double) location.get(PROP_LATITUDE);
		Double longitude = (Double) location.get(PROP_LONGITUDE);
		Double elevation = (Double) source.get(PROP_ELEVATION);

		target.setLatitude(latitude);
		target.setLongitude(longitude);
		target.setElevation(elevation);
	}

	/**
	 * Convert SPI object to Mongo DBObject.
	 * 
	 * @param source
	 * @return
	 */
	public static DBObject toDBObject(IDeviceLocation source) {
		BasicDBObject result = new BasicDBObject();
		MongoDeviceLocation.toDBObject(source, result);
		return result;
	}

	/**
	 * Convert a DBObject into the SPI equivalent.
	 * 
	 * @param source
	 * @return
	 */
	public static IDeviceLocation fromDBObject(DBObject source) {
		DeviceLocation result = new DeviceLocation();
		MongoDeviceLocation.fromDBObject(source, result);
		return result;
	}
}
