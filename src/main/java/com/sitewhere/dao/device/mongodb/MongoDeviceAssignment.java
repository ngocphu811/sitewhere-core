/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.sitewhere.dao.device.mongodb;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.DeviceLocation;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceLocation;

/**
 * Used to load or save device assignment data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceAssignment {

	/** Property for active date */
	public static final String PROP_ACTIVE_DATE = "activeDate";

	/** Property for asset id */
	public static final String PROP_ASSET_ID = "assetId";

	/** Property for asset type */
	public static final String PROP_ASSET_TYPE = "assetType";

	/** Property for released date */
	public static final String PROP_RELEASED_DATE = "releasedDate";

	/** Property for status */
	public static final String PROP_STATUS = "status";

	/** Property for token */
	public static final String PROP_TOKEN = "token";

	/** Property for device hardware id */
	public static final String PROP_DEVICE_HARDWARE_ID = "deviceHardwareId";

	/** Property for site token */
	public static final String PROP_SITE_TOKEN = "siteToken";

	/** Property for last location */
	public static final String PROP_LAST_LOCATION = "lastLocation";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IDeviceAssignment source, BasicDBObject target) {
		if (source.getActiveDate() != null) {
			target.append(PROP_ACTIVE_DATE, source.getActiveDate());
		}
		target.append(PROP_ASSET_ID, source.getAssetId());
		if (source.getAssetType() != null) {
			target.append(PROP_ASSET_TYPE, source.getAssetType().name());
		}
		if (source.getReleasedDate() != null) {
			target.append(PROP_RELEASED_DATE, source.getReleasedDate());
		}
		if (source.getStatus() != null) {
			target.append(PROP_STATUS, source.getStatus().name());
		}
		target.append(PROP_TOKEN, source.getToken());
		target.append(PROP_DEVICE_HARDWARE_ID, source.getDeviceHardwareId());
		target.append(PROP_SITE_TOKEN, source.getSiteToken());

		if (source.getLastLocation() != null) {
			setLocation(source.getLastLocation(), target);
		}

		MongoSiteWhereEntity.toDBObject(source, target);
		MongoDeviceEntityMetadata.toDBObject(source, target);
	}

	/**
	 * Set location fields for the assignment.
	 * 
	 * @param source
	 * @param target
	 */
	public static void setLocation(IDeviceLocation source, DBObject target) {
		BasicDBObject location = new BasicDBObject();
		MongoDeviceLocation.toDBObject(source, location);
		target.put(PROP_LAST_LOCATION, location);
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	public static void fromDBObject(DBObject source, DeviceAssignment target) {
		Date activeDate = (Date) source.get(PROP_ACTIVE_DATE);
		String assetId = (String) source.get(PROP_ASSET_ID);
		String assetType = (String) source.get(PROP_ASSET_TYPE);
		Date releasedDate = (Date) source.get(PROP_RELEASED_DATE);
		String status = (String) source.get(PROP_STATUS);
		String token = (String) source.get(PROP_TOKEN);
		String deviceHardwareId = (String) source.get(PROP_DEVICE_HARDWARE_ID);
		String siteToken = (String) source.get(PROP_SITE_TOKEN);

		if (activeDate != null) {
			target.setActiveDate(activeDate);
		}
		target.setAssetId(assetId);
		if (assetType != null) {
			target.setAssetType(AssetType.valueOf(assetType));
		}
		if (releasedDate != null) {
			target.setReleasedDate(releasedDate);
		}
		if (status != null) {
			target.setStatus(DeviceAssignmentStatus.valueOf(status));
		}
		target.setToken(token);
		target.setDeviceHardwareId(deviceHardwareId);
		target.setSiteToken(siteToken);

		DBObject lastLocation = (DBObject) source.get(PROP_LAST_LOCATION);
		if (lastLocation != null) {
			IDeviceLocation location = MongoDeviceLocation.fromDBObject(lastLocation);
			target.setLastLocation(DeviceLocation.copy(location));
		}

		MongoSiteWhereEntity.fromDBObject(source, target);
		MongoDeviceEntityMetadata.fromDBObject(source, target);
	}

	/**
	 * Convert SPI object to Mongo DBObject.
	 * 
	 * @param source
	 * @return
	 */
	public static BasicDBObject toDBObject(IDeviceAssignment source) {
		BasicDBObject result = new BasicDBObject();
		MongoDeviceAssignment.toDBObject(source, result);
		return result;
	}

	/**
	 * Convert a DBObject into the SPI equivalent.
	 * 
	 * @param source
	 * @return
	 */
	public static DeviceAssignment fromDBObject(DBObject source) {
		DeviceAssignment result = new DeviceAssignment();
		MongoDeviceAssignment.fromDBObject(source, result);
		return result;
	}
}