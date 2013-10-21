/*
 * MongoPersistence.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.dao.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sitewhere.rest.model.common.MetadataProviderEntity;
import com.sitewhere.rest.service.search.SearchResults;
import com.sitewhere.security.LoginManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.common.IDateRangeSearchCriteria;
import com.sitewhere.spi.common.ISearchCriteria;

/**
 * Common handlers for persisting Mongo data.
 * 
 * @author Derek
 */
public class MongoPersistence {

	/**
	 * Common handler for creating new objects. Assures that errors are handled in a
	 * consistent way.
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
	 * Common handler for updating existing objects. Assures that errors are handled in a
	 * consistent way.
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
	 * Common handler for deleting objects. Assures that errors are handled in a
	 * consistent way.
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
	 * Search the given collection using the provided query and sort. Return the paged
	 * seaerch results.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public static <T> SearchResults<T> search(Class<T> api, DBCollection collection, DBObject query,
			DBObject sort, ISearchCriteria criteria) {
		int offset = Math.max(0, criteria.getPageNumber() - 1) * criteria.getPageSize();
		DBCursor cursor = collection.find(query).skip(offset).limit(criteria.getPageSize()).sort(sort);
		List<T> matches = new ArrayList<T>();
		SearchResults<T> results = new SearchResults<T>(matches);
		MongoConverter<T> converter = MongoConverters.getConverterFor(api);
		try {
			results.setNumResults(cursor.count());
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(converter.convert(match));
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	/**
	 * Appends filter criteria onto exiting query based on the given date range.
	 * 
	 * @param query
	 * @param criteria
	 */
	public static void addDateSearchCriteria(BasicDBObject query, String dateField,
			IDateRangeSearchCriteria criteria) {
		if ((criteria.getStartDate() == null) && (criteria.getEndDate() == null)) {
			return;
		}
		BasicDBObject dateClause = new BasicDBObject();
		if (criteria.getStartDate() != null) {
			dateClause.append("$gte", criteria.getStartDate());
		}
		if (criteria.getEndDate() != null) {
			dateClause.append("$lte", criteria.getEndDate());
		}
		query.put(dateField, dateClause);
	}

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
}