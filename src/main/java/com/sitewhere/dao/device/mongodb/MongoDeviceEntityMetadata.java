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
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.spi.device.IMetadataEntry;
import com.sitewhere.spi.device.IMetadataProvider;

/**
 * Used to load or save device entity metdata to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceEntityMetadata {

	/** Property for entity metadata */
	public static final String PROP_METADATA = "metadata";

	/** Attribute name for measurement name */
	private static final String PROP_NAME = "name";

	/** Attribute name for measurement value */
	private static final String PROP_VALUE = "value";

	/**
	 * Store data into a DBObject using default property name.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IMetadataProvider source, DBObject target) {
		MongoDeviceEntityMetadata.toDBObject(PROP_METADATA, source, target);
	}

	/**
	 * Store data into a DBObject.
	 * 
	 * @param propertyName
	 * @param source
	 * @param target
	 */
	public static void toDBObject(String propertyName, IMetadataProvider source, DBObject target) {
		List<BasicDBObject> props = new ArrayList<BasicDBObject>();
		for (IMetadataEntry entry : source.getMetadata()) {
			BasicDBObject prop = new BasicDBObject();
			prop.put(PROP_NAME, entry.getName());
			prop.put(PROP_VALUE, entry.getValue());
			props.add(prop);
		}
		target.put(propertyName, props);
	}

	/**
	 * Load data from a DBObject using default property name.
	 * 
	 * @param source
	 * @param target
	 */
	public static void fromDBObject(DBObject source, IMetadataProvider target) {
		MongoDeviceEntityMetadata.fromDBObject(PROP_METADATA, source, target);
	}

	/**
	 * Load data from a DBObject.
	 * 
	 * @param PropertyName
	 * @param source
	 * @param target
	 */
	@SuppressWarnings("unchecked")
	public static void fromDBObject(String propertyName, DBObject source, IMetadataProvider target) {
		List<DBObject> props = (List<DBObject>) source.get(propertyName);
		if (props != null) {
			for (DBObject prop : props) {
				String name = (String) prop.get(PROP_NAME);
				String value = (String) prop.get(PROP_VALUE);
				target.addOrReplaceMetadata(name, value);
			}
		}
	}
}