package com.sitewhere.dao.device.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.rest.model.device.Site;
import com.sitewhere.spi.device.ISite;

/**
 * Used to load or save site data to MongoDB.
 * 
 * @author dadams
 */
public class MongoSite {

	/** Property for name */
	public static final String PROP_NAME = "name";

	/** Property for description */
	public static final String PROP_DESCRIPTION = "description";

	/** Property for image URL */
	public static final String PROP_IMAGE_URL = "imageUrl";

	/** Property for token */
	public static final String PROP_TOKEN = "token";

	/** Property for map type */
	public static final String PROP_MAP_TYPE = "mapType";

	/** Property for map metadata */
	public static final String PROP_MAP_MATADATA = "mapMetadata";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(ISite source, BasicDBObject target) {
		target.append(PROP_NAME, source.getName());
		target.append(PROP_DESCRIPTION, source.getDescription());
		target.append(PROP_IMAGE_URL, source.getImageUrl());
		target.append(PROP_MAP_TYPE, source.getMapType());
		target.append(PROP_TOKEN, source.getToken());

		MongoSiteWhereEntity.toDBObject(source, target);
		MongoDeviceEntityMetadata.toDBObject(PROP_MAP_MATADATA, source.getMapMetadata(), target);
		MongoDeviceEntityMetadata.toDBObject(source, target);
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	public static void fromDBObject(DBObject source, Site target) {
		String name = (String) source.get(PROP_NAME);
		String description = (String) source.get(PROP_DESCRIPTION);
		String imageUrl = (String) source.get(PROP_IMAGE_URL);
		String token = (String) source.get(PROP_TOKEN);
		String mapType = (String) source.get(PROP_MAP_TYPE);

		target.setName(name);
		target.setDescription(description);
		target.setImageUrl(imageUrl);
		target.setToken(token);
		target.setMapType(mapType);
		
		MongoSiteWhereEntity.fromDBObject(source, target);
		MongoDeviceEntityMetadata.fromDBObject(PROP_MAP_MATADATA, source, target.getMapMetadata());
		MongoDeviceEntityMetadata.fromDBObject(source, target);
	}

	/**
	 * Convert SPI object to Mongo DBObject.
	 * 
	 * @param source
	 * @return
	 */
	public static BasicDBObject toDBObject(ISite source) {
		BasicDBObject result = new BasicDBObject();
		MongoSite.toDBObject(source, result);
		return result;
	}

	/**
	 * Convert a DBObject into the SPI equivalent.
	 * 
	 * @param source
	 * @return
	 */
	public static Site fromDBObject(DBObject source) {
		Site result = new Site();
		MongoSite.fromDBObject(source, result);
		return result;
	}
}