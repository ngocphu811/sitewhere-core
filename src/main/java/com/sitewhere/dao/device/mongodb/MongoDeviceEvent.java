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
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.rest.model.device.DeviceEvent;
import com.sitewhere.spi.device.IDeviceEvent;

/**
 * Used to load or save device event data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceEvent {

	/** Property for unique event id */
	public static final String PROP_EVENT_ID = "_id";

	/** Property for site token */
	public static final String PROP_SITE_TOKEN = "siteToken";

	/** Property for device assignment token */
	public static final String PROP_DEVICE_ASSIGNMENT_TOKEN = "deviceAssignmentToken";

	/** Property for asset name at time of event */
	public static final String PROP_ASSET_NAME = "assetName";

	/** Property for time measurements were taken */
	public static final String PROP_EVENT_DATE = "eventDate";

	/** Property for time measurements were received */
	public static final String PROP_RECEIVED_DATE = "receivedDate";

	/** Property for alert ids */
	public static final String PROP_ALERT_IDS = "alerts";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IDeviceEvent source, BasicDBObject target) {
		target.append(PROP_SITE_TOKEN, source.getSiteToken());
		target.append(PROP_DEVICE_ASSIGNMENT_TOKEN, source.getDeviceAssignmentToken());
		target.append(PROP_ASSET_NAME, source.getAssetName());
		target.append(PROP_EVENT_DATE, source.getEventDate());
		target.append(PROP_RECEIVED_DATE, source.getReceivedDate());
		target.append(PROP_ALERT_IDS, source.getAlertIds());

		MongoDeviceEntityMetadata.toDBObject(source, target);
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	@SuppressWarnings("unchecked")
	public static void fromDBObject(DBObject source, DeviceEvent target) {
		if (source.get(PROP_EVENT_ID) != null) {
			String id = ((ObjectId) source.get(PROP_EVENT_ID)).toString();
			target.setId(id);
		}
		String siteToken = (String) source.get(PROP_SITE_TOKEN);
		String assignmentToken = (String) source.get(PROP_DEVICE_ASSIGNMENT_TOKEN);
		String assetName = (String) source.get(PROP_ASSET_NAME);
		Date eventDate = (Date) source.get(PROP_EVENT_DATE);
		Date receivedDate = (Date) source.get(PROP_RECEIVED_DATE);
		List<String> alertIds = (List<String>) source.get(PROP_ALERT_IDS);

		target.setSiteToken(siteToken);
		target.setDeviceAssignmentToken(assignmentToken);
		target.setAssetName(assetName);
		target.setEventDate(eventDate);
		target.setReceivedDate(receivedDate);
		target.setAlertIds(alertIds);

		MongoDeviceEntityMetadata.fromDBObject(source, target);
	}
}