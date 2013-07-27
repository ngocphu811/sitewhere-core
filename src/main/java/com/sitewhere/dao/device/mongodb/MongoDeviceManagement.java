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

import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sitewhere.core.device.Utils;
import com.sitewhere.dao.mongodb.SiteWhereMongoClient;
import com.sitewhere.rest.model.device.Device;
import com.sitewhere.rest.model.device.DeviceAlert;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.DeviceEventBatchResponse;
import com.sitewhere.rest.model.device.DeviceLocation;
import com.sitewhere.rest.model.device.DeviceMeasurements;
import com.sitewhere.rest.model.device.MetadataEntry;
import com.sitewhere.rest.model.device.MetadataProvider;
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.rest.model.device.Zone;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.common.ILocation;
import com.sitewhere.spi.device.AlertSource;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAlert;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceEventBatch;
import com.sitewhere.spi.device.IDeviceEventBatchResponse;
import com.sitewhere.spi.device.IDeviceLocation;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceMeasurements;
import com.sitewhere.spi.device.IDeviceSearchCriteria;
import com.sitewhere.spi.device.IMetadataEntry;
import com.sitewhere.spi.device.IMetadataProvider;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.device.IZone;
import com.sitewhere.spi.device.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.request.ISiteCreateRequest;
import com.sitewhere.spi.device.request.IZoneCreateRequest;
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
	 * @see com.sitewhere.spi.device.IDeviceManagement#createDevice(com.sitewhere.spi.device.request.
	 * IDeviceCreateRequest)
	 */
	public IDevice createDevice(IDeviceCreateRequest request) throws SiteWhereException {
		IDevice existing = getDeviceByHardwareId(request.getHardwareId());
		if (existing != null) {
			throw new SiteWhereSystemException(ErrorCode.DuplicateHardwareId, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}
		Device newDevice = new Device();
		newDevice.setAssetId(request.getAssetId());
		newDevice.setHardwareId(request.getHardwareId());
		newDevice.setComments(request.getComments());

		MetadataProvider.copy(request, newDevice);
		MongoPersistence.initializeEntityMetadata(newDevice);

		DBCollection devices = getMongoClient().getDevicesCollection();
		DBObject created = MongoDevice.toDBObject(newDevice);
		MongoPersistence.insert(devices, created);
		return newDevice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDevice(java.lang.String,
	 * com.sitewhere.spi.device.request.IDeviceCreateRequest)
	 */
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws SiteWhereException {
		DBObject existing = assertDevice(hardwareId);

		// Can not update the hardware id on a device.
		if ((request.getHardwareId() != null) && (!request.getHardwareId().equals(hardwareId))) {
			throw new SiteWhereSystemException(ErrorCode.DeviceHardwareIdCanNotBeChanged, ErrorLevel.ERROR,
					HttpServletResponse.SC_BAD_REQUEST);
		}

		// Copy any non-null fields.
		Device updatedDevice = MongoDevice.fromDBObject(existing);
		if (request.getAssetId() != null) {
			updatedDevice.setAssetId(request.getAssetId());
		}
		if (request.getComments() != null) {
			updatedDevice.setComments(request.getComments());
		}
		if ((request.getMetadata() != null) && (request.getMetadata().size() > 0)) {
			updatedDevice.getMetadata().clear();
			MetadataProvider.copy(request, updatedDevice);
		}
		updatedDevice.setUpdatedDate(new Date());
		DBObject updated = MongoDevice.toDBObject(updatedDevice);

		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		MongoPersistence.update(devices, query, updated);
		return MongoDevice.fromDBObject(updated);
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
		DBObject match = assertDeviceAssignment(device.getAssignmentToken());
		return MongoDeviceAssignment.fromDBObject(match);
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
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteDevice(java.lang.String, boolean)
	 */
	public IDevice deleteDevice(String hardwareId, boolean force) throws SiteWhereException {
		DBObject existing = assertDevice(hardwareId);
		if (force) {
			DBCollection devices = getMongoClient().getDevicesCollection();
			MongoPersistence.delete(devices, existing);
			return MongoDevice.fromDBObject(existing);
		} else {
			MongoSiteWhereEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
			DBCollection devices = getMongoClient().getDevicesCollection();
			MongoPersistence.update(devices, query, existing);
			return MongoDevice.fromDBObject(existing);
		}
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
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createDeviceAssignment(com.sitewhere.spi.device.request.
	 * IDeviceAssignmentCreateRequest)
	 */
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws SiteWhereException {
		// Verify foreign references.
		DBObject site = getSiteDBObjectByToken(request.getSiteToken());
		if (site == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		DBObject device = assertDevice(request.getDeviceHardwareId());
		if (device.get(MongoDevice.PROP_ASSIGNMENT_TOKEN) != null) {
			throw new SiteWhereSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
		}

		DeviceAssignment newAssignment = new DeviceAssignment();
		newAssignment.setToken(UUID.randomUUID().toString());
		newAssignment.setSiteToken(request.getSiteToken());
		newAssignment.setDeviceHardwareId(request.getDeviceHardwareId());
		newAssignment.setAssetType(request.getAssetType());
		newAssignment.setAssetId(request.getAssetId());
		newAssignment.setActiveDate(new Date());
		newAssignment.setStatus(DeviceAssignmentStatus.Active);

		MongoPersistence.initializeEntityMetadata(newAssignment);
		MetadataProvider.copy(request, newAssignment);

		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		DBObject created = MongoDeviceAssignment.toDBObject(newAssignment);
		MongoPersistence.insert(assignments, created);

		// Update device to point to created assignment.
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, request.getDeviceHardwareId());
		device.put(MongoDevice.PROP_ASSIGNMENT_TOKEN, newAssignment.getToken());
		MongoPersistence.update(devices, query, device);
		return newAssignment;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAssignment(java.lang.String, boolean)
	 */
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws SiteWhereException {
		DBObject existing = assertDeviceAssignment(token);
		if (force) {
			DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
			MongoPersistence.delete(assignments, existing);
			return MongoDeviceAssignment.fromDBObject(existing);
		} else {
			MongoSiteWhereEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
			DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
			MongoPersistence.update(assignments, query, existing);
			return MongoDeviceAssignment.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceForAssignment(com .sitewhere.spi.device
	 * .IDeviceAssignment)
	 */
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		DBObject device = getDeviceDBObjectByHardwareId(assignment.getDeviceHardwareId());
		if (device != null) {
			return MongoDevice.fromDBObject(device);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getSiteForAssignment(com.sitewhere .spi.device.
	 * IDeviceAssignment)
	 */
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		DBObject site = getSiteDBObjectByToken(assignment.getSiteToken());
		if (site != null) {
			return MongoSite.fromDBObject(site);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentMetadata (java.lang.String,
	 * com.sitewhere.spi.device.IMetadataProvider)
	 */
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws SiteWhereException {
		DBObject match = assertDeviceAssignment(token);
		MongoDeviceEntityMetadata.toDBObject(metadata, match);
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		MongoPersistence.update(assignments, query, match);
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
		DBObject match = assertDeviceAssignment(token);
		match.put(MongoDeviceAssignment.PROP_STATUS, status.name());
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentLocation(java.lang.String,
	 * java.lang.String)
	 */
	public IDeviceAssignment updateDeviceAssignmentLocation(String token, String locationId)
			throws SiteWhereException {
		DBObject locationObj = assertDeviceLocation(locationId);
		IDeviceLocation location = MongoDeviceLocation.fromDBObject(locationObj);
		DBObject match = assertDeviceAssignment(token);
		MongoDeviceAssignment.setLocation(location, match);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#addDeviceEventBatch(java.lang.String,
	 * com.sitewhere.spi.device.IDeviceEventBatch)
	 */
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws SiteWhereException {
		DeviceEventBatchResponse response = new DeviceEventBatchResponse();
		DBObject match = assertDeviceAssignment(assignmentToken);
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		for (IDeviceMeasurementsCreateRequest measurements : batch.getMeasurements()) {
			response.getCreatedMeasurements().add(addDeviceMeasurements(assignment, measurements));
		}
		for (IDeviceLocationCreateRequest location : batch.getLocations()) {
			response.getCreatedLocations().add(addDeviceLocation(assignment, location));
		}
		for (IDeviceAlertCreateRequest alert : batch.getAlerts()) {
			response.getCreatedAlerts().add(addDeviceAlert(assignment, alert));
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#endDeviceAssignment(java.lang .String)
	 */
	public IDeviceAssignment endDeviceAssignment(String token) throws SiteWhereException {
		DBObject match = assertDeviceAssignment(token);
		match.put(MongoDeviceAssignment.PROP_RELEASED_DATE, Calendar.getInstance().getTime());
		match.put(MongoDeviceAssignment.PROP_STATUS, DeviceAssignmentStatus.Released.name());
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);

		// Remove device assignment reference.
		DBCollection devices = getMongoClient().getDevicesCollection();
		String hardwareId = (String) match.get(MongoDeviceAssignment.PROP_DEVICE_HARDWARE_ID);
		DBObject deviceMatch = getDeviceDBObjectByHardwareId(hardwareId);
		deviceMatch.removeField(MongoDevice.PROP_ASSIGNMENT_TOKEN);
		query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		MongoPersistence.update(devices, query, deviceMatch);

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
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_DEVICE_HARDWARE_ID, hardwareId);
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
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceMeasurements(com.sitewhere.spi.device.IDeviceAssignment
	 * , com.sitewhere.spi.device.request.IDeviceMeasurementsCreateRequest)
	 */
	public IDeviceMeasurements addDeviceMeasurements(IDeviceAssignment assignment,
			IDeviceMeasurementsCreateRequest request) throws SiteWhereException {
		DeviceMeasurements measurements = new DeviceMeasurements();
		measurements.setSiteToken(assignment.getSiteToken());
		measurements.setDeviceAssignmentToken(assignment.getToken());
		measurements.setAssetName(Utils.getAssetNameForAssignment(assignment));
		measurements.setEventDate(request.getEventDate());
		measurements.setReceivedDate(new Date());
		for (IMetadataEntry entry : request.getMeasurements()) {
			measurements.addOrReplaceMeasurement(entry.getName(), entry.getValue());
		}
		MetadataProvider.copy(request, measurements);

		DBCollection measurementColl = getMongoClient().getMeasurementsCollection();
		DBObject mObject = MongoDeviceMeasurements.toDBObject(measurements);
		MongoPersistence.insert(measurementColl, mObject);
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

	public void associateAlertWithMeasurements(String alertId, String measurementsId)
			throws SiteWhereException {
		throw new SiteWhereException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceLocation(com.sitewhere.spi.device.IDeviceAssignment
	 * , com.sitewhere.spi.device.request.IDeviceLocationCreateRequest)
	 */
	public IDeviceLocation addDeviceLocation(IDeviceAssignment assignment,
			IDeviceLocationCreateRequest request) throws SiteWhereException {
		DeviceLocation location = new DeviceLocation();
		location.setSiteToken(assignment.getSiteToken());
		location.setDeviceAssignmentToken(assignment.getToken());
		location.setAssetName(Utils.getAssetNameForAssignment(assignment));
		location.setEventDate(request.getEventDate());
		location.setReceivedDate(new Date());
		location.setLatitude(request.getLatitude());
		location.setLongitude(request.getLongitude());
		location.setElevation(request.getElevation());
		MetadataProvider.copy(request, location);

		DBCollection locationsColl = getMongoClient().getLocationsCollection();
		DBObject locObject = MongoDeviceLocation.toDBObject(location);
		MongoPersistence.insert(locationsColl, locObject);
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
	 * @see com.sitewhere.spi.device.IDeviceManagement#associateAlertWithLocation(java.lang.String,
	 * java.lang.String)
	 */
	public IDeviceLocation associateAlertWithLocation(String alertId, String locationId)
			throws SiteWhereException {
		// Make sure the location id reference is valid.
		DBObject locObj = assertDeviceLocation(locationId);
		IDeviceLocation location = MongoDeviceLocation.fromDBObject(locObj);

		// Make sure the alert id reference is valid.
		assertDeviceAlert(alertId);

		// If alert id is not already in the list, add it.
		List<String> alertIds = location.getAlertIds();
		if (!alertIds.contains(alertId)) {
			alertIds.add(alertId);
			locObj.put(MongoDeviceEvent.PROP_ALERT_IDS, alertIds);
			DBObject query = new BasicDBObject(MongoDeviceEvent.PROP_EVENT_ID, new ObjectId(locationId));
			MongoPersistence.update(getMongoClient().getLocationsCollection(), query, locObj);
		}
		return MongoDeviceLocation.fromDBObject(locObj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceAlert(com.sitewhere.spi.device.IDeviceAssignment,
	 * com.sitewhere.spi.device.request.IDeviceAlertCreateRequest)
	 */
	public IDeviceAlert addDeviceAlert(IDeviceAssignment assignment, IDeviceAlertCreateRequest request)
			throws SiteWhereException {
		DeviceAlert alert = new DeviceAlert();
		alert.setSiteToken(assignment.getSiteToken());
		alert.setDeviceAssignmentToken(assignment.getToken());
		alert.setAssetName(Utils.getAssetNameForAssignment(assignment));
		alert.setEventDate(request.getEventDate());
		alert.setReceivedDate(new Date());
		alert.setSource(AlertSource.Device);
		alert.setType(request.getType());
		alert.setMessage(request.getMessage());
		alert.setAcknowledged(false);
		MetadataProvider.copy(request, alert);

		DBCollection alertsColl = getMongoClient().getAlertsCollection();
		DBObject alertObject = MongoDeviceAlert.toDBObject(alert);
		MongoPersistence.insert(alertsColl, alertObject);
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
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createSite(com.sitewhere.spi.device.request.ISiteCreateRequest
	 * )
	 */
	public ISite createSite(ISiteCreateRequest request) throws SiteWhereException {
		Site site = new Site();
		site.setName(request.getName());
		site.setDescription(request.getDescription());
		site.setImageUrl(request.getImageUrl());
		site.setMapType(request.getMapType());
		site.setToken(UUID.randomUUID().toString());

		MongoPersistence.initializeEntityMetadata(site);
		MetadataProvider.copy(request, site);
		MetadataProvider.copy(request.getMapMetadata(), site.getMapMetadata());

		DBCollection sites = getMongoClient().getSitesCollection();
		DBObject created = MongoSite.toDBObject(site);
		MongoPersistence.insert(sites, created);
		return MongoSite.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateSite(java.lang.String,
	 * com.sitewhere.spi.device.request.ISiteCreateRequest)
	 */
	public ISite updateSite(String token, ISiteCreateRequest request) throws SiteWhereException {
		DBCollection sites = getMongoClient().getSitesCollection();
		DBObject match = getSiteDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}

		Site site = MongoSite.fromDBObject(match);
		site.setName(request.getName());
		site.setDescription(request.getDescription());
		site.setImageUrl(request.getImageUrl());
		site.setMapType(request.getMapType());
		site.setMetadata(new ArrayList<MetadataEntry>());
		site.setMapMetadata(new MetadataProvider());
		site.setUpdatedDate(new Date());
		site.setUpdatedBy("admin");

		MetadataProvider.copy(request, site);
		MetadataProvider.copy(request.getMapMetadata(), site.getMapMetadata());

		DBObject updated = MongoSite.toDBObject(site);

		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, token);
		MongoPersistence.update(sites, query, updated);
		return MongoSite.fromDBObject(updated);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteSite(java.lang.String, boolean)
	 */
	public ISite deleteSite(String siteToken, boolean force) throws SiteWhereException {
		DBObject existing = assertSite(siteToken);
		if (force) {
			DBCollection sites = getMongoClient().getSitesCollection();
			MongoPersistence.delete(sites, existing);
			return MongoSite.fromDBObject(existing);
		} else {
			MongoSiteWhereEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, siteToken);
			DBCollection sites = getMongoClient().getSitesCollection();
			MongoPersistence.update(sites, query, existing);
			return MongoSite.fromDBObject(existing);
		}
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
	 * @see com.sitewhere.spi.device.IDeviceManagement#createZone(com.sitewhere.spi.device.ISite,
	 * com.sitewhere.spi.device.request.IZoneCreateRequest)
	 */
	public IZone createZone(ISite site, IZoneCreateRequest request) throws SiteWhereException {
		Zone zone = new Zone();
		zone.setToken(UUID.randomUUID().toString());
		zone.setSiteToken(site.getToken());
		zone.setName(request.getName());
		zone.setBorderColor(request.getBorderColor());
		zone.setFillColor(request.getFillColor());
		zone.setOpacity(request.getOpacity());

		MongoPersistence.initializeEntityMetadata(zone);
		MetadataProvider.copy(request, zone);

		for (ILocation coordinate : request.getCoordinates()) {
			zone.getCoordinates().add(coordinate);
		}

		DBCollection zones = getMongoClient().getZonesCollection();
		DBObject created = MongoZone.toDBObject(zone);
		MongoPersistence.insert(zones, created);
		return MongoZone.fromDBObject(created);
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
	 * Return the {@link DBObject} for the site with the given token. Throws an exception if the token is not
	 * found.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertSite(String token) throws SiteWhereException {
		DBObject match = getSiteDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.INFO);
		}
		return match;
	}

	/**
	 * Return the {@link DBObject} for the device with the given hardware id. Throws an exception if the
	 * hardware id is not found.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertDevice(String hardwareId) throws SiteWhereException {
		DBObject match = getDeviceDBObjectByHardwareId(hardwareId);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.INFO);
		}
		return match;
	}

	/**
	 * Return the {@link DBObject} for the assignment with the given token. Throws an exception if the token
	 * is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertDeviceAssignment(String token) throws SiteWhereException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		return match;
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
	 * Return the {@link DBObject} for the device location with the given id. Throws an exception if the id is
	 * not valid.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertDeviceLocation(String id) throws SiteWhereException {
		DBObject match = getDeviceLocationById(id);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceLocationId, ErrorLevel.ERROR);
		}
		return match;
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

	/**
	 * Return the {@link DBObject} for the device measurements with the given id. Throws an exception if the
	 * id is not valid.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertDeviceMeasurements(String id) throws SiteWhereException {
		DBObject match = getDeviceMeasurementsById(id);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceMeasurementsId, ErrorLevel.ERROR);
		}
		return match;
	}

	/**
	 * Find a device alert by unique event id.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getDeviceAlertById(String id) throws SiteWhereException {
		DBCollection coll = getMongoClient().getAlertsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceEvent.PROP_EVENT_ID, new ObjectId(id));
		return coll.findOne(query);
	}

	/**
	 * Return the {@link DBObject} for the device alert with the given id. Throws an exception if the id is
	 * not valid.
	 * 
	 * @param id
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertDeviceAlert(String id) throws SiteWhereException {
		DBObject match = getDeviceAlertById(id);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAlertId, ErrorLevel.ERROR);
		}
		return match;
	}

	public SiteWhereMongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(SiteWhereMongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}