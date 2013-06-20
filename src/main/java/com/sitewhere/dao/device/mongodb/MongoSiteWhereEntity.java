package com.sitewhere.dao.device.mongodb;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sitewhere.rest.model.device.MetadataProviderEntity;
import com.sitewhere.spi.common.ISiteWhereEntity;

/**
 * Used to load or save SiteWhereEntity data to MongoDB.
 * 
 * @author dadams
 */
public class MongoSiteWhereEntity {

	/** Property for date entity was created */
	public static final String PROP_CREATED_DATE = "createdDate";

	/** Property for user that created entity */
	public static final String PROP_CREATED_BY = "createdBy";

	/** Property for date entity was last updated */
	public static final String PROP_UPDATED_DATE = "updatedDate";

	/** Property for user that updated entity */
	public static final String PROP_UPDATED_BY = "updatedBy";

	/** Property for deleted flag */
	public static final String PROP_DELETED = "deleted";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(ISiteWhereEntity source, BasicDBObject target) {
		target.append(PROP_CREATED_DATE, MongoDataUtils.calendarAsDate(source.getCreatedDate()));
		target.append(PROP_CREATED_BY, source.getCreatedBy());
		target.append(PROP_UPDATED_DATE, MongoDataUtils.calendarAsDate(source.getUpdatedDate()));
		target.append(PROP_UPDATED_BY, source.getUpdatedBy());
		target.append(PROP_DELETED, source.isDeleted());
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	public static void fromDBObject(DBObject source, MetadataProviderEntity target) {
		Date createdDate = (Date) source.get(PROP_CREATED_DATE);
		String createdBy = (String) source.get(PROP_CREATED_BY);
		Date updatedDate = (Date) source.get(PROP_UPDATED_DATE);
		String updatedBy = (String) source.get(PROP_UPDATED_BY);
		Boolean deleted = (Boolean) source.get(PROP_DELETED);

		target.setCreatedDate(MongoDataUtils.dateAsCalendar(createdDate));
		target.setCreatedBy(createdBy);
		target.setUpdatedDate(MongoDataUtils.dateAsCalendar(updatedDate));
		target.setUpdatedBy(updatedBy);
		target.setDeleted(false);
		if (deleted != null) {
			target.setDeleted(deleted);
		}
	}

	/**
	 * Set deleted flag on DBObject.
	 * 
	 * @param source
	 * @param deleted
	 */
	public static void setDeleted(DBObject source, boolean deleted) {
		source.put(PROP_DELETED, deleted);
	}
}