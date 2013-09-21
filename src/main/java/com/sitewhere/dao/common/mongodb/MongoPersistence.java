/*
 * MongoPersistence.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.dao.common.mongodb;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sitewhere.rest.model.common.MetadataProviderEntity;
import com.sitewhere.security.SitewhereAuthentication;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.user.IUser;

/**
 * Common handlers for persisting Mongo data.
 * 
 * @author Derek
 */
public class MongoPersistence {

	/**
	 * Common handler for creating new objects. Assures that errors are handled in a consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @throws SiteWhereException
	 */
	public static void insert(DBCollection collection, DBObject object) throws SiteWhereException {
		WriteResult result = collection.insert(object);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error during insert: " + result.getLastError().toString());
		}
	}

	/**
	 * Common handler for updating existing objects. Assures that errors are handled in a consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @throws SiteWhereException
	 */
	public static void update(DBCollection collection, DBObject query, DBObject object)
			throws SiteWhereException {
		WriteResult result = collection.update(query, object);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error during update: " + result.getLastError().toString());
		}
	}

	/**
	 * Common handler for deleting objects. Assures that errors are handled in a consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @throws SiteWhereException
	 */
	public static void delete(DBCollection collection, DBObject object) throws SiteWhereException {
		WriteResult result = collection.remove(object);
		if (!result.getLastError().ok()) {
			throw new SiteWhereException("Error during delete: " + result.getLastError().toString());
		}
	}

	/**
	 * Initialize entity fields.
	 * 
	 * @param entity
	 * @throws SiteWhereException
	 */
	public static void initializeEntityMetadata(MetadataProviderEntity entity) throws SiteWhereException {
		entity.setCreatedDate(new Date());
		entity.setCreatedBy(getCurrentlyLoggedInUser().getUsername());
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
		entity.setUpdatedBy(getCurrentlyLoggedInUser().getUsername());
	}

	/**
	 * Get the currently logged in user from Spring Security.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	public static IUser getCurrentlyLoggedInUser() throws SiteWhereException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new SiteWhereSystemException(ErrorCode.NotLoggedIn, ErrorLevel.ERROR,
					HttpServletResponse.SC_FORBIDDEN);
		}
		if (!(auth instanceof SitewhereAuthentication)) {
			throw new SiteWhereException("Authentication was not of expected type: "
					+ SitewhereAuthentication.class.getName());
		}
		return (IUser) ((SitewhereAuthentication) auth).getPrincipal();
	}
}