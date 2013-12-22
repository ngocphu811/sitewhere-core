/*
 * SiteWherePersistence.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.sitewhere.rest.model.common.MetadataProvider;
import com.sitewhere.rest.model.common.MetadataProviderEntity;
import com.sitewhere.rest.model.device.DeviceAlert;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.DeviceAssignmentState;
import com.sitewhere.rest.model.device.DeviceEvent;
import com.sitewhere.rest.model.device.DeviceLocation;
import com.sitewhere.rest.model.device.DeviceMeasurement;
import com.sitewhere.rest.model.device.DeviceMeasurements;
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.rest.model.device.Zone;
import com.sitewhere.rest.model.user.GrantedAuthority;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.security.LoginManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.common.ILocation;
import com.sitewhere.spi.device.AlertLevel;
import com.sitewhere.spi.device.AlertSource;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDeviceAlert;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceAssignmentState;
import com.sitewhere.spi.device.IDeviceEventBatch;
import com.sitewhere.spi.device.IDeviceMeasurement;
import com.sitewhere.spi.device.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceEventCreateRequest;
import com.sitewhere.spi.device.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.request.ISiteCreateRequest;
import com.sitewhere.spi.device.request.IZoneCreateRequest;
import com.sitewhere.spi.user.request.IGrantedAuthorityCreateRequest;
import com.sitewhere.spi.user.request.IUserCreateRequest;

/**
 * Common methods needed by device service provider implementations.
 * 
 * @author Derek
 */
public class SiteWherePersistence {

	/** Password encoder */
	private static MessageDigestPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	/**
	 * Initialize entity fields.
	 * 
	 * @param entity
	 * @throws SiteWhereException
	 */
	public static void initializeEntityMetadata(MetadataProviderEntity entity) throws SiteWhereException {
		entity.setCreatedDate(new Date());
		entity.setCreatedBy(LoginManager.getCurrentlyLoggedInUser().getUsername());
		entity.setDeleted(false);
	}

	/**
	 * Set updated fields.
	 * 
	 * @param entity
	 * @throws SiteWhereException
	 */
	public static void setUpdatedEntityMetadata(MetadataProviderEntity entity) throws SiteWhereException {
		entity.setUpdatedDate(new Date());
		entity.setUpdatedBy(LoginManager.getCurrentlyLoggedInUser().getUsername());
	}

	/**
	 * Common logic for creating new site object and populating it from request.
	 * 
	 * @param source
	 * @param uuid
	 * @return
	 * @throws SiteWhereException
	 */
	public static Site siteCreateLogic(ISiteCreateRequest source, String uuid) throws SiteWhereException {
		Site site = new Site();
		site.setName(source.getName());
		site.setDescription(source.getDescription());
		site.setImageUrl(source.getImageUrl());
		site.setMapType(source.getMapType());
		site.setToken(uuid);

		SiteWherePersistence.initializeEntityMetadata(site);
		MetadataProvider.copy(source, site);
		MetadataProvider.copy(source.getMapMetadata(), site.getMapMetadata());
		return site;
	}

	/**
	 * Common logic for copying data from site update request to existing site.
	 * 
	 * @param source
	 * @param target
	 * @throws SiteWhereException
	 */
	public static void siteUpdateLogic(ISiteCreateRequest source, Site target) throws SiteWhereException {
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setImageUrl(source.getImageUrl());
		target.setMapType(source.getMapType());
		target.clearMetadata();
		target.setMapMetadata(new MetadataProvider());

		MetadataProvider.copy(source, target);
		MetadataProvider.copy(source.getMapMetadata(), target.getMapMetadata());
		SiteWherePersistence.setUpdatedEntityMetadata(target);
	}

	/**
	 * Common logic for creating a device assignment from a request.
	 * 
	 * @param source
	 * @param siteToken
	 * @param uuid
	 * @return
	 * @throws SiteWhereException
	 */
	public static DeviceAssignment deviceAssignmentCreateLogic(IDeviceAssignmentCreateRequest source,
			String siteToken, String uuid) throws SiteWhereException {
		DeviceAssignment newAssignment = new DeviceAssignment();
		newAssignment.setToken(uuid);
		newAssignment.setSiteToken(source.getSiteToken());
		newAssignment.setDeviceHardwareId(source.getDeviceHardwareId());
		newAssignment.setAssignmentType(source.getAssignmentType());
		newAssignment.setAssetId(source.getAssetId());
		newAssignment.setActiveDate(new Date());
		newAssignment.setStatus(DeviceAssignmentStatus.Active);

		SiteWherePersistence.initializeEntityMetadata(newAssignment);
		MetadataProvider.copy(source, newAssignment);

		return newAssignment;
	}

	/**
	 * Common creation logic for all device events.
	 * 
	 * @param request
	 * @param assignment
	 * @param target
	 */
	public static void deviceEventCreateLogic(IDeviceEventCreateRequest request,
			IDeviceAssignment assignment, DeviceEvent target) {
		target.setSiteToken(assignment.getSiteToken());
		target.setDeviceAssignmentToken(assignment.getToken());
		target.setAssignmentType(assignment.getAssignmentType());
		target.setAssetId(assignment.getAssetId());
		target.setEventDate(request.getEventDate());
		target.setReceivedDate(new Date());
		MetadataProvider.copy(request, target);
	}

	/**
	 * Common logic for creating {@link DeviceMeasurements} from
	 * {@link IDeviceMeasurementsCreateRequest}.
	 * 
	 * @param request
	 * @param assignment
	 * @return
	 * @throws SiteWhereException
	 */
	public static DeviceMeasurements deviceMeasurementsCreateLogic(IDeviceMeasurementsCreateRequest request,
			IDeviceAssignment assignment) throws SiteWhereException {
		DeviceMeasurements measurements = new DeviceMeasurements();
		deviceEventCreateLogic(request, assignment, measurements);
		for (String key : request.getMeasurements().keySet()) {
			measurements.addOrReplaceMeasurement(key, request.getMeasurement(key));
		}
		return measurements;
	}

	/**
	 * Common logic for creating {@link DeviceLocation} from
	 * {@link IDeviceLocationCreateRequest}.
	 * 
	 * @param assignment
	 * @param request
	 * @return
	 * @throws SiteWhereException
	 */
	public static DeviceLocation deviceLocationCreateLogic(IDeviceAssignment assignment,
			IDeviceLocationCreateRequest request) throws SiteWhereException {
		DeviceLocation location = new DeviceLocation();
		deviceEventCreateLogic(request, assignment, location);
		location.setLatitude(request.getLatitude());
		location.setLongitude(request.getLongitude());
		location.setElevation(request.getElevation());
		return location;
	}

	/**
	 * Common logic for creating {@link DeviceAlert} from
	 * {@link IDeviceAlertCreateRequest}.
	 * 
	 * @param assignment
	 * @param request
	 * @return
	 * @throws SiteWhereException
	 */
	public static DeviceAlert deviceAlertCreateLogic(IDeviceAssignment assignment,
			IDeviceAlertCreateRequest request) throws SiteWhereException {
		DeviceAlert alert = new DeviceAlert();
		deviceEventCreateLogic(request, assignment, alert);
		alert.setSource(AlertSource.Device);
		if (request.getLevel() != null) {
			alert.setLevel(request.getLevel());
		} else {
			alert.setLevel(AlertLevel.Info);
		}
		alert.setType(request.getType());
		alert.setMessage(request.getMessage());
		return alert;
	}

	/**
	 * Creates an updated {@link DeviceAssignmentState} based on existing state and a
	 * batch of events that should update the state.
	 * 
	 * @param assignment
	 * @param batch
	 * @return
	 * @throws SiteWhereException
	 */
	public static DeviceAssignmentState assignmentStateUpdateLogic(IDeviceAssignment assignment,
			IDeviceEventBatch batch) throws SiteWhereException {
		DeviceAssignmentState state = new DeviceAssignmentState();
		assignmentStateLocationUpdateLogic(assignment, state, batch);
		assignmentStateMeasurementsUpdateLogic(assignment, state, batch);
		assignmentStateAlertsUpdateLogic(assignment, state, batch);
		return state;
	}

	/**
	 * Update state "last location" based on new locations from batch.
	 * 
	 * @param assignment
	 * @param updated
	 * @param batch
	 * @throws SiteWhereException
	 */
	public static void assignmentStateLocationUpdateLogic(IDeviceAssignment assignment,
			DeviceAssignmentState updated, IDeviceEventBatch batch) throws SiteWhereException {
		IDeviceAssignmentState existing = assignment.getState();
		if ((existing != null) && (existing.getLastLocation() != null)) {
			updated.setLastLocation(DeviceLocation.copy(existing.getLastLocation()));
		}
		if ((batch.getLocations() != null) && (!batch.getLocations().isEmpty())) {
			// Find latest location if multiple are passed.
			IDeviceLocationCreateRequest latest = batch.getLocations().get(0);
			for (IDeviceLocationCreateRequest lc : batch.getLocations()) {
				if ((lc.getEventDate() != null) && (lc.getEventDate().after(latest.getEventDate()))) {
					latest = lc;
				}
			}
			// Make sure existing location measurement not after latest.
			if ((updated.getLastLocation() == null)
					|| (latest.getEventDate().after(updated.getLastLocation().getEventDate()))) {
				updated.setLastLocation(deviceLocationCreateLogic(assignment, latest));
			}
		}
	}

	/**
	 * Update state "latest measurements" based on new measurements from batch.
	 * 
	 * @param assignment
	 * @param updated
	 * @param batch
	 * @throws SiteWhereException
	 */
	public static void assignmentStateMeasurementsUpdateLogic(IDeviceAssignment assignment,
			DeviceAssignmentState updated, IDeviceEventBatch batch) throws SiteWhereException {
		IDeviceAssignmentState existing = assignment.getState();
		Map<String, IDeviceMeasurement> measurementsById = new HashMap<String, IDeviceMeasurement>();
		if ((existing != null) && (existing.getLatestMeasurements() != null)) {
			for (IDeviceMeasurement m : existing.getLatestMeasurements()) {
				measurementsById.put(m.getName(), m);
			}
		}
		if ((batch.getMeasurements() != null) && (!batch.getMeasurements().isEmpty())) {
			for (IDeviceMeasurementsCreateRequest request : batch.getMeasurements()) {
				for (String key : request.getMeasurements().keySet()) {
					IDeviceMeasurement em = measurementsById.get(key);
					if ((em == null) || (em.getEventDate().before(request.getEventDate()))) {
						DeviceMeasurement newMeasurement = new DeviceMeasurement();
						deviceEventCreateLogic(request, assignment, newMeasurement);
						newMeasurement.setName(key);
						newMeasurement.setValue(request.getMeasurement(key));
						measurementsById.put(key, newMeasurement);
					}
				}
			}
		}
		updated.getLatestMeasurements().clear();
		for (IDeviceMeasurement m : measurementsById.values()) {
			updated.getLatestMeasurements().add(m);
		}
	}

	/**
	 * Update state "latest alerts" based on new alerts from batch.
	 * 
	 * @param assignment
	 * @param updated
	 * @param batch
	 * @throws SiteWhereException
	 */
	public static void assignmentStateAlertsUpdateLogic(IDeviceAssignment assignment,
			DeviceAssignmentState updated, IDeviceEventBatch batch) throws SiteWhereException {
		IDeviceAssignmentState existing = assignment.getState();
		Map<String, IDeviceAlert> alertsById = new HashMap<String, IDeviceAlert>();
		if ((existing != null) && (existing.getLatestAlerts() != null)) {
			for (IDeviceAlert a : existing.getLatestAlerts()) {
				alertsById.put(a.getType(), a);
			}
		}
		if ((batch.getAlerts() != null) && (!batch.getAlerts().isEmpty())) {
			for (IDeviceAlertCreateRequest request : batch.getAlerts()) {
				IDeviceAlert ea = alertsById.get(request.getType());
				if ((ea == null) || (ea.getEventDate().before(request.getEventDate()))) {
					DeviceAlert newAlert = deviceAlertCreateLogic(assignment, request);
					alertsById.put(newAlert.getType(), newAlert);
				}
			}
		}
		updated.getLatestAlerts().clear();
		for (IDeviceAlert a : alertsById.values()) {
			updated.getLatestAlerts().add(a);
		}
	}

	/**
	 * Common logic for creating a zone based on an incoming request.
	 * 
	 * @param source
	 * @param siteToken
	 * @param uuid
	 * @return
	 * @throws SiteWhereException
	 */
	public static Zone zoneCreateLogic(IZoneCreateRequest source, String siteToken, String uuid)
			throws SiteWhereException {
		Zone zone = new Zone();
		zone.setToken(uuid);
		zone.setSiteToken(siteToken);
		zone.setName(source.getName());
		zone.setBorderColor(source.getBorderColor());
		zone.setFillColor(source.getFillColor());
		zone.setOpacity(source.getOpacity());

		SiteWherePersistence.initializeEntityMetadata(zone);
		MetadataProvider.copy(source, zone);

		for (ILocation coordinate : source.getCoordinates()) {
			zone.getCoordinates().add(coordinate);
		}
		return zone;
	}

	/**
	 * Common code for copying information from an update request to an existing zone.
	 * 
	 * @param source
	 * @param target
	 * @throws SiteWhereException
	 */
	public static void zoneUpdateLogic(IZoneCreateRequest source, Zone target) throws SiteWhereException {
		target.setName(source.getName());
		target.setBorderColor(source.getBorderColor());
		target.setFillColor(source.getFillColor());
		target.setOpacity(source.getOpacity());

		target.getCoordinates().clear();
		for (ILocation coordinate : source.getCoordinates()) {
			target.getCoordinates().add(coordinate);
		}

		SiteWherePersistence.setUpdatedEntityMetadata(target);
		MetadataProvider.copy(source, target);
	}

	/**
	 * Common logic for creating a user based on an incoming request.
	 * 
	 * @param source
	 * @return
	 * @throws SiteWhereException
	 */
	public static User userCreateLogic(IUserCreateRequest source) throws SiteWhereException {
		User user = new User();
		user.setUsername(source.getUsername());
		user.setHashedPassword(passwordEncoder.encodePassword(source.getPassword(), null));
		user.setFirstName(source.getFirstName());
		user.setLastName(source.getLastName());
		user.setLastLogin(null);
		user.setStatus(source.getStatus());
		user.setAuthorities(source.getAuthorities());

		MetadataProvider.copy(source, user);
		SiteWherePersistence.initializeEntityMetadata(user);
		return user;
	}

	/**
	 * Common code for copying information from an update request to an existing user.
	 * 
	 * @param source
	 * @param target
	 * @throws SiteWhereException
	 */
	public static void userUpdateLogic(IUserCreateRequest source, User target) throws SiteWhereException {
		if (source.getUsername() != null) {
			target.setUsername(source.getUsername());
		}
		if (source.getPassword() != null) {
			target.setHashedPassword(passwordEncoder.encodePassword(source.getPassword(), null));
		}
		if (source.getFirstName() != null) {
			target.setFirstName(source.getFirstName());
		}
		if (source.getLastName() != null) {
			target.setLastName(source.getLastName());
		}
		if (source.getStatus() != null) {
			target.setStatus(source.getStatus());
		}
		if (source.getAuthorities() != null) {
			target.setAuthorities(source.getAuthorities());
		}
		if ((source.getMetadata() != null) && (source.getMetadata().size() > 0)) {
			target.getMetadata().clear();
			MetadataProvider.copy(source, target);
		}
		SiteWherePersistence.setUpdatedEntityMetadata(target);
	}

	/**
	 * Common logic for creating a granted authority based on an incoming request.
	 * 
	 * @param source
	 * @return
	 * @throws SiteWhereException
	 */
	public static GrantedAuthority grantedAuthorityCreateLogic(IGrantedAuthorityCreateRequest source)
			throws SiteWhereException {
		GrantedAuthority auth = new GrantedAuthority();
		auth.setAuthority(source.getAuthority());
		auth.setDescription(source.getDescription());
		return auth;
	}

	/**
	 * Common logic for encoding a plaintext password.
	 * 
	 * @param plaintext
	 * @return
	 */
	public static String encodePassoword(String plaintext) {
		return passwordEncoder.encodePassword(plaintext, null);
	}
}