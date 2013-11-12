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
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.security.LoginManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.request.ISiteCreateRequest;

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
}