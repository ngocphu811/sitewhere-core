/*
 * SiteWherePersistence.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core.device;

import java.util.ArrayList;
import java.util.Date;

import com.sitewhere.rest.model.common.MetadataEntry;
import com.sitewhere.rest.model.common.MetadataProvider;
import com.sitewhere.rest.model.common.MetadataProviderEntity;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.rest.model.device.Zone;
import com.sitewhere.security.LoginManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.common.ILocation;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.ISiteCreateRequest;
import com.sitewhere.spi.device.request.IZoneCreateRequest;

/**
 * Common methods needed by device service provider implementations.
 * 
 * @author Derek
 */
public class SiteWherePersistence {

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
		target.setMetadata(new ArrayList<MetadataEntry>());
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
}