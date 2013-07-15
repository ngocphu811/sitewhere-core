/*
 * MongoPersistence.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.dao.device.mongodb;

import java.util.Date;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sitewhere.rest.model.device.MetadataProviderEntity;
import com.sitewhere.spi.SiteWhereException;

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
	 * Initialize entity fields.
	 * 
	 * @param entity
	 */
	public static void initializeEntityMetadata(MetadataProviderEntity entity) {
		entity.setCreatedDate(new Date());
		entity.setCreatedBy("admin");
		entity.setDeleted(false);
	}
}