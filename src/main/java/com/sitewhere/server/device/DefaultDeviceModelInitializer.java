/*
 * DefaultDeviceModelInitializer.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.server.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.request.DeviceAlertCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceAssignmentCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceMeasurementsCreateRequest;
import com.sitewhere.rest.model.device.request.SiteCreateRequest;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.DeviceAssignmentType;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceMeasurements;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.server.device.IDeviceModelInitializer;

/**
 * Used to load a default site/devices/assignments/events so that there is demo data in
 * the system. The server only offers this functionality if no sites already exist.
 * 
 * @author Derek
 */
public class DefaultDeviceModelInitializer implements IDeviceModelInitializer {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultDeviceModelInitializer.class);

	/** Prefix for create site log message */
	public static final String PREFIX_CREATE_SITE = "[Create Site]";

	/** Prefix for create device log message */
	public static final String PREFIX_CREATE_DEVICE = "[Create Device]";

	/** Prefix for create assignment log message */
	public static final String PREFIX_CREATE_ASSIGNMENT = "[Create Assignment]";

	/** Number of devices to create */
	public static final int NUM_SITES = 1;

	/** Number of devices/assignments to create */
	public static final int ASSIGNMENTS_PER_SITE = 5;

	/** Number of events per assignment */
	public static final int EVENTS_PER_ASSIGNMENT = 50;

	/** Image URL assocaited with sites */
	public static final String SITE_IMAGE_URL =
			"http://i.telegraph.co.uk/multimedia/archive/01809/satellite_1809335c.jpg";

	/** Available choices for devices/assignments that track location */
	protected static AssignmentChoice[] LOCATION_TRACKERS = {
			new AssignmentChoice("175", "Equipment Tracker", DeviceAssignmentType.Hardware, "300"),
			new AssignmentChoice("175", "Equipment Tracker", DeviceAssignmentType.Hardware, "301"),
			new AssignmentChoice("175", "Equipment Tracker", DeviceAssignmentType.Hardware, "302"),
			new AssignmentChoice("174", "Equipment Tracker", DeviceAssignmentType.Hardware, "303"),
			new AssignmentChoice("174", "Equipment Tracker", DeviceAssignmentType.Hardware, "304") };

	/** Device management implementation */
	protected IDeviceManagement deviceManagement;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.device.IDeviceModelInitializer#initialize(com.sitewhere
	 * .spi.device.IDeviceManagement)
	 */
	@Override
	public void initialize(IDeviceManagement deviceManagement) throws SiteWhereException {
		this.deviceManagement = deviceManagement;

		// Use the system account for logging "created by" on created elements.
		SecurityContextHolder.getContext().setAuthentication(SiteWhereServer.getSystemAuthentication());

		List<ISite> sites = createSites();
		for (ISite site : sites) {
			List<IDeviceAssignment> assignments = createAssignments(site);
			for (IDeviceAssignment assignment : assignments) {
				assignment.getActiveDate();
			}
		}

		SecurityContextHolder.getContext().setAuthentication(null);
	}

	/**
	 * Create example sites.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	public List<ISite> createSites() throws SiteWhereException {
		List<ISite> results = new ArrayList<ISite>();
		for (int x = 0; x < NUM_SITES; x++) {
			SiteCreateRequest request = new SiteCreateRequest();
			request.setName("Construction Site " + (x + 1));
			request.setDescription("A construction site with many high-value assets that should "
					+ "not be taken offset. The system provides location tracking for the assets and notifies "
					+ "administrators if any of the assets move outside of the general site area or "
					+ "into areas where they are not allowed.");
			request.setImageUrl(SITE_IMAGE_URL);
			request.setMapType("mapquest");
			results.add(getDeviceManagement().createSite(request));
			LOGGER.info(PREFIX_CREATE_SITE + " " + request.getName());
		}
		return results;
	}

	/**
	 * Create devices for a site and assign them.
	 * 
	 * @param site
	 * @return
	 * @throws SiteWhereException
	 */
	public List<IDeviceAssignment> createAssignments(ISite site) throws SiteWhereException {
		List<IDeviceAssignment> results = new ArrayList<IDeviceAssignment>();
		for (int x = 0; x < ASSIGNMENTS_PER_SITE; x++) {
			AssignmentChoice assnChoice = getRandomAssignmentChoice();

			DeviceCreateRequest request = new DeviceCreateRequest();
			request.setHardwareId(UUID.randomUUID().toString());
			request.setComments(assnChoice.getDeviceDescriptionBase() + " " + (x + 1) + ".");
			request.setAssetId(assnChoice.getDeviceAssetId());
			IDevice device = getDeviceManagement().createDevice(request);
			LOGGER.info(PREFIX_CREATE_DEVICE + " " + device.getHardwareId());

			DeviceAssignmentCreateRequest assnRequest = new DeviceAssignmentCreateRequest();
			assnRequest.setAssignmentType(assnChoice.getAssignmentType());
			assnRequest.setAssetId(assnChoice.getAssignmentAssetId());
			assnRequest.setDeviceHardwareId(device.getHardwareId());
			assnRequest.setSiteToken(site.getToken());
			assnRequest.addOrReplaceMetadata("S/N", UUID.randomUUID().toString());
			IDeviceAssignment assignment = getDeviceManagement().createDeviceAssignment(assnRequest);
			LOGGER.info(PREFIX_CREATE_ASSIGNMENT + " " + assignment.getToken());

			results.add(assignment);
		}
		return results;
	}

	/**
	 * Create device measurements associated with an assignment.
	 * 
	 * @param assignment
	 * @return
	 * @throws SiteWhereException
	 */
	public List<IDeviceMeasurements> createDeviceMeasurements(DeviceAssignment assignment, Date start)
			throws SiteWhereException {
		long current = start.getTime();
		List<IDeviceMeasurements> results = new ArrayList<IDeviceMeasurements>();
		for (int x = 0; x < EVENTS_PER_ASSIGNMENT; x++) {
			DeviceMeasurementsCreateRequest mreq = new DeviceMeasurementsCreateRequest();
			mreq.addOrReplaceMeasurement("engine.temperature", 145.0);
			mreq.setEventDate(new Date(current));
			results.add(getDeviceManagement().addDeviceMeasurements(assignment, mreq));

			DeviceAlertCreateRequest areq = new DeviceAlertCreateRequest();
			areq.setType("fire.alarm");
			areq.setMessage("Fire alarm has been triggered on the third floor.");
			areq.setEventDate(new Date(current));
			getDeviceManagement().addDeviceAlert(assignment, areq);
			
			current += 10000;
		}
		return results;
	}

	/**
	 * Gets a random location tracker assignment choice entry.
	 * 
	 * @return
	 */
	protected AssignmentChoice getRandomAssignmentChoice() {
		int slot = (int) Math.floor(LOCATION_TRACKERS.length * Math.random());
		return LOCATION_TRACKERS[slot];
	}

	/**
	 * Internal class for choosing device/asset assignments that make sense.
	 * 
	 * @author Derek
	 */
	private static class AssignmentChoice {

		private String devAssetId;
		private String devDescBase;
		private DeviceAssignmentType assnType;
		private String assnAssetId;

		public AssignmentChoice(String devAssetId, String devDescBase, DeviceAssignmentType assnType,
				String assnAssetId) {
			this.devAssetId = devAssetId;
			this.devDescBase = devDescBase;
			this.assnType = assnType;
			this.assnAssetId = assnAssetId;
		}

		protected String getDeviceAssetId() {
			return devAssetId;
		}

		protected String getDeviceDescriptionBase() {
			return devDescBase;
		}

		protected DeviceAssignmentType getAssignmentType() {
			return assnType;
		}

		protected String getAssignmentAssetId() {
			return assnAssetId;
		}
	}

	protected IDeviceManagement getDeviceManagement() {
		return deviceManagement;
	}

	protected void setDeviceManagement(IDeviceManagement deviceManagement) {
		this.deviceManagement = deviceManagement;
	}
}