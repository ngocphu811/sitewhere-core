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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sitewhere.dao.mongodb.SiteWhereMongoClient;
import com.sitewhere.rest.model.device.Device;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.rest.model.device.Zone;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAlert;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceLocation;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceMeasurements;
import com.sitewhere.spi.device.IDeviceSearchCriteria;
import com.sitewhere.spi.device.IMetadataProvider;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.device.IZone;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;

/**
 * Device management implementation that uses MongoDB for persistence.
 * 
 * @author dadams
 */
public class MongoDeviceManagement implements IDeviceManagement {

	/** Injected with global SiteWhere Mongo client */
	private SiteWhereMongoClient mongoClient;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#createDevice(com.sitewhere .spi.device.IDevice)
	 */
	public IDevice createDevice(IDevice device) throws SiteWhereException {
		IDevice existing = getDeviceByHardwareId(device.getHardwareId());
		if (existing != null) {
			throw new SiteWhereSystemException(ErrorCode.DuplicateHardwareId, ErrorLevel.ERROR);
		}
		Device newDevice = new Device();
		newDevice.setAssetId(device.getAssetId());
		newDevice.setHardwareId(device.getHardwareId());
		newDevice.setComments(device.getComments());
		newDevice.setCreatedDate(Calendar.getInstance());
		newDevice.setCreatedBy("admin");
		newDevice.setDeleted(false);

		DBCollection devices = getMongoClient().getDevicesCollection();
		DBObject created = MongoDevice.toDBObject(newDevice);
		WriteResult result = devices.insert(created);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error saving device: " + result.getLastError().toString());
		}
		return newDevice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceByHardwareId(java .lang.String)
	 */
	public IDevice getDeviceByHardwareId(String hardwareId) throws SiteWhereException {
		DBObject dbDevice = getDeviceDBObjectByHardwareId(hardwareId);
		if (dbDevice != null) {
			return MongoDevice.fromDBObject(dbDevice);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getCurrentDeviceAssignment (com.sitewhere.spi.device
	 * .IDevice)
	 */
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws SiteWhereException {
		if (device.getAssignmentToken() == null) {
			return null;
		}
		DBObject match = getDeviceAssignmentDBObjectByToken(device.getAssignmentToken());
		return MongoDeviceAssignment.fromDBObject(match);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceMetadata(java. lang.String,
	 * com.sitewhere.spi.device.IMetadataProvider)
	 */
	public IDevice updateDeviceMetadata(String hardwareId, IMetadataProvider metadata)
			throws SiteWhereException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		DBObject match = getDeviceDBObjectByHardwareId(hardwareId);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.INFO);
		}
		MongoDeviceEntityMetadata.toDBObject(metadata, match);
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		devices.update(query, match);
		return MongoDevice.fromDBObject(match);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDevices(com.sitewhere.spi.device.IDeviceSearchCriteria)
	 */
	public List<IDevice> listDevices(IDeviceSearchCriteria criteria) throws SiteWhereException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		DBObject dbCriteria = new BasicDBObject();
		if (!criteria.isIncludeDeleted()) {
			MongoSiteWhereEntity.setDeleted(dbCriteria, false);
		}
		DBCursor cursor = devices.find(dbCriteria).sort(
				new BasicDBObject(MongoSiteWhereEntity.PROP_CREATED_DATE, -1));

		List<IDevice> matches = new ArrayList<IDevice>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDevice.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listUnassignedDevices()
	 */
	public List<IDevice> listUnassignedDevices() throws SiteWhereException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_ASSIGNMENT_TOKEN, null);
		DBCursor cursor = devices.find(query).sort(
				new BasicDBObject(MongoSiteWhereEntity.PROP_CREATED_DATE, -1));

		List<IDevice> matches = new ArrayList<IDevice>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDevice.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteDevice(java.lang.String)
	 */
	public IDevice deleteDevice(String hardwareId) throws SiteWhereException {
		DBObject existing = getDeviceDBObjectByHardwareId(hardwareId);
		if (existing == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
		}
		MongoSiteWhereEntity.setDeleted(existing, true);
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		DBCollection devices = getMongoClient().getDevicesCollection();
		devices.update(query, existing);
		return MongoDevice.fromDBObject(existing);
	}

	/**
	 * Get the DBObject containing site information that matches the given token.
	 * 
	 * @param token
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getDeviceDBObjectByHardwareId(String hardwareId) throws SiteWhereException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		DBObject result = devices.findOne(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#createDeviceAssignment(java .lang.String,
	 * java.lang.String, com.sitewhere.spi.asset.AssetType, java.lang.String)
	 */
	public IDeviceAssignment createDeviceAssignment(String siteToken, String hardwareId, AssetType assetType,
			String assetId) throws SiteWhereException {
		// Verify foreign references.
		DBObject site = getSiteDBObjectByToken(siteToken);
		if (site == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		DBObject device = getDeviceDBObjectByHardwareId(hardwareId);
		if (device == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
		}
		if (device.get(MongoDevice.PROP_ASSIGNMENT_TOKEN) != null) {
			throw new SiteWhereSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
		}

		DeviceAssignment newAssignment = new DeviceAssignment();
		newAssignment.setToken(UUID.randomUUID().toString());
		newAssignment.setSiteToken(siteToken);
		newAssignment.setDeviceHardwareId(hardwareId);
		newAssignment.setAssetType(assetType);
		newAssignment.setAssetId(assetId);
		newAssignment.setActiveDate(Calendar.getInstance());
		newAssignment.setStatus(DeviceAssignmentStatus.Active);
		newAssignment.setCreatedDate(Calendar.getInstance());
		newAssignment.setCreatedBy("admin");
		newAssignment.setDeleted(false);

		DBObject created = MongoDeviceAssignment.toDBObject(newAssignment);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		WriteResult result = assignments.insert(created);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error saving device assignment: "
					+ result.getLastError().toString());
		}

		// Update device to point to created assignment.
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		device.put(MongoDevice.PROP_ASSIGNMENT_TOKEN, newAssignment.getToken());
		result = devices.update(query, device);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error updating device with new assignment: "
					+ result.getLastError().toString());
		}
		return newAssignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceForAssignment(com .sitewhere.spi.device
	 * .IDeviceAssignment)
	 */
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		DBObject device = getDeviceDBObjectByHardwareId(assignment.getDeviceHardwareId());
		return MongoDevice.fromDBObject(device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getSiteForAssignment(com.sitewhere .spi.device.
	 * IDeviceAssignment)
	 */
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		DBObject site = getSiteDBObjectByToken(assignment.getSiteToken());
		return MongoSite.fromDBObject(site);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentMetadata (java.lang.String,
	 * com.sitewhere.spi.device.IMetadataProvider)
	 */
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		MongoDeviceEntityMetadata.toDBObject(metadata, match);
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		assignments.update(query, match);
		return MongoDeviceAssignment.fromDBObject(match);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentStatus (java.lang.String,
	 * com.sitewhere.spi.device.DeviceAssignmentStatus)
	 */
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		match.put(MongoDeviceAssignment.PROP_STATUS, String.valueOf(status.getStatusCode()));
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		WriteResult result = assignments.update(query, match);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error updating device assignment status: "
					+ result.getLastError().toString());
		}
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentLocation (java.lang.String,
	 * com.sitewhere.spi.device.IDeviceLocation)
	 */
	public IDeviceAssignment updateDeviceAssignmentLocation(String token, IDeviceLocation location)
			throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		MongoDeviceAssignment.setLocation(location, match);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		WriteResult result = assignments.update(query, match);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error updating device assignment location: "
					+ result.getLastError().toString());
		}
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#endDeviceAssignment(java.lang .String)
	 */
	public IDeviceAssignment endDeviceAssignment(String token) throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		match.put(MongoDeviceAssignment.PROP_RELEASED_DATE, Calendar.getInstance().getTime());
		match.put(MongoDeviceAssignment.PROP_STATUS,
				String.valueOf(DeviceAssignmentStatus.Released.getStatusCode()));
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		WriteResult result = assignments.update(query, match);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error ending device assignment: "
					+ result.getLastError().toString());
		}

		// Remove device assignment reference.
		DBCollection devices = getMongoClient().getDevicesCollection();
		String hardwareId = (String) match.get(MongoDeviceAssignment.PROP_DEVICE_HARDARE_ID);
		DBObject deviceMatch = getDeviceDBObjectByHardwareId(hardwareId);
		deviceMatch.removeField(MongoDevice.PROP_ASSIGNMENT_TOKEN);
		query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		result = devices.update(query, deviceMatch);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error removing assignment reference from device: "
					+ result.getLastError().toString());
		}

		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentHistory (java.lang.String)
	 */
	public List<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId) throws SiteWhereException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_DEVICE_HARDARE_ID, hardwareId);
		DBCursor cursor = assignments.find(query).sort(
				new BasicDBObject(MongoDeviceAssignment.PROP_ACTIVE_DATE, -1));

		List<IDeviceAssignment> matches = new ArrayList<IDeviceAssignment>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceAssignment.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentsNear(double , double, double, int)
	 */
	public List<IDeviceAssignment> getDeviceAssignmentsNear(double latitude, double longitude,
			double maxDistance, int maxResults) throws SiteWhereException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject filter = new BasicDBObject("$nearSphere", new double[] { longitude, latitude });
		filter.put("$maxDistance", maxDistance / 3963192);
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_LAST_LOCATION + "."
				+ MongoDeviceLocation.PROP_LATLONG, filter);
		DBCursor cursor = assignments.find(query);
		List<IDeviceAssignment> matches = new ArrayList<IDeviceAssignment>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceAssignment.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		System.out.println("SEARCH RETURNED " + matches.size() + " matches.");
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentsForSite (java.lang.String)
	 */
	public List<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken) throws SiteWhereException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_SITE_TOKEN, siteToken);
		DBCursor cursor = assignments.find(query);
		List<IDeviceAssignment> matches = new ArrayList<IDeviceAssignment>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceAssignment.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentByToken (java.lang.String)
	 */
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match != null) {
			return MongoDeviceAssignment.fromDBObject(match);
		}
		return null;
	}

	/**
	 * Find the DBObject for a device assignment based on unique token.
	 * 
	 * @param token
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getDeviceAssignmentDBObjectByToken(String token) throws SiteWhereException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		DBObject result = assignments.findOne(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addDeviceMeasurements(com. sitewhere.spi.device
	 * .IDeviceMeasurements)
	 */
	public IDeviceMeasurements addDeviceMeasurements(IDeviceMeasurements measurements)
			throws SiteWhereException {
		DBCollection measurementColl = getMongoClient().getMeasurementsCollection();
		DBObject mObject = MongoDeviceMeasurements.toDBObject(measurements);
		measurementColl.insert(mObject);
		return MongoDeviceMeasurements.fromDBObject(mObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceMeasurements(java .lang.String, int)
	 */
	public List<IDeviceMeasurements> listDeviceMeasurements(String assignmentToken, int maxCount)
			throws SiteWhereException {
		DBCollection measurementColl = getMongoClient().getMeasurementsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN,
				assignmentToken);
		DBCursor cursor = measurementColl.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceMeasurements> matches = new ArrayList<IDeviceMeasurements>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceMeasurements.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceMeasurementsForSite (java.lang.String, int)
	 */
	public List<IDeviceMeasurements> listDeviceMeasurementsForSite(String siteToken, int maxCount)
			throws SiteWhereException {
		DBCollection measurementColl = getMongoClient().getMeasurementsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken);
		DBCursor cursor = measurementColl.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceMeasurements> matches = new ArrayList<IDeviceMeasurements>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceMeasurements.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addAlertForMeasurements(java .lang.String,
	 * com.sitewhere.spi.device.IDeviceAlert)
	 */
	public IDeviceAlert addAlertForMeasurements(String measurementsId, IDeviceAlert alert)
			throws SiteWhereException {
		throw new SiteWhereException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addDeviceLocation(com.sitewhere .spi.device.
	 * IDeviceLocation)
	 */
	public IDeviceLocation addDeviceLocation(IDeviceLocation location) throws SiteWhereException {
		DBCollection locationsColl = getMongoClient().getLocationsCollection();
		DBObject locObject = MongoDeviceLocation.toDBObject(location);
		locationsColl.insert(locObject);
		return MongoDeviceLocation.fromDBObject(locObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceLocations(java.lang .String, int)
	 */
	public List<IDeviceLocation> listDeviceLocations(String assignmentToken, int maxCount)
			throws SiteWhereException {
		DBCollection locationsColl = getMongoClient().getLocationsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN,
				assignmentToken);
		DBCursor cursor = locationsColl.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceLocation> matches = new ArrayList<IDeviceLocation>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceLocation.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceLocationsForSite (java.lang.String, int)
	 */
	public List<IDeviceLocation> listDeviceLocationsForSite(String siteToken, int maxCount)
			throws SiteWhereException {
		DBCollection locationsColl = getMongoClient().getLocationsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken);
		DBCursor cursor = locationsColl.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceLocation> matches = new ArrayList<IDeviceLocation>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceLocation.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceLocations(java.util.List, java.util.Date,
	 * java.util.Date)
	 */
	public List<IDeviceLocation> listDeviceLocations(List<String> assignmentTokens, Date start, Date end)
			throws SiteWhereException {
		DBCollection locationsColl = getMongoClient().getLocationsCollection();
		BasicDBObject query = new BasicDBObject();
		query.put(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, new BasicDBObject("$in", assignmentTokens));
		BasicDBObject dateClause = new BasicDBObject("$gte", start).append("$lte", end);
		query.put(MongoDeviceEvent.PROP_EVENT_DATE, dateClause);
		DBCursor cursor = locationsColl.find(query).sort(
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceLocation> matches = new ArrayList<IDeviceLocation>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceLocation.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addAlertForLocation(java.lang .String,
	 * com.sitewhere.spi.device.IDeviceAlert)
	 */
	@SuppressWarnings("unchecked")
	public IDeviceAlert addAlertForLocation(String locationId, IDeviceAlert alert) throws SiteWhereException {
		DBObject location = getDeviceLocationById(locationId);
		if (location == null) {
			throw new SiteWhereException("Device location not found for id: " + locationId);
		}
		IDeviceAlert created = addDeviceAlert(alert);
		List<String> alertIds = (List<String>) location.get(MongoDeviceEvent.PROP_ALERT_IDS);
		if (!alertIds.contains(created.getId())) {
			alertIds.add(created.getId());
			location.put(MongoDeviceEvent.PROP_ALERT_IDS, alertIds);
			DBObject query = new BasicDBObject(MongoDeviceEvent.PROP_EVENT_ID, new ObjectId(locationId));
			WriteResult result = getMongoClient().getLocationsCollection().update(query, location);
			if (!result.getLastError().ok()) {
				throw new SiteWhereException("Error associating alert with location: "
						+ result.getLastError().toString());
			}
		}
		return created;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addDeviceAlert(com.sitewhere .spi.device.IDeviceAlert )
	 */
	public IDeviceAlert addDeviceAlert(IDeviceAlert alert) throws SiteWhereException {
		DBCollection alertsColl = getMongoClient().getAlertsCollection();
		DBObject alertObject = MongoDeviceAlert.toDBObject(alert);
		alertsColl.insert(alertObject);
		return MongoDeviceAlert.fromDBObject(alertObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceAlerts(java.lang .String, int)
	 */
	public List<IDeviceAlert> listDeviceAlerts(String assignmentToken, int maxCount)
			throws SiteWhereException {
		DBCollection alerts = getMongoClient().getAlertsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN,
				assignmentToken);
		DBCursor cursor = alerts.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceAlert> matches = new ArrayList<IDeviceAlert>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceAlert.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceAlertsForSite(java .lang.String, int)
	 */
	public List<IDeviceAlert> listDeviceAlertsForSite(String siteToken, int maxCount)
			throws SiteWhereException {
		DBCollection alerts = getMongoClient().getAlertsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken);
		DBCursor cursor = alerts.find(query).limit(maxCount)
				.sort(new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1));

		List<IDeviceAlert> matches = new ArrayList<IDeviceAlert>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoDeviceAlert.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#createSite(com.sitewhere.spi .device.ISite)
	 */
	public ISite createSite(ISite input) throws SiteWhereException {
		Site newSite = Site.copy(input);
		newSite.setToken(UUID.randomUUID().toString());
		newSite.setCreatedDate(Calendar.getInstance());
		newSite.setCreatedBy("admin");
		newSite.setDeleted(false);

		DBCollection sites = getMongoClient().getSitesCollection();
		DBObject created = MongoSite.toDBObject(newSite);
		WriteResult result = sites.insert(created);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error saving site: " + result.getLastError().toString());
		}
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateSite(com.sitewhere.spi .device.ISite)
	 */
	public ISite updateSite(ISite input) throws SiteWhereException {
		DBCollection sites = getMongoClient().getSitesCollection();
		DBObject match = getSiteDBObjectByToken(input.getToken());
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		BasicDBObject update = MongoSite.toDBObject(input);
		update.append(MongoSiteWhereEntity.PROP_CREATED_DATE,
				match.get(MongoSiteWhereEntity.PROP_CREATED_DATE));
		update.append(MongoSiteWhereEntity.PROP_CREATED_BY, match.get(MongoSiteWhereEntity.PROP_CREATED_BY));
		update.append(MongoSiteWhereEntity.PROP_UPDATED_DATE, Calendar.getInstance().getTime());
		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, input.getToken());
		sites.update(query, update);
		return MongoSite.fromDBObject(update);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getSiteByToken(java.lang.String )
	 */
	public ISite getSiteByToken(String token) throws SiteWhereException {
		DBObject result = getSiteDBObjectByToken(token);
		if (result != null) {
			return MongoSite.fromDBObject(result);
		}
		return null;
	}

	/**
	 * Get the DBObject containing site information that matches the given token.
	 * 
	 * @param token
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getSiteDBObjectByToken(String token) throws SiteWhereException {
		DBCollection sites = getMongoClient().getSitesCollection();
		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, token);
		DBObject result = sites.findOne(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listSites()
	 */
	public List<ISite> listSites() throws SiteWhereException {
		DBCollection sites = getMongoClient().getSitesCollection();
		DBCursor cursor = sites.find().sort(new BasicDBObject(MongoSite.PROP_NAME, 1));

		List<ISite> matches = new ArrayList<ISite>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoSite.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#createZone(com.sitewhere.spi .device.IZone)
	 */
	public IZone createZone(IZone input) throws SiteWhereException {
		Zone zone = Zone.copy(input);
		zone.setToken(UUID.randomUUID().toString());
		zone.setCreatedDate(Calendar.getInstance());

		DBCollection zones = getMongoClient().getZonesCollection();
		DBObject created = MongoZone.toDBObject(zone);
		WriteResult result = zones.insert(created);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error saving zone: " + result.getLastError().toString());
		}
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getZone(java.lang.String)
	 */
	public IZone getZone(String zoneToken) throws SiteWhereException {
		DBCollection zones = getMongoClient().getZonesCollection();
		BasicDBObject query = new BasicDBObject(MongoZone.PROP_TOKEN, zoneToken);
		DBObject found = zones.findOne(query);
		if (found == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidZoneToken, ErrorLevel.ERROR);
		}
		return MongoZone.fromDBObject(found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listZones(java.lang.String)
	 */
	public List<IZone> listZones(String siteToken) throws SiteWhereException {
		DBCollection zones = getMongoClient().getZonesCollection();
		BasicDBObject query = new BasicDBObject(MongoZone.PROP_SITE_TOKEN, siteToken);
		DBCursor cursor = zones.find(query).sort(
				new BasicDBObject(MongoSiteWhereEntity.PROP_CREATED_DATE, -1));

		List<IZone> matches = new ArrayList<IZone>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoZone.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteZone(java.lang.String)
	 */
	public IZone deleteZone(String zoneToken) throws SiteWhereException {
		IZone existing = getZone(zoneToken);
		DBCollection zones = getMongoClient().getZonesCollection();
		BasicDBObject query = new BasicDBObject(MongoZone.PROP_TOKEN, zoneToken);
		WriteResult result = zones.remove(query);
		if (!result.getLastError().ok()) {
			throw new SiteWhereSystemException(ErrorCode.ZoneDeleteFailed, ErrorLevel.ERROR);
		}
		return existing;
	}

	/**
	 * Find a device location by unique event id.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getDeviceLocationById(String id) throws SiteWhereException {
		DBCollection coll = getMongoClient().getLocationsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_EVENT_ID, new ObjectId(id));
		return coll.findOne(query);
	}

	/**
	 * Find a device measurements by unique event id.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getDeviceMeasurementsById(String id) throws SiteWhereException {
		DBCollection coll = getMongoClient().getMeasurementsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_EVENT_ID, new ObjectId(id));
		return coll.findOne(query);
	}

	public SiteWhereMongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(SiteWhereMongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}