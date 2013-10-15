/*
 * DeviceManagementMetricsFacade.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.server.metrics;

import java.util.Date;
import java.util.List;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sitewhere.rest.service.search.SearchResults;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.common.IMetadataProvider;
import com.sitewhere.spi.common.ISearchCriteria;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAlert;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceEventBatch;
import com.sitewhere.spi.device.IDeviceEventBatchResponse;
import com.sitewhere.spi.device.IDeviceLocation;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceMeasurements;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.device.IZone;
import com.sitewhere.spi.device.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.request.ISiteCreateRequest;
import com.sitewhere.spi.device.request.IZoneCreateRequest;

/**
 * Wraps a device management implementation in a facade that gathers metrics about each
 * API call.
 * 
 * @author Derek
 */
public class DeviceManagementMetricsFacade implements IDeviceManagement {

	/** Delgate instance that actually does the work */
	private IDeviceManagement delegate;

	/** Times invocations of addDeviceEventBatch() */
	private final Timer addDeviceEventBatchTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "addDeviceEventBatch", "timer"));
	
	/** Times invocations of getDeviceAssignmentsForSite() */
	private final Timer getDeviceAssignmentsForSiteTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "getDeviceAssignmentsForSite", "timer"));
	
	/** Times invocations of addDeviceMeasurements() */
	private final Timer addDeviceMeasurementsTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "addDeviceMeasurements", "timer"));
	
	/** Times invocations of listDeviceMeasurements() */
	private final Timer listDeviceMeasurementsTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceMeasurements", "timer"));
	
	/** Times invocations of listDeviceMeasurementsForSite() */
	private final Timer listDeviceMeasurementsForSiteTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceMeasurementsForSite", "timer"));
	
	/** Times invocations of addDeviceLocation() */
	private final Timer addDeviceLocationTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "addDeviceLocation", "timer"));
	
	/** Times invocations of listDeviceLocations() */
	private final Timer listDeviceLocationsTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceLocations", "timer"));
	
	/** Times invocations of listDeviceLocationsForSite() */
	private final Timer listDeviceLocationsForSiteTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceLocationsForSite", "timer"));
	
	/** Times invocations of addDeviceAlert() */
	private final Timer addDeviceAlertTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "addDeviceAlert", "timer"));
	
	/** Times invocations of listDeviceAlerts() */
	private final Timer listDeviceAlertsTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceAlerts", "timer"));
	
	/** Times invocations of listDeviceAlertsForSite() */
	private final Timer listDeviceAlertsForSiteTimer = getMetrics().timer(
			MetricRegistry.name(IDeviceManagement.class, "listDeviceAlertsForSite", "timer"));
	
	public DeviceManagementMetricsFacade(IDeviceManagement delegate) {
		this.delegate = delegate;
	}

	/**
	 * Get the central metrics registry.
	 * 
	 * @return
	 */
	public MetricRegistry getMetrics() {
		return SiteWhereServer.getInstance().getMetricRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createDevice(com.sitewhere.spi.device
	 * .request.IDeviceCreateRequest)
	 */
	public IDevice createDevice(IDeviceCreateRequest device) throws SiteWhereException {
		return delegate.createDevice(device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getDeviceByHardwareId(java.lang.String)
	 */
	public IDevice getDeviceByHardwareId(String hardwareId) throws SiteWhereException {
		return delegate.getDeviceByHardwareId(hardwareId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateDevice(java.lang.String,
	 * com.sitewhere.spi.device.request.IDeviceCreateRequest)
	 */
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws SiteWhereException {
		return delegate.updateDevice(hardwareId, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getCurrentDeviceAssignment(com.sitewhere
	 * .spi.device.IDevice)
	 */
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws SiteWhereException {
		return delegate.getCurrentDeviceAssignment(device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDevices(boolean,
	 * com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDevice> listDevices(boolean includeDeleted, ISearchCriteria criteria)
			throws SiteWhereException {
		return delegate.listDevices(includeDeleted, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listUnassignedDevices(com.sitewhere.
	 * spi.common.ISearchCriteria)
	 */
	public SearchResults<IDevice> listUnassignedDevices(ISearchCriteria criteria) throws SiteWhereException {
		return delegate.listUnassignedDevices(criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteDevice(java.lang.String,
	 * boolean)
	 */
	public IDevice deleteDevice(String hardwareId, boolean force) throws SiteWhereException {
		return delegate.deleteDevice(hardwareId, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createDeviceAssignment(com.sitewhere
	 * .spi.device.request.IDeviceAssignmentCreateRequest)
	 */
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws SiteWhereException {
		return delegate.createDeviceAssignment(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentByToken(java.lang
	 * .String)
	 */
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
		return delegate.getDeviceAssignmentByToken(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAssignment(java.lang.String,
	 * boolean)
	 */
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws SiteWhereException {
		return delegate.deleteDeviceAssignment(token, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getDeviceForAssignment(com.sitewhere
	 * .spi.device.IDeviceAssignment)
	 */
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		return delegate.getDeviceForAssignment(assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getSiteForAssignment(com.sitewhere.spi
	 * .device.IDeviceAssignment)
	 */
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		return delegate.getSiteForAssignment(assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentMetadata(java.
	 * lang.String, com.sitewhere.spi.common.IMetadataProvider)
	 */
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws SiteWhereException {
		return delegate.updateDeviceAssignmentMetadata(token, metadata);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentStatus(java.lang
	 * .String, com.sitewhere.spi.device.DeviceAssignmentStatus)
	 */
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws SiteWhereException {
		return delegate.updateDeviceAssignmentStatus(token, status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignmentLocation(java.
	 * lang.String, java.lang.String)
	 */
	public IDeviceAssignment updateDeviceAssignmentLocation(String token, String locationId)
			throws SiteWhereException {
		return delegate.updateDeviceAssignmentLocation(token, locationId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceEventBatch(java.lang.String,
	 * com.sitewhere.spi.device.IDeviceEventBatch)
	 */
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws SiteWhereException {
		final Timer.Context context = addDeviceEventBatchTimer.time();
		try {
			return delegate.addDeviceEventBatch(assignmentToken, batch);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#endDeviceAssignment(java.lang.String)
	 */
	public IDeviceAssignment endDeviceAssignment(String token) throws SiteWhereException {
		return delegate.endDeviceAssignment(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentHistory(java.lang
	 * .String, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId,
			ISearchCriteria criteria) throws SiteWhereException {
		return delegate.getDeviceAssignmentHistory(hardwareId, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentsForSite(java.lang
	 * .String, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken,
			ISearchCriteria criteria) throws SiteWhereException {
		final Timer.Context context = getDeviceAssignmentsForSiteTimer.time();
		try {
			return delegate.getDeviceAssignmentsForSite(siteToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentsNear(double,
	 * double, double, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAssignment> getDeviceAssignmentsNear(double latitude, double longitude,
			double maxDistance, ISearchCriteria criteria) throws SiteWhereException {
		return delegate.getDeviceAssignmentsNear(latitude, longitude, maxDistance, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceMeasurements(com.sitewhere.
	 * spi.device.IDeviceAssignment,
	 * com.sitewhere.spi.device.request.IDeviceMeasurementsCreateRequest)
	 */
	public IDeviceMeasurements addDeviceMeasurements(IDeviceAssignment assignment,
			IDeviceMeasurementsCreateRequest measurements) throws SiteWhereException {
		final Timer.Context context = addDeviceMeasurementsTimer.time();
		try {
			return delegate.addDeviceMeasurements(assignment, measurements);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDeviceMeasurements(java.lang.String,
	 * com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceMeasurements> listDeviceMeasurements(String siteToken,
			ISearchCriteria criteria) throws SiteWhereException {
		final Timer.Context context = listDeviceMeasurementsTimer.time();
		try {
			return delegate.listDeviceMeasurements(siteToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDeviceMeasurementsForSite(java.lang
	 * .String, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(String siteToken,
			ISearchCriteria criteria) throws SiteWhereException {
		final Timer.Context context = listDeviceMeasurementsForSiteTimer.time();
		try {
			return delegate.listDeviceMeasurementsForSite(siteToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#associateAlertWithMeasurements(java.
	 * lang.String, java.lang.String)
	 */
	public void associateAlertWithMeasurements(String alertId, String measurementsId)
			throws SiteWhereException {
		delegate.associateAlertWithMeasurements(alertId, measurementsId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceLocation(com.sitewhere.spi.
	 * device.IDeviceAssignment,
	 * com.sitewhere.spi.device.request.IDeviceLocationCreateRequest)
	 */
	public IDeviceLocation addDeviceLocation(IDeviceAssignment assignment,
			IDeviceLocationCreateRequest request) throws SiteWhereException {
		final Timer.Context context = addDeviceLocationTimer.time();
		try {
			return delegate.addDeviceLocation(assignment, request);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDeviceLocations(java.lang.String,
	 * com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceLocation> listDeviceLocations(String assignmentToken, ISearchCriteria criteria)
			throws SiteWhereException {
		final Timer.Context context = listDeviceLocationsTimer.time();
		try {
			return delegate.listDeviceLocations(assignmentToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDeviceLocationsForSite(java.lang
	 * .String, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceLocation> listDeviceLocationsForSite(String siteToken,
			ISearchCriteria criteria) throws SiteWhereException {
		final Timer.Context context = listDeviceLocationsForSiteTimer.time();
		try {
			return delegate.listDeviceLocationsForSite(siteToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceLocations(java.util.List,
	 * java.util.Date, java.util.Date, com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceLocation> listDeviceLocations(List<String> assignmentTokens, Date start,
			Date end, ISearchCriteria criteria) throws SiteWhereException {
		return delegate.listDeviceLocations(assignmentTokens, start, end, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#associateAlertWithLocation(java.lang
	 * .String, java.lang.String)
	 */
	public IDeviceLocation associateAlertWithLocation(String alertId, String locationId)
			throws SiteWhereException {
		return delegate.associateAlertWithLocation(alertId, locationId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#addDeviceAlert(com.sitewhere.spi.device
	 * .IDeviceAssignment, com.sitewhere.spi.device.request.IDeviceAlertCreateRequest)
	 */
	public IDeviceAlert addDeviceAlert(IDeviceAssignment assignment, IDeviceAlertCreateRequest request)
			throws SiteWhereException {
		final Timer.Context context = addDeviceAlertTimer.time();
		try {
			return delegate.addDeviceAlert(assignment, request);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceAlerts(java.lang.String,
	 * com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAlert> listDeviceAlerts(String assignmentToken, ISearchCriteria criteria)
			throws SiteWhereException {
		final Timer.Context context = listDeviceAlertsTimer.time();
		try {
			return delegate.listDeviceAlerts(assignmentToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#listDeviceAlertsForSite(java.lang.String
	 * , com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAlert> listDeviceAlertsForSite(String siteToken, ISearchCriteria criteria)
			throws SiteWhereException {
		final Timer.Context context = listDeviceAlertsForSiteTimer.time();
		try {
			return delegate.listDeviceAlertsForSite(siteToken, criteria);
		} finally {
			context.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createSite(com.sitewhere.spi.device.
	 * request.ISiteCreateRequest)
	 */
	public ISite createSite(ISiteCreateRequest request) throws SiteWhereException {
		return delegate.createSite(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteSite(java.lang.String,
	 * boolean)
	 */
	public ISite deleteSite(String siteToken, boolean force) throws SiteWhereException {
		return delegate.deleteSite(siteToken, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateSite(java.lang.String,
	 * com.sitewhere.spi.device.request.ISiteCreateRequest)
	 */
	public ISite updateSite(String siteToken, ISiteCreateRequest request) throws SiteWhereException {
		return delegate.updateSite(siteToken, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getSiteByToken(java.lang.String)
	 */
	public ISite getSiteByToken(String token) throws SiteWhereException {
		return delegate.getSiteByToken(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listSites(com.sitewhere.spi.common.
	 * ISearchCriteria)
	 */
	public SearchResults<ISite> listSites(ISearchCriteria criteria) throws SiteWhereException {
		return delegate.listSites(criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.IDeviceManagement#createZone(com.sitewhere.spi.device.
	 * ISite, com.sitewhere.spi.device.request.IZoneCreateRequest)
	 */
	public IZone createZone(ISite site, IZoneCreateRequest request) throws SiteWhereException {
		return delegate.createZone(site, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#updateZone(java.lang.String,
	 * com.sitewhere.spi.device.request.IZoneCreateRequest)
	 */
	public IZone updateZone(String token, IZoneCreateRequest request) throws SiteWhereException {
		return delegate.updateZone(token, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#getZone(java.lang.String)
	 */
	public IZone getZone(String zoneToken) throws SiteWhereException {
		return delegate.getZone(zoneToken);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#listZones(java.lang.String,
	 * com.sitewhere.spi.common.ISearchCriteria)
	 */
	public SearchResults<IZone> listZones(String siteToken, ISearchCriteria criteria)
			throws SiteWhereException {
		return delegate.listZones(siteToken, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.device.IDeviceManagement#deleteZone(java.lang.String,
	 * boolean)
	 */
	public IZone deleteZone(String zoneToken, boolean force) throws SiteWhereException {
		return delegate.deleteZone(zoneToken, force);
	}
}